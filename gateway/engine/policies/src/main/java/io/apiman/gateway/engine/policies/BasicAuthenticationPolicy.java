/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.metrics.RequestMetric;
import io.apiman.gateway.engine.policies.auth.JDBCIdentityValidator;
import io.apiman.gateway.engine.policies.auth.LDAPIdentityValidator;
import io.apiman.gateway.engine.policies.auth.StaticIdentityValidator;
import io.apiman.gateway.engine.policies.config.BasicAuthenticationConfig;
import io.apiman.gateway.engine.policies.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.policy.PolicyContextKeys;

import org.apache.commons.codec.binary.Base64;

/**
 * An implementation of an apiman policy that supports multiple styles of authentication.
 * Specifically this policy is responsible for authenticating the inbound request prior
 * to proxying the request to the back end service.  If the authentication fails then
 * the back end system is never invoked.
 *
 * @author eric.wittmann@redhat.com
 */
public class BasicAuthenticationPolicy extends AbstractMappedPolicy<BasicAuthenticationConfig> {

    private static final StaticIdentityValidator staticIdentityValidator = new StaticIdentityValidator();
    private static final LDAPIdentityValidator ldapIdentityValidator = new LDAPIdentityValidator();
    private static final JDBCIdentityValidator jdbcIdentityValidator = new JDBCIdentityValidator();

    /**
     * Constructor.
     */
    public BasicAuthenticationPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.AbstractPolicy#getConfigurationClass()
     */
    @Override
    protected Class<BasicAuthenticationConfig> getConfigurationClass() {
        return BasicAuthenticationConfig.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(final ServiceRequest request, final IPolicyContext context, final BasicAuthenticationConfig config,
            final IPolicyChain<ServiceRequest> chain) {
        String authHeader = request.getHeaders().get("Authorization"); //$NON-NLS-1$
        boolean requireBasic = config.getRequireBasicAuth() == null ? Boolean.TRUE : config.getRequireBasicAuth();

        // Handle the case where no authentication credentials are provided
        if (authHeader == null || authHeader.trim().isEmpty()) {
            if (requireBasic) {
                sendAuthFailure(context, chain, config, PolicyFailureCodes.BASIC_AUTH_REQUIRED);
                return;
            } else {
                chain.doApply(request);
                return;
            }
        }

        // Handle the case where auth credentials are provided but they aren't BASIC
        // credentials (e.g. BEARER-TOKEN)
        if (!authHeader.toUpperCase().startsWith("BASIC ")) { //$NON-NLS-1$
            if (requireBasic) {
                sendAuthFailure(context, chain, config, PolicyFailureCodes.BASIC_AUTH_REQUIRED);
                return;
            } else {
                chain.doApply(request);
                return;
            }
        }

        // Check transport security
        if (config.isRequireTransportSecurity() && !request.isTransportSecure()) {
            sendAuthFailure(context, chain, config, PolicyFailureCodes.TRANSPORT_SECURITY_REQUIRED);
        }

        // Parse the Authorization http header.
        String username = null;
        String password = null;
        try {
            String userpassEncoded = authHeader.substring(6);
            byte[] decoded = Base64.decodeBase64(userpassEncoded);
            String data = new String(decoded, "UTF-8"); //$NON-NLS-1$
            int sepIdx = data.indexOf(':');
            if (sepIdx > 0) {
                username = data.substring(0, sepIdx);
                password = data.substring(sepIdx + 1);
            } else {
                username = data;
            }
        } catch (Throwable t) {
            // TODO log this error to apiman::logger
            sendAuthFailure(context, chain, config, PolicyFailureCodes.BASIC_AUTH_FAILED);
            return;
        }

        // Asynchronously validate the inbound requests's basic auth credentials
        final String forwardedUsername = username;
        validateCredentials(username, password, request, context, config, new IAsyncResultHandler<Boolean>() {
            @Override
            public void handle(IAsyncResult<Boolean> result) {
                if (result.isError()) {
                    chain.throwError(result.getError());
                } else {
                    if (result.getResult()) {
                        String forwardIdentityHttpHeader = config.getForwardIdentityHttpHeader();
                        if (forwardIdentityHttpHeader != null && !forwardIdentityHttpHeader.trim().isEmpty()) {
                            request.getHeaders().put(forwardIdentityHttpHeader, forwardedUsername);
                        }
                        RequestMetric metric = context.getAttribute(PolicyContextKeys.REQUEST_METRIC, (RequestMetric) null);
                        if (metric != null) {
                            metric.setUser(forwardedUsername);
                        }
                        // Remove the authorization header so that it doesn't get passed through to the backend service
                        // TODO: make this optional - perhaps they *want* the auth header passed through?
                        request.getHeaders().remove("Authorization"); //$NON-NLS-1$
                        chain.doApply(request);
                    } else {
                        sendAuthFailure(context, chain, config, PolicyFailureCodes.BASIC_AUTH_FAILED);
                    }
                }
            }
        });
    }

    /**
     * Validate the inbound authentication credentials.
     * @param username
     * @param password
     * @param request
     * @param context
     * @param config
     * @param handler
     */
    private void validateCredentials(String username, String password, ServiceRequest request, IPolicyContext context,
            BasicAuthenticationConfig config, IAsyncResultHandler<Boolean> handler) {
        if (config.getStaticIdentity() != null) {
            staticIdentityValidator.validate(username, password, request, context, config.getStaticIdentity(), handler);
        } else if (config.getLdapIdentity() != null) {
            ldapIdentityValidator.validate(username, password, request, context, config.getLdapIdentity(), handler);
        } else if (config.getJdbcIdentity() != null) {
            jdbcIdentityValidator.validate(username, password, request, context, config.getJdbcIdentity(), handler);
        } else {
            handler.handle(AsyncResultImpl.create(Boolean.FALSE));
        }
    }

    /**
     * Sends the 'unauthenticated' response as a policy failure.
     * @param context
     * @param chain
     * @param config
     * @param reason
     */
    protected void sendAuthFailure(IPolicyContext context, IPolicyChain<?> chain, BasicAuthenticationConfig config, int reason) {
        IPolicyFailureFactoryComponent pff = context.getComponent(IPolicyFailureFactoryComponent.class);
        PolicyFailure failure = pff.createFailure(PolicyFailureType.Authentication, reason, Messages.i18n.format("BasicAuthenticationPolicy.AuthenticationFailed")); //$NON-NLS-1$
        String realm = config.getRealm();
        if (realm == null || realm.trim().isEmpty()) {
            realm = "Service"; //$NON-NLS-1$
        }
        failure.getHeaders().put("WWW-Authenticate", String.format("BASIC realm=\"%1$s\"", realm)); //$NON-NLS-1$ //$NON-NLS-2$
        chain.doFailure(failure);
    }

}
