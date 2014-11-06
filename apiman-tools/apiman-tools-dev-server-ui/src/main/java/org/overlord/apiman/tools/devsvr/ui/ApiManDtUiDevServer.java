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

package org.overlord.apiman.tools.devsvr.ui;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.overlord.apiman.dt.ui.client.shared.beans.ApiAuthType;
import org.overlord.apiman.dt.ui.server.UIConfig;
import org.overlord.apiman.dt.ui.server.auth.AuthTokenGenerator;
import org.overlord.apiman.dt.ui.server.servlets.ConfigurationServlet;
import org.overlord.apiman.dt.ui.server.servlets.TokenRefreshServlet;
import org.overlord.apiman.dt.ui.server.servlets.UrlFetchProxyServlet;
import org.overlord.commons.dev.server.DevServerEnvironment;
import org.overlord.commons.dev.server.ErraiDevServer;
import org.overlord.commons.dev.server.MultiDefaultServlet;
import org.overlord.commons.dev.server.discovery.ErraiWebAppModuleFromMavenDiscoveryStrategy;
import org.overlord.commons.dev.server.discovery.JarModuleFromIDEDiscoveryStrategy;
import org.overlord.commons.dev.server.discovery.JarModuleFromMavenDiscoveryStrategy;
import org.overlord.commons.dev.server.discovery.WebAppModuleFromIDEDiscoveryStrategy;
import org.overlord.commons.gwt.server.filters.GWTCacheControlFilter;
import org.overlord.commons.gwt.server.filters.ResourceCacheControlFilter;
import org.overlord.commons.i18n.server.filters.LocaleFilter;
import org.overlord.commons.ui.header.OverlordHeaderDataJS;

