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
package io.apiman.manager.api.core.config;

import io.apiman.common.config.ConfigFactory;
import io.apiman.common.es.util.EsConstants;
import io.apiman.common.logging.IApimanLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

/**
 * Configuration object for the API Manager.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class ApiManagerConfig {

    public static final String APIMAN_MANAGER_CONFIG_LOGGER = "apiman-manager.config.logger"; //$NON-NLS-1$

    public static final String APIMAN_API_KEY_GENERATOR_TYPE = "apiman-manager.api-keys.generator.type"; //$NON-NLS-1$

    public static final String APIMAN_DATA_ENCRYPTER_TYPE = "apiman.encrypter.type"; //$NON-NLS-1$

    public static final String APIMAN_MANAGER_NEW_USER_BOOTSTRAPPER_TYPE = "apiman-manager.user-bootstrapper.type"; //$NON-NLS-1$

    public static final String APIMAN_MANAGER_FEATURES_ORG_CREATE_ADMIN_ONLY = "apiman-manager.config.features.org-create-admin-only"; //$NON-NLS-1$

    /*
     * Database/hibernate properties
     */
    public static final String APIMAN_MANAGER_HIBERNATE_DIALECT = "apiman.hibernate.dialect"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_HIBERNATE_DS = "apiman.hibernate.connection.datasource"; //$NON-NLS-1$


    /* -------------------------------------------------------
     * Storage
     * ------------------------------------------------------- */
    public static final String APIMAN_MANAGER_STORAGE_TYPE = "apiman-manager.storage.type"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_JPA_INITIALIZE = "apiman-manager.storage.jpa.initialize"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_CLIENT_FACTORY = "apiman-manager.storage.es.client-factory"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_INITIALIZE = "apiman-manager.storage.es.initialize"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_INDEX_NAME = "apiman-manager.storage.es.index"; //$NON-NLS-1$

    public static final String APIMAN_MANAGER_STORAGE_QUERY_TYPE = "apiman-manager.storage-query.type"; //$NON-NLS-1$

    public static final String APIMAN_MANAGER_API_CATALOG_TYPE = "apiman-manager.api-catalog.type"; //$NON-NLS-1$

    /* -------------------------------------------------------
     * Metrics
     * ------------------------------------------------------- */
    public static final String APIMAN_MANAGER_METRICS_TYPE = "apiman-manager.metrics.type"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_METRICS_ES_CLIENT_FACTORY = "apiman-manager.metrics.es.client-factory"; //$NON-NLS-1$

    public static final String APIMAN_MANAGER_SECURITY_CONTEXT_TYPE = "apiman-manager.security-context.type"; //$NON-NLS-1$

    public static final String APIMAN_PLUGIN_REPOSITORIES = "apiman.plugins.repositories"; //$NON-NLS-1$
    public static final String APIMAN_PLUGIN_REGISTRIES = "apiman-manager.plugins.registries"; //$NON-NLS-1$

    private final Configuration config;

    /**
     * Constructor.
     */
    public ApiManagerConfig() {
        config = loadProperties();
    }

    /**
     * Loads the config properties.
     */
    protected Configuration loadProperties() {
        return ConfigFactory.createConfig();
    }

    /**
     * @return the configured plugin repositories
     */
    public Set<URI> getPluginRepositories() {
        Set<URI> rval = new HashSet<>();
        String repositories = config.getString(APIMAN_PLUGIN_REPOSITORIES);
        if (repositories != null) {
            String[] split = repositories.split(","); //$NON-NLS-1$
            for (String repository : split) {
                try {
                    repository = repository.trim();
                    if (!repository.isEmpty()) {
                        if (repository.startsWith("file:")) { //$NON-NLS-1$
                            repository = repository.replace('\\', '/');
                        }
                        rval.add(new URI(repository.trim()));
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return rval;
    }

    /**
     * @return the configured plugin registries
     */
    public Set<URI> getPluginRegistries() {
        Set<URI> rval = new HashSet<>();
        String registries = config.getString(APIMAN_PLUGIN_REGISTRIES);
        if (registries != null) {
            String[] split = registries.split(","); //$NON-NLS-1$
            for (String registry : split) {
                try {
                    registry = registry.trim();
                    if (!registry.isEmpty()) {
                        if (registry.startsWith("file:")) { //$NON-NLS-1$
                            registry = registry.replace('\\', '/');
                        }
                        rval.add(new URI(registry));
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return rval;
    }

    public boolean isAdminOnlyOrgCreationEnabled() {
        return config.getBoolean(APIMAN_MANAGER_FEATURES_ORG_CREATE_ADMIN_ONLY, false);
    }

    public String getSecurityContextType() {
        return config.getString(APIMAN_MANAGER_SECURITY_CONTEXT_TYPE, "default"); //$NON-NLS-1$
    }

    /**
     * @return the configured user bootstrapper type
     */
    public String getNewUserBootstrapperType() {
        return config.getString(APIMAN_MANAGER_NEW_USER_BOOTSTRAPPER_TYPE, null);
    }

    /**
     * @return any custom properties associated with the user bootstrapper (useful for custom impls)
     */
    public Map<String, String> getNewUserBootstrapperProperties() {
        return getPrefixedProperties("apiman-manager.user-bootstrapper."); //$NON-NLS-1$
    }

    /**
     * @return the configured hibernate data source
     */
    public String getHibernateDataSource() {
        return config.getString(APIMAN_MANAGER_HIBERNATE_DS, null);
    }

    /**
     * @return the configured hibernate dialect
     */
    public String getHibernateDialect() {
        return config.getString(APIMAN_MANAGER_HIBERNATE_DIALECT, null);
    }

    /**
     * @return the configured storage type
     */
    public String getStorageType() {
        return config.getString(APIMAN_MANAGER_STORAGE_TYPE, "jpa"); //$NON-NLS-1$
    }

    /**
     * @return the configured storage query type
     */
    public String getStorageQueryType() {
        return config.getString(APIMAN_MANAGER_STORAGE_QUERY_TYPE, getStorageType());
    }

    /**
     * @return true if the elasticsearch index should be initialized if not found
     */
    public boolean isInitializeStorageJPA() {
        return config.getBoolean(APIMAN_MANAGER_STORAGE_JPA_INITIALIZE, false);
    }

    /**
     * @return the configured API catalog query type
     */
    public String getApiCatalogType() {
        return config.getString(APIMAN_MANAGER_API_CATALOG_TYPE, null);
    }

    /**
     * @return the elasticsearch client factory or null if not configured
     */
    public String getStorageESClientFactory() {
        return config.getString(APIMAN_MANAGER_STORAGE_ES_CLIENT_FACTORY);
    }

    /**
     * @return a map of config properties for the es client factory
     */
    public Map<String, String> getStorageESClientFactoryConfig() {
        return getPrefixedProperties("apiman-manager.storage.es."); //$NON-NLS-1$
    }

    /**
     * @return the storage es index name
     */
    public String getStorageESIndexName() {
        return config.getString(APIMAN_MANAGER_STORAGE_ES_INDEX_NAME, EsConstants.MANAGER_INDEX_NAME);
    }

    /**
     * @return true if the elasticsearch index should be initialized if not found
     */
    public boolean isInitializeStorageES() {
        return config.getBoolean(APIMAN_MANAGER_STORAGE_ES_INITIALIZE, true);
    }

    /**
     * @return the configured API key generator type
     */
    public String getApiKeyGeneratorType() {
        return config.getString(APIMAN_API_KEY_GENERATOR_TYPE, "uuid"); //$NON-NLS-1$
    }

    /**
     * @return the configured storage type
     */
    public String getMetricsType() {
        return config.getString(APIMAN_MANAGER_METRICS_TYPE, "es"); //$NON-NLS-1$
    }

    /**
     * @return the elasticsearch client factory or null if not configured
     */
    public String getMetricsESClientFactory() {
        return config.getString(APIMAN_MANAGER_METRICS_ES_CLIENT_FACTORY);
    }

    /**
     * @return a map of config properties for the es client factory
     */
    public Map<String, String> getMetricsESClientFactoryConfig() {
        return getPrefixedProperties("apiman-manager.metrics.es."); //$NON-NLS-1$
    }

    /**
     * @return any custom properties associated with the storage (useful for custom impls)
     */
    public Map<String, String> getStorageProperties() {
        return getPrefixedProperties("apiman-manager.storage."); //$NON-NLS-1$
    }

    /**
     * @return any custom properties associated with the storage query impl
     */
    public Map<String, String> getStorageQueryProperties() {
        return getPrefixedProperties("apiman-manager.storage-query."); //$NON-NLS-1$
    }

    /**
     * @return any custom properties associated with the IDM storage impl (useful for custom impls)
     */
    public Map<String, String> getIdmStorageProperties() {
        return getPrefixedProperties("apiman-manager.idm-storage."); //$NON-NLS-1$
    }

    /**
     * @return any custom properties associated with the metrics accessor impl
     */
    public Map<String, String> getMetricsProperties() {
        return getPrefixedProperties("apiman-manager.metrics."); //$NON-NLS-1$
    }

    /**
     * @return any custom properties associated with the custom API Key generator
     */
    public Map<String, String> getApiKeyGeneratorProperties() {
        return getPrefixedProperties("apiman-manager.api-keys.generator."); //$NON-NLS-1$
    }

    /**
     * @return any custom properties associated with the API Catalog impl
     */
    public Map<String, String> getApiCatalogProperties() {
        return getPrefixedProperties("apiman-manager.api-catalog."); //$NON-NLS-1$
    }

    /**
     * @return the configured data encrypter
     */
    public String getDataEncrypterType() {
        return config.getString(APIMAN_DATA_ENCRYPTER_TYPE, null);
    }

    /**
     * @return any custom properties associated with the data encrypter
     */
    public Map<String, String> getDataEncrypterProperties() {
        return getPrefixedProperties("apiman.encrypter."); //$NON-NLS-1$
    }

    /**
     * Gets a map of properties prefixed by the given string.
     */
    protected Map<String, String> getPrefixedProperties(String prefix) {
        Map<String, String> rval = new HashMap<>();
        Iterator<String> keys = getConfig().getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith(prefix)) {
                String value = getConfig().getString(key);
                key = key.substring(prefix.length());
                rval.put(key, value);
            }
        }
        return rval;
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
