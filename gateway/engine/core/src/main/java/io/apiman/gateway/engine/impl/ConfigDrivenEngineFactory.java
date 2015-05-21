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
package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.policy.IPolicyFactory;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Factory for creating the engine, obviously.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConfigDrivenEngineFactory extends AbstractEngineFactory {

    private IEngineConfig engineConfig;

    /**
     * Constructor.
     * @param engineConfig the engine config
     */
    public ConfigDrivenEngineFactory(IEngineConfig engineConfig) {
        this.engineConfig = engineConfig;
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createRegistry()
     */
    @Override
    protected IRegistry createRegistry() {
        Class<? extends IRegistry> c = engineConfig.getRegistryClass();
        Map<String, String> config = engineConfig.getRegistryConfig();
        IRegistry registry = create(c, config);
        return new SecureRegistryWrapper(registry);
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createPluginRegistry()
     */
    @Override
    protected IPluginRegistry createPluginRegistry() {
        Class<? extends IPluginRegistry> c = engineConfig.getPluginRegistryClass();
        Map<String, String> config = engineConfig.getPluginRegistryConfig();
        return create(c, config);
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createComponentRegistry()
     */
    @Override
    protected IComponentRegistry createComponentRegistry() {
        return new ConfigDrivenComponentRegistry(engineConfig);
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createConnectorFactory()
     */
    @Override
    protected IConnectorFactory createConnectorFactory() {
        Class<? extends IConnectorFactory> c = engineConfig.getConnectorFactoryClass();
        Map<String, String> config = engineConfig.getConnectorFactoryConfig();
        return create(c, config);
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createPolicyFactory()
     */
    @Override
    protected IPolicyFactory createPolicyFactory() {
        Class<? extends IPolicyFactory> c = engineConfig.getPolicyFactoryClass();
        Map<String, String> config = engineConfig.getPolicyFactoryConfig();
        return create(c, config);
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createMetrics()
     */
    @Override
    protected IMetrics createMetrics() {
        Class<? extends IMetrics> c = engineConfig.getMetricsClass();
        Map<String, String> config = engineConfig.getMetricsConfig();
        return create(c, config);
    }

    /**
     * Creates a new instance of the given type, passing the given config
     * map if possible (if the class has a Map constructor).
     * @param type the type to create
     * @param config config to pass
     * @return a new instance of 'type'
     */
    protected static <T> T create(Class<T> type, Map<String, String> config) {
        try {
            Constructor<T> constructor = type.getConstructor(Map.class);
            return constructor.newInstance(config);
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
