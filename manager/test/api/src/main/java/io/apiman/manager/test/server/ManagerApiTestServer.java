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
import io.apiman.manager.test.server.deployments.H2Deployment;
import io.apiman.manager.test.server.deployments.ITestDatabaseDeployment;
import io.apiman.manager.test.server.deployments.MariaDbDeployment;
import io.apiman.manager.test.server.deployments.MsSqlDeployment;
import io.apiman.manager.test.server.deployments.MySqlDeployment;
import io.apiman.manager.test.server.deployments.PostgresDeployment;
import io.apiman.test.common.util.TestUtil;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import javax.servlet.DispatcherType;

import net.snowflake.client.jdbc.internal.org.bouncycastle.util.Arrays;
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
    protected Server server;

    static final Map<String, ITestDatabaseDeployment> deploymentTypes = Map.of(
            "h2", new H2Deployment(),
            "postgres", new PostgresDeployment(),
            "mssql", new MsSqlDeployment(),
            "mysql", new MySqlDeployment(),
            "mariadb", new MariaDbDeployment()
    );
    private final String containerImageName;

    protected ITestDatabaseDeployment deployment;

    /**
     * Constructor.
     */
    public ManagerApiTestServer(String database, String containerImageName) {
        this.containerImageName = containerImageName;
        Objects.requireNonNull(database, "Must select a valid database to use for Apiman Manager API." +
                " Allowed values " + deploymentTypes.keySet());

        deployment = deploymentTypes.get(database);
    }

    /**
     * Start/run the server.
     */
    public void start() throws Exception {
        long startTime = System.currentTimeMillis();
        System.out.println("**** Starting Server (" + getClass().getSimpleName() + ")");
        preStart();

        deployment.start(containerImageName);

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
        deployment.stop();
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
        TestUtil.setProperty("liquibase.should.run", "true");
//        TestUtil.setProperty("hibernate.hbm2ddl.import_files", "import.sql");
//        TestUtil.setProperty("apiman.hibernate.hbm2ddl.auto", "create-drop");
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
}
