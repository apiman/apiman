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
package io.apiman.plugins.jwt;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.PrematureJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

/**
 * Policy failures
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class PolicyFailureFactory {
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int AUTH_NO_TRANSPORT_SECURITY = 12003;
    private static final int AUTH_VERIFICATION_ERROR = 12004;
    private static final int AUTH_NOT_PROVIDED = 12005;
    private static final int AUTH_JWT_EXPIRED = 12006;
    private static final int AUTH_JWT_MALFORMED = 12007;
    private static final int AUTH_JWT_SIGNATURE_EXCEPTION = 12008;
    private static final int AUTH_JWT_CLAIM_FAILURE = 12009;
    private static final int AUTH_JWT_PREMATURE = 12010;
    private static final int AUTH_JWT_UNSUPPORTED_JWT = 12011;

    private static final PolicyFailureFactory INSTANCE = new PolicyFailureFactory();


    public static PolicyFailureFactory getInstance() {
        return INSTANCE;
    }

    public PolicyFailure jwtExpired(IPolicyContext context, ExpiredJwtException e) {
        return createAuthenticationPolicyFailure(context, AUTH_JWT_EXPIRED,
                e.getLocalizedMessage());
    }

    public PolicyFailure jwtPremature(IPolicyContext context, PrematureJwtException e) {
        return createAuthenticationPolicyFailure(context, AUTH_JWT_PREMATURE,
                e.getLocalizedMessage());
    }

    public PolicyFailure jwtMalformed(IPolicyContext context, MalformedJwtException e) {
        return createAuthenticationPolicyFailure(context, AUTH_JWT_MALFORMED,
                e.getLocalizedMessage());
    }

    public PolicyFailure signatureException(IPolicyContext context, SignatureException e) {
        return createAuthenticationPolicyFailure(context, AUTH_JWT_SIGNATURE_EXCEPTION,
                e.getLocalizedMessage());
    }

    public PolicyFailure invalidClaim(IPolicyContext context, ClaimJwtException e) {
        return createAuthenticationPolicyFailure(context, AUTH_JWT_CLAIM_FAILURE,
                e.getLocalizedMessage());
    }

    public PolicyFailure unsupportedJwt(IPolicyContext context, UnsupportedJwtException e) {
        return createAuthenticationPolicyFailure(context, AUTH_JWT_UNSUPPORTED_JWT,
                Messages.getString("JWTPolicy.NoTransportSecurity")); //$NON-NLS-1$
    }

    public PolicyFailure noAuthenticationProvided(IPolicyContext context) {
        return createAuthenticationPolicyFailure(context, AUTH_NOT_PROVIDED,
                Messages.getString("JWTPolicy.NoTokenGiven")); //$NON-NLS-1$
    }

    public PolicyFailure genericFailure(IPolicyContext context, Exception e) {
        return createAuthenticationPolicyFailure(context, AUTH_VERIFICATION_ERROR,
                e.getLocalizedMessage());
    }

    public PolicyFailure noTransportSecurity(IPolicyContext context) {
        return createAuthenticationPolicyFailure(context, AUTH_NO_TRANSPORT_SECURITY,
                Messages.getString("JWTPolicy.NoTransportSecurity")); //$NON-NLS-1$
    }

    private PolicyFailure createAuthenticationPolicyFailure(IPolicyContext context, int failureCode,
            String message) {
        PolicyFailure pf = getFailureFactory(context).createFailure(PolicyFailureType.Authentication,
                failureCode, message);
        pf.setResponseCode(HTTP_UNAUTHORIZED);
        return pf;
    }

    private IPolicyFailureFactoryComponent getFailureFactory(IPolicyContext context) {
        return context.getComponent(IPolicyFailureFactoryComponent.class);
    }

}
