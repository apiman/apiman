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
package org.overlord.apiman.rt.engine;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.overlord.apiman.rt.engine.policy.IPolicyFactory;

/**
 * Factory for creating the engine, obviously.
 * 
 * @author eric.wittmann@redhat.com
 */
public class EngineFactory {

    /**
     * Call this to create a new engine. This method uses the global engine
     * config singleton to create the engine.
     * 
     * @param config the engine configuration
     * @return a new apiman runtime engine
     */
    public static final IEngine createEngine() {
        IRegistry registry = createRegistry();
        IConnectorFactory cfactory = createConnectionFactory();
        IPolicyFactory pfactory = createPolicyFactory();
        
        IEngine engine = new EngineImpl(registry, cfactory, pfactory);
        return engine;
    }

    /**
     * Creates the proper registry given information found in the global engine
     * config.
     * @return a new registry instance
     */
    private static IRegistry createRegistry() {
        Class<IRegistry> c = EngineConfig.getRegistryClass();
        Map<String, String> config = EngineConfig.getRegistryConfig();
        return create(c, config);
    }

    /**
     * Creates a connection factory from configuration information.
     * @return a new connection factory
     */
    private static IConnectorFactory createConnectionFactory() {
        Class<IConnectorFactory> c = EngineConfig.getConnectorFactoryClass();
        Map<String, String> config = EngineConfig.getConnectorFactoryConfig();
        return create(c, config);
    }

    /**
     * Creates a policy factory from configuration information.
     * @return a new policy factory
     */
    private static IPolicyFactory createPolicyFactory() {
        Class<IPolicyFactory> c = EngineConfig.getPolicyFactoryClass();
        Map<String, String> config = EngineConfig.getPolicyFactoryConfig();
        return create(c, config);
    }
    
    /**
     * Creates a new instance of the given type, passing the given config
     * map if possible (if the class has a Map constructor).
     * @param type the type to create
     * @param config config to pass
     * @return a new instance of 'type'
     */
    private static <T> T create(Class<T> type, Map<String, String> config) {
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
