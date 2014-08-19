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
package org.overlord.apiman.rt.engine.impl;

import org.overlord.apiman.rt.engine.IComponentRegistry;
import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IEngine;
import org.overlord.apiman.rt.engine.IEngineFactory;
import org.overlord.apiman.rt.engine.IRegistry;
import org.overlord.apiman.rt.engine.policy.IPolicyFactory;

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
     * 
     * @param engineConfig
     */
    public final IEngine createEngine() {
        IRegistry registry = createRegistry();
        IComponentRegistry componentRegistry = createComponentRegistry();
        IConnectorFactory cfactory = createConnectorFactory();
        IPolicyFactory pfactory = createPolicyFactory();
        
        IEngine engine = new EngineImpl(registry, componentRegistry, cfactory, pfactory);
        return engine;
    }

    /**
     * Creates a registry.
     * @param engineConfig 
     * @return a new registry instance
     */
    protected abstract IRegistry createRegistry();

    /**
     * Creates a component registry.
     * @param engineConfig 
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
     * @param engineConfig 
     * @return a new policy factory
     */
    protected abstract IPolicyFactory createPolicyFactory();

}
