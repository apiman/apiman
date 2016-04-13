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
package io.apiman.gateway.engine.ispn;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ISharedStateComponent;

import java.util.Collections;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Shared state component backed by an ISPN cache.  This allows the shared state
 * to be easily clusterable.
 *
 * @author eric.wittmann@redhat.com
 */
public class InfinispanSharedStateComponent extends AbstractInfinispanComponent implements ISharedStateComponent {

    private static final String DEFAULT_CACHE_CONTAINER = "java:jboss/infinispan/apiman"; //$NON-NLS-1$
    private static final String DEFAULT_CACHE = "shared-state"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public InfinispanSharedStateComponent() {
        this(Collections.EMPTY_MAP);
    }

    /**
     * Constructor.
     * @param config the config
     */
    public InfinispanSharedStateComponent(Map<String, String> config) {
        super(config, DEFAULT_CACHE_CONTAINER, DEFAULT_CACHE);
    }

    /**
     * @see io.apiman.gateway.engine.components.ISharedStateComponent#getProperty(java.lang.String, java.lang.String, java.lang.Object, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void getProperty(String namespace, String propertyName, T defaultValue,
            IAsyncResultHandler<T> handler) {
        QName qname = new QName(namespace, propertyName);
        if (getCache().containsKey(qname)) {
            try {
                T rval = (T) getCache().get(qname);
                handler.handle(AsyncResultImpl.create(rval));
            } catch (Exception e) {
                handler.handle(AsyncResultImpl.<T>create(e));
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
        QName qname = new QName(namespace, propertyName);
        try {
            getCache().put(qname, value);
            handler.handle(AsyncResultImpl.create((Void)null));
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.<Void>create(e));
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.ISharedStateComponent#clearProperty(java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void clearProperty(String namespace, String propertyName, IAsyncResultHandler<Void> handler) {
        QName qname = new QName(namespace, propertyName);
        try {
            getCache().remove(qname);
            handler.handle(AsyncResultImpl.create((Void)null));
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.<Void>create(e));
        }
    }
}
