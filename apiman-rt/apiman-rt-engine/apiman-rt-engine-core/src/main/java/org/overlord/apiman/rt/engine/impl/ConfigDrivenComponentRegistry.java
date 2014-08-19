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

import java.util.HashMap;
import java.util.Map;

import org.overlord.apiman.rt.engine.IComponent;
import org.overlord.apiman.rt.engine.IComponentRegistry;
import org.overlord.apiman.rt.engine.IEngineConfig;
import org.overlord.apiman.rt.engine.beans.exceptions.ComponentNotFoundException;

/**
 * A simple component registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConfigDrivenComponentRegistry implements IComponentRegistry {
    
    private IEngineConfig engineConfig;
    private Map<Class<? extends IComponent>, IComponent> components = new HashMap<Class<? extends IComponent>, IComponent>();
    
    /**
     * Constructor.
     * @param engineConfig 
     */
    public ConfigDrivenComponentRegistry(IEngineConfig engineConfig) {
        this.engineConfig = engineConfig;
    }

    /**
     * @see org.overlord.apiman.rt.engine.IComponentRegistry#getComponent(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IComponent> T getComponent(Class<T> componentType) throws ComponentNotFoundException {
        if (components.containsKey(componentType)) {
            return (T) components.get(componentType);
        } else {
            return createAndRegisterComponent(componentType);
        }
    }

    /**
     * Creates the component and registers it in the registry.
     * @param componentType
     * @throws ComponentNotFoundException
     */
    public <T extends IComponent> T createAndRegisterComponent(Class<T> componentType) throws ComponentNotFoundException {
        try {
            synchronized (components) {
                Class<T> componentClass = engineConfig.getComponentClass(componentType);
                Map<String, String> componentConfig = engineConfig.getComponentConfig(componentType);
                T component = ConfigDrivenEngineFactory.create(componentClass, componentConfig);
                components.put(componentType, component);
                return component;
            }
        } catch (Exception e) {
            throw new ComponentNotFoundException(componentType.getName());
        }
    }

}
