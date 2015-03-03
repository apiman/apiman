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
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineFactory;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.policy.IPolicyFactory;

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
    public final IEngine createEngine() {
        IRegistry registry = createRegistry();
        IPluginRegistry pluginRegistry = createPluginRegistry();
        IComponentRegistry componentRegistry = createComponentRegistry();
        IConnectorFactory cfactory = createConnectorFactory();
        IPolicyFactory pfactory = createPolicyFactory();
        IMetrics metrics = createMetrics();
        
        IEngine engine = new EngineImpl(registry, pluginRegistry, componentRegistry, cfactory, pfactory, metrics);
        return engine;
    }

    /**
     * Creates a registry.
     * @return a new registry instance
     */
    protected abstract IRegistry createRegistry();

    /**
     * Creates a plugin registry.
     * @return a new registry instance
     */
    protected abstract IPluginRegistry createPluginRegistry();

    /**
     * Creates a component registry.
     * @return a new registry instance
     */
    protected abstract IComponentRegistry createComponentRegistry();

    /**
     * Creates a connector factory.
     * @return a new connection factory
     */
    protected abstract IConnectorFactory createConnectorFactory();

    /**
     * Creates a policy factory.
     * @return a new policy factory
     */
    protected abstract IPolicyFactory createPolicyFactory();

    /**
     * Creates the metrics system.
     * @return the metrics object
     */
    protected abstract IMetrics createMetrics();

}
