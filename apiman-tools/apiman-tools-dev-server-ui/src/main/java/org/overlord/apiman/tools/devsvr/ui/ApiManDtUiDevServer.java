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
import org.jboss.errai.bus.server.servlet.DefaultBlockingServlet;
import org.jboss.weld.environment.servlet.BeanManagerResourceBindingListener;
import org.jboss.weld.environment.servlet.Listener;
import org.overlord.apiman.dt.ui.server.ApiManUI;
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
        return "apiman-dt-ui";
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#preConfig()
     */
    @Override
    protected void preConfig() {
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
        environment.addModule("apiman-dt-ui",
                new WebAppModuleFromIDEDiscoveryStrategy(ApiManUI.class),
                new ErraiWebAppModuleFromMavenDiscoveryStrategy(ApiManUI.class));
        environment.addModule("overlord-commons-uiheader",
                new JarModuleFromIDEDiscoveryStrategy(OverlordHeaderDataJS.class, "src/main/resources/META-INF/resources"),
                new JarModuleFromMavenDiscoveryStrategy(OverlordHeaderDataJS.class, "/META-INF/resources"));
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
        apiManDtUI.setWelcomeFiles(new String[] { "index.html" });
        apiManDtUI.setSecurityHandler(createSecurityHandler());
        apiManDtUI.setContextPath("/apiman-ui");
        apiManDtUI.setWelcomeFiles(new String[] { "index.html" });
        apiManDtUI.setResourceBase(environment.getModuleDir("apiman-dt-ui").getCanonicalPath());
        apiManDtUI.setInitParameter("errai.properties", "/WEB-INF/errai.properties");
        apiManDtUI.setInitParameter("login.config", "/WEB-INF/login.config");
        apiManDtUI.setInitParameter("users.properties", "/WEB-INF/users.properties");
        apiManDtUI.addEventListener(new Listener());
        apiManDtUI.addEventListener(new BeanManagerResourceBindingListener());
        apiManDtUI.addFilter(GWTCacheControlFilter.class, "/app/*", EnumSet.of(DispatcherType.REQUEST));
        apiManDtUI.addFilter(ResourceCacheControlFilter.class, "/css/*", EnumSet.of(DispatcherType.REQUEST));
        apiManDtUI.addFilter(ResourceCacheControlFilter.class, "/images/*", EnumSet.of(DispatcherType.REQUEST));
        apiManDtUI.addFilter(ResourceCacheControlFilter.class, "/js/*", EnumSet.of(DispatcherType.REQUEST));
        apiManDtUI.addFilter(LocaleFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        // Servlets
        ServletHolder erraiServlet = new ServletHolder(DefaultBlockingServlet.class);
        erraiServlet.setInitOrder(1);
        apiManDtUI.addServlet(erraiServlet, "*.erraiBus");
        ServletHolder headerDataServlet = new ServletHolder(OverlordHeaderDataJS.class);
        headerDataServlet.setInitParameter("app-id", "apiman-dt-ui");
        apiManDtUI.addServlet(headerDataServlet, "/js/overlord-header-data.js");
        // File resources
        ServletHolder resources = new ServletHolder(new MultiDefaultServlet());
        resources.setInitParameter("resourceBase", "/");
        resources.setInitParameter("resourceBases", environment.getModuleDir("apiman-dt-ui").getCanonicalPath()
                + "|" + environment.getModuleDir("overlord-commons-uiheader").getCanonicalPath());
        resources.setInitParameter("dirAllowed", "true");
        resources.setInitParameter("pathInfoOnly", "false");
        String[] fileTypes = new String[] { "html", "js", "css", "png", "gif" };
        for (String fileType : fileTypes) {
            apiManDtUI.addServlet(resources, "*." + fileType);
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
            l.putUser(user, Credential.getCredential(user), new String[] {"user"});
        }
        l.setName("apimanrealm");

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"user"});
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("apimanrealm");
        csh.addConstraintMapping(cm);
        csh.setLoginService(l);

        return csh;
    }

    private static final String [] USERS = { "admin", "eric", "gary", "kurt" };
}
