/*
 * Copyright 2015 JBoss Inc
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

import io.apiman.gateway.engine.DependsOnComponents;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.components.ICacheStoreComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalReadStream;
import io.apiman.gateway.engine.io.ISignalWriteStream;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * An infinispan implementation of a cache store.
 *
 * @author eric.wittmann@redhat.com
 */
@DependsOnComponents({ IBufferFactoryComponent.class })
public class InfinispanCacheStoreComponent extends AbstractInfinispanComponent implements ICacheStoreComponent {

    private static final String DEFAULT_CACHE_CONTAINER = "java:jboss/infinispan/apiman"; //$NON-NLS-1$
    private static final String DEFAULT_CACHE = "cachestore"; //$NON-NLS-1$

    private Object mutex = new Object();
    private IBufferFactoryComponent bufferFactory;

    /**
     * Constructor.
     */
    public InfinispanCacheStoreComponent() {
        this(Collections.EMPTY_MAP);
    }

    /**
     * Constructor.
     * @param config the config
     */
    public InfinispanCacheStoreComponent(Map<String, String> config) {
        super(config, DEFAULT_CACHE_CONTAINER, DEFAULT_CACHE);
    }


    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#put(java.lang.String, java.lang.Object, long)
     */
    @Override
    public <T> void put(String cacheKey, T jsonObject, long timeToLive) throws IOException {
        synchronized (mutex) {
            InfinispanCacheEntry entry = (InfinispanCacheEntry) getCache().get(cacheKey);
            if (entry == null) {
                entry = new InfinispanCacheEntry();
            }
            entry.setHead(jsonObject);
            entry.setExpiresOn(System.currentTimeMillis() + (timeToLive * 1000));
            getCache().put(cacheKey, entry);
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#putBinary(java.lang.String, java.lang.Object, long)
     */
    @Override
    public <T> ISignalWriteStream putBinary(final String cacheKey, final T jsonObject, final long timeToLive)
            throws IOException {
        final IApimanBuffer dataBuffer = bufferFactory.createBuffer(0);
        return new ISignalWriteStream() {
            private boolean finished = false;
            private boolean aborted = false;
            @Override
            public void abort() {
                aborted = true;
                finished = false;
            }
            @Override
            public boolean isFinished() {
                return finished;
            }
            @Override
            public void write(IApimanBuffer chunk) {
                dataBuffer.append(chunk);
            }
            @Override
            public void end() {
                if (!aborted) {
                    synchronized (mutex) {
                        InfinispanCacheEntry entry = (InfinispanCacheEntry) getCache().get(cacheKey);
                        if (entry == null) {
                            entry = new InfinispanCacheEntry();
                        }
                        entry.setHead(jsonObject);
                        entry.setExpiresOn(System.currentTimeMillis() + (timeToLive * 1000));
                        entry.setData(dataBuffer.getBytes());
                        getCache().put(cacheKey, entry);
                    }
                }
            }
        };
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#get(java.lang.String, java.lang.Class, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void get(String cacheKey, Class<T> type, IAsyncResultHandler<T> handler) {
        T rval;
        synchronized (mutex) {
            InfinispanCacheEntry entry = (InfinispanCacheEntry) getCache().get(cacheKey);
            if (entry != null) {
                if (System.currentTimeMillis() >= entry.getExpiresOn()) {
                    getCache().remove(cacheKey);
                    rval = null;
                } else {
                    rval = (T) entry.getHead();
                }
            } else {
                rval = null;
            }
        }
        handler.handle(AsyncResultImpl.create(rval));
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#getBinary(java.lang.String, java.lang.Class, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void getBinary(String cacheKey, Class<T> type,
            IAsyncResultHandler<ISignalReadStream<T>> handler) {
        ISignalReadStream<T> rval;
        synchronized (mutex) {
            InfinispanCacheEntry entry = (InfinispanCacheEntry) getCache().get(cacheKey);
            if (entry != null) {
                if (entry.getExpiresOn() <= System.currentTimeMillis()) {
                    getCache().remove(cacheKey);
                    rval = null;
                } else {
                    final T head = (T) entry.getHead();
                    final IApimanBuffer dataBuffer = bufferFactory.createBuffer(entry.getData());
                    rval = new ISignalReadStream<T>() {
                        boolean finished = false;
                        IAsyncHandler<IApimanBuffer> bodyHandler;
                        IAsyncHandler<Void> endHandler;

                        @Override
                        public void bodyHandler(IAsyncHandler<IApimanBuffer> bodyHandler) {
                            this.bodyHandler = bodyHandler;
                        }
                        @Override
                        public void endHandler(IAsyncHandler<Void> endHandler) {
                            this.endHandler = endHandler;
                        }
                        @Override
                        public T getHead() {
                            return head;
                        }
                        @Override
                        public boolean isFinished() {
                            return finished;
                        }
                        @Override
                        public void abort() {
                            finished = true;
                        }
                        @Override
                        public void transmit() {
                            bodyHandler.handle(dataBuffer);
                            endHandler.handle(null);
                        }
                    };
                }
            } else {
                rval = null;
            }
        }
        handler.handle(AsyncResultImpl.create(rval));
    }

    /**
     * @param bufferFactory the bufferFactory to set
     */
    public void setBufferFactory(IBufferFactoryComponent bufferFactory) {
        this.bufferFactory = bufferFactory;
    }

}
