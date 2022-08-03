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

import io.apiman.common.servlet.ApimanCorsFilter;
import io.apiman.common.servlet.AuthenticationFilter;
import io.apiman.common.servlet.DisableCachingFilter;
import io.apiman.common.servlet.RootResourceFilter;
import io.apiman.manager.api.security.impl.DefaultSecurityContextFilter;
import io.apiman.manager.api.war.TransactionWatchdogFilter;
import io.apiman.manager.test.util.ManagerTestUtils;
import io.apiman.manager.test.util.ManagerTestUtils.TestType;
import io.apiman.test.common.util.TestUtil;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.servlet.DispatcherType;

import org.apache.commons.dbcp.BasicDataSource;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Credential;
import org.elasticsearch.client.RestHighLevelClient;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.weld.environment.servlet.BeanManagerResourceBindingListener;
import org.jboss.weld.environment.servlet.Listener;
import org.jdbi.v3.core.Jdbi;
import org.testcontainers.shaded.org.bouncycastle.util.Arrays;

/**
 * This class starts up an embedded Jetty test server so that integration tests
 * can be performed.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({"nls", "javadoc"})
public class ManagerApiTestServer {

    public static RestHighLevelClient ES_CLIENT = null;

    /*
     * The jetty server
     */
    private Server server;

    /*
     * DataSource created - only if using JPA
     */
    private BasicDataSource ds = null;

    /**
     * Constructor.
     */
    public ManagerApiTestServer(Map<String, String> config) {
    }

    public ManagerApiTestServer() {}

    /**
     * Start/run the server.
     */
    public void start() throws Exception {
        long startTime = System.currentTimeMillis();
        System.out.println("**** Starting Server (" + getClass().getSimpleName() + ")");
        preStart();

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        addModulesToJetty(handlers);

        // Create the server.
        int serverPort = serverPort();
        server = new Server(serverPort);
        server.setHandler(handlers);
        server.start();
        long endTime = System.currentTimeMillis();
        System.out.println("******* Started in " + (endTime - startTime) + "ms");
    }

    /**
     * Stop the server.
     * @throws Exception
     */
    public void stop() throws Exception {
        server.stop();
        if (ds != null) {
            // Drop everything from the in-memory DB in case a subsequent run re-uses this: could end up with unexpected DB state.
            try (var conn = ds.getConnection()){
                conn.prepareStatement("DROP ALL OBJECTS DELETE FILES").executeUpdate();
                conn.commit();
            };
            ds.close();
            InitialContext ctx = TestUtil.initialContext();
            ctx.unbind("java:/apiman/datasources/apiman-manager");
        }
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
    protected void preStart() throws Exception {
        if (ManagerTestUtils.getTestType() == TestType.jpa) {
            TestUtil.setProperty("liquibase.should.run", "false");
            TestUtil.setProperty("hibernate.hbm2ddl.import_files", "import.sql");
            TestUtil.setProperty("apiman.hibernate.hbm2ddl.auto", "create-drop");
            TestUtil.setProperty("apiman.hibernate.connection.datasource", "java:/apiman/datasources/apiman-manager");
            TestUtil.setProperty("apiman-manager.config.features.rest-response-should-contain-stacktraces", "true");

            // Set data directory to be test data dir
            File apimanConfigDir = new File("src/test/resources/apiman/config");
            if (!apimanConfigDir.exists()) {
                throw new NoSuchFileException("src/test/resources/apiman/config not found. If you are using Eclipse you may need "
                   + "to set the working directory manually (works automatically on all other platforms)");
            }
            TestUtil.setProperty("apiman.config.dir", apimanConfigDir.getAbsolutePath());
            var apimanDataDir = new File("src/test/resources/apiman/data");
            TestUtil.setProperty("apiman.data.dir", apimanDataDir.getAbsolutePath());

            try {
                InitialContext ctx = TestUtil.initialContext();
                TestUtil.ensureCtx(ctx, "java:/apiman");
                TestUtil.ensureCtx(ctx, "java:/apiman/datasources");
                String dbOutputPath = System.getProperty("apiman.test.h2-output-dir", null);
                if (dbOutputPath != null) {
                    ds = createFileDatasource(new File(dbOutputPath));
                } else {
                    ds = createInMemoryDatasource();
                }
                try {
                    // For H2 versions older than 1.4.200 this ensures JSON type exists.
                    Jdbi.create(ds).useHandle(h -> {
                        h.getConnection().setAutoCommit(false);
                        h.execute("CREATE domain IF NOT EXISTS json AS other");
                        h.commit();
                    });
                } catch (Exception e) {}
                ctx.bind("java:/apiman/datasources/apiman-manager", ds);
            } catch (NameAlreadyBoundException nbe) {
                nbe.printStackTrace();
            }
        }
    }

    /**
     * Creates an in-memory datasource.
     * @throws SQLException
     */
    private static BasicDataSource createInMemoryDatasource() throws SQLException {
        TestUtil.setProperty("apiman.hibernate.dialect", "io.apiman.manager.api.jpa.ApimanH2Dialect");
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(Driver.class.getName());
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setUrl("jdbc:h2:mem:test-apiman-inmem;DB_CLOSE_DELAY=-1");
        // Use this for trace level JDBC logging
        Connection connection = ds.getConnection();
        connection.close();
        System.out.println("DataSource created and bound to JNDI.");
        return ds;
    }

    /**
     * Creates an h2 file based datasource.
     * @throws SQLException
     */
    private static BasicDataSource createFileDatasource(File outputDirectory) throws SQLException {
        TestUtil.setProperty("apiman.hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(Driver.class.getName());
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setUrl("jdbc:h2:" + outputDirectory.toString() + "/apiman-manager-api;MVCC=true");
        Connection connection = ds.getConnection();
        connection.close();
        System.out.println("DataSource created and bound to JNDI.");
        return ds;
    }

    /**
     * Configure the web application(s).
     * @param handlers
     * @throws Exception
     */
    protected void addModulesToJetty(ContextHandlerCollection handlers) throws Exception {
        /* *************
         * Manager API
         * ************* */
        ServletContextHandler apiManServer = new ServletContextHandler(ServletContextHandler.SESSIONS);
        apiManServer.setSecurityHandler(createSecurityHandler());
        apiManServer.setContextPath("/apiman");
        apiManServer.addEventListener(new Listener());
        apiManServer.addEventListener(new BeanManagerResourceBindingListener());
        apiManServer.addEventListener(new ResteasyBootstrap());
        apiManServer.addFilter(DatabaseSeedFilter.class, "/db-seeder", EnumSet.of(DispatcherType.REQUEST));
//        apiManServer.addFilter(LocaleFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(ApimanCorsFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(DisableCachingFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        configureAuthentication(apiManServer);
        apiManServer.addFilter(DefaultSecurityContextFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(TransactionWatchdogFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(RootResourceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));


        apiManServer.setInitParameter("resteasy.injector.factory", "org.jboss.resteasy.cdi.CdiInjectorFactory");
        // apiManServer.setInitParameter("resteasy.scan", "true");
        apiManServer.setInitParameter("resteasy.scan.providers", "true");
        apiManServer.setInitParameter("resteasy.scan.resources", "true");
        apiManServer.setInitParameter("resteasy.servlet.mapping.prefix", "");
        //apiManServer.setInitParameter("resteasy.providers", "io.apiman.manager.api.providers.JacksonObjectMapperProvider");

        ServletHolder resteasyServlet = new ServletHolder(new HttpServletDispatcher());
        resteasyServlet.setInitParameter("javax.ws.rs.Application", TestManagerApiApplication.class.getName());
        apiManServer.addServlet(resteasyServlet, "/*");

        // Add the web contexts to jetty
        handlers.addHandler(apiManServer);

        /* *************
         * Mock Gateway (to test publishing of APIs from dt to rt)
         * ************* */
        ServletContextHandler mockGatewayServer = new ServletContextHandler(ServletContextHandler.SESSIONS);
        mockGatewayServer.setSecurityHandler(createSecurityHandler());
        mockGatewayServer.setContextPath("/mock-gateway");
        ServletHolder mockGatewayServlet = new ServletHolder(new MockGatewayServlet());
        mockGatewayServer.addServlet(mockGatewayServlet, "/*");

        // Add the web contexts to jetty
        handlers.addHandler(mockGatewayServer);
    }

    /**
     * @param apiManServer
     */
    private void configureAuthentication(ServletContextHandler apiManServer) {
        apiManServer.addFilter(AuthenticationFilter.class, "/actions/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(AuthenticationFilter.class, "/system/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(AuthenticationFilter.class, "/currentuser/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(AuthenticationFilter.class, "/gateways/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(AuthenticationFilter.class, "/organizations/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(AuthenticationFilter.class, "/permissions/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(AuthenticationFilter.class, "/plugins/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(AuthenticationFilter.class, "/policyDefs/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(AuthenticationFilter.class, "/roles/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(AuthenticationFilter.class, "/search/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(AuthenticationFilter.class, "/users/*", EnumSet.of(DispatcherType.REQUEST));
        apiManServer.addFilter(AuthenticationFilter.class, "/developer/*", EnumSet.of(DispatcherType.REQUEST));
    }

    /**
     * Creates a basic auth security handler.
     */
    private SecurityHandler createSecurityHandler() {
        HashLoginService l = new HashLoginService();
        UserStore userStore = new UserStore();
        l.setUserStore(userStore);

        for (String [] userInfo : TestUsers.USERS) {
            String user = userInfo[0];
            String pwd = userInfo[1];
            String[] roles = userInfo[4].split(",");
            if (user.startsWith("admin")) {
                roles = Arrays.append(roles, "apiadmin");
            }
            userStore.addUser(user, Credential.getCredential(pwd), roles);
        }
        l.setName("apimanrealm");

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("apimanrealm");
        csh.setLoginService(l);

        return csh;
    }

    public void flush() {
    }
}
