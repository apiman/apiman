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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.config.AuthorizationConfig;
import io.apiman.gateway.engine.policies.config.AuthorizationRule;
import io.apiman.gateway.engine.policies.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Adds authorization capabilities to apiman. This policy allows users to
 * specify what roles the authenticated user must have in order to be allowed to
 * call the service.
 *
 * This policy works in conjunction with a compatible Authentication policy,
 * such as the Basic authentication policy.  The assumption is that such a
 * policy will extract the roles from the source of identity (either during
 * authentication or as a followup step).  These roles will be stored in the
 * policy context for use by this Authorization policy.  The roles are
 * represented as a simple set of strings.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuthorizationPolicy extends AbstractMappedPolicy<AuthorizationConfig> {

    public static final String AUTHENTICATED_USER_ROLES = "io.apiman.policies.auth::authenticated-user-roles"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public AuthorizationPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<AuthorizationConfig> getConfigurationClass() {
        return AuthorizationConfig.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ServiceRequest request, IPolicyContext context, AuthorizationConfig config,
            IPolicyChain<ServiceRequest> chain) {
        Set<String> userRoles = context.getAttribute(AUTHENTICATED_USER_ROLES, (HashSet<String>) null);
        String verb = request.getType();
        String resource = request.getDestination();

        // If no roles are set in the context - then fail with a configuration error
        if (userRoles == null) {
            String msg = Messages.i18n.format("AuthorizationPolicy.MissingRoles"); //$NON-NLS-1$
            PolicyFailure failure = context.getComponent(IPolicyFailureFactoryComponent.class).createFailure(
                    PolicyFailureType.Other, PolicyFailureCodes.CONFIGURATION_ERROR, msg);
            chain.doFailure(failure);
            return;
        }

        if (isAuthorized(config, verb, resource, userRoles)) {
            chain.doApply(request);
        } else {
            String msg = Messages.i18n.format("AuthorizationPolicy.Unauthorized"); //$NON-NLS-1$
            PolicyFailure failure = context.getComponent(IPolicyFailureFactoryComponent.class).createFailure(
                    PolicyFailureType.Authorization, PolicyFailureCodes.USER_NOT_AUTHORIZED, msg);
            chain.doFailure(failure);
        }
    }

    /**
     * Checks the verb and resource against the requirements configured for the
     * policy.  Returns true iff the user has all of the required roles (multiple
     * roles may be required depending on configuration).
     *
     * Note that if the configuration does not include any
     *
     * @param config
     * @param verb
     * @param resource
     * @param userRoles
     */
    private boolean isAuthorized(AuthorizationConfig config, String verb, String resource, Set<String> userRoles) {
        if (resource == null || resource.trim().length() == 0) {
            resource = "/"; //$NON-NLS-1$
        }
        boolean authorized = true;
        for (AuthorizationRule authorizationRule : config.getRules()) {
            boolean verbMatches = "*".equals(authorizationRule.getVerb()) || verb.equalsIgnoreCase(authorizationRule.getVerb()); //$NON-NLS-1$
            if (verbMatches && resource.matches(authorizationRule.getPathPattern())) {
                // the verb and resource matched the rule - so enforce the role here!
                authorized = authorized && userRoles.contains(authorizationRule.getRole());
            }
        }
        return authorized;
    }

}
