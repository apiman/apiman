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
package io.apiman.gateway.engine.impl;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An in-memory implementation of the {@link ICacheStoreComponent} interface.
 * This implementation simply stores cached data in memory.
 *
 * @author eric.wittmann@redhat.com
 */
@DependsOnComponents( { IBufferFactoryComponent.class } )
public class InMemoryCacheStoreComponent implements ICacheStoreComponent {

    private Object mapMutex = new Object();
    private Object cacheSizeMutex = new Object();

    private Map<String, Long> expireOnMap = new HashMap<>();
    private Map<String, Object> objectCache = new LinkedHashMap<>();
    private Map<String, IApimanBuffer> dataCache = new HashMap<>();
    private long cacheSize = 0;
    private long maxCacheSize = 10 * 1024 * 1024L; // 10 MB

    private IBufferFactoryComponent bufferFactory;

    /**
     * Constructor.
     */
    public InMemoryCacheStoreComponent() {
    }

    /**
     * Constructor.
     * @param config
     */
    public InMemoryCacheStoreComponent(Map<String, String> config) {
        String mcs = config.get("maxCacheSize"); //$NON-NLS-1$
        if (mcs != null) {
            maxCacheSize = new Long(mcs);
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#put(java.lang.String, java.lang.Object, long)
     */
    @Override
    public <T> void put(String cacheKey, T jsonObject, long timeToLive) throws IOException {
        synchronized (mapMutex) {
            expireOnMap.put(cacheKey, System.currentTimeMillis() + (timeToLive * 1000));
            objectCache.put(cacheKey, jsonObject);
            dataCache.put(cacheKey, null);
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#putBinary(java.lang.String, java.lang.Object, long)
     */
    @Override
    public <T> ISignalWriteStream putBinary(final String cacheKey, final T jsonObject, final long timeToLive)
            throws IOException {
        final IApimanBuffer buffer = bufferFactory.createBuffer();
        synchronized (mapMutex) {
            expireOnMap.put(cacheKey, System.currentTimeMillis() + (timeToLive * 1000));
            objectCache.put(cacheKey, jsonObject);
            dataCache.put(cacheKey, buffer);
        }
        return new ISignalWriteStream() {
            private boolean finished = false;
            @Override
            public void abort(Throwable t) {
                finished = true;
            }
            @Override
            public boolean isFinished() {
                return finished;
            }
            @Override
            public void write(IApimanBuffer chunk) {
                buffer.append(chunk);
            }
            @Override
            public void end() {
                finished = true;
                synchronized (cacheSizeMutex) {
                    cacheSize += buffer.length();
                    if (cacheSize > maxCacheSize) {
                        synchronized (mapMutex) {
                            while (cacheSize > maxCacheSize) {
                                String cacheKey = objectCache.keySet().iterator().next();
                                objectCache.remove(cacheKey);
                                expireOnMap.remove(cacheKey);
                                IApimanBuffer removedBuffer = dataCache.remove(cacheKey);
                                if (removedBuffer != null) {
                                    cacheSize -= removedBuffer.length();
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#get(java.lang.String, java.lang.Class, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> void get(String cacheKey, Class<T> type, IAsyncResultHandler<T> handler) {
        boolean expired = false;
        Object rval;
        synchronized (mapMutex) {
            rval = objectCache.get(cacheKey);
            if (rval != null) {
                Long expiresOn = expireOnMap.get(cacheKey);
                if (System.currentTimeMillis() > expiresOn ) {
                    expired = true;
                }
            }
        }
        if (expired) {
            synchronized (cacheSizeMutex) {
                synchronized (mapMutex) {
                    objectCache.remove(cacheKey);
                    expireOnMap.remove(cacheKey);
                    IApimanBuffer buffer = dataCache.remove(cacheKey);
                    if (buffer != null) {
                        cacheSize -= buffer.length();
                    }
                }
            }
            rval = null;
        }
        handler.handle(AsyncResultImpl.create((T) rval));
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#getBinary(java.lang.String, java.lang.Class, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void getBinary(String cacheKey, Class<T> type,
            IAsyncResultHandler<ISignalReadStream<T>> handler) {
        boolean expired = false;
        ISignalReadStream<T> rval;
        Object object;

        IApimanBuffer buffer;
        synchronized (mapMutex) {
            object = objectCache.get(cacheKey);
            if (object != null) {
                Long expiresOn = expireOnMap.get(cacheKey);
                if (System.currentTimeMillis() > expiresOn ) {
                    expired = true;
                }
            }
            buffer = dataCache.get(cacheKey);
            if (buffer == null) {
                object = null;
            }
        }

        if (object == null) {
            rval = null;
        } else if (expired) {
            synchronized (cacheSizeMutex) {
                synchronized (mapMutex) {
                    objectCache.remove(cacheKey);
                    expireOnMap.remove(cacheKey);
                    dataCache.remove(cacheKey);
                    if (buffer != null) {
                        cacheSize -= buffer.length();
                    }
                }
            }
            rval = null;
        } else {
            @SuppressWarnings("unchecked")
            final T head = (T) object;
            final IApimanBuffer data = buffer;
            rval = new ISignalReadStream<T>() {
                IAsyncHandler<IApimanBuffer> bodyHandler;
                IAsyncHandler<Void> endHandler;
                boolean finished = false;

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
                public void abort(Throwable t) {
                    finished = true;
                }
                @Override
                public void transmit() {
                    bodyHandler.handle(data);
                    endHandler.handle(null);
                }
            };
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
