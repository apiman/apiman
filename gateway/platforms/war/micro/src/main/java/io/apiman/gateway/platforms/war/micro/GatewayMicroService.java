/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.apiman.gateway.platforms.war.micro;

import io.apiman.common.servlet.ApimanCorsFilter;
import io.apiman.common.servlet.AuthenticationFilter;
import io.apiman.common.servlet.DisableCachingFilter;
import io.apiman.common.servlet.LocaleFilter;
import io.apiman.common.servlet.RootResourceFilter;
import io.apiman.gateway.engine.GatewayConfigProperties;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.components.ICacheStoreComponent;
import io.apiman.gateway.engine.components.IJdbcComponent;
import io.apiman.gateway.engine.components.ILdapComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.es.*;
import io.apiman.gateway.engine.es.EsSharedStateComponent;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.impl.DefaultJdbcComponent;
import io.apiman.gateway.engine.impl.DefaultLdapComponent;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
import io.apiman.gateway.platforms.servlet.PolicyFailureFactoryComponent;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;
import io.apiman.gateway.platforms.war.filters.HttpRequestThreadLocalFilter;
import io.apiman.gateway.platforms.war.listeners.WarGatewayBootstrapper;
import io.apiman.gateway.platforms.war.servlets.WarGatewayServlet;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

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
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;

