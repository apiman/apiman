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
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.Flush;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.naming.InitialContext;
import javax.servlet.DispatcherType;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Credential;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.weld.environment.servlet.BeanManagerResourceBindingListener;
import org.jboss.weld.environment.servlet.Listener;

/**
 * This class starts up an embedded Jetty test server so that integration tests
 * can be performed.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({"nls", "javadoc"})
public class ManagerApiTestServer {

    private static final String ES_CLUSTER_NAME = "_apimantest";
    //public static Client ES_CLIENT = null;
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
    private Node node = null;
    //private Client client = null;
    private JestClient client = null;
    private static final int JEST_TIMEOUT = 6000;
    private static final String ES_DEFAULT_PORT = "9200";
    private static final String ES_DEFAULT_HOST = "192.168.99.100";

    //String connectionUrl = "http://localhost:9200";
    //String connectionUrl = "http://localhost:6500";

    /**
     * Constructor.
     */
    public ManagerApiTestServer(Map<String, String> config) {
        //super(config);
    }

    @SuppressWarnings("serial")
    public ManagerApiTestServer() {
//
//        super(new LinkedHashMap<String, String>() {{
//            put("client.initialize", "false");
//            put("client.type", "jest");
//            put("client.index", "apiman_manager");
//            put("client.port", ES_DEFAULT_PORT);// FIXME
//            put("client.host", ES_DEFAULT_HOST);
//        }});
    }

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
            ds.close();
            InitialContext ctx = TestUtil.initialContext();
            ctx.unbind("java:comp/env/jdbc/ApiManagerDS");
        }
        //if (node != null) {
            //if ("true".equals(System.getProperty("apiman.test.es-delete-index", "true"))) {
            	client.execute(new DeleteIndex.Builder("apiman_manager").build());
                client.execute(new Flush.Builder().build());
            //}
        //}

        try {
            CountDownLatch latch = new CountDownLatch(1);

            // Important! Or will get cached client that assumes the DB schema has already been created
            // and subtly horrible things will happen, and you'll waste a whole day debugging it! :-)
            DefaultESClientFactory.clearClientCache();

//            getClient().executeAsync(new Delete.Builder(getDefaultIndexName()).build(),
//                    new JestResultHandler<JestResult>() {
//
//                @Override
//                public void completed(JestResult result) {
//                    latch.countDown();
//                    System.out.println("=== Deleted index: " + result.getJsonString());
//                }
//
//                @Override
//                public void failed(Exception ex) {
//                    latch.countDown();
//                    System.err.println("=== Failed to delete index: " + ex.getMessage());
//                    throw new RuntimeException(ex);
//                }
//            });

            Flush flush = new Flush.Builder().build();
//            getClient().execute(flush);
            Thread.sleep(100);

            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
        if (ManagerTestUtils.getTestType() == TestType.es && node == null) {
            System.out.println("Creating the ES node.");
            File esHome = new File("target/es");
            String esHomeSP = System.getProperty("apiman.test.es-home", null);
            if (esHomeSP != null) {
                esHome = new File(esHomeSP);
            }
            if (esHome.isDirectory()) {
                FileUtils.deleteDirectory(esHome);
            }

            String clusterName = System.getProperty("apiman.test.es-cluster-name", ES_CLUSTER_NAME);

            Builder settings = Settings.builder()
                    .put("path.home", esHome.getAbsolutePath())
                    .put("http.port", "6500-6600")
                    .put("transport.tcp.port", "6600-6700")
                    .put("transport.type", "local")
//                    .put("discovery.type", "local")
                    .put("cluster.name", clusterName)
                    .put("http.type", "netty4")
                    .put("node.ingest", "true");

            boolean isPersistent = "true".equals(System.getProperty("apiman.test.es-persistence", "false"));
            if (!isPersistent) {
                System.out.println("Creating non-persistent ES");
                //settings.put("index.store.type", "mmapfs");
//                settings.put("index.store.type", "memory");//.put("gateway.type", "none");
                        //.put("index.number_of_shards", 1).put("index.number_of_replicas", 1);
//                node = NodeBuilder.nodeBuilder().client(false).clusterName(clusterName).data(true).local(true)
//                        .settings(settings).build();
                // settings.put("node.local_storage", "false");

            } else {
                System.out.println("Creating *persistent* ES here: " + esHome);
//                node = NodeBuilder.nodeBuilder().client(false).clusterName(clusterName).data(true).local(false)
//                        .settings(settings).build();
                  settings.put("node.local_storage", "true");
            }

            System.out.println("Starting the ES node.");
            Collection<Class<? extends Plugin>> plugins = Arrays.asList(Netty4Plugin.class);



//            node = new PluginConfigurableNode(settings.build(), plugins);
//            node.start();

            System.out.println("ES node was successfully started.");

            // TODO parameterize this

            JestClientFactory factory = new JestClientFactory();
            factory.setHttpClientConfig(new HttpClientConfig.Builder("http://" + ES_DEFAULT_HOST + ":" + ES_DEFAULT_PORT).multiThreaded(true)
                    .connTimeout(JEST_TIMEOUT ).readTimeout(JEST_TIMEOUT).build());
            client = factory.getObject();
            ES_CLIENT = client;
        }
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

    public Node getESNode() {
        return node;
    }

    public JestClient getESClient() {
        return client;
    }
}
