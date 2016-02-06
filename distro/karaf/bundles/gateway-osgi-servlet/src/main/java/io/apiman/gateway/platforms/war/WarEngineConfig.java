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
package io.apiman.gateway.platforms.war;

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginClassLoader;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.common.util.ReflectionUtils;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.gateway.engine.*;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.impl.DefaultPolicyErrorWriter;
import io.apiman.gateway.engine.impl.DefaultPolicyFailureWriter;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Global access to configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
public class WarEngineConfig implements IEngineConfig {

    public static final String APIMAN_GATEWAY_REGISTRY_CLASS = "apiman-gateway.registry"; //$NON-NLS-1$
    public static final String APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS = "apiman-gateway.plugin-registry"; //$NON-NLS-1$
    public static final String APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS = "apiman-gateway.connector-factory"; //$NON-NLS-1$
    public static final String APIMAN_GATEWAY_POLICY_FACTORY_CLASS = "apiman-gateway.policy-factory"; //$NON-NLS-1$
    public static final String APIMAN_GATEWAY_METRICS_CLASS = "apiman-gateway.metrics"; //$NON-NLS-1$

    public static final String APIMAN_DATA_ENCRYPTER_TYPE = "apiman.encrypter.type"; //$NON-NLS-1$

    public static final String APIMAN_GATEWAY_COMPONENT_PREFIX = "apiman-gateway.components."; //$NON-NLS-1$

    public static final String APIMAN_GATEWAY_WRITER_FORMATTER_CLASS = "apiman-gateway.writers.policy-failure"; //$NON-NLS-1$
    public static final String APIMAN_GATEWAY_ERROR_WRITER_CLASS = "apiman-gateway.writers.error"; //$NON-NLS-1$

    public static Configuration config;

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

