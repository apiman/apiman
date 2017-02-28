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
package io.apiman.gateway.engine;

import io.apiman.common.logging.IDelegateFactory;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.gateway.engine.logging.ILogger;
import io.apiman.gateway.engine.policy.IPolicyFactory;

import java.util.List;
import java.util.Map;

/**
 * Engine configuration - used by the engine factory when creating the engine.  :)
 *
 * @author eric.wittmann@redhat.com
 */
public interface IEngineConfig {

    /**
     * @param pluginRegistry the plugin registry
     * @return the class to use as the {@link IRegistry}
     */
    public Class<? extends IRegistry> getRegistryClass(IPluginRegistry pluginRegistry);

    /**
     * @return all properties to be passed to the registry
     */
    public Map<String, String> getRegistryConfig();

    /**
     * @param pluginRegistry the plugin registry
     * @return the class to use as the {@link IDataEncrypter}
     */
    public Class<? extends IDataEncrypter> getDataEncrypterClass(IPluginRegistry pluginRegistry);

    /**
     * @return all properties to be passed to the registry
     */
    public Map<String, String> getDataEncrypterConfig();

    /**
     * @return the class to use as the {@link IPluginRegistry}
     */
    public Class<? extends IPluginRegistry> getPluginRegistryClass();

    /**
     * @return all properties to be passed to the plugin registry
     */
    public Map<String, String> getPluginRegistryConfig();

    /**
     * @param pluginRegistry the plugin registry
     * @return the class to use as the {@link IConnectorFactory}
     */
    public Class<? extends IConnectorFactory> getConnectorFactoryClass(IPluginRegistry pluginRegistry);

    public Class<? extends IApiRequestPathParser> getApiRequestPathParserClass(IPluginRegistry pluginRegistry);

    public Map<String, String> getApiRequestPathParserConfig();

    /**
     * @return all properties to be passed to the factory
     */
    public Map<String, String> getConnectorFactoryConfig();

    /**
     * @param pluginRegistry the plugin registry
     * @return the class to use as the {@link IPolicyFactory}
     */
    public Class<? extends IPolicyFactory> getPolicyFactoryClass(IPluginRegistry pluginRegistry);

    /**
     * @return all properties to be passed to the factory
     */
    public Map<String, String> getPolicyFactoryConfig();

    /**
     * @param componentType the component type
     * @param pluginRegistry the plugin registry
     * @return the class to use for the given component
     */
    public <T extends IComponent> Class<? extends T> getComponentClass(Class<? extends T> componentType, IPluginRegistry pluginRegistry);

    /**
     * @param componentType the component type
     * @return all properties to be passed to the factory
     */
    public <T extends IComponent> Map<String, String> getComponentConfig(Class<T> componentType);

    /**
     * @param pluginRegistry the plugin registry
     * @return the class to use as the {@link IMetrics}
     */
    public Class<? extends IMetrics> getMetricsClass(IPluginRegistry pluginRegistry);

    /**
     * @return all properties to be passed to the factory
     */
    public Map<String, String> getMetricsConfig();

    /**
     * @param pluginRegistry the plugin registry
     * @return the class to use as the {@link IGatewayInitializer}
     */
    public List<EngineConfigTuple<? extends IGatewayInitializer>> getGatewayInitializers(IPluginRegistry pluginRegistry);

    /**
     * @param pluginRegistry the plugin registry
     * @return the factory used to create instances of {@link ILogger}.
     */
    public Class<? extends IDelegateFactory> getLoggerFactoryClass(IPluginRegistry pluginRegistry);

    /**
     * @return all properties to be passed to the factory
     */
    public Map<String, String> getLoggerFactoryConfig();

    /**
     * @param pluginRegistry the plugin registry
     * @return the class to use as the {@link IPolicyErrorWriter}
     */
    Class<? extends IPolicyErrorWriter> getPolicyErrorWriterClass(IPluginRegistry pluginRegistry);

    /**
     * @return all properties to be passed to the error formatter
     */
    Map<String, String> getPolicyErrorWriterConfig();

    /**
     * @param pluginRegistry The plugin registry
     * @return the class to use as the {@link IPolicyFailureWriter}
     */
    Class<? extends IPolicyFailureWriter> getPolicyFailureWriterClass(IPluginRegistry pluginRegistry);

    /**
     * @return all properties to be passed to the failure formatter
     */
    Map<String, String> getPolicyFailureWriterConfig();
}
