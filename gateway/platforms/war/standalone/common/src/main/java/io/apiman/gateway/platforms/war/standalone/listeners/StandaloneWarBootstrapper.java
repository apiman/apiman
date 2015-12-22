package io.apiman.gateway.platforms.war.standalone.listeners;

import io.apiman.gateway.engine.components.*;
import io.apiman.gateway.engine.es.*;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
import io.apiman.gateway.platforms.servlet.PolicyFailureFactoryComponent;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;
import io.apiman.gateway.platforms.war.WarEngineConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author pcornish
 */
public class StandaloneWarBootstrapper implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneWarBootstrapper.class);
    private static final String APIMAN_CONFIG_FILE_PATH = "apiman.config-file-path";
    private static final String APIMAN_CONFIG_FILE_NAME = "/apiman.properties";

    // Public constructor is required by servlet spec
    public StandaloneWarBootstrapper() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
        configure();
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
        loadConfigurationIntoSystemProperties();

        configurePluginRegistry();
        configureRegistry();
        configureConnectorFactory();
        configurePolicyFactory();

        configureMetrics();

        // Register test components
        registerComponents();
    }

    /**
     * Load the configuration file into system properties so that they are resolved
     * by {@link WarEngineConfig}.
     */
    private void loadConfigurationIntoSystemProperties() {
        final Properties configFile = new Properties();

        final String configFilePath = System.getProperty(APIMAN_CONFIG_FILE_PATH);
        if (StringUtils.isNotEmpty(configFilePath)) {
            // read from file
            LOGGER.info("Loading configuration from file: {}", configFilePath);

            try (InputStream configStream = FileUtils.openInputStream(new File(configFilePath))) {
                configFile.load(configStream);

            } catch (IOException e) {
                throw new RuntimeException(String.format(
                        "Error loading configuration from file: %s", configFilePath), e);
            }

        } else {
            // search the classpath
            LOGGER.info("Loading configuration from classpath file: {}", APIMAN_CONFIG_FILE_NAME);

            try (InputStream configStream = StandaloneWarBootstrapper.class.getResourceAsStream(APIMAN_CONFIG_FILE_NAME)) {
                if (null == configStream) {
                    throw new IOException(String.format("Unable to load classpath file: %s", APIMAN_CONFIG_FILE_NAME));
                }
                configFile.load(configStream);

            } catch (IOException e) {
                throw new RuntimeException(String.format(
                        "Error loading configuration from classpath file: '%s'. Set system property '%s'?",
                        APIMAN_CONFIG_FILE_NAME, APIMAN_CONFIG_FILE_PATH), e);
            }
        }

        // push into system properties
        configFile.forEach((key, value) -> System.setProperty((String) key, (String) value));
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
    }

    /**
     * The shared state component.
     */
    protected void registerSharedStateComponent() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName(),
                ESSharedStateComponent.class.getName());
    }

    /**
     * The cache store component.
     */
    protected void registerCacheStoreComponent() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ICacheStoreComponent.class.getSimpleName(),
                ESCacheStoreComponent.class.getName());
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
    }

    /**
     * Configure the metrics system.
     */
    protected void configureMetrics() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS, ESMetrics.class.getName());
    }
}