/**
 * A dev server for APIMan.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiManDtUiDevServer extends ErraiDevServer {

    private static final int API_PORT  = 7070;
    private static final int GATEWAY_PORT  = 6666;
    private static final String APIMAN_RT_API_SERVER_PORT = "apiman.api.server.port"; //$NON-NLS-1$
    private static final String APIMAN_RT_GATEWAY_SERVER_PORT = "apiman.gateway.server.port"; //$NON-NLS-1$

    /**
     * Main entry point.
     * @param args
     */
    public static void main(String [] args) throws Exception {
        ApiManDtUiDevServer devServer = new ApiManDtUiDevServer(args);
        devServer.enableDebug();
        devServer.go();
    }

    /**
     * Constructor.
     * @param args
     */
    public ApiManDtUiDevServer(String [] args) {
        super(args);
    }
    
    /**
     * @see org.overlord.commons.dev.server.ErraiDevServer#getErraiModuleId()
     */
    @Override
    protected String getErraiModuleId() {
        return "apiman-dt-ui"; //$NON-NLS-1$
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#preConfig()
     */
    @Override
    protected void preConfig() {
        int apiPort = getApiPort();
        int gatewayPort = getGatewayPort();
        
        System.setProperty(UIConfig.APIMAN_DT_UI_API_ENDPOINT,
                "http://localhost:" + apiPort + "/apiman-dt-api"); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty(UIConfig.APIMAN_DT_UI_API_AUTH_TYPE, ApiAuthType.authToken.toString());
        System.setProperty(UIConfig.APIMAN_DT_UI_API_AUTH_TOKEN_GENERATOR, AuthTokenGenerator.class.getName());
        System.setProperty(UIConfig.APIMAN_DT_UI_GATEWAY_URL, "http://localhost:" + gatewayPort + "/gateway"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return the gateway port to use
     */
    private static int getGatewayPort() {
        int port = GATEWAY_PORT;
        if (System.getProperty(APIMAN_RT_GATEWAY_SERVER_PORT) != null) {
            port = new Integer(System.getProperty(APIMAN_RT_GATEWAY_SERVER_PORT));
        }
        return port;
    }

    /**
     * @return the gateway port to use
     */
    private static int getApiPort() {
        int port = API_PORT;
        if (System.getProperty(APIMAN_RT_API_SERVER_PORT) != null) {
            port = new Integer(System.getProperty(APIMAN_RT_API_SERVER_PORT));
        }
        return port;
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#createDevEnvironment()
     */
    @Override
    protected ApiManDtUiDevServerEnvironment createDevEnvironment() {
        return new ApiManDtUiDevServerEnvironment(args);
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#addModules(org.overlord.commons.dev.server.ApiManDtUiDevServerEnvironment)
     */
    @Override
    protected void addModules(DevServerEnvironment environment) {
        environment.addModule("apiman-dt-ui", //$NON-NLS-1$
                new WebAppModuleFromIDEDiscoveryStrategy(UIConfig.class),
                new ErraiWebAppModuleFromMavenDiscoveryStrategy(UIConfig.class));
        environment.addModule("overlord-commons-uiheader", //$NON-NLS-1$
                new JarModuleFromIDEDiscoveryStrategy(OverlordHeaderDataJS.class, "src/main/resources/META-INF/resources"), //$NON-NLS-1$
                new JarModuleFromMavenDiscoveryStrategy(OverlordHeaderDataJS.class, "/META-INF/resources")); //$NON-NLS-1$
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#addModulesToJetty(org.overlord.commons.dev.server.ApiManDtUiDevServerEnvironment, org.eclipse.jetty.server.handler.ContextHandlerCollection)
     */
    @Override
    protected void addModulesToJetty(DevServerEnvironment environment, ContextHandlerCollection handlers)
            throws Exception {
        super.addModulesToJetty(environment, handlers);
        /* *************
         * APIMan DT UI
         * ************* */
        ServletContextHandler apiManDtUI = new ServletContextHandler(ServletContextHandler.SESSIONS);
        apiManDtUI.setWelcomeFiles(new String[] { "index.html" }); //$NON-NLS-1$
        apiManDtUI.setSecurityHandler(createSecurityHandler());
        apiManDtUI.setContextPath("/apiman-ui"); //$NON-NLS-1$
        apiManDtUI.setWelcomeFiles(new String[] { "index.html" }); //$NON-NLS-1$
        apiManDtUI.setResourceBase(environment.getModuleDir("apiman-dt-ui").getCanonicalPath()); //$NON-NLS-1$
        apiManDtUI.addFilter(GWTCacheControlFilter.class, "/app/*", EnumSet.of(DispatcherType.REQUEST)); //$NON-NLS-1$
        apiManDtUI.addFilter(ResourceCacheControlFilter.class, "/css/*", EnumSet.of(DispatcherType.REQUEST)); //$NON-NLS-1$
        apiManDtUI.addFilter(ResourceCacheControlFilter.class, "/images/*", EnumSet.of(DispatcherType.REQUEST)); //$NON-NLS-1$
        apiManDtUI.addFilter(ResourceCacheControlFilter.class, "/js/*", EnumSet.of(DispatcherType.REQUEST)); //$NON-NLS-1$
        apiManDtUI.addFilter(LocaleFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST)); //$NON-NLS-1$
        // Servlets
        ServletHolder headerDataServlet = new ServletHolder(OverlordHeaderDataJS.class);
        headerDataServlet.setInitParameter("app-id", "apiman-dt-ui"); //$NON-NLS-1$ //$NON-NLS-2$
        apiManDtUI.addServlet(headerDataServlet, "/js/overlord-header-data.nocache.js"); //$NON-NLS-1$
        apiManDtUI.addServlet(ConfigurationServlet.class, "/js/configuration.nocache.js"); //$NON-NLS-1$
        apiManDtUI.addServlet(TokenRefreshServlet.class, "/rest/tokenRefresh"); //$NON-NLS-1$
        apiManDtUI.addServlet(UrlFetchProxyServlet.class, "/proxies/fetch"); //$NON-NLS-1$
        // File resources
        ServletHolder resources = new ServletHolder(new MultiDefaultServlet());
        resources.setInitParameter("resourceBase", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        resources.setInitParameter("resourceBases", environment.getModuleDir("apiman-dt-ui").getCanonicalPath() //$NON-NLS-1$ //$NON-NLS-2$
                + "|" + environment.getModuleDir("overlord-commons-uiheader").getCanonicalPath()); //$NON-NLS-1$ //$NON-NLS-2$
        resources.setInitParameter("dirAllowed", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        resources.setInitParameter("pathInfoOnly", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        String[] fileTypes = new String[] { "html", "js", "css", "png", "gif", "woff", "ttf" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        for (String fileType : fileTypes) {
            apiManDtUI.addServlet(resources, "*." + fileType); //$NON-NLS-1$
        }

        // Add the web contexts to jetty
        handlers.addHandler(apiManDtUI);
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#postStart(org.overlord.commons.dev.server.ApiManDtUiDevServerEnvironment)
     */
    @Override
    protected void postStart(DevServerEnvironment environment) throws Exception {
    }

    /**
     * Creates a basic auth security handler.
     */
    private SecurityHandler createSecurityHandler() {
        HashLoginService l = new HashLoginService();
        for (String user : USERS) {
            String pwd = user;
            String[] roles = new String[] { "apiuser" }; //$NON-NLS-1$
            if (user.startsWith("admin")) //$NON-NLS-1$
                roles = new String[] { "apiuser", "apiadmin"}; //$NON-NLS-1$ //$NON-NLS-2$
            l.putUser(user, Credential.getCredential(pwd), roles);
        }
        l.setName("apimanrealm"); //$NON-NLS-1$

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"apiuser", "apiadmin"}); //$NON-NLS-1$ //$NON-NLS-2$
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*"); //$NON-NLS-1$

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("apimanrealm"); //$NON-NLS-1$
        csh.addConstraintMapping(cm);
        csh.setLoginService(l);
        csh.setSessionRenewedOnAuthentication(false);

        return csh;
    }

    private static final String [] USERS = { "admin", "eric", "gary", "kevin", "admin2", "bwayne", "ckent", "dprince" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
}
