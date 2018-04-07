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

import io.apiman.common.logging.IDelegateFactory;
import io.apiman.common.util.crypt.CurrentDataEncrypter;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.gateway.engine.IApiRequestPathParser;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineFactory;
import io.apiman.gateway.engine.IGatewayInitializer;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.policy.IPolicyFactory;

import java.util.List;

/**
 * Base class useful for creating engine factories.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractEngineFactory implements IEngineFactory {

    /**
     * Constructor.
     */
    public AbstractEngineFactory() {
    }

    /**
     * Call this to create a new engine. This method uses the engine
     * config singleton to create the engine.
     */
    @Override
    public final IEngine createEngine() {
        IPluginRegistry pluginRegistry = createPluginRegistry();
        IDataEncrypter encrypter = createDataEncrypter(pluginRegistry);
        CurrentDataEncrypter.instance = encrypter;
        IRegistry registry = createRegistry(pluginRegistry, encrypter);
        IComponentRegistry componentRegistry = createComponentRegistry(pluginRegistry);
        IConnectorFactory cfactory = createConnectorFactory(pluginRegistry);
        IPolicyFactory pfactory = createPolicyFactory(pluginRegistry);
        IMetrics metrics = createMetrics(pluginRegistry);
        IDelegateFactory logFactory = createLoggerFactory(pluginRegistry);
        IApiRequestPathParser pathParser = createRequestPathParser(pluginRegistry);

        List<IGatewayInitializer> initializers = createInitializers(pluginRegistry);
        for (IGatewayInitializer initializer : initializers) {
            initializer.initialize();
        }

        complete();
        return new EngineImpl(registry, pluginRegistry, componentRegistry, cfactory, pfactory, metrics, logFactory, pathParser);
    }

    /**
     * Creates a plugin registry.
     * @return a new registry instance
     */
    protected abstract IPluginRegistry createPluginRegistry();

    /**
     * Creates a data encrypter.
     * @param pluginRegistry the plugin registry
     * @return a new data encrypter
     */
    protected abstract IDataEncrypter createDataEncrypter(IPluginRegistry pluginRegistry);

    /**
     * Creates a registry.
     * @param pluginRegistry the plugin registry
     * @param encrypter the data encrypter
     * @return a new registry instance
     */
    protected abstract IRegistry createRegistry(IPluginRegistry pluginRegistry, IDataEncrypter encrypter);

    /**
     * Creates a component registry.
     * @param pluginRegistry the plugin registry
     * @return a new registry instance
     */
    protected abstract IComponentRegistry createComponentRegistry(IPluginRegistry pluginRegistry);

    /**
     * Creates a connector factory.
     * @param pluginRegistry the plugin registry
     * @return a new connection factory
     */
    protected abstract IConnectorFactory createConnectorFactory(IPluginRegistry pluginRegistry);

    /**
     * Creates a policy factory.
     * @param pluginRegistry the plugin registry
     * @return a new policy factory
     */
    protected abstract IPolicyFactory createPolicyFactory(IPluginRegistry pluginRegistry);

    /**
     * Creates the metrics system.
     * @param pluginRegistry the plugin registry
     * @return the metrics object
     */
    protected abstract IMetrics createMetrics(IPluginRegistry pluginRegistry);

    /**
     * Creates the gateway initializers.
     * @param pluginRegistry
     */
    protected abstract List<IGatewayInitializer> createInitializers(IPluginRegistry pluginRegistry);

    /**
     * Creates the logger factory
     * @return anew log factory
     */
    protected abstract IDelegateFactory createLoggerFactory(IPluginRegistry pluginRegistry);


    /**
     * Creates the request path parser.
     * @param pluginRegistry the plugin registry
     * @return the request path parser
     */
    protected abstract IApiRequestPathParser createRequestPathParser(IPluginRegistry pluginRegistry);

    /**
     * Call when the engine factory has completed initial startup/loading..
     */
    protected abstract void complete();

}
