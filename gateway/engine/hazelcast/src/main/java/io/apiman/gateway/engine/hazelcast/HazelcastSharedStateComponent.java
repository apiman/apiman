/*
 * Copyright 2017 Pete Cornish
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
package io.apiman.gateway.engine.hazelcast;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ISharedStateComponent;

import java.util.Map;

/**
 * Shared state component backed by a Hazelcast Map. This allows the shared state
 * to be easily clusterable.
 *
 * @author Pete Cornish
 */
public class HazelcastSharedStateComponent extends AbstractHazelcastComponent implements ISharedStateComponent {
    private static final String STORE_NAME = "shared-state"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public HazelcastSharedStateComponent(Map<String, String> componentConfig) {
        super(componentConfig, STORE_NAME);
    }

    /**
     * @see io.apiman.gateway.engine.components.ISharedStateComponent#getProperty(java.lang.String, java.lang.String, java.lang.Object, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void getProperty(String namespace, String propertyName, T defaultValue, IAsyncResultHandler<T> handler) {
        final String namespacedKey = buildNamespacedKey(namespace, propertyName);
        if (getMap().containsKey(namespacedKey)) {
            try {
                T rval = (T) getMap().get(namespacedKey);
                handler.handle(AsyncResultImpl.create(rval));
            } catch (Exception e) {
                handler.handle(AsyncResultImpl.create(e));
            }

        } else {
            handler.handle(AsyncResultImpl.create(defaultValue));
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.ISharedStateComponent#setProperty(java.lang.String, java.lang.String, java.lang.Object, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void setProperty(String namespace, String propertyName, T value, IAsyncResultHandler<Void> handler) {
        final String namespacedKey = buildNamespacedKey(namespace, propertyName);
        try {
            getMap().put(namespacedKey, value);
            handler.handle(AsyncResultImpl.create((Void) null));
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(e));
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.ISharedStateComponent#clearProperty(java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void clearProperty(String namespace, String propertyName, IAsyncResultHandler<Void> handler) {
        final String namespacedKey = buildNamespacedKey(namespace, propertyName);
        try {
            getMap().remove(namespacedKey);
            handler.handle(AsyncResultImpl.create((Void) null));
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(e));
        }
    }
}
