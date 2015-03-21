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

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IDataStoreComponent;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * An in-memory only implementation of the data store component. This
 * implementation is generally used for testing and embedded situations. It does
 * not work in a cluster and is not persistent across server restarts.
 *
 * @author rubenrm1@gmail.com
 */
public class InMemoryDataStoreComponent implements IDataStoreComponent {

    private Map<QName, Object> sharedState = new HashMap<>();

    /**
     * Constructor.
     */
    public InMemoryDataStoreComponent() {
    }

    /**
     * @see io.apiman.gateway.engine.components.IDataStoreComponent#hasProperty(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> boolean hasProperty(String namespace, String propertyName) {
        T value = null;
        synchronized (sharedState) {
            QName key = new QName(namespace, propertyName);
            value = (T) sharedState.get(key);
        }
        return value != null;
    }
    
    /**
     * @see io.apiman.gateway.engine.components.IDataStoreComponent#getProperty(java.lang.String, java.lang.String, java.lang.Object, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void getProperty(String namespace, String propertyName, T defaultValue,
            IAsyncResultHandler<T> handler) {
        T value = null;
        synchronized (sharedState) {
            QName key = new QName(namespace, propertyName);
            value = (T) sharedState.get(key);
        }
        if (value == null) {
            value = defaultValue;
        }
        handler.handle(AsyncResultImpl.create(value));
    }

    /**
     * @see io.apiman.gateway.engine.components.IDataStoreComponent#setProperty(java.lang.String, java.lang.String, java.lang.Object, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void setProperty(String namespace, String propertyName, T value, IAsyncResultHandler<T> handler) {
        setProperty(namespace, propertyName, value, null, handler);
    }

    /**
     * This implementation will ignore the expiration time
     * 
     * @see io.apiman.gateway.engine.components.IDataStoreComponent#setProperty(java.lang.String, java.lang.String, java.lang.Object, java.lang.Long, IAsyncResultHandler)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void setProperty(String namespace, String propertyName, T value, Long expiration,
            IAsyncResultHandler<T> handler) {
        QName key = new QName(namespace, propertyName);
        T oldValue = null;
        synchronized (sharedState) {
            oldValue = (T) sharedState.get(key);
            sharedState.put(key, value);
        }
        handler.handle(AsyncResultImpl.create(oldValue));
    }

    /**
     * @see io.apiman.gateway.engine.components.IDataStoreComponent#clearProperty(java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void clearProperty(String namespace, String propertyName, IAsyncResultHandler<T> handler) {
        QName key = new QName(namespace, propertyName);
        T oldValue = null;
        synchronized (sharedState) {
            oldValue = (T) sharedState.remove(key);
        }
        handler.handle(AsyncResultImpl.create(oldValue));
    }

}