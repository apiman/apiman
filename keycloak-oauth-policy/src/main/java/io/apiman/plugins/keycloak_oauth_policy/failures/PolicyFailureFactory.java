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

    private static final int AUTH_MISSING_ROLE = 11001;
    private static final int AUTH_BLACKLISTED_TOKEN = 11002;
    private static final int AUTH_NO_TRANSPORT_SECURITY = 11003;
    private static final int AUTH_VERIFICATION_ERROR = 11004;
    private static final int AUTH_NOT_PROVIDED = 11005;

    public PolicyFailure noAuthenticationProvided(IPolicyContext context) {
        return createAuthenticationPolicyFailure(context, AUTH_NOT_PROVIDED,
                Messages.getString("KeycloakOauthPolicy.NoTokenGiven")); //$NON-NLS-1$
    }

    public PolicyFailure verificationException(IPolicyContext context, VerificationException e) {
        return createAuthenticationPolicyFailure(context, AUTH_VERIFICATION_ERROR, e.getMessage());
    }

    public PolicyFailure noTransportSecurity(IPolicyContext context) {
        return createAuthenticationPolicyFailure(context, AUTH_NO_TRANSPORT_SECURITY,
                Messages.getString("KeycloakOauthPolicy.NoTransportSecurity")); //$NON-NLS-1$
    }

    public PolicyFailure blacklistedToken(IPolicyContext context) {
        return createAuthenticationPolicyFailure(context, AUTH_BLACKLISTED_TOKEN,
                Messages.getString("KeycloakOauthPolicy.BlacklistedToken")); //$NON-NLS-1$
    }

    public PolicyFailure doesNotHoldRequiredRoles(IPolicyContext context) {
        return createAuthorizationPolicyFailure(context, AUTH_MISSING_ROLE, Messages.getString("KeycloakOauthPolicy.DoesNotHoldRequiredRoles")); //$NON-NLS-1$
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
