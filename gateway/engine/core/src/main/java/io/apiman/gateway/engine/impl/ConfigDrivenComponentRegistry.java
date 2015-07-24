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

import io.apiman.common.util.ReflectionUtils;
import io.apiman.gateway.engine.DependsOnComponents;
import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IRequiresInitialization;
import io.apiman.gateway.engine.beans.exceptions.ComponentNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple component registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConfigDrivenComponentRegistry implements IComponentRegistry {

    private IEngineConfig engineConfig;
    private IPluginRegistry pluginRegistry;
    private Map<Class<? extends IComponent>, IComponent> components = new HashMap<>();

    /**
     * Constructor.
     * @param engineConfig the engine config
     */
    public ConfigDrivenComponentRegistry(IEngineConfig engineConfig, IPluginRegistry pluginRegistry) {
        this.engineConfig = engineConfig;
        this.pluginRegistry = pluginRegistry;
    }

    /**
     * @see io.apiman.gateway.engine.IComponentRegistry#getComponents()
     */
    @Override
    public Collection<IComponent> getComponents() {
        return components.values();
    }

    /**
     * @see io.apiman.gateway.engine.IComponentRegistry#getComponent(java.lang.Class)
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
     * @param componentType the component type
     * @return the component
     * @throws ComponentNotFoundException when a policy tries to get a component from
     * the context but the component doesn't exist or is otherwise not available.
     */
    public <T extends IComponent> T createAndRegisterComponent(Class<T> componentType) throws ComponentNotFoundException {
        try {
            synchronized (components) {
                Class<T> componentClass = engineConfig.getComponentClass(componentType, pluginRegistry);
                Map<String, String> componentConfig = engineConfig.getComponentConfig(componentType);
                T component = create(componentClass, componentConfig);
                components.put(componentType, component);

                // Because components are lazily created, we need to initialize them here
                // if necessary.
                DependsOnComponents annotation = componentClass.getAnnotation(DependsOnComponents.class);
                if (annotation != null) {
                    Class<? extends IComponent>[] value = annotation.value();
                    for (Class<? extends IComponent> theC : value) {
                        Method setter = ReflectionUtils.findSetter(componentClass, theC);
                        if (setter != null) {
                            IComponent injectedComponent = getComponent(theC);
                            try {
                                setter.invoke(component, new Object[] { injectedComponent });
                            } catch (IllegalAccessException | IllegalArgumentException
                                    | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                if (component instanceof IRequiresInitialization) {
                    ((IRequiresInitialization) component).initialize();
                }

                return component;
            }
        } catch (Exception e) {
            throw new ComponentNotFoundException(componentType.getName());
        }
    }

    /**
     * Add a component that may have been instantiated elsewhere.
     * @param klazz component class
     * @param component instantiated component of same class
     */
    protected void addComponentMapping(Class<? extends IComponent> klazz, IComponent component) {
        components.put(klazz, component);
    }

    protected <T> T create(Class<T> type, Map<String, String> config) {
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
