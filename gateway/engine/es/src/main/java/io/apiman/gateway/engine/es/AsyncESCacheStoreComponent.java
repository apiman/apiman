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
package io.apiman.gateway.engine.es;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.gateway.engine.DependsOnComponents;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.components.ICacheStoreComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalReadStream;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Get;
import io.searchbox.core.Index;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

/**
 * An elasticsearch implementation of a cache store.
 *
 * @author eric.wittmann@redhat.com
 */
@DependsOnComponents({ IBufferFactoryComponent.class })
public class AsyncESCacheStoreComponent extends AbstractESComponent implements ICacheStoreComponent {

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private IBufferFactoryComponent bufferFactory;

    /**
     * Constructor.
     * @param config the configuration
     */
    public AsyncESCacheStoreComponent(Map<String, String> config) {
        super(config);
    }

    /**
     * @param bufferFactory the bufferFactory to set
     */
    public void setBufferFactory(IBufferFactoryComponent bufferFactory) {
        this.bufferFactory = bufferFactory;
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#put(java.lang.String, java.lang.Object, long)
     */
    @Override
    public <T> void put(String cacheKey, T jsonObject, long timeToLive) throws IOException {
        ESCacheEntry entry = new ESCacheEntry();
        entry.setData(null);
        entry.setExpiresOn(System.currentTimeMillis() + (timeToLive * 1000));
        entry.setHead(mapper.writeValueAsString(entry));
        Index index = new Index.Builder(entry).refresh(false).index(getIndexName())
                .type("cacheEntry").id(cacheKey).build(); //$NON-NLS-1$
        getClient().executeAsync(index, new JestResultHandler<JestResult>() {
            @Override
            public void completed(JestResult result) {
                // If it worked, great!
            }
            @Override
            public void failed(Exception e) {
                // TODO report this error
                // If it failed, too bad :(  But not a reason to kill the request
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#putBinary(java.lang.String, java.lang.Object, long)
     */
    @Override
    public <T> ISignalWriteStream putBinary(final String cacheKey, final T jsonObject, final long timeToLive)
            throws IOException {
        final ESCacheEntry entry = new ESCacheEntry();
        entry.setExpiresOn(System.currentTimeMillis() + (timeToLive * 1000));
        entry.setHead(mapper.writeValueAsString(jsonObject));
        final IApimanBuffer data = bufferFactory.createBuffer();
        return new ISignalWriteStream() {
            boolean finished = false;
            boolean aborted = false;
            @Override
            public void abort() {
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
                    Index index = new Index.Builder(entry).refresh(false).index(getIndexName())
                            .type("cacheEntry").id(cacheKey).build(); //$NON-NLS-1$
                    getClient().executeAsync(index, new JestResultHandler<JestResult>() {
                        @Override
                        public void completed(JestResult result) {
                            // If it worked, great!
                        }
                        @Override
                        public void failed(Exception e) {
                            // TODO report this error
                            // If it failed, too bad :(  But not a reason to kill the request
                        }
                    });
                }
                finished = true;
            }
        };
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#get(java.lang.String, java.lang.Class, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void get(String cacheKey, final Class<T> type, final IAsyncResultHandler<T> handler) {
        Get get = new Get.Builder(getIndexName(), cacheKey).type("cacheEntry").build(); //$NON-NLS-1$
        getClient().executeAsync(get, new JestResultHandler<JestResult>() {
            @SuppressWarnings("unchecked")
            @Override
            public void completed(JestResult result) {
                if (result.isSucceeded()) {
                    ESCacheEntry cacheEntry = result.getSourceAsObject(ESCacheEntry.class);
                    try {
                        T rval = (T) mapper.reader(type).readValue(cacheEntry.getHead());
                        handler.handle(AsyncResultImpl.create(rval));
                    } catch (IOException e) {
                        // TODO log this error.
                        handler.handle(AsyncResultImpl.create((T) null));
                    }
                } else {
                    handler.handle(AsyncResultImpl.create((T) null));
                }
            }
            @Override
            public void failed(Exception ex) {
                handler.handle(AsyncResultImpl.create(ex, type));
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#getBinary(java.lang.String, java.lang.Class, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void getBinary(final String cacheKey, final Class<T> type,
            final IAsyncResultHandler<ISignalReadStream<T>> handler) {
        Get get = new Get.Builder(getIndexName(), cacheKey).type("cacheEntry").build(); //$NON-NLS-1$
        getClient().executeAsync(get, new JestResultHandler<JestResult>() {
            @SuppressWarnings("unchecked")
            @Override
            public void completed(JestResult result) {
                // Did the GET succeed?  If not, return null.
                // TODO log the error
                if (!result.isSucceeded()) {
                    handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
                    return;
                }

                // Is the cache entry expired?  If so return null.
                ESCacheEntry cacheEntry = result.getSourceAsObject(ESCacheEntry.class);
                if (System.currentTimeMillis() > cacheEntry.getExpiresOn() ) {
                    // Cache item has expired.  Return null instead of the cached data.
                    handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
                    return;
                }

                try {
                    final T head = (T) mapper.reader(type).readValue(cacheEntry.getHead());
                    String b64Data = cacheEntry.getData();
                    final IApimanBuffer data = bufferFactory.createBuffer(Base64.decodeBase64(b64Data));
                    ISignalReadStream<T> rval = new ISignalReadStream<T>() {
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
                        public void abort() {
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
                } catch (IOException e) {
                    // TODO log this error
                    handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
                }
            }
            @Override
            public void failed(Exception ex) {
                // TODO log this exception
                // If an error occurs, don't fail - just move on.
                handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.es.AbstractESComponent#getIndexName()
     */
    @Override
    protected String getIndexName() {
        return ESConstants.CACHE_INDEX_NAME;
    }

}
