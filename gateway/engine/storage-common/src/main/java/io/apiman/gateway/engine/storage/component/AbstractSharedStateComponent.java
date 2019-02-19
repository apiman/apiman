/*
 * Copyright 2018 Pete Cornish
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
package io.apiman.gateway.engine.storage.component;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.storage.store.IBackingStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;

/**
 * Shared state component backed by a store.
 *
 * @author Pete Cornish
 */
public abstract class AbstractSharedStateComponent extends AbstractStorageComponent implements ISharedStateComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSharedStateComponent.class);
    private static final String STORE_NAME = "shared-state"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public AbstractSharedStateComponent(IBackingStoreProvider storeProvider) {
        super(storeProvider, STORE_NAME);
    }

    /**
     * @see ISharedStateComponent#getProperty(String, String, Object, IAsyncResultHandler)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void getProperty(String namespace, String propertyName, T defaultValue, IAsyncResultHandler<T> handler) {
        if (isNull(defaultValue)) {
            handler.handle(AsyncResultImpl.create(new Exception("Null defaultValue is not allowed.")));
            return;
        }
        final String namespacedKey = buildNamespacedKey(namespace, propertyName);
        if (getStore().containsKey(namespacedKey)) {
            try {
                final Class<T> valueClass = (Class<T>) defaultValue.getClass();
                final T rval = getStore().get(namespacedKey, valueClass);
                handler.handle(AsyncResultImpl.create(rval));

            } catch (Exception e) {
                LOGGER.error("Error reading from shared state with namespace: {} and key: {}",
                        namespace, propertyName, e);

                handler.handle(AsyncResultImpl.create(e));
            }

        } else {
            handler.handle(AsyncResultImpl.create(defaultValue));
        }
    }

    /**
     * @see ISharedStateComponent#setProperty(String, String, Object, IAsyncResultHandler)
     */
    @Override
    public <T> void setProperty(String namespace, String propertyName, T value, IAsyncResultHandler<Void> handler) {
        final String namespacedKey = buildNamespacedKey(namespace, propertyName);
        try {
            getStore().put(namespacedKey, value);
            handler.handle(AsyncResultImpl.create((Void) null));

        } catch (Exception e) {
            LOGGER.error("Error writing to shared state with namespace: {} and key: {}",
                    namespace, propertyName, e);

            handler.handle(AsyncResultImpl.create(e));
        }
    }

    /**
     * @see ISharedStateComponent#clearProperty(String, String, IAsyncResultHandler)
     */
    @Override
    public <T> void clearProperty(String namespace, String propertyName, IAsyncResultHandler<Void> handler) {
        final String namespacedKey = buildNamespacedKey(namespace, propertyName);
        try {
            getStore().remove(namespacedKey);
            handler.handle(AsyncResultImpl.create((Void) null));

        } catch (Exception e) {
            LOGGER.error("Error removing entry from shared state with namespace: {} and key: {}",
                    namespace, propertyName, e);

            handler.handle(AsyncResultImpl.create(e));
        }
    }
}
