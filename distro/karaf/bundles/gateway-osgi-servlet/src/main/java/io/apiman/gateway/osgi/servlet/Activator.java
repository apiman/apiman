package io.apiman.gateway.osgi.servlet;

import io.apiman.gateway.platforms.war.WarEngineConfig;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Dictionary;

public class Activator implements BundleActivator {

    /**
     * OSGI Service references.
     */
    private ServiceReference<ConfigurationAdmin> configAdminReference;

    protected static Dictionary apimanProps;

    /**
     * Called when the OSGi framework starts our bundle.
     */
    public void start(BundleContext context) throws Exception {

        /**
         * Register the OSGI Config Admin Service to retrieve the Apiman.properties
         */
        configAdminReference = context.getServiceReference(ConfigurationAdmin.class);
        while (configAdminReference == null) {
            configAdminReference = context.getServiceReference(ConfigurationAdmin.class);
        }
        ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) context.getService(configAdminReference);
        Configuration configuration = configurationAdmin.getConfiguration("io.apiman.gateway", null);
        apimanProps = configuration.getProperties();
        WarEngineConfig.setConfig(apimanProps);
    }

    /**
     * Called when the OSGi framework stops our bundle
     */
    public void stop(BundleContext bc) throws Exception {
        if (configAdminReference != null) {
            bc.ungetService(configAdminReference);
        }
    }
}
