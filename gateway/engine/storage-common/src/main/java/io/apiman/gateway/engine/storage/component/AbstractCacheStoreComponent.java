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

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.components.ICacheStoreComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalReadStream;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.engine.storage.model.CacheEntry;
import io.apiman.gateway.engine.storage.store.IBackingStoreProvider;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import static io.apiman.gateway.engine.storage.util.BackingStoreUtil.JSON_MAPPER;

/**
 * Cache component backed by a store.
 *
 * @author Pete Cornish
 */
public abstract class AbstractCacheStoreComponent extends AbstractStorageComponent implements ICacheStoreComponent {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(AbstractCacheStoreComponent.class);
    private static final String STORE_NAME = "cache"; //$NON-NLS-1$

    private IBufferFactoryComponent bufferFactory;

    /**
     * Constructor.
     */
    public AbstractCacheStoreComponent(IBackingStoreProvider storeProvider) {
        super(storeProvider, STORE_NAME);
    }

    /**
     * @param bufferFactory the bufferFactory to set
     */
    public void setBufferFactory(IBufferFactoryComponent bufferFactory) {
        this.bufferFactory = bufferFactory;
    }

    /**
     * @see ICacheStoreComponent#put(String, Object, long)
     */
    @Override
    public <T> void put(String cacheKey, T jsonObject, long timeToLive) throws IOException {
        final CacheEntry entry = new CacheEntry();
        entry.setData(null);
        entry.setExpiresOn(System.currentTimeMillis() + (timeToLive * 1000));
        entry.setHead(JSON_MAPPER.writeValueAsString(jsonObject));
        try {
            getStore().put(cacheKey, entry, timeToLive);
        } catch (Throwable e) {
            LOGGER.error(e, "Error writing cache entry with key: {}", cacheKey);
        }
    }

    /**
     * @see ICacheStoreComponent#putBinary(String, Object, long)
     */
    @Override
    public <T> ISignalWriteStream putBinary(final String cacheKey, final T jsonObject, final long timeToLive)
            throws IOException {
        final CacheEntry entry = new CacheEntry();
        entry.setExpiresOn(System.currentTimeMillis() + (timeToLive * 1000));
        entry.setHead(JSON_MAPPER.writeValueAsString(jsonObject));

        final IApimanBuffer data = bufferFactory.createBuffer();
        return new ISignalWriteStream() {
            boolean finished = false;
            boolean aborted = false;

            @Override
            public void abort(Throwable t) {
                finished = true;
                aborted = false;
            }

            @Override
            public boolean isFinished() {
                return finished;
            }

            @Override
            public void write(IApimanBuffer chunk) {
                data.append(chunk);
            }

            @Override
            public void end() {
                if (!aborted) {
                    entry.setData(Base64.encodeBase64String(data.getBytes()));
                    try {
                        getStore().put(cacheKey, entry, timeToLive);
                    } catch (Throwable e) {
                        LOGGER.error(e, "Error writing binary cache entry with key: {}", cacheKey);
                    }
                }
                finished = true;
            }
        };
    }

    /**
     * @see ICacheStoreComponent#get(String, Class, IAsyncResultHandler)
     */
    @Override
    public <T> void get(String cacheKey, final Class<T> type, final IAsyncResultHandler<T> handler) {
        try {
            final CacheEntry cacheEntry = getStore().get(cacheKey, CacheEntry.class);
            if (null != cacheEntry) {
                try {
                    @SuppressWarnings("unchecked") final T head = JSON_MAPPER.readValue(cacheEntry.getHead(), type);
                    handler.handle(AsyncResultImpl.create(head));
                } catch (Exception e) {
                    LOGGER.error(e, "Error reading cache entry with key: {}", cacheKey);
                    handler.handle(AsyncResultImpl.create((T) null));
                }
            } else {
                handler.handle(AsyncResultImpl.create((T) null));
            }
        } catch (Throwable e) {
            handler.handle(AsyncResultImpl.create(e, type));
        }
    }

    /**
     * @see ICacheStoreComponent#getBinary(String, Class, IAsyncResultHandler)
     */
    @Override
    public <T> void getBinary(final String cacheKey, final Class<T> type,
                              final IAsyncResultHandler<ISignalReadStream<T>> handler) {
        try {
            final CacheEntry cacheEntry = getStore().get(cacheKey, CacheEntry.class);

            // Did the fetch succeed? If not, return null.
            if (null == cacheEntry) {
                handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
                return;
            }

            // Is the cache entry expired?  If so return null.
            if (System.currentTimeMillis() > cacheEntry.getExpiresOn()) {
                // Cache item has expired.  Return null instead of the cached data.
                handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
                return;
            }

            try {
                @SuppressWarnings("unchecked") final T head = (T) JSON_MAPPER.readValue(cacheEntry.getHead(), type);
                final String b64Data = cacheEntry.getData();
                final IApimanBuffer data = bufferFactory.createBuffer(Base64.decodeBase64(b64Data));
                final ISignalReadStream<T> rval = new ISignalReadStream<T>() {
                    IAsyncHandler<IApimanBuffer> bodyHandler;
                    IAsyncHandler<Void> endHandler;
                    boolean finished = false;
                    boolean aborted = false;

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
                        aborted = true;
                    }

                    @Override
                    public void transmit() {
                        if (!aborted) {
                            bodyHandler.handle(data);
                            endHandler.handle(null);
                        }
                        finished = true;
                    }
                };
                handler.handle(AsyncResultImpl.create(rval));
            } catch (Throwable e) {
                LOGGER.error(e, "Error reading binary cache entry with key: {}", cacheKey);
                handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
            }
        } catch (Throwable e) {
            handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
        }
    }
}
