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
package io.apiman.plugins.keycloak_oauth_policy.failures;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.keycloak_oauth_policy.Messages;

import org.keycloak.VerificationException;

/**
 * Policy failures
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class PolicyFailureFactory {

    private static final int HTTP_UNAUTHORIZED = 401;

    public PolicyFailure noAuthenticationProvided(IPolicyContext context) {
        return createAuthenticationPolicyFailure(context, 0,
                Messages.getString("KeycloakOauthPolicy.NoTokenGiven")); //$NON-NLS-1$
    }

    public PolicyFailure verificationException(IPolicyContext context, VerificationException e) {
        return createAuthenticationPolicyFailure(context, 1, e.getMessage());
    }

    public PolicyFailure noTransportSecurity(IPolicyContext context) {
        return createAuthenticationPolicyFailure(context, 2,
                Messages.getString("KeycloakOauthPolicy.NoTransportSecurity")); //$NON-NLS-1$
    }

    public PolicyFailure blacklistedToken(IPolicyContext context) {
        return createAuthenticationPolicyFailure(context, 3,
                Messages.getString("KeycloakOauthPolicy.BlacklistedToken")); //$NON-NLS-1$
    }

    public PolicyFailure doesNotHoldRequiredRoles(IPolicyContext context) {
        return createAuthorizationPolicyFailure(context, 4, Messages.getString("KeycloakOauthPolicy.DoesNotHoldRequiredRoles"));
    }

    private PolicyFailure createAuthenticationPolicyFailure(IPolicyContext context, int failureCode,
            String message) {
        PolicyFailure pf = getFailureFactory(context).createFailure(PolicyFailureType.Authentication,
                failureCode, message);
        pf.setResponseCode(HTTP_UNAUTHORIZED);
        return pf;
    }

    private PolicyFailure createAuthorizationPolicyFailure(IPolicyContext context, int failureCode,
            String message) {
        PolicyFailure pf = getFailureFactory(context).createFailure(PolicyFailureType.Authorization,
                failureCode, message);
        pf.setResponseCode(HTTP_UNAUTHORIZED);
        return pf;
    }

    private IPolicyFailureFactoryComponent getFailureFactory(IPolicyContext context) {
        return context.getComponent(IPolicyFailureFactoryComponent.class);
    }
}
