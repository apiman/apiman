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
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.components.ICacheStoreComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.es.ESCacheStoreComponent;
import io.apiman.gateway.engine.es.ESMetrics;
import io.apiman.gateway.engine.es.ESRateLimiterComponent;
import io.apiman.gateway.engine.es.ESSharedStateComponent;
import io.apiman.gateway.engine.es.PollCachingESRegistry;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.impl.DefaultJdbcComponent;
import io.apiman.gateway.engine.impl.DefaultLdapComponent;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
import io.apiman.gateway.platforms.servlet.PolicyFailureFactoryComponent;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;
import io.apiman.gateway.platforms.war.WarEngineConfig;
import io.apiman.gateway.platforms.war.filters.HttpRequestThreadLocalFilter;
import io.apiman.gateway.platforms.war.listeners.WarGatewayBootstrapper;
import io.apiman.gateway.platforms.war.servlets.WarGatewayServlet;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

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
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IBufferFactoryComponent.class.getSimpleName(),
                ByteBufferFactoryComponent.class.getName());
    }

    /**
     * The policy failure factory component.
     */
    protected void registerPolicyFailureFactoryComponent() {
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IPolicyFailureFactoryComponent.class.getSimpleName(),
                PolicyFailureFactoryComponent.class.getName());
    }

    /**
     * The rate limiter component.
     */
    protected void registerRateLimiterComponent() {
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName(),
                ESRateLimiterComponent.class.getName());
        setConfigProperty("apiman-gateway.components.IRateLimiterComponent.client.type", "jest");
        setConfigProperty("apiman-gateway.components.IRateLimiterComponent.client.protocol", "${apiman.es.protocol}");
        setConfigProperty("apiman-gateway.components.IRateLimiterComponent.client.host", "${apiman.es.host}");
        setConfigProperty("apiman-gateway.components.IRateLimiterComponent.client.port", "${apiman.es.port}");
        setConfigProperty("apiman-gateway.components.IRateLimiterComponent.client.username", "${apiman.es.username}");
        setConfigProperty("apiman-gateway.components.IRateLimiterComponent.client.password", "${apiman.es.password}");
    }

    /**
     * The shared state component.
     */
    protected void registerSharedStateComponent() {
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName(),
                ESSharedStateComponent.class.getName());
        setConfigProperty("apiman-gateway.components.ISharedStateComponent.client.type", "jest");
        setConfigProperty("apiman-gateway.components.ISharedStateComponent.client.protocol", "${apiman.es.protocol}");
        setConfigProperty("apiman-gateway.components.ISharedStateComponent.client.host", "${apiman.es.host}");
        setConfigProperty("apiman-gateway.components.ISharedStateComponent.client.port", "${apiman.es.port}");
        setConfigProperty("apiman-gateway.components.ISharedStateComponent.client.username", "${apiman.es.username}");
        setConfigProperty("apiman-gateway.components.ISharedStateComponent.client.password", "${apiman.es.password}");
    }

    /**
     * The cache store component.
     */
    protected void registerCacheStoreComponent() {
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ICacheStoreComponent.class.getSimpleName(),
                ESCacheStoreComponent.class.getName());
        setConfigProperty("apiman-gateway.components.ICacheStoreComponent.client.type", "jest");
        setConfigProperty("apiman-gateway.components.ICacheStoreComponent.client.protocol", "${apiman.es.protocol}");
        setConfigProperty("apiman-gateway.components.ICacheStoreComponent.client.host", "${apiman.es.host}");
        setConfigProperty("apiman-gateway.components.ICacheStoreComponent.client.port", "${apiman.es.port}");
        setConfigProperty("apiman-gateway.components.ICacheStoreComponent.client.index", "apiman_cache");
        setConfigProperty("apiman-gateway.components.ICacheStoreComponent.client.username", "${apiman.es.username}");
        setConfigProperty("apiman-gateway.components.ICacheStoreComponent.client.password", "${apiman.es.password}");
    }

    /**
     * The jdbc component.
     */
    protected void registerJdbcComponent() {
        setConfigProperty("apiman-gateway.components.IJdbcComponent", DefaultJdbcComponent.class.getName());
    }

    /**
     * The ldap component.
     */
    protected void registerLdapComponent() {
        setConfigProperty("apiman-gateway.components.ILdapComponent", DefaultLdapComponent.class.getName());
    }

    /**
     * The policy factory component.
     */
    protected void configurePolicyFactory() {
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_POLICY_FACTORY_CLASS, PolicyFactoryImpl.class.getName());
    }

    /**
     * The connector factory.
     */
    protected void configureConnectorFactory() {
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS, HttpConnectorFactory.class.getName());
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS + ".http.timeouts.read", "25");
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS + ".http.timeouts.write", "25");
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS + ".http.timeouts.connect", "10");
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS + ".http.followRedirects", "true");
    }

    /**
     * The plugin registry.
     */
    protected void configurePluginRegistry() {
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS, DefaultPluginRegistry.class.getName());
    }

    /**
     * The registry.
     */
    protected void configureRegistry() {
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS, PollCachingESRegistry.class.getName());
        setConfigProperty("apiman-gateway.registry.client.type", "jest");
        setConfigProperty("apiman-gateway.registry.client.protocol", "${apiman.es.protocol}");
        setConfigProperty("apiman-gateway.registry.client.host", "${apiman.es.host}");
        setConfigProperty("apiman-gateway.registry.client.port", "${apiman.es.port}");
        setConfigProperty("apiman-gateway.registry.client.username", "${apiman.es.username}");
        setConfigProperty("apiman-gateway.registry.client.password", "${apiman.es.password}");
    }

    /**
     * Configure the metrics system.
     */
    protected void configureMetrics() {
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS, ESMetrics.class.getName());
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.type", "jest");
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.protocol", "${apiman.es.protocol}");
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.host", "${apiman.es.host}");
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.port", "${apiman.es.port}");
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.username", "${apiman.es.username}");
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.password", "${apiman.es.password}");
        setConfigProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.index", "apiman_metrics");
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
        for (User user : Users.getUsers()) {
            l.putUser(user.getId(), Credential.getCredential(user.getPassword()), user.getRolesAsArray());
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
