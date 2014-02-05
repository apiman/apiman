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

package org.overlord.apiman.tools.devsvr.api;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.naming.InitialContext;
import javax.servlet.DispatcherType;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
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
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.weld.environment.servlet.BeanManagerResourceBindingListener;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.servlet.ConversationPropagationFilter;
import org.overlord.apiman.tools.devsvr.rest.ApiManDtDevServerApplication;
import org.overlord.commons.dev.server.DevServer;
import org.overlord.commons.dev.server.DevServerEnvironment;
import org.overlord.commons.gwt.server.filters.SimpleCorsFilter;
import org.overlord.commons.i18n.server.filters.LocaleFilter;

/**
 * A dev server for APIMan.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiManDtApiDevServer extends DevServer {

    private DataSource ds = null;

    /**
     * Main entry point.
     * @param args
     */
    public static void main(String [] args) throws Exception {
        ApiManDtApiDevServer devServer = new ApiManDtApiDevServer(args);
        devServer.enableDebug();
        devServer.go();
    }
    
    /**
     * @see org.overlord.commons.dev.server.DevServer#serverPort()
     */
    @Override
    protected int serverPort() {
        return 7070;
    }

    /**
     * Constructor.
     * @param args
     */
    public ApiManDtApiDevServer(String [] args) {
        super(args);
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#preConfig()
     */
    @Override
    protected void preConfig() {
        try {
            InitialContext ctx = new InitialContext();
            ctx.bind("java:datasources", new InitialContext());
            ds = createInMemoryDatasource();
            ctx.bind("java:datasources/ApiManDT", ds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an in-memory datasource.
     * @throws SQLException
     */
    private static DataSource createInMemoryDatasource() throws SQLException {
        System.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(Driver.class.getName());
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        Connection connection = ds.getConnection();
        connection.close();
        System.out.println("DataSource created and bound to JNDI.");
        return ds;
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#createDevEnvironment()
     */
    @Override
    protected ApiManDtApiDevServerEnvironment createDevEnvironment() {
        return new ApiManDtApiDevServerEnvironment(args);
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#addModules(org.overlord.commons.dev.server.ApiManDtApiDevServerEnvironment)
     */
    @Override
    protected void addModules(DevServerEnvironment environment) {
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#addModulesToJetty(org.overlord.commons.dev.server.ApiManDtApiDevServerEnvironment, org.eclipse.jetty.server.handler.ContextHandlerCollection)
     */
    @Override
    protected void addModulesToJetty(DevServerEnvironment environment, ContextHandlerCollection handlers)
            throws Exception {
        /* *************
         * APIMan DT API
         * ************* */
        ServletContextHandler apiManServer = new ServletContextHandler(ServletContextHandler.SESSIONS);
        apiManServer.setContextPath("/apiman-api");
        apiManServer.addEventListener(new Listener());
        apiManServer.addEventListener(new BeanManagerResourceBindingListener());
        apiManServer.addEventListener(new ResteasyBootstrap());
        apiManServer.addFilter(LocaleFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(SimpleCorsFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(ConversationPropagationFilter.class, "*", EnumSet.of(DispatcherType.REQUEST));
        ServletHolder resteasyServlet = new ServletHolder(new HttpServletDispatcher());
        resteasyServlet.setInitParameter("javax.ws.rs.Application", ApiManDtDevServerApplication.class.getName());
        apiManServer.addServlet(resteasyServlet, "/*");

        apiManServer.setInitParameter("resteasy.injector.factory", "org.jboss.resteasy.cdi.CdiInjectorFactory");
        apiManServer.setInitParameter("resteasy.scan", "true");
        apiManServer.setInitParameter("resteasy.servlet.mapping.prefix", "");

        // Add the web contexts to jetty
        handlers.addHandler(apiManServer);
    }

    /**
     * @see org.overlord.commons.dev.server.DevServer#postStart(org.overlord.commons.dev.server.ApiManDtApiDevServerEnvironment)
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
