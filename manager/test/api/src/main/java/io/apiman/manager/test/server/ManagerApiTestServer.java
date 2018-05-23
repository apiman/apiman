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
import io.apiman.gateway.engine.es.DefaultESClientFactory;
import io.apiman.manager.api.security.impl.DefaultSecurityContextFilter;
import io.apiman.manager.api.war.TransactionWatchdogFilter;
import io.apiman.manager.test.util.ManagerTestUtils;
import io.apiman.manager.test.util.ManagerTestUtils.TestType;
import io.apiman.test.common.util.TestUtil;
import io.searchbox.client.JestClient;
import io.searchbox.indices.ClearCache;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.Flush;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
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

import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

/**
 * This class starts up an embedded Jetty test server so that integration tests
 * can be performed.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({"nls", "javadoc"})
public class ManagerApiTestServer {

    private static final String ES_CLUSTER_NAME = "_apimantest";
    public static JestClient ES_CLIENT = null;

    /*
     * The jetty server
     */
    private Server server;

    /*
     * DataSource created - only if using JPA
     */
    private BasicDataSource ds = null;

    /*
     * The elasticsearch node and client - only if using ES
     */
    private EmbeddedElastic node = null;
    private JestClient client = null;
    private static final int JEST_TIMEOUT = 6000;
    private static final Integer ES_DEFAULT_PORT = 19250;
    private static final String ES_DEFAULT_HOST = "localhost";
    private static final String ES_DEFAULT_INDEX = "apiman_manager";

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

    private void deleteAndFlush() throws IOException {
        if (client != null) {
            //System.out.println("FLUSH AND DELETE>>>>>>");
            client.execute(new DeleteIndex.Builder(ES_DEFAULT_INDEX).build());
            client.execute(new Flush.Builder().build());
            DefaultESClientFactory.clearClientCache();
        }
    }

    /**
     * Stop the server.
     * @throws Exception
     */
    public void stop() throws Exception {
        if (node != null) {
            deleteAndFlush();
            node.stop();
            System.out.println("================ STOPPED ES ================ ");
        }
        server.stop();
        if (ds != null) {
            ds.close();
            InitialContext ctx = TestUtil.initialContext();
            ctx.unbind("java:comp/env/jdbc/ApiManagerDS");
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
            TestUtil.setProperty("apiman.hibernate.hbm2ddl.auto", "create-drop");
            TestUtil.setProperty("apiman.hibernate.connection.datasource", "java:/comp/env/jdbc/ApiManagerDS");
            try {
                InitialContext ctx = TestUtil.initialContext();
                TestUtil.ensureCtx(ctx, "java:/comp/env");
                TestUtil.ensureCtx(ctx, "java:/comp/env/jdbc");
                String dbOutputPath = System.getProperty("apiman.test.h2-output-dir", null);
                if (dbOutputPath != null) {
                    ds = createFileDatasource(new File(dbOutputPath));
                } else {
                    ds = createInMemoryDatasource();
                }
                ctx.bind("java:/comp/env/jdbc/ApiManagerDS", ds);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (ManagerTestUtils.getTestType() == TestType.es) {
            try {
                File esDownloadCache = new File(System.getenv("HOME") + "/.cache/apiman/elasticsearch");
                esDownloadCache.getParentFile().mkdirs();

                node = EmbeddedElastic.builder()
                            .withElasticVersion("5.6.9")
                            .withDownloadDirectory(esDownloadCache)
                            .withSetting(PopularProperties.CLUSTER_NAME, "apiman")
                            .withSetting(PopularProperties.HTTP_PORT, ES_DEFAULT_PORT)
                            .withCleanInstallationDirectoryOnStop(true)
                            .withStartTimeout(1, TimeUnit.MINUTES)
                            .build()
                            .start();
            } catch (IOException | InterruptedException e) {
                 throw new RuntimeException(e);
            }
            // Create client before flush
            client = createJestClient();
            deleteAndFlush();
            // Recreate client again as index needs re-initialising (see index-settings.json) -- TODO refactor this
            client = createJestClient();
            ES_CLIENT = client;
        }
    }

    private static JestClient createJestClient() {
        Map<String, String> config = new HashMap<>();
        config.put("client.protocol", "http");
        config.put("client.host", ES_DEFAULT_HOST);
        config.put("client.port", String.valueOf(ES_DEFAULT_PORT));
        config.put("client.timeout", String.valueOf(JEST_TIMEOUT));
        config.put("client.initialize", "true");
        return new DefaultESClientFactory().createClient(config, ES_DEFAULT_INDEX);
    }

    /**
     * Creates an in-memory datasource.
     * @throws SQLException
     */
    private static BasicDataSource createInMemoryDatasource() throws SQLException {
        TestUtil.setProperty("apiman.hibernate.dialect", "org.hibernate.dialect.H2Dialect");
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
        ServletHolder resteasyServlet = new ServletHolder(new HttpServletDispatcher());
        resteasyServlet.setInitParameter("javax.ws.rs.Application", TestManagerApiApplication.class.getName());
        apiManServer.addServlet(resteasyServlet, "/*");

        apiManServer.setInitParameter("resteasy.injector.factory", "org.jboss.resteasy.cdi.CdiInjectorFactory");
        apiManServer.setInitParameter("resteasy.scan", "true");
        apiManServer.setInitParameter("resteasy.servlet.mapping.prefix", "");

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
    }

    /**
     * Creates a basic auth security handler.
     */
    private SecurityHandler createSecurityHandler() {
        HashLoginService l = new HashLoginService();
        for (String [] userInfo : TestUsers.USERS) {
            String user = userInfo[0];
            String pwd = userInfo[1];
            String[] roles = new String[] { "apiuser" };
            if (user.startsWith("admin"))
                roles = new String[] { "apiuser", "apiadmin"};
            l.putUser(user, Credential.getCredential(pwd), roles);
        }
        l.setName("apimanrealm");

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("apimanrealm");
        csh.setLoginService(l);

        return csh;
    }

    public EmbeddedElastic getESNode() {
        return node;
    }

    public JestClient getESClient() {
        return client;
    }

    public void flush() throws IOException {
        if (client != null) {
            System.out.println("FLUSH>>>>>>");
            client.execute(new Flush.Builder().addIndex(ES_DEFAULT_INDEX).force().waitIfOngoing().build());
            client.execute(new ClearCache.Builder().addIndex(ES_DEFAULT_INDEX).build());
        }
    }
}
