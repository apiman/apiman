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

import org.keycloak.RSATokenVerifier;
import org.keycloak.VerificationException;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * @author Marc Savy <msavy@redhat.com>
 */
public class KeyCloakOauthPolicy extends AbstractMappedPolicy<KeycloakOauthConfigBean> {

    private static final int HTTP_UNAUTHORIZED = 401;
    private final String AUTHORIZATION_KEY = "Authorization"; //$NON-NLS-1$
    private final String ACCESS_TOKEN_QUERY_KEY = "access_token"; //$NON-NLS-1$
    
    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<KeycloakOauthConfigBean> getConfigurationClass() {
        return KeycloakOauthConfigBean.class;
    }
    
    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ServiceRequest request, IPolicyContext context, 
            KeycloakOauthConfigBean config, IPolicyChain<ServiceRequest> chain) {
                
        String tokenString = request.getHeaders().get(AUTHORIZATION_KEY);
        
        if(tokenString == null) {
            tokenString = request.getQueryParams().get(ACCESS_TOKEN_QUERY_KEY);
        }
        
        if(tokenString == null && config.getRequireOauth()) {
            chain.doFailure(noAuthenticationProvidedFailure());
            return;
        }
        
        try {
            RSATokenVerifier.verifyToken(tokenString, 
                    config.getRealmCertificate().getPublicKey(),
                    config.getRealm());
                        
            chain.doApply(request);
        } catch (VerificationException e) {
            chain.doFailure(verificationExceptionFailure(e));
        }   
    }

    private PolicyFailure noAuthenticationProvidedFailure() {
        PolicyFailure pf = createUnauthorizedPolicyFailure();
        pf.setMessage("OAuth AUTHORIZATION header or access_token query parameter must be provided.");
        pf.setFailureCode(0);
        return pf;
    }

    private PolicyFailure verificationExceptionFailure(VerificationException e) {
        PolicyFailure pf = createUnauthorizedPolicyFailure();
        pf.setMessage(e.getMessage());
        pf.setFailureCode(1);
        return pf;
    }
    
    private PolicyFailure createUnauthorizedPolicyFailure() {
        PolicyFailure pf = new PolicyFailure();
        pf.setType(PolicyFailureType.Authorization);
        pf.setResponseCode(HTTP_UNAUTHORIZED);
        return pf;       
    }
}
