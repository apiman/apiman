package io.apiman.common.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ConfigDirectoryFinder {

    /**
     * Return the standard Apiman config directory.
     *
     * Following precedence is used:
     * <ul>
     *     <li><code>${apiman.config.dir}</code></li>
     *     <li><code>${jboss.server.config.dir}</code></li>
     *     <li><code>${catalina.home}/conf</code></li>
     * </ul>
     */
    public static Path getConfigDirectory() {
        // Grand unified conf directory!
        String confDir = System.getProperty("apiman.config.dir");
        if (confDir != null) {
            return Paths.get(confDir);
        }

        // If that wasn't set, then check to see if we're running in wildfly/eap
        confDir = System.getProperty("jboss.server.config.dir");
        if (confDir != null) {
            return Paths.get(confDir);
        }

        // If that didn't work, try to locate a tomcat data directory
        confDir = System.getProperty("catalina.home");
        if (confDir != null) {
            return Paths.get(confDir, "conf");
        }
        throw new IllegalStateException("No config directory has been set. Please set apiman.config.dir=<data dir>");
    }

    /**
     * Return the standard Apiman data directory.
     *
     * Following precedence is used:
     * <ul>
     *     <li><code>${apiman.data.dir}</code></li>
     *     <li><code>${jboss.server.data.dir}</code></li>
     *     <li><code>${catalina.home}/data</code></li>
     * </ul>
     */
    public static Path getDataDirectory() {
        // Grand unified data directory!
        String dataDir = System.getProperty("apiman.data.dir");
        if (dataDir != null) {
            return Paths.get(dataDir);
        }

        // If that wasn't set, then check to see if we're running in wildfly/eap
        dataDir = System.getProperty("jboss.server.data.dir");
        if (dataDir != null) {
            return Paths.get(dataDir, "apiman");
        }

        // If that didn't work, try to locate a tomcat data directory
        dataDir = System.getProperty("catalina.home");
        if (dataDir != null) {
            return Paths.get(dataDir, "conf");
        }
        throw new IllegalStateException("No data directory has been set. Please set apiman.data.dir=<data dir>");
    }

}