/**
 * This class starts up an embedded Jetty test server so that integration tests
 * can be performed.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class GatewayMicroService {

    private Server server;

    /**
     * Constructor.
     */

    public GatewayMicroService() {
        configure();
    }

    /**
     * Configure the gateway options.
     */
    protected void configure() {
        configureGlobalVars();
        configurePluginRegistry();
        configureRegistry();
        configureConnectorFactory();
        configurePolicyFactory();

        configureMetrics();

        // Register test components
        registerComponents();
    }

    /**
     * Configure some global variables in the system properties.
     */
    protected void configureGlobalVars() {
        setConfigProperty("apiman.es.protocol", "http");
        setConfigProperty("apiman.es.host", "localhost");
        setConfigProperty("apiman.es.port", "9200");
        setConfigProperty("apiman.es.username", "");
        setConfigProperty("apiman.es.password", "");
    }

    /**
     * Register the components.
     */
    protected void registerComponents() {
        registerBufferFactoryComponent();
        registerSharedStateComponent();
        registerRateLimiterComponent();
        registerPolicyFailureFactoryComponent();
        registerCacheStoreComponent();
        registerJdbcComponent();
        registerLdapComponent();
    }

    /**
     * The buffer factory component.
     */
    private void registerBufferFactoryComponent() {
        setConfigProperty(GatewayConfigProperties.COMPONENT_PREFIX + IBufferFactoryComponent.class.getSimpleName(),
                ByteBufferFactoryComponent.class.getName());
    }

    /**
     * The policy failure factory component.
     */
    protected void registerPolicyFailureFactoryComponent() {
        setConfigProperty(GatewayConfigProperties.COMPONENT_PREFIX + IPolicyFailureFactoryComponent.class.getSimpleName(),
                PolicyFailureFactoryComponent.class.getName());
    }

    /**
     * The rate limiter component.
     */
    protected void registerRateLimiterComponent() {
        String componentPropName = GatewayConfigProperties.COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName();
        setConfigProperty(componentPropName,
                EsRateLimiterComponent.class.getName());
        setConfigProperty(componentPropName + ".client.type", "es");
        setConfigProperty(componentPropName + ".client.protocol", "${apiman.es.protocol}");
        setConfigProperty(componentPropName + ".client.host", "${apiman.es.host}");
        setConfigProperty(componentPropName + ".client.port", "${apiman.es.port}");
        setConfigProperty(componentPropName + ".client.username", "${apiman.es.username}");
        setConfigProperty(componentPropName + ".client.password", "${apiman.es.password}");
    }

    /**
     * The shared state component.
     */
    protected void registerSharedStateComponent() {
        String componentPropName = GatewayConfigProperties.COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName();
        setConfigProperty(componentPropName,
                EsSharedStateComponent.class.getName());
        setConfigProperty(componentPropName + ".client.type", "es");
        setConfigProperty(componentPropName + ".client.protocol", "${apiman.es.protocol}");
        setConfigProperty(componentPropName + ".client.host", "${apiman.es.host}");
        setConfigProperty(componentPropName + ".client.port", "${apiman.es.port}");
        setConfigProperty(componentPropName + ".client.username", "${apiman.es.username}");
        setConfigProperty(componentPropName + ".client.password", "${apiman.es.password}");
    }

    /**
     * The cache store component.
     */
    protected void registerCacheStoreComponent() {
        String componentPropName = GatewayConfigProperties.COMPONENT_PREFIX + ICacheStoreComponent.class.getSimpleName();
        setConfigProperty(componentPropName,
                EsCacheStoreComponent.class.getName());
        setConfigProperty(componentPropName + ".client.type", "es");
        setConfigProperty(componentPropName + ".client.protocol", "${apiman.es.protocol}");
        setConfigProperty(componentPropName + ".client.host", "${apiman.es.host}");
        setConfigProperty(componentPropName + ".client.port", "${apiman.es.port}");
        setConfigProperty(componentPropName + ".client.indexPrefix", "apiman_cache");
        setConfigProperty(componentPropName + ".client.username", "${apiman.es.username}");
        setConfigProperty(componentPropName + ".client.password", "${apiman.es.password}");
    }

    /**
     * The jdbc component.
     */
    protected void registerJdbcComponent() {
        String componentPropName = GatewayConfigProperties.COMPONENT_PREFIX + IJdbcComponent.class.getSimpleName();
        setConfigProperty(componentPropName, DefaultJdbcComponent.class.getName());
    }

    /**
     * The ldap component.
     */
    protected void registerLdapComponent() {
        String componentPropName = GatewayConfigProperties.COMPONENT_PREFIX + ILdapComponent.class.getSimpleName();
        setConfigProperty(componentPropName, DefaultLdapComponent.class.getName());
    }

    /**
     * The policy factory component.
     */
    protected void configurePolicyFactory() {
        setConfigProperty(GatewayConfigProperties.POLICY_FACTORY_CLASS, PolicyFactoryImpl.class.getName());
    }

    /**
     * The connector factory.
     */
    protected void configureConnectorFactory() {
        setConfigProperty(GatewayConfigProperties.CONNECTOR_FACTORY_CLASS, HttpConnectorFactory.class.getName());
        setConfigProperty(GatewayConfigProperties.CONNECTOR_FACTORY_CLASS + ".http.timeouts.read", "25");
        setConfigProperty(GatewayConfigProperties.CONNECTOR_FACTORY_CLASS + ".http.timeouts.write", "25");
        setConfigProperty(GatewayConfigProperties.CONNECTOR_FACTORY_CLASS + ".http.timeouts.connect", "10");
        setConfigProperty(GatewayConfigProperties.CONNECTOR_FACTORY_CLASS + ".http.followRedirects", "true");
    }

    /**
     * The plugin registry.
     */
    protected void configurePluginRegistry() {
        setConfigProperty(GatewayConfigProperties.PLUGIN_REGISTRY_CLASS, DefaultPluginRegistry.class.getName());
    }

    /**
     * The registry.
     */
    protected void configureRegistry() {
        setConfigProperty(GatewayConfigProperties.REGISTRY_CLASS, PollCachingEsRegistry.class.getName());
        setConfigProperty(GatewayConfigProperties.REGISTRY_CLASS + ".client.type", "es");
        setConfigProperty(GatewayConfigProperties.REGISTRY_CLASS + ".client.protocol", "${apiman.es.protocol}");
        setConfigProperty(GatewayConfigProperties.REGISTRY_CLASS + ".client.host", "${apiman.es.host}");
        setConfigProperty(GatewayConfigProperties.REGISTRY_CLASS + ".client.port", "${apiman.es.port}");
        setConfigProperty(GatewayConfigProperties.REGISTRY_CLASS + ".client.username", "${apiman.es.username}");
        setConfigProperty(GatewayConfigProperties.REGISTRY_CLASS + ".client.password", "${apiman.es.password}");
    }

    /**
     * Configure the metrics system.
     */
    protected void configureMetrics() {
        setConfigProperty(GatewayConfigProperties.METRICS_CLASS, EsMetrics.class.getName());
        setConfigProperty(GatewayConfigProperties.METRICS_CLASS + ".client.type", "es");
        setConfigProperty(GatewayConfigProperties.METRICS_CLASS + ".client.protocol", "${apiman.es.protocol}");
        setConfigProperty(GatewayConfigProperties.METRICS_CLASS + ".client.host", "${apiman.es.host}");
        setConfigProperty(GatewayConfigProperties.METRICS_CLASS + ".client.port", "${apiman.es.port}");
        setConfigProperty(GatewayConfigProperties.METRICS_CLASS + ".client.username", "${apiman.es.username}");
        setConfigProperty(GatewayConfigProperties.METRICS_CLASS + ".client.password", "${apiman.es.password}");
        setConfigProperty(GatewayConfigProperties.METRICS_CLASS + ".client.indexPrefix", "apiman_metrics");
    }

    /**
     * Sets a system property if it's not already set.
     * @param propName
     * @param propValue
     */
    protected void setConfigProperty(String propName, String propValue) {
        if (System.getProperty(propName) == null) {
            System.setProperty(propName, propValue);
        }
    }

    /**
     * Start/run the server.
     * @throws Exception when any exception occurs
     */
    public void start() throws Exception {
        long startTime = System.currentTimeMillis();

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        addModulesToJetty(handlers);

        // Create the server.
        int serverPort = serverPort();
        System.out.println("**** Starting Gateway (" + getClass().getSimpleName() + ") on port: " + serverPort);
        server = new Server(serverPort);
        server.setHandler(handlers);
        server.start();
        long endTime = System.currentTimeMillis();
        System.out.println("******* Started in " + (endTime - startTime) + "ms");
    }

    /**
     * Stop the server.
     * @throws Exception when any exception occurs
     */
    public void stop() throws Exception {
        server.stop();
    }

    /**
     * @return the server port.
     */
    public int serverPort() {
        return Integer.parseInt(System.getProperty("apiman.micro.gateway.port", "7777"));
    }

    /**
     * Configure the web application(s).
     * @param handlers
     * @throws Exception
     */
    protected void addModulesToJetty(ContextHandlerCollection handlers) throws Exception {
    	/* *************
         * Gateway API
         * ************* */
        ServletContextHandler gatewayApiServer = new ServletContextHandler(ServletContextHandler.SESSIONS);
        addSecurityHandler(gatewayApiServer);
        gatewayApiServer.setContextPath("/api");
        gatewayApiServer.addEventListener(new ResteasyBootstrap());
        gatewayApiServer.addEventListener(new WarGatewayBootstrapper());
        gatewayApiServer.addFilter(HttpRequestThreadLocalFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        gatewayApiServer.addFilter(LocaleFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        gatewayApiServer.addFilter(ApimanCorsFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        gatewayApiServer.addFilter(DisableCachingFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        addApiAuthFilter(gatewayApiServer);
        gatewayApiServer.addFilter(RootResourceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        ServletHolder resteasyServlet = new ServletHolder(new HttpServletDispatcher());
        resteasyServlet.setInitParameter("javax.ws.rs.Application", GatewayMicroServiceApplication.class.getName());
        gatewayApiServer.addServlet(resteasyServlet, "/*");
        gatewayApiServer.setInitParameter("resteasy.servlet.mapping.prefix", "");

        handlers.addHandler(gatewayApiServer);


        /* *************
         * Gateway
         * ************* */
        ServletContextHandler gatewayServer = new ServletContextHandler(ServletContextHandler.SESSIONS);
        addSecurityHandler(gatewayServer);
        gatewayServer.setContextPath("/gateway");
        ServletHolder servlet = new ServletHolder(new WarGatewayServlet());
        gatewayServer.addServlet(servlet, "/*");

        handlers.addHandler(gatewayServer);
    }


    /**
     * @param apiManServer
     * @throws Exception
     */
    protected void addSecurityHandler(ServletContextHandler apiManServer) throws Exception {
        apiManServer.setSecurityHandler(createSecurityHandler());
    }

    /**
     * @param apiManServer
     */
    protected void addApiAuthFilter(ServletContextHandler apiManServer) {
        apiManServer.addFilter(AuthenticationFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    /**
     * Creates a basic auth security handler.
     */
    protected SecurityHandler createSecurityHandler() throws Exception {
        HashLoginService l = new HashLoginService();
        UserStore userStore = new UserStore();
        l.setUserStore(userStore);
        for (User user : Users.getUsers()) {
            userStore.addUser(user.getId(), Credential.getCredential(user.getPassword()), user.getRolesAsArray());
        }
        l.setName("apimanrealm");

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("apimanrealm");
        csh.setLoginService(l);

        return csh;
    }

    /**
     * @throws InterruptedException when interrupted
     */
    public void join() throws InterruptedException {
        server.join();
    }

}
