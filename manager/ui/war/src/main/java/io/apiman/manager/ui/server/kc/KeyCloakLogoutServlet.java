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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;

/**
 * Used to log the user out of apiman when keycloak is the auth provider.
 *
 * @author eric.wittmann@redhat.com
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class KeyCloakLogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1095811654448544162L;

    /**
     * Constructor.
     */
    public KeyCloakLogoutServlet() {
    }

    /**
     * In setups where the Keycloak backend URL is different to the frontend URL, Keycloak's Java adapter currently
     * (incorrectly, AFAICT?) uses the frontend URL for logging out.
     * <p>
     * For example, this means it won't actually log out in situations where the internal Docker network has a different
     * name to the external network.
     * <p>
     * We work around this by forcing the backend URL with a delegate. It's not pretty...
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        forceBackendLogout(req);
        if (req.getSession() != null && !req.getSession().isNew()) {
            req.getSession().invalidate();
        }
        resp.sendRedirect("/apimanui"); //$NON-NLS-1$
    }

    // Evil, don't look!
    private void forceBackendLogout(HttpServletRequest req) {
        RefreshableKeycloakSecurityContext session = (RefreshableKeycloakSecurityContext) req.getAttribute("org.keycloak.KeycloakSecurityContext");
        KeycloakDeployment deployment = new DeploymentWithLogoutViaBackend(session.getDeployment());
        session.logout(deployment);
    }
}
