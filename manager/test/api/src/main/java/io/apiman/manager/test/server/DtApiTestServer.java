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
package io.apiman.manager.test.server;

import io.apiman.common.servlet.AuthenticationFilter;
import io.apiman.manager.api.security.impl.DefaultSecurityContextFilter;
import io.apiman.manager.api.war.jetty8.JettyDtApiApplication;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.servlet.DispatcherType;

import org.apache.commons.dbcp.BasicDataSource;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Credential;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.weld.environment.servlet.BeanManagerResourceBindingListener;
import org.jboss.weld.environment.servlet.Listener;
import org.overlord.commons.gwt.server.filters.SimpleCorsFilter;
import org.overlord.commons.i18n.server.filters.LocaleFilter;

/**
 * This class starts up an embedded Jetty test server so that integration tests
 * can be performed.
 *
 * @author eric.wittmann@redhat.com
 */
public class DtApiTestServer {

    private BasicDataSource ds = null;
    private Server server;
    
    /**
     * Constructor.
     */
    public DtApiTestServer() {
    }
    
    /**
     * Start/run the server.
     */
    public void start() throws Exception {
        long startTime = System.currentTimeMillis();
        System.out.println("**** Starting Server (" + getClass().getSimpleName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        preStart();

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        addModulesToJetty(handlers);

        // Create the server.
        int serverPort = serverPort();
        server = new Server(serverPort);
        server.setHandler(handlers);
        server.start();
        long endTime = System.currentTimeMillis();
        System.out.println("******* Started in " + (endTime - startTime) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Stop the server.
     * @throws Exception
     */
    public void stop() throws Exception {
        server.stop();
        ds.close();
        InitialContext ctx = new InitialContext();
        ctx.unbind("java:comp/env/jdbc/ApiManDT"); //$NON-NLS-1$
    }

    /**
     * The server port.
     */
    public int serverPort() {
        return 7070;
    }

    /**
     * Stuff to do before the server is started.
     */
    protected void preStart() {
        System.setProperty("hibernate.hbm2ddl.auto", "create-drop"); //$NON-NLS-1$ //$NON-NLS-2$

        try {
            InitialContext ctx = new InitialContext();
            ensureCtx(ctx, "java:/comp/env"); //$NON-NLS-1$
            ensureCtx(ctx, "java:/comp/env/jdbc"); //$NON-NLS-1$
            ds = createInMemoryDatasource();
            ctx.bind("java:/comp/env/jdbc/ApiManDT", ds); //$NON-NLS-1$
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Ensure that the given name is bound to a context.
     * @param ctx
     * @param name
     * @throws NamingException 
     */
    private void ensureCtx(InitialContext ctx, String name) throws NamingException {
        try {
            ctx.bind(name, new InitialContext());
        } catch (NameAlreadyBoundException e) {
            // this is ok
        }
    }

    /**
     * Creates an in-memory datasource.
     * @throws SQLException
     */
    private static BasicDataSource createInMemoryDatasource() throws SQLException {
        System.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect"); //$NON-NLS-1$ //$NON-NLS-2$
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(Driver.class.getName());
        ds.setUsername("sa"); //$NON-NLS-1$
        ds.setPassword(""); //$NON-NLS-1$
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"); //$NON-NLS-1$
        Connection connection = ds.getConnection();
        connection.close();
        System.out.println("DataSource created and bound to JNDI."); //$NON-NLS-1$
        return ds;
    }

    /**
     * Configure the web application(s).
     * @param handlers
     * @throws Exception
     */
    protected void addModulesToJetty(ContextHandlerCollection handlers) throws Exception {
        /* *************
         * APIMan DT API
         * ************* */
        ServletContextHandler apiManServer = new ServletContextHandler(ServletContextHandler.SESSIONS);
        apiManServer.setSecurityHandler(createSecurityHandler());
        apiManServer.setContextPath("/apiman"); //$NON-NLS-1$
        apiManServer.addEventListener(new Listener());
        apiManServer.addEventListener(new BeanManagerResourceBindingListener());
        apiManServer.addEventListener(new ResteasyBootstrap());
        apiManServer.addFilter(DatabaseSeedFilter.class, "/db-seeder", EnumSet.of(DispatcherType.REQUEST)); //$NON-NLS-1$
        apiManServer.addFilter(LocaleFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST)); //$NON-NLS-1$
        apiManServer.addFilter(SimpleCorsFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST)); //$NON-NLS-1$
        apiManServer.addFilter(AuthenticationFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST)); //$NON-NLS-1$
        apiManServer.addFilter(DefaultSecurityContextFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST)); //$NON-NLS-1$
        ServletHolder resteasyServlet = new ServletHolder(new HttpServletDispatcher());
        resteasyServlet.setInitParameter("javax.ws.rs.Application", JettyDtApiApplication.class.getName()); //$NON-NLS-1$
        apiManServer.addServlet(resteasyServlet, "/*"); //$NON-NLS-1$

        apiManServer.setInitParameter("resteasy.injector.factory", "org.jboss.resteasy.cdi.CdiInjectorFactory"); //$NON-NLS-1$ //$NON-NLS-2$
        apiManServer.setInitParameter("resteasy.scan", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        apiManServer.setInitParameter("resteasy.servlet.mapping.prefix", ""); //$NON-NLS-1$ //$NON-NLS-2$

        // Add the web contexts to jetty
        handlers.addHandler(apiManServer);
        
        /* *************
         * Mock Gateway (to test publishing of Services from dt to rt)
         * ************* */
        ServletContextHandler mockGatewayServer = new ServletContextHandler(ServletContextHandler.SESSIONS);
        mockGatewayServer.setSecurityHandler(createSecurityHandler());
        mockGatewayServer.setContextPath("/mock-gateway"); //$NON-NLS-1$
        ServletHolder mockGatewayServlet = new ServletHolder(new MockGatewayServlet());
        mockGatewayServer.addServlet(mockGatewayServlet, "/*"); //$NON-NLS-1$

        // Add the web contexts to jetty
        handlers.addHandler(mockGatewayServer);
    }

    /**
     * Creates a basic auth security handler.
     */
    private SecurityHandler createSecurityHandler() {
        HashLoginService l = new HashLoginService();
        for (String [] userInfo : TestUsers.USERS) {
            String user = userInfo[0];
            String pwd = userInfo[1];
            String[] roles = new String[] { "apiuser" }; //$NON-NLS-1$
            if (user.startsWith("admin")) //$NON-NLS-1$
                roles = new String[] { "apiuser", "apiadmin"}; //$NON-NLS-1$ //$NON-NLS-2$
            l.putUser(user, Credential.getCredential(pwd), roles);
        }
        l.setName("apimanrealm"); //$NON-NLS-1$

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("apimanrealm"); //$NON-NLS-1$
        csh.setLoginService(l);

        return csh;
    }

}