    public static void setConfig(Dictionary dict) {
        config = new BaseConfiguration();
        Map<String, Object> map = new HashMap<String, Object>(dict.size());
        Enumeration<String> keys = dict.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            config.addProperty(key, dict.get(key));
        }
    }

    /**
     * Returns the given configuration property name or the provided default
     * value if not found.
     * @param propertyName the property name
     * @param defaultValue the default value
     * @return the config property
     */
    public String getConfigProperty(String propertyName, String defaultValue) {
        return getConfig().getString(propertyName, defaultValue);
    }

    /**
     * @see IEngineConfig#getRegistryClass(IPluginRegistry)
     */
    @Override
    public Class<? extends IRegistry> getRegistryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(APIMAN_GATEWAY_REGISTRY_CLASS, IRegistry.class, pluginRegistry);
    }

    /**
     * @return all properties to be passed to the registry
     */
    @Override
    public Map<String, String> getRegistryConfig() {
        return getConfigMap(APIMAN_GATEWAY_REGISTRY_CLASS);
    }

    /**
     * @return the class to use as the {@link IPluginRegistry}
     */
    @Override
    public Class<IPluginRegistry> getPluginRegistryClass() {
        return (Class<IPluginRegistry>) loadConfigClass(APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS, IPluginRegistry.class, null);
    }

    /**
     * @return all properties to be passed to the registry
     */
    @Override
    public Map<String, String> getPluginRegistryConfig() {
        Map<String, String> configMap = getConfigMap(APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS);
        String pluginsDirOverride = System.getProperty(APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS + ".pluginsDir"); //$NON-NLS-1$
        if (pluginsDirOverride != null) {
            configMap.put("pluginsDir", pluginsDirOverride); //$NON-NLS-1$
        }
        return configMap;
    }

    /**
     * @see IEngineConfig#getConnectorFactoryClass(IPluginRegistry)
     */
    @Override
    public Class<? extends IConnectorFactory> getConnectorFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS, IConnectorFactory.class, pluginRegistry);
    }

    /**
     * @return all properties to be passed to the factory
     */
    @Override
    public Map<String, String> getConnectorFactoryConfig() {
        return getConfigMap(APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS);
    }

    /**
     * @see IEngineConfig#getPolicyFactoryClass(IPluginRegistry)
     */
    @Override
    public Class<? extends IPolicyFactory> getPolicyFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(APIMAN_GATEWAY_POLICY_FACTORY_CLASS, IPolicyFactory.class, pluginRegistry);
    }

    /**
     * @return all properties to be passed to the factory
     */
    @Override
    public Map<String, String> getPolicyFactoryConfig() {
        return getConfigMap(APIMAN_GATEWAY_POLICY_FACTORY_CLASS);
    }

    /**
     * @see IEngineConfig#getMetricsClass(IPluginRegistry)
     */
    @Override
    public Class<? extends IMetrics> getMetricsClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(APIMAN_GATEWAY_METRICS_CLASS, IMetrics.class, pluginRegistry);
    }

    /**
     * @return all properties to be passed to the factory
     */
    @Override
    public Map<String, String> getMetricsConfig() {
        return getConfigMap(APIMAN_GATEWAY_METRICS_CLASS);
    }

    /**
     * @see IEngineConfig#getComponentClass(Class, IPluginRegistry)
     */
    @Override
    public <T extends IComponent> Class<T> getComponentClass(Class<T> componentType,
            IPluginRegistry pluginRegistry) {
        return (Class<T>) loadConfigClass(APIMAN_GATEWAY_COMPONENT_PREFIX + componentType.getSimpleName(), componentType, pluginRegistry);
    }

    /**
     * @return all properties to be passed to the factory
     */
    @Override
    public <T extends IComponent> Map<String, String> getComponentConfig(Class<T> componentType) {
        return getConfigMap(APIMAN_GATEWAY_COMPONENT_PREFIX + componentType.getSimpleName());
    }

    /**
     * @see IEngineConfig#getDataEncrypterClass(IPluginRegistry)
     */
    @Override
    public Class<? extends IDataEncrypter> getDataEncrypterClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(APIMAN_DATA_ENCRYPTER_TYPE, IDataEncrypter.class, pluginRegistry);
    }

    /**
     * @see IEngineConfig#getDataEncrypterConfig()
     */
    @Override
    public Map<String, String> getDataEncrypterConfig() {
        return getConfigMap(APIMAN_DATA_ENCRYPTER_TYPE);
    }

    /**
     * @return the class to use as the {@link IPolicyFailureWriter}
     */
    public Class<IPolicyFailureWriter> getPolicyFailureWriterClass(IPluginRegistry pluginRegistry) {
        return (Class<IPolicyFailureWriter>) loadConfigClass(APIMAN_GATEWAY_WRITER_FORMATTER_CLASS,
                IPolicyFailureWriter.class, pluginRegistry, DefaultPolicyFailureWriter.class);
    }

    /**
     * @return all properties to be passed to the failure formatter
     */
    public Map<String, String> getPolicyFailureWriterConfig() {
        return getConfigMap(APIMAN_GATEWAY_WRITER_FORMATTER_CLASS);
    }

    /**
     * @return the class to use as the {@link IPolicyErrorWriter}
     */
    public Class<IPolicyErrorWriter> getPolicyErrorWriterClass(IPluginRegistry pluginRegistry) {
        return (Class<IPolicyErrorWriter>) loadConfigClass(APIMAN_GATEWAY_ERROR_WRITER_CLASS,
                IPolicyErrorWriter.class, pluginRegistry, DefaultPolicyErrorWriter.class);
    }

    /**
     * @return all properties to be passed to the error formatter
     */
    public Map<String, String> getPolicyErrorWriterConfig() {
        return getConfigMap(APIMAN_GATEWAY_ERROR_WRITER_CLASS);
    }

    /**
     * @return a loaded class
     */
    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> loadConfigClass(String property, Class<T> type, IPluginRegistry pluginRegistry) {
        Class<? extends T> rval = loadConfigClass(property, type, pluginRegistry, null);
        if (rval == null) {
            throw new RuntimeException("No " + type.getSimpleName() + " class configured."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return rval;
    }

    /**
     * @return a loaded class
     */
    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> loadConfigClass(String property, Class<T> type, IPluginRegistry pluginRegistry, Class<? extends T> defaultClass) {
        String componentSpec = getConfig().getString(property);
        if (componentSpec == null) {
            return defaultClass;
        }

        try {
            if (componentSpec.startsWith("class:")) { //$NON-NLS-1$
                Class<?> c = ReflectionUtils.loadClass(componentSpec.substring("class:".length())); //$NON-NLS-1$
                return (Class<T>) c;
            } else if (componentSpec.startsWith("plugin:")) { //$NON-NLS-1$
                PluginCoordinates coordinates = PluginCoordinates.fromPolicySpec(componentSpec);
                if (coordinates == null) {
                    throw new IllegalArgumentException("Invalid plugin component spec: " + componentSpec); //$NON-NLS-1$
                }
                int ssidx = componentSpec.indexOf('/');
                if (ssidx == -1) {
                    throw new IllegalArgumentException("Invalid plugin component spec: " + componentSpec); //$NON-NLS-1$
                }
                String classname = componentSpec.substring(ssidx + 1);
                Future<IAsyncResult<Plugin>> pluginF = pluginRegistry.loadPlugin(coordinates, null);
                IAsyncResult<Plugin> pluginR = pluginF.get();
                if (pluginR.isError()) {
                    throw new RuntimeException(pluginR.getError());
                }
                Plugin plugin = pluginR.getResult();
                PluginClassLoader classLoader = plugin.getLoader();
                Class<?> class1 = classLoader.loadClass(classname);
                return (Class<T>) class1;
            } else {
                Class<?> c = ReflectionUtils.loadClass(componentSpec);
                return (Class<T>) c;
            }
        } catch (ClassNotFoundException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets all properties in the engine configuration that are prefixed
     * with the given prefix.
     * @param prefix
     * @return all prefixed properties
     */
    private Map<String, String> getConfigMap(String prefix) {
        Map<String, String> rval = new HashMap<>();
        Iterator<?> keys = config.getKeys(prefix);
        while (keys.hasNext()) {
            String key = String.valueOf(keys.next());
            if (key.equals(prefix)) {
                continue;
            }
            String shortKey = key.substring(prefix.length() + 1);
            rval.put(shortKey, config.getString(key));
        }
        return rval;
    }
}
