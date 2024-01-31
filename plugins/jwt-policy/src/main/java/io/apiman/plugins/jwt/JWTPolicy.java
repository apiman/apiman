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

import com.auth0.jwk.*;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.jwt.beans.ForwardAuthInfo;
import io.apiman.plugins.jwt.beans.JWTPolicyBean;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.PrematureJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.lang.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

/**
 * Generic JWT/S Policy.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class JWTPolicy extends AbstractMappedPolicy<JWTPolicyBean> {

    private static final String AUTHORIZATION_KEY = "Authorization"; //$NON-NLS-1$
    private static final String ACCESS_TOKEN_QUERY_KEY = "access_token"; //$NON-NLS-1$
    private static final String BEARER = "bearer "; //$NON-NLS-1$
    private static final PolicyFailureFactory FAILURE_FACTORY = PolicyFailureFactory.getInstance();
    private static final String[] schemes = {"http","https"};
    private static final UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);
    private JwkProvider provider = null;
    private String providerUrl = null;

    @Override
    public Class<JWTPolicyBean> getConfigurationClass() {
        return JWTPolicyBean.class;
    }

    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, JWTPolicyBean config, IPolicyChain<ApiRequest> chain) {

        String jwt = Optional.ofNullable(request.getHeaders().get(AUTHORIZATION_KEY))
                // If seems to be bearer token
                .filter(e -> e.toLowerCase().startsWith(BEARER))
                // Get out token value
                .map(e -> e.substring(BEARER.length()))
                // Otherwise attempt to get from the access_token query param
                .orElse(request.getQueryParams().get(ACCESS_TOKEN_QUERY_KEY));

        // If transport security required and is not secure.
        if (config.getRequireTransportSecurity() && !request.isTransportSecure()) {
            chain.doFailure(FAILURE_FACTORY.noTransportSecurity(context));
            return;
        }

        // If JWT required and none provided
        if (config.getRequireJWT() && jwt == null) {
            chain.doFailure(FAILURE_FACTORY.noAuthenticationProvided(context));
            return;
        }

        if (jwt != null) {
            try {
                Map<String, Object> claims = validateJwt(jwt, config);
                forwardHeaders(request, config, jwt, claims);
                stripAuthTokens(request, config);
                chain.doApply(request);
            } catch (ExpiredJwtException e) {
                chain.doFailure(FAILURE_FACTORY.jwtExpired(context, e));
            } catch (PrematureJwtException e) {
                chain.doFailure(FAILURE_FACTORY.jwtPremature(context, e));
            } catch (MalformedJwtException e) {
                chain.doFailure(FAILURE_FACTORY.jwtMalformed(context, e));
            } catch (SignatureException e) {
                if (urlValidator.isValid(config.getSigningKeyString())){
                    // This will clear the cache of the jwk(s) provider
                    provider = getNewJwksProvider(config.getSigningKeyString());
                }
                chain.doFailure(FAILURE_FACTORY.signatureException(context, e));
            } catch (InvalidClaimException e) {
                chain.doFailure(FAILURE_FACTORY.invalidClaim(context, e));
            } catch (UnsupportedJwtException e) {
                chain.doFailure(FAILURE_FACTORY.unsupportedJwt(context, e));
            } catch (Exception e) {
                chain.doFailure(FAILURE_FACTORY.genericFailure(context, e));
            }
        } else {
            chain.doApply(request);
        }
    }

    private Map<String, Object> validateJwt(String token, JWTPolicyBean config)
            throws ExpiredJwtException, PrematureJwtException, MalformedJwtException, SignatureException, InvalidClaimException {

        // check if we have to use jwk(s)
        if (urlValidator.isValid(config.getSigningKeyString())){
            if ( (provider == null) || !config.getSigningKeyString().equals(providerUrl) ) {
                provider = getNewJwksProvider(config.getSigningKeyString());
                providerUrl = config.getSigningKeyString();
            }

            Jwk jwk;
            String tokenKid = config.getKid();
            if ((config.getKid() == null) || config.getKid().equals("null")) {
                // No specific kid was set in the config, so let's support key rollover and try to find the one
                // specified in the token.
                String tokenNoSig = StringUtils.substringBeforeLast(token, ".") + ".";
                tokenKid = (String) Jwts.parser().parse(tokenNoSig).getHeader().getOrDefault("kid", null);
            }
            try {
                jwk = provider.get(tokenKid);
                if (config.getSigningKey() == null || !(config.getSigningKey().equals(jwk.getPublicKey()))) {
                    config.setSigningKey(jwk.getPublicKey());
                }
            } catch (JwkException e) {
               throw new SignatureException("JWK was not found with kid: " + tokenKid, e);
            }
        }

        JwtParser parser = Jwts.parser()
                .setSigningKey(config.getSigningKey())
                .setAllowedClockSkewSeconds(config.getAllowedClockSkew());

        // Set all claims
        config.getRequiredClaims().stream() // TODO add type variable to allow dates, etc
            .forEach(requiredClaim -> parser.require(requiredClaim.getClaimName(), requiredClaim.getClaimValue()));

        return parser.parse(token, new ConfigCheckingJwtHandler(config));
    }

    private void stripAuthTokens(ApiRequest request, JWTPolicyBean config) {
        if (config.getStripTokens()) {
            request.getHeaders().remove(AUTHORIZATION_KEY);
            request.getQueryParams().remove(ACCESS_TOKEN_QUERY_KEY);
        }
    }

    private void forwardHeaders(ApiRequest request, JWTPolicyBean config, String rawToken, Map<String, Object> claims) {
        for (ForwardAuthInfo entry : config.getForwardAuthInfo()) {
            // Add the header if we've been able to look it up, else it'll just be empty.
            Object claimValue = ACCESS_TOKEN_QUERY_KEY.equals(entry.getField()) ? rawToken : claims.get(entry.getField());
            if (claimValue != null) {
                request.getHeaders().put(entry.getHeader(), Objects.nullSafeToString(claimValue));
            }
        }
    }

    private GuavaCachedJwkProvider getNewJwksProvider(String url){
        JwkProvider http = null;
        try {
            http = new UrlJwkProvider(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return new GuavaCachedJwkProvider(http);
    }
}
