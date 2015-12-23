package io.apiman.gateway.platforms.war.standalone.listeners;

import io.apiman.gateway.platforms.war.WarEngineConfig;
import io.apiman.gateway.platforms.war.listeners.WarGatewayBootstrapper;
import io.apiman.gateway.platforms.war.standalone.i18n.Messages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContextEvent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String APIMAN_CONFIG_FILE_PATH = "apiman.config-file-path"; //$NON-NLS-1$
    private static final String APIMAN_CONFIG_FILE_NAME = "/apiman.properties"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public StandaloneWarBootstrapper() {
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
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

            LOGGER.info(Messages.i18n.format("StandaloneWarBootstrapper.LoadingConfigFromFile", configFilePath)); //$NON-NLS-1$

            try (InputStream configStream = FileUtils.openInputStream(new File(configFilePath))) {
                configFile.load(configStream);

            } catch (IOException e) {
                throw new RuntimeException(Messages.i18n.format("StandaloneWarBootstrapper.ErrorLoadingConfigFromFile", configFilePath), e); //$NON-NLS-1$
            }

        } else {
            // search the classpath
            LOGGER.info(Messages.i18n.format("StandaloneWarBootstrapper.LoadingConfigFromCP", APIMAN_CONFIG_FILE_NAME)); //$NON-NLS-1$

            try (InputStream configStream = StandaloneWarBootstrapper.class.getResourceAsStream(APIMAN_CONFIG_FILE_NAME)) {
                if (null == configStream) {
                    throw new IOException(Messages.i18n.format("StandaloneWarBootstrapper.ErrorLoadingConfigFromCP", APIMAN_CONFIG_FILE_NAME)); //$NON-NLS-1$
                }
                configFile.load(configStream);
            } catch (IOException e) {
                throw new RuntimeException(
                        Messages.i18n.format("StandaloneWarBootstrapper.ErrorLoadingConfigFromCP-2", //$NON-NLS-1$
                                APIMAN_CONFIG_FILE_NAME, APIMAN_CONFIG_FILE_PATH),
                        e);
            }
        }

        for (Entry<Object, Object> entry : configFile.entrySet()) {
            System.setProperty((String) entry.getKey(), (String) entry.getValue());
        }
    }
}
