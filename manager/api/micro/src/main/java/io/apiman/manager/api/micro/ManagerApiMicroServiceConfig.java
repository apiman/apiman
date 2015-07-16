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
package io.apiman.manager.api.micro;

import io.apiman.manager.api.core.config.ApiManagerConfig;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.jpa.IJpaProperties;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Configuration for the API Manager back-end micro service.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ManagerApiMicroServiceConfig extends ApiManagerConfig implements IJpaProperties {

    public static final String APIMAN_PLUGIN_DIRECTORY = "apiman.plugins.plugin-directory"; //$NON-NLS-1$

    @Inject
    @ApimanLogger(ManagerApiMicroService.class)
    private IApimanLogger log;

    /**
     * Constructor.
     */
    public ManagerApiMicroServiceConfig() {
    }
    
    /**
     * @see io.apiman.manager.api.core.config.ApiManagerConfig#loadProperties()
     */
    @Override
    protected Configuration loadProperties() {
        CompositeConfiguration config = (CompositeConfiguration) super.loadProperties();
        try {
            config.addConfiguration(new PropertiesConfiguration(getClass().getClassLoader().getResource("micro-apiman.properties"))); //$NON-NLS-1$
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        return config;
    }

    /**
     * @return the configured plugin directory
     */
    public File getPluginDirectory() {
        String pluginDirPath = getConfig().getString(APIMAN_PLUGIN_DIRECTORY, null);
        try {
            if (pluginDirPath == null) {
                File tempFile = File.createTempFile("apiman", "plugins"); //$NON-NLS-1$ //$NON-NLS-2$
                if (tempFile.isFile()) {
                    tempFile.delete();
                }
                tempFile.mkdirs();
                pluginDirPath = tempFile.getAbsolutePath();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File pluginsDir = new File(pluginDirPath);
        return pluginsDir;
    }

    /**
     * @see io.apiman.manager.api.jpa.IJpaProperties#getAllHibernateProperties()
     */
    @Override
    public Map<String, String> getAllHibernateProperties() {
        Map<String, String> rval = new HashMap<>();
        Iterator<String> keys = getConfig().getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("apiman.hibernate.")) { //$NON-NLS-1$
                String value = getConfig().getString(key);
                key = key.substring("apiman.".length()); //$NON-NLS-1$
                rval.put(key, value);
            }
        }
        return rval;
    }

}
