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
package io.apiman.manager.ui.server.servlets;

import io.apiman.manager.ui.client.shared.beans.BearerTokenCredentialsBean;
import io.apiman.manager.ui.server.auth.ITokenGenerator;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The token refresh servlet - provides a JSON REST endpoint that the UI
 * can use to acquire a refresh token.  This is implemented as a servlet
 * rather than a jax-rs endpoint just to keep the server-side of the UI
 * as brain-dead simple as possible.
 *
 * @author eric.wittmann@redhat.com
 */
public class TokenRefreshServlet extends AbstractUIServlet {

    private static final long serialVersionUID = 7721708152826837757L;
    private static Logger logger = LoggerFactory.getLogger(TokenRefreshServlet.class);

    private transient ITokenGenerator tokenGenerator;
    
    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        logger.debug("Refreshing authentication token."); //$NON-NLS-1$
        ITokenGenerator tokenGenerator = getTokenGenerator();
        String token = tokenGenerator.generateToken(req);
        int refresh = tokenGenerator.getRefreshPeriod();
        BearerTokenCredentialsBean btcb = new BearerTokenCredentialsBean();
        btcb.setToken(token);
        btcb.setRefreshPeriod(refresh);
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json"); //$NON-NLS-1$
        resp.setDateHeader("Date", System.currentTimeMillis()); //$NON-NLS-1$
        resp.setDateHeader("Expires", System.currentTimeMillis() - 86400000L); //$NON-NLS-1$
        resp.setHeader("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
        resp.setHeader("Cache-control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$
        mapper.writer().writeValue(resp.getOutputStream(), btcb);
    }
    
    /**
     * Gets an instance of the configured token generator.
     */
    private ITokenGenerator getTokenGenerator() {
        if (tokenGenerator == null) {
            String tokenGeneratorClassName = getConfig().getManagementApiAuthTokenGenerator();
            if (tokenGeneratorClassName == null)
                throw new RuntimeException("No token generator class specified."); //$NON-NLS-1$
            try {
                Class<?> c = Class.forName(tokenGeneratorClassName);
                tokenGenerator = (ITokenGenerator) c.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Error creating token generator."); //$NON-NLS-1$
            }
        }
        return tokenGenerator;
    }

}
