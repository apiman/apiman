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

import io.apiman.manager.ui.server.IUIConfig;
import io.apiman.manager.ui.server.auth.ITokenGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * Base class for UI servlets.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractUIServlet extends HttpServlet {

    private static final long serialVersionUID = -7455553362628233074L;

    private IUIConfig config;
    private transient ITokenGenerator tokenGenerator;

    /**
     * Constructor.
     */
    public AbstractUIServlet() {
    }

    /**
     * @return the UI config
     */
    protected IUIConfig getConfig() {
        if (config == null) {
            config = ServiceRegistryUtil.getSingleService(IUIConfig.class);
        }
        return config;
    }

    /**
     * Gets an instance of the configured token generator.
     */
    protected ITokenGenerator getTokenGenerator() throws ServletException {
        if (tokenGenerator == null) {
            String tokenGeneratorClassName = getConfig().getManagementApiAuthTokenGenerator();
            if (tokenGeneratorClassName == null)
                throw new ServletException("No token generator class specified."); //$NON-NLS-1$
            try {
                Class<?> c = Class.forName(tokenGeneratorClassName);
                tokenGenerator = (ITokenGenerator) c.newInstance();
            } catch (Exception e) {
                throw new ServletException("Error creating token generator."); //$NON-NLS-1$
            }
        }
        return tokenGenerator;
    }

}
