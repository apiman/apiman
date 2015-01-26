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

import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.beans.exceptions.ComponentNotFoundException;
import io.apiman.gateway.engine.components.IDataStoreComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.ISharedStateComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * A default component registry, useful primarily for testing and 
 * bootstrapping.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultComponentRegistry implements IComponentRegistry {
    
    private Map<Class<? extends IComponent>, IComponent> components = new HashMap<Class<? extends IComponent>, IComponent>();
    
    /**
     * Constructor.
     */
    public DefaultComponentRegistry() {
        components.put(ISharedStateComponent.class, new InMemorySharedStateComponent());
        components.put(IDataStoreComponent.class, new InMemoryDataStoreComponent());
        components.put(IRateLimiterComponent.class, new InMemoryRateLimiterComponent());
        components.put(IPolicyFailureFactoryComponent.class, new DefaultPolicyFailureFactoryComponent());
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
            throw new ComponentNotFoundException(componentType.getName());
        }
    }

}
