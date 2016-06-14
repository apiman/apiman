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
package io.apiman.manager.platform.servlets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.manager.ui.server.UIVersion;
import io.apiman.manager.ui.server.auth.ITokenGenerator;
import io.apiman.manager.ui.server.beans.*;
import io.apiman.manager.ui.server.servlets.AbstractUIServlet;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Generates the initial configuration JSON used by the UI when it first loads
 * up. This initial JSON is loaded into the client-side.
 *
 * Also responsible for pushing updated configuration to the client if it
 * changes.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConfigurationServlet extends AbstractUIServlet {

    private static final long serialVersionUID = -1529967410524613367L;

    /**
     * Constructor.
     */
    public ConfigurationServlet() {
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        try {
            Class.forName("io.apiman.manager.platform.WarUIConfig"); //$NON-NLS-1$
        } catch (Throwable t) {
            t.printStackTrace();
        }

        JsonGenerator g = null;
        try {
            response.getOutputStream().write("window.APIMAN_CONFIG_DATA = ".getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
            JsonFactory f = new JsonFactory();
            g = f.createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            g.setCodec(mapper);
            g.useDefaultPrettyPrinter();

            // Get data from various sources.
            String endpoint = getConfig().getManagementApiEndpoint();
            if (endpoint == null) {
                endpoint = getDefaultEndpoint(request);
            }
            UIVersion version = UIVersion.get();
            ApiAuthType authType = getConfig().getManagementApiAuthType();

            ConfigurationBean configBean = new ConfigurationBean();
            configBean.setApiman(new AppConfigurationBean());
            configBean.setUser(new UserConfigurationBean());
            configBean.setUi(new UiConfigurationBean());
            configBean.setApi(new ApiConfigurationBean());
            configBean.getUi().setHeader("community"); //$NON-NLS-1$
            configBean.getUi().setMetrics(getConfig().isMetricsEnabled());
            configBean.getApiman().setVersion(version.getVersionString());
            configBean.getApiman().setBuiltOn(version.getVersionDate());
            configBean.getApiman().setLogoutUrl(getConfig().getLogoutUrl());
            configBean.getUser().setUsername(request.getRemoteUser());
            configBean.getApi().setEndpoint(endpoint);
            configBean.getApi().setAuth(new ApiAuthConfigurationBean());
            switch (authType) {
                case authToken: {
                    configBean.getApi().getAuth().setType(ApiAuthType.authToken);
                    String tokenGeneratorClassName = getConfig().getManagementApiAuthTokenGenerator();
                    if (tokenGeneratorClassName == null) {
                        throw new ServletException("No token generator class specified."); //$NON-NLS-1$
                    }
                    Class<?> c = Class.forName(tokenGeneratorClassName);
                    ITokenGenerator tokenGenerator = (ITokenGenerator) c.newInstance();
                    configBean.getApi().getAuth().setBearerToken(tokenGenerator.generateToken(request));
                    break;
                }
                case basic: {
                    configBean.getApi().getAuth().setType(ApiAuthType.basic);
                    configBean.getApi().getAuth().setBasic(new BasicAuthCredentialsBean());
                    String username = getConfig().getManagementApiAuthUsername();
                    String password = getConfig().getManagementApiAuthPassword();
                    configBean.getApi().getAuth().getBasic().setUsername(username);
                    configBean.getApi().getAuth().getBasic().setPassword(password);
                    break;
                }
                case bearerToken: {
                    configBean.getApi().getAuth().setType(ApiAuthType.bearerToken);
                    String tokenGeneratorClassName = getConfig().getManagementApiAuthTokenGenerator();
                    if (tokenGeneratorClassName == null)
                        throw new ServletException("No token generator class specified."); //$NON-NLS-1$
                    Class<?> c = Class.forName(tokenGeneratorClassName);
                    ITokenGenerator tokenGenerator = (ITokenGenerator) c.newInstance();
                    configBean.getApi().getAuth().setBearerToken(tokenGenerator.generateToken(request));
                    break;
                }
                case samlBearerToken: {
                    configBean.getApi().getAuth().setType(ApiAuthType.samlBearerToken);
                    String tokenGeneratorClassName = getConfig().getManagementApiAuthTokenGenerator();
                    if (tokenGeneratorClassName == null)
                        throw new ServletException("No token generator class specified."); //$NON-NLS-1$
                    Class<?> c = Class.forName(tokenGeneratorClassName);
                    ITokenGenerator tokenGenerator = (ITokenGenerator) c.newInstance();
                    configBean.getApi().getAuth().setBearerToken(tokenGenerator.generateToken(request));
                    break;
                }
            }
            g.writeObject(configBean);

            g.flush();
            response.getOutputStream().write(";".getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            IOUtils.closeQuietly(g);
        }
    }
}
