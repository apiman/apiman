/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.plugins.soap_authorization_policy;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policies.PolicyFailureCodes;
import io.apiman.gateway.engine.policies.config.MultipleMatchType;
import io.apiman.gateway.engine.policies.config.UnmatchedRequestType;
import io.apiman.gateway.engine.policies.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Adds authorization capabilities to apiman. This policy allows users to
 * specify what roles the authenticated user must have in order to be allowed to
 * call the API.
 *
 * This policy works in conjunction with a compatible Authentication policy,
 * such as the Basic authentication policy.  The assumption is that such a
 * policy will extract the roles from the source of identity (either during
 * authentication or as a followup step).  These roles will be stored in the
 * policy context for use by this Authorization policy.  The roles are
 * represented as a simple set of strings.
 *
 * @author eric.wittmann@redhat.com
 * @author rachel.yordan@redhat.com
 */
public class SoapAuthorizationPolicy extends AbstractMappedPolicy<SoapAuthorizationConfig> {

    public static final String AUTHENTICATED_USER_ROLES = "io.apiman.policies.auth::authenticated-user-roles"; //$NON-NLS-1$
    public static final String HEADER_SOAP_ACTION = "SOAPAction";

    public SoapAuthorizationPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<SoapAuthorizationConfig> getConfigurationClass() {
        return SoapAuthorizationConfig.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, SoapAuthorizationConfig config,
            IPolicyChain<ApiRequest> chain) {
        Set<String> userRoles = context.getAttribute(AUTHENTICATED_USER_ROLES, (HashSet<String>) null);
        String action = request.getHeaders().get(HEADER_SOAP_ACTION);
        String resource = request.getDestination();

        // If no roles are set in the context - then fail with a configuration error
        if (userRoles == null) {
            String msg = Messages.i18n.format("SoapAuthorizationPolicy.MissingRoles"); //$NON-NLS-1$
            PolicyFailure failure = context.getComponent(IPolicyFailureFactoryComponent.class).createFailure(
                    PolicyFailureType.Other, PolicyFailureCodes.CONFIGURATION_ERROR, msg);
            chain.doFailure(failure);
            return;
        }

        if (isAuthorized(config, action, resource, userRoles)) {
            chain.doApply(request);
        } else {
            String msg = Messages.i18n.format("SoapAuthorizationPolicy.Unauthorized"); //$NON-NLS-1$
            PolicyFailure failure = context.getComponent(IPolicyFailureFactoryComponent.class).createFailure(
                    PolicyFailureType.Authorization, PolicyFailureCodes.USER_NOT_AUTHORIZED, msg);
            chain.doFailure(failure);
        }
    }

    /**
     * Checks the action and resource against the requirements configured for the
     * policy.  Returns true if the user has all of the required roles (multiple
     * roles may be required depending on configuration).
     *
     * Note that if the configuration does not include any
     *
     * @param config
     * @param action
     * @param resource
     * @param userRoles
     */
    private boolean isAuthorized(SoapAuthorizationConfig config, String action, String resource, Set<String> userRoles) {
        if (resource == null || resource.trim().length() == 0) {
            resource = "/"; //$NON-NLS-1$
        }
        // If multiMatch is set to 'any', then start out with authorized = false, and we need to
        // find at least one match to turn authorized to true.  If it's set to "all" (the default)
        // then start out authorized, and it requires *every* matching rule to pass or else it'll
        // switch to false.
        boolean authorized = true;
        if (config.getMultiMatch() == MultipleMatchType.any) {
            authorized = false;
        }
        boolean matchFound = false;
        for (SoapAuthorizationRule soapAuthorizationRule : config.getRules()) {
            boolean actionMatches = "*".equals(soapAuthorizationRule.getAction()) || action.equalsIgnoreCase(soapAuthorizationRule.getAction()); //$NON-NLS-1$
            boolean ruleMatches = resource.matches(soapAuthorizationRule.getPathPattern());
            if (actionMatches && ruleMatches) {
                // the action and resource matched the rule - so enforce the role here!
                boolean userHasRole = userRoles.contains(soapAuthorizationRule.getRole());
                matchFound = true;

                // If the multiMatch setting is "at least one matching rule" then do a logical
                // OR operation.  If it's set to "all matching rules" then do a logical AND.
                if (config.getMultiMatch() == MultipleMatchType.any) {
                    authorized = authorized || userHasRole;
                } else {
                    authorized = authorized && userHasRole;
                }
            }
        }

        // If no authorization rules matched the request, what do we do?
        if (!matchFound) {
            if (config.getRequestUnmatched() == UnmatchedRequestType.pass) {
                authorized = true;
            } else {
                authorized = false;
            }
        }

        return authorized;
    }

}
