/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.plugins.keycloak_oauth_policy;

import org.apache.commons.lang.StringUtils;
import org.keycloak.RSATokenVerifier;
import org.keycloak.VerificationException;
import org.keycloak.representations.AccessToken;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.keycloak_oauth_policy.beans.ForwardAuthInfo;
import io.apiman.plugins.keycloak_oauth_policy.beans.KeycloakOauthConfigBean;

/**
 * A Keycloak OAuth policy.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class KeycloakOauthPolicy extends AbstractMappedPolicy<KeycloakOauthConfigBean> {

    private static final int HTTP_UNAUTHORIZED = 401;
    private final String AUTHORIZATION_KEY = "Authorization"; //$NON-NLS-1$
    private final String ACCESS_TOKEN_QUERY_KEY = "access_token"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<KeycloakOauthConfigBean> getConfigurationClass() {
        return KeycloakOauthConfigBean.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.
     * ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object,
     * io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ServiceRequest request, IPolicyContext context, KeycloakOauthConfigBean config,
            IPolicyChain<ServiceRequest> chain) {

        IPolicyFailureFactoryComponent ff = context.getComponent(IPolicyFailureFactoryComponent.class);

        String rawToken = getRawAuthToken(request);

        if (rawToken == null && config.getRequireOauth()) {
            chain.doFailure(noAuthenticationProvidedFailure(ff));
            return;
        }

        try {
            AccessToken parsedToken = RSATokenVerifier.verifyToken(rawToken, config.getRealmCertificate()
                    .getPublicKey(), config.getRealm());

            forwardHeaders(request, config, rawToken, parsedToken);
            stripAuthTokens(request, config);

            chain.doApply(request);
        } catch (VerificationException e) {
            chain.doFailure(verificationExceptionFailure(ff, e));
        }
    }

    private String getRawAuthToken(ServiceRequest request) {
        String rawToken = StringUtils.strip(request.getHeaders().get(AUTHORIZATION_KEY));

        if (rawToken != null && StringUtils.startsWith(rawToken, "Bearer ")) { //$NON-NLS-1$
            rawToken = StringUtils.removeStart(rawToken, "Bearer "); //$NON-NLS-1$
        } else {
            rawToken = request.getQueryParams().get(ACCESS_TOKEN_QUERY_KEY);
        }

        return rawToken;
    }

    private void stripAuthTokens(ServiceRequest request, KeycloakOauthConfigBean config) {
        if (config.getStripTokens()) {
            request.getHeaders().remove(AUTHORIZATION_KEY);
            request.getQueryParams().remove(ACCESS_TOKEN_QUERY_KEY);
        }
    }

    private void forwardHeaders(ServiceRequest request, KeycloakOauthConfigBean config, String rawToken,
            AccessToken parsedToken) {
        if (config.getForwardAuthInfo().size() == 0)
            return;

        for (ForwardAuthInfo entry : config.getForwardAuthInfo()) {
            String fieldValue = null;

            // TODO consider allowing any field to be specified by using string + reflection or MethodHandle
            // There are significant potential performance issues with this, however.
            switch (entry.getField()) {
            case ACCESS_TOKEN:
                fieldValue = rawToken;
            case EMAIL:
                fieldValue = parsedToken.getEmail();
            case NAME:
                fieldValue = parsedToken.getName();
            case SUBJECT:
                fieldValue = parsedToken.getSubject();
            case USERNAME:
                fieldValue = parsedToken.getPreferredUsername();
            }
            request.getHeaders().put(entry.getHeader(), fieldValue);
        }
    }

    private PolicyFailure noAuthenticationProvidedFailure(IPolicyFailureFactoryComponent ff) {
        return createUnauthorizedPolicyFailure(ff, 0,
                Messages.getString("KeycloakOauthPolicy.no_token_given")); //$NON-NLS-1$
    }

    private PolicyFailure verificationExceptionFailure(IPolicyFailureFactoryComponent ff,
            VerificationException e) {
        return createUnauthorizedPolicyFailure(ff, 1, e.getMessage());
    }

    private PolicyFailure createUnauthorizedPolicyFailure(IPolicyFailureFactoryComponent ff, int failureCode,
            String message) {
        PolicyFailure pf = ff.createFailure(PolicyFailureType.Authorization, failureCode, message);
        pf.setResponseCode(HTTP_UNAUTHORIZED);
        return pf;
    }
}
