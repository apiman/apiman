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
package io.apiman.manager.ui.server.wildfly8;

import io.apiman.manager.ui.client.shared.beans.BearerTokenCredentialsBean;
import io.apiman.manager.ui.server.auth.ITokenGenerator;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.util.Time;

/**
 * A token generator when using KeyCloak as the authentication provider for apiman.
 *
 * @author eric.wittmann@redhat.com
 */
public class KeyCloakBearerTokenGenerator implements ITokenGenerator {

    /**
     * Constructor.
     */
    public KeyCloakBearerTokenGenerator() {
    }

    /**
     * @see io.apiman.manager.ui.server.auth.ITokenGenerator#generateToken(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public BearerTokenCredentialsBean generateToken(HttpServletRequest request) {
        BearerTokenCredentialsBean bean = new BearerTokenCredentialsBean();
        
        KeycloakSecurityContext session = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        if (session != null) {
            bean.setToken(session.getTokenString());
            int nowInSeconds = Time.currentTime();
            int expiresInSeconds = session.getToken().getExpiration();
            
            if (expiresInSeconds <= nowInSeconds) {
                bean.setRefreshPeriod(1);
            } else {
                bean.setRefreshPeriod(expiresInSeconds - nowInSeconds);
            }
        } else {
            bean.setToken("LOGGED_OUT"); //$NON-NLS-1$
            bean.setRefreshPeriod(30);
        }
        
        return bean;
    }

}
