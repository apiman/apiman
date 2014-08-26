/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.apiman.rt.war;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.overlord.apiman.rt.engine.IComponent;
import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IEngineConfig;
import org.overlord.apiman.rt.engine.IRegistry;
import org.overlord.apiman.rt.engine.i18n.Messages;
import org.overlord.apiman.rt.engine.policy.IPolicyFactory;
import org.overlord.commons.config.ConfigurationFactory;

/**
 * Global access to configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
public class WarEngineConfig implements IEngineConfig {

    public static final String APIMAN_RT_CONFIG_FILE_NAME     = "apiman-rt.config.file.name"; //$NON-NLS-1$
    public static final String APIMAN_RT_CONFIG_FILE_REFRESH  = "apiman-rt.config.file.refresh"; //$NON-NLS-1$

    public static final String APIMAN_RT_REGISTRY_CLASS = "apiman-rt.registry"; //$NON-NLS-1$
    public static final String APIMAN_RT_CONNECTOR_FACTORY_CLASS = "apiman-rt.connector-factory"; //$NON-NLS-1$
    public static final String APIMAN_RT_POLICY_FACTORY_CLASS = "apiman-rt.policy-factory"; //$NON-NLS-1$
    
    public static final String APIMAN_RT_COMPONENT_PREFIX = "apiman-rt.components."; //$NON-NLS-1$

    public static final String APIMAN_RT_GATEWAY_SERVER_PORT = "apiman-rt.gateway.server.port"; //$NON-NLS-1$

    public static Configuration config;
    static {
        String configFile = System.getProperty(APIMAN_RT_CONFIG_FILE_NAME);
        String refreshDelayStr = System.getProperty(APIMAN_RT_CONFIG_FILE_REFRESH);
        Long refreshDelay = 5000l;
        if (refreshDelayStr != null) {
            refreshDelay = new Long(refreshDelayStr);
        }

        config = ConfigurationFactory.createConfig(
                configFile,
                "apiman.properties", //$NON-NLS-1$
                refreshDelay,
                null,
                WarEngineConfig.class);
    }

    /**
     * Constructor.
     */
    public WarEngineConfig() {
    }

    /**
     * @return the configuration
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * @return the class to use as the {@link IRegistry}
     */
    public Class<IRegistry> getRegistryClass() {
        return loadConfigClass(APIMAN_RT_REGISTRY_CLASS, IRegistry.class);
    }

    /**
     * @return all properties to be passed to the registry
     */
    public Map<String, String> getRegistryConfig() {
        return getConfigMap(APIMAN_RT_REGISTRY_CLASS + "."); //$NON-NLS-1$
    }

    /**
     * @return the class to use as the {@link IConnectorFactory}
     */
    public Class<IConnectorFactory> getConnectorFactoryClass() {
        return loadConfigClass(APIMAN_RT_CONNECTOR_FACTORY_CLASS, IConnectorFactory.class);
    }

    /**
     * @return all properties to be passed to the factory
     */
    public Map<String, String> getConnectorFactoryConfig() {
        return getConfigMap(APIMAN_RT_CONNECTOR_FACTORY_CLASS + "."); //$NON-NLS-1$
    }

    /**
     * @return the class to use as the {@link IPolicyFactory}
     */
    public Class<IPolicyFactory> getPolicyFactoryClass() {
        return loadConfigClass(APIMAN_RT_POLICY_FACTORY_CLASS, IPolicyFactory.class);
    }

    /**
     * @return all properties to be passed to the factory
     */
    public Map<String, String> getPolicyFactoryConfig() {
        return getConfigMap(APIMAN_RT_POLICY_FACTORY_CLASS + "."); //$NON-NLS-1$
    }

    /**
     * @return the class to use for the given component
     */
    public <T extends IComponent> Class<T> getComponentClass(Class<T> componentType) {
        return loadConfigClass(APIMAN_RT_COMPONENT_PREFIX + componentType.getSimpleName(), componentType);
    }

    /**
     * @return all properties to be passed to the factory
     */
    public <T extends IComponent> Map<String, String> getComponentConfig(Class<T> componentType) {
        return getConfigMap(APIMAN_RT_COMPONENT_PREFIX + componentType.getSimpleName() + "."); //$NON-NLS-1$
    }

    /**
     * @return the configured server port
     */
    public int getServerPort() {
        return config.getInt(APIMAN_RT_GATEWAY_SERVER_PORT, 8080);
    }

    /**
     * @return a loaded class
     */
    @SuppressWarnings("unchecked")
    private <T> Class<T> loadConfigClass(String property, Class<T> type) {
        String classname = getConfig().getString(property);
        if (classname == null) {
            throw new RuntimeException("No " + type.getSimpleName() + " class configured."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        try {
            Class<T> c = (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(classname);
            return c;
        } catch (ClassNotFoundException e) {
            // Not found via Class.forName() - try other mechanisms.
        }
        try {
            Class<T> c = (Class<T>) Class.forName(classname);
            return c;
        } catch (ClassNotFoundException e) {
            // Not found via Class.forName() - try other mechanisms.
        }
        throw new RuntimeException(Messages.i18n.format("EngineConfig.FailedToLoadClass", classname)); //$NON-NLS-1$
    }

    /**
     * Gets all properties in the engine configuration that are prefixed
     * with the given prefix.
     * @param prefix
     * @return all prefixed properties
     */
    private Map<String, String> getConfigMap(String prefix) {
        Map<String, String> rval = new HashMap<String, String>();
        Iterator<?> keys = config.getKeys(prefix + "."); //$NON-NLS-1$
        while (keys.hasNext()) {
            String key = String.valueOf(keys.next());
            rval.put(key, config.getString(key));
        }
        return rval;
    }
}
