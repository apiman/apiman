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
import io.apiman.gateway.engine.components.ICacheStoreComponent;
import io.apiman.gateway.engine.components.IExecuteBlockingComponent;
import io.apiman.gateway.engine.components.IJdbcComponent;
import io.apiman.gateway.engine.components.ILdapComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.ISharedStateComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A default component registry, useful primarily for testing and
 * bootstrapping.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultComponentRegistry implements IComponentRegistry {

    private Map<Class<? extends IComponent>, IComponent> components = new HashMap<>();

    /**
     * Constructor.
     */
    public DefaultComponentRegistry() {
        registerSharedStateComponent();
        registerRateLimiterComponent();
        registerPolicyFailureFactoryComponent();
        registerHttpClientComponent();
        registerBufferFactoryComponent();
        registerCacheStoreComponent();
        registerJdbcComponent();
        registerLdapComponent();
        registerExecuteBlockingComponent();
    }

    /**
     * @see io.apiman.gateway.engine.IComponentRegistry#getComponents()
     */
    @Override
    public Collection<IComponent> getComponents() {
        return components.values();
    }

    protected void registerHttpClientComponent() {
        // there's no platform-independent impl of this
    }

    protected void registerBufferFactoryComponent() {
        // there's no platform-independent impl of this
    }

    protected void registerPolicyFailureFactoryComponent() {
        addComponent(IPolicyFailureFactoryComponent.class, new DefaultPolicyFailureFactoryComponent());
    }

    protected void registerRateLimiterComponent() {
        addComponent(IRateLimiterComponent.class, new SingleNodeRateLimiterComponent());
    }

    protected void registerSharedStateComponent() {
        addComponent(ISharedStateComponent.class, new InMemorySharedStateComponent());
    }

    protected void registerCacheStoreComponent() {
        addComponent(ICacheStoreComponent.class, new InMemoryCacheStoreComponent());
    }

    protected void registerJdbcComponent() {
        addComponent(IJdbcComponent.class, new DefaultJdbcComponent());
    }

    protected void registerLdapComponent() {
        addComponent(ILdapComponent.class, new DefaultLdapComponent());
    }

    protected void registerExecuteBlockingComponent() {
        addComponent(IExecuteBlockingComponent.class, new DefaultExecuteBlockingComponent());
    }

    /**
     * Adds a component to the registry.
     * @param componentType
     * @param component
     */
    protected <T extends IComponent> void addComponent(Class<T> componentType, T component) {
        components.put(componentType, component);
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
