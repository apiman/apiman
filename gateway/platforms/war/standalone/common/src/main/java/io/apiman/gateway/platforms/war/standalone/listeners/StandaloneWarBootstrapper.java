package io.apiman.gateway.platforms.war.standalone.listeners;

import io.apiman.gateway.platforms.war.WarEngineConfig;
import io.apiman.gateway.platforms.war.listeners.WarGatewayBootstrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Work-around for JBoss/Wildfly specific code in {@link WarGatewayBootstrapper} that prevents use of
 * 'apiman.properties' file to configure the engine.
 * <p>
 * More specifically, the standard WAR bootstrapper calls
 * {@link io.apiman.common.config.ConfigFileConfiguration#create(String)} via
 * {@link io.apiman.common.config.ConfigFactory#createConfig()}, which defaults to using 'empty.properties'
 * if the System property 'jboss.server.config.dir' is not defined.
 * </p>
 *
 * @author pcornish
 */
public class StandaloneWarBootstrapper extends WarGatewayBootstrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneWarBootstrapper.class);
    private static final String APIMAN_CONFIG_FILE_PATH = "apiman.config-file-path";
    private static final String APIMAN_CONFIG_FILE_NAME = "/apiman.properties";

    /**
     * Constructor.
     */
    public StandaloneWarBootstrapper() {
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
        loadConfigurationIntoSystemProperties();

        super.contextInitialized(sce);
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
}
