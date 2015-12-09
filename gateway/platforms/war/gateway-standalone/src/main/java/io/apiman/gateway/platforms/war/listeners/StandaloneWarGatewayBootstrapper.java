package io.apiman.gateway.platforms.war.listeners; /**
 * @author pete
 */

import io.apiman.gateway.engine.components.*;
import io.apiman.gateway.engine.es.*;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
import io.apiman.gateway.platforms.servlet.PolicyFailureFactoryComponent;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;
import io.apiman.gateway.platforms.war.WarEngineConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;

public class StandaloneWarGatewayBootstrapper extends WarGatewayBootstrapper {

    // Public constructor is required by servlet spec
    public StandaloneWarGatewayBootstrapper() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
        configure();
        super.contextInitialized(sce);
    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context 
         (the Web application) is undeployed or 
         Application Server shuts down.
      */
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
        System.setProperty("apiman.es.protocol", "http");
        System.setProperty("apiman.es.host", "localhost");
        System.setProperty("apiman.es.port", "9200");
        System.setProperty("apiman.es.cluster-name", "apiman");
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
    }

    /**
     * The buffer factory component.
     */
    private void registerBufferFactoryComponent() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IBufferFactoryComponent.class.getSimpleName(),
                ByteBufferFactoryComponent.class.getName());
    }

    /**
     * The policy failure factory component.
     */
    protected void registerPolicyFailureFactoryComponent() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IPolicyFailureFactoryComponent.class.getSimpleName(),
                PolicyFailureFactoryComponent.class.getName());
    }

    /**
     * The rate limiter component.
     */
    protected void registerRateLimiterComponent() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName(),
                ESRateLimiterComponent.class.getName());
        System.setProperty("apiman-gateway.components.IRateLimiterComponent.client.type", "jest");
        System.setProperty("apiman-gateway.components.IRateLimiterComponent.client.cluster-name", "${apiman.es.cluster-name}");
        System.setProperty("apiman-gateway.components.IRateLimiterComponent.client.protocol", "${apiman.es.protocol}");
        System.setProperty("apiman-gateway.components.IRateLimiterComponent.client.host", "${apiman.es.host}");
        System.setProperty("apiman-gateway.components.IRateLimiterComponent.client.port", "${apiman.es.port}");
    }

    /**
     * The shared state component.
     */
    protected void registerSharedStateComponent() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName(),
                ESSharedStateComponent.class.getName());
        System.setProperty("apiman-gateway.components.ISharedStateComponent.client.type", "jest");
        System.setProperty("apiman-gateway.components.ISharedStateComponent.client.cluster-name", "${apiman.es.cluster-name}");
        System.setProperty("apiman-gateway.components.ISharedStateComponent.client.protocol", "${apiman.es.protocol}");
        System.setProperty("apiman-gateway.components.ISharedStateComponent.client.host", "${apiman.es.host}");
        System.setProperty("apiman-gateway.components.ISharedStateComponent.client.port", "${apiman.es.port}");
    }

    /**
     * The cache store component.
     */
    protected void registerCacheStoreComponent() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ICacheStoreComponent.class.getSimpleName(),
                ESCacheStoreComponent.class.getName());
        System.setProperty("apiman-gateway.components.ICacheStoreComponent.client.type", "jest");
        System.setProperty("apiman-gateway.components.ICacheStoreComponent.client.cluster-name", "${apiman.es.cluster-name}");
        System.setProperty("apiman-gateway.components.ICacheStoreComponent.client.protocol", "${apiman.es.protocol}");
        System.setProperty("apiman-gateway.components.ICacheStoreComponent.client.host", "${apiman.es.host}");
        System.setProperty("apiman-gateway.components.ICacheStoreComponent.client.port", "${apiman.es.port}");
        System.setProperty("apiman-gateway.components.ICacheStoreComponent.client.index", "apiman_cache");
    }

    /**
     * The policy factory component.
     */
    protected void configurePolicyFactory() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_POLICY_FACTORY_CLASS, PolicyFactoryImpl.class.getName());
    }

    /**
     * The connector factory.
     */
    protected void configureConnectorFactory() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS, HttpConnectorFactory.class.getName());
    }

    /**
     * The plugin registry.
     */
    protected void configurePluginRegistry() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS, DefaultPluginRegistry.class.getName());
    }

    /**
     * The registry.
     */
    protected void configureRegistry() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS, ESRegistry.class.getName());
        System.setProperty("apiman-gateway.registry.client.type", "jest");
        System.setProperty("apiman-gateway.registry.client.cluster-name", "${apiman.es.cluster-name}");
        System.setProperty("apiman-gateway.registry.client.protocol", "${apiman.es.protocol}");
        System.setProperty("apiman-gateway.registry.client.host", "${apiman.es.host}");
        System.setProperty("apiman-gateway.registry.client.port", "${apiman.es.port}");
    }

    /**
     * Configure the metrics system.
     */
    protected void configureMetrics() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS, ESMetrics.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.type", "jest");
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.cluster-name", System.getProperty("apiman-test.es-metrics.cluster-name", "${apiman.es.cluster-name}"));
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.protocol", System.getProperty("apiman-test.es-metrics.host", "${apiman.es.protocol}"));
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.host", System.getProperty("apiman-test.es-metrics.host", "${apiman.es.host}"));
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.port", System.getProperty("apiman-test.es-metrics.port", "${apiman.es.port}"));
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS + ".client.index", System.getProperty("apiman-test.es-metrics.index", "apiman_metrics"));
    }
}
