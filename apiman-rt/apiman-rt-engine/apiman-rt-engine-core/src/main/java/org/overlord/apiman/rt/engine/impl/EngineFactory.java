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

import java.lang.reflect.Constructor;
import java.util.Map;

import org.overlord.apiman.rt.engine.IComponentRegistry;
import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IEngine;
import org.overlord.apiman.rt.engine.IEngineConfig;
import org.overlord.apiman.rt.engine.IEngineFactory;
import org.overlord.apiman.rt.engine.IRegistry;
import org.overlord.apiman.rt.engine.policy.IPolicyFactory;

/**
 * Factory for creating the engine, obviously.
 * 
 * @author eric.wittmann@redhat.com
 */
public class EngineFactory implements IEngineFactory {
    
    private IEngineConfig engineConfig;
    
    /**
     * Constructor.
     * @param engineConfig
     */
    public EngineFactory(IEngineConfig engineConfig) {
        this.engineConfig = engineConfig;
    }
    
    /**
     * Call this to create a new engine. This method uses the global engine
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
     * Creates the proper registry given information found in the global engine
     * config.
     * @param engineConfig 
     * @return a new registry instance
     */
    protected IRegistry createRegistry() {
        Class<IRegistry> c = engineConfig.getRegistryClass();
        Map<String, String> config = engineConfig.getRegistryConfig();
        return create(c, config);
    }

    /**
     * Creates the proper component registry given information found in the global engine
     * config.
     * @param engineConfig 
     * @return a new registry instance
     */
    protected IComponentRegistry createComponentRegistry() {
        // TODO This should be pluggable - should be done as part of the apiman plugin framework work
        return new ComponentRegistryImpl(engineConfig);
    }

    /**
     * Creates a connection factory from configuration information.
     * @param engineConfig 
     * @return a new connection factory
     */
    protected IConnectorFactory createConnectorFactory() {
        Class<IConnectorFactory> c = engineConfig.getConnectorFactoryClass();
        Map<String, String> config = engineConfig.getConnectorFactoryConfig();
        return create(c, config);
    }

    /**
     * Creates a policy factory from configuration information.
     * @param engineConfig 
     * @return a new policy factory
     */
    protected IPolicyFactory createPolicyFactory() {
        Class<IPolicyFactory> c = engineConfig.getPolicyFactoryClass();
        Map<String, String> config = engineConfig.getPolicyFactoryConfig();
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
        } catch (Exception e) {
            // Probably doesn't have a map c'tor - so try a no-arg c'tor instead
        }
        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
