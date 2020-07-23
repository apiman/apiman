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
package io.apiman.manager.ui.server.kc;

import io.apiman.manager.ui.server.auth.ITokenGenerator;
import io.apiman.manager.ui.server.beans.BearerTokenCredentialsBean;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.common.util.Time;

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
            int nowInSeconds = getCurrentTime();
            long expiresInSeconds = session.getToken().getExp();

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

    /**
     * This method looks strange - it is designed to support both newer and older
     * versions of keycloak.  The {@link Time} class was repackaged, so to support
     * older versions of keycloak we will try to fall back to loading an older
     * version of the {@link Time} class.
     * @return the current time
     */
    protected int getCurrentTime() {
        try {
            return org.keycloak.common.util.Time.currentTime();
        } catch (Throwable e) {
            try {
                Class<?> tc = Class.forName("org.keycloak.util.Time"); //$NON-NLS-1$
                Method method = tc.getMethod("currentTime"); //$NON-NLS-1$
                Object object = method.invoke(null);
                return (Integer) object;
            } catch (Throwable e1) {
                return (int) (System.currentTimeMillis() / 1000);
            }
        }
    }

}
