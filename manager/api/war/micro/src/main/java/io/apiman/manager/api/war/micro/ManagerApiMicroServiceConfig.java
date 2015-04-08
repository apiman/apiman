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
package io.apiman.manager.api.war.micro;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;

/**
 * Configuration for the API Manager back-end micro service.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ManagerApiMicroServiceConfig {

    public static final String APIMAN_MANAGER_STORAGE_TYPE = "apiman-manager.storage.type"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_HOST = "apiman-manager.storage.es.host"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_PORT = "apiman-manager.storage.es.port"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_CLUSTER_NAME = "apiman-manager.storage.es.cluster-name"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_STORAGE_ES_INITIALIZE = "apiman-manager.storage.es.initialize"; //$NON-NLS-1$

    public static final String APIMAN_PLUGIN_REPOSITORIES = "apiman.plugins.repositories"; //$NON-NLS-1$
    public static final String APIMAN_PLUGIN_DIRECTORY = "apiman.plugins.plugin-directory"; //$NON-NLS-1$

    public static final String DEFAULT_ES_CLUSTER_NAME = "apiman"; //$NON-NLS-1$
    

    private Configuration config;
    
    /**
     * Constructor.
     */
    public ManagerApiMicroServiceConfig() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        // All configuration comes from system properties ATM
        config = new SystemConfiguration();
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
    
    /**
     * @return the configured storage type
     */
    public String getStorageType() {
        return config.getString(APIMAN_MANAGER_STORAGE_TYPE, "es"); //$NON-NLS-1$
    }
    
    /**
     * @return the elasticsearch host
     */
    public String getESHost() {
        return config.getString(APIMAN_MANAGER_STORAGE_ES_HOST, "localhost"); //$NON-NLS-1$
    }
    
    /**
     * @return the elasticsearch port
     */
    public int getESPort() {
        return config.getInt(APIMAN_MANAGER_STORAGE_ES_PORT, 9300);
    }
    
    /**
     * @return the elasticsearch cluster name
     */
    public String getESClusterName() {
        return config.getString(APIMAN_MANAGER_STORAGE_ES_CLUSTER_NAME, DEFAULT_ES_CLUSTER_NAME);
    }
    
    /**
     * @return true if the elasticsearch index should be initialized if not found
     */
    public boolean isInitializeES() {
        return config.getBoolean(APIMAN_MANAGER_STORAGE_ES_INITIALIZE, true);
    }

    /**
     * @return the configured plugin directory
     */
    public File getPluginDirectory() {
        String pluginDirPath = config.getString(APIMAN_PLUGIN_DIRECTORY, null);
        if (pluginDirPath == null) {
            throw new RuntimeException("Missing environment variable: " + APIMAN_PLUGIN_DIRECTORY); //$NON-NLS-1$
        }
        File pluginsDir = new File(pluginDirPath);
        return pluginsDir;
    }
    
}
