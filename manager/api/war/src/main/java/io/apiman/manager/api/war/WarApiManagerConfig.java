/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.api.war;

import io.apiman.common.config.ConfigFileConfiguration;
import io.apiman.common.config.SystemPropertiesConfiguration;
import io.apiman.manager.api.core.logging.IApimanLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;

/**
 * Configuration object for the API Manager.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class WarApiManagerConfig {

    public static final String APIMAN_MANAGER_CONFIG_LOGGER = "apiman-manager.config.logger"; //$NON-NLS-1$

    /* -------------------------------------------------------
     * Storage
     * ------------------------------------------------------- */
    public static final String APIMAN_MANAGER_STORAGE_TYPE = "apiman-manager.storage.type"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_PROTOCOL = "apiman-manager.storage.es.protocol"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_HOST = "apiman-manager.storage.es.host"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_PORT = "apiman-manager.storage.es.port"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_CLUSTER_NAME = "apiman-manager.storage.es.cluster-name"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_INITIALIZE = "apiman-manager.storage.es.initialize"; //$NON-NLS-1$

    /* -------------------------------------------------------
     * Metrics
     * ------------------------------------------------------- */
    public static final String APIMAN_MANAGER_METRICS_TYPE = "apiman-manager.metrics.type"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_METRICS_ES_PROTOCOL = "apiman-manager.metrics.es.protocol"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_METRICS_ES_HOST = "apiman-manager.metrics.es.host"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_METRICS_ES_PORT = "apiman-manager.metrics.es.port"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_METRICS_ES_CLUSTER_NAME = "apiman-manager.metrics.es.cluster-name"; //$NON-NLS-1$

    public static final String APIMAN_MANAGER_SECURITY_CONTEXT_TYPE = "apiman-manager.security-context.type"; //$NON-NLS-1$

    public static final String APIMAN_PLUGIN_REPOSITORIES = "apiman.plugins.repositories"; //$NON-NLS-1$

    public static final String DEFAULT_ES_CLUSTER_NAME = "apiman"; //$NON-NLS-1$

    private static Configuration config;
    static {
        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
        compositeConfiguration.addConfiguration(new SystemPropertiesConfiguration());
        compositeConfiguration.addConfiguration(ConfigFileConfiguration.create("apiman.properties")); //$NON-NLS-1$
        config = compositeConfiguration;
    }

    /**
     * Constructor.
     */
    public WarApiManagerConfig() {
    }

    /**
     * @return the configured plugin repositories
     */
    public Set<URL> getPluginRepositories() {
        Set<URL> rval = new HashSet<>();
        String repositories = config.getString(APIMAN_PLUGIN_REPOSITORIES);
        if (repositories != null) {
            String[] split = repositories.split(","); //$NON-NLS-1$
            for (String repository : split) {
                try {
                    rval.add(new URL(repository.trim()));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return rval;
    }

    public String getSecurityContextType() {
        return config.getString(APIMAN_MANAGER_SECURITY_CONTEXT_TYPE, "default"); //$NON-NLS-1$
    }

    /**
     * @return the configured storage type
     */
    public String getStorageType() {
        return config.getString(APIMAN_MANAGER_STORAGE_TYPE, "jpa"); //$NON-NLS-1$
    }

    /**
     * @return the elasticsearch protocol
     */
    public String getStorageESProtocol() {
        return config.getString(APIMAN_MANAGER_STORAGE_ES_PROTOCOL, "http"); //$NON-NLS-1$
    }

    /**
     * @return the elasticsearch host
     */
    public String getStorageESHost() {
        return config.getString(APIMAN_MANAGER_STORAGE_ES_HOST, "localhost"); //$NON-NLS-1$
    }

    /**
     * @return the elasticsearch port
     */
    public int getStorageESPort() {
        return config.getInt(APIMAN_MANAGER_STORAGE_ES_PORT, 19300);
    }

    /**
     * @return the elasticsearch cluster name
     */
    public String getStorageESClusterName() {
        return config.getString(APIMAN_MANAGER_STORAGE_ES_CLUSTER_NAME, DEFAULT_ES_CLUSTER_NAME);
    }

    /**
     * @return true if the elasticsearch index should be initialized if not found
     */
    public boolean isInitializeStorageES() {
        return config.getBoolean(APIMAN_MANAGER_STORAGE_ES_INITIALIZE, true);
    }

    /**
     * @return the configured storage type
     */
    public String getMetricsType() {
        return config.getString(APIMAN_MANAGER_METRICS_TYPE, "es"); //$NON-NLS-1$
    }

    /**
     * @return the elasticsearch protocol
     */
    public String getMetricsESProtocol() {
        return config.getString(APIMAN_MANAGER_METRICS_ES_PROTOCOL, "http"); //$NON-NLS-1$
    }

    /**
     * @return the elasticsearch host
     */
    public String getMetricsESHost() {
        return config.getString(APIMAN_MANAGER_METRICS_ES_HOST, "localhost"); //$NON-NLS-1$
    }

    /**
     * @return the elasticsearch port
     */
    public int getMetricsESPort() {
        return config.getInt(APIMAN_MANAGER_METRICS_ES_PORT, 19300);
    }

    /**
     * @return the elasticsearch cluster name
     */
    public String getMetricsESClusterName() {
        return config.getString(APIMAN_MANAGER_METRICS_ES_CLUSTER_NAME, DEFAULT_ES_CLUSTER_NAME);
    }

    /**
     * @return the configuration
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * 'Simple', 'JSON' or FQDN with {@link IApimanLogger} implementation.
     *
     * @return Logger name or FQDN
     */
    public String getLoggerName() {
        return config.getString(APIMAN_MANAGER_CONFIG_LOGGER);
    }

}
