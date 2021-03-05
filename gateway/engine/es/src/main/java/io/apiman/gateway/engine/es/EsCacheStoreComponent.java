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

import io.apiman.common.es.util.AbstractEsComponent;
import io.apiman.common.es.util.EsConstants;
import io.apiman.common.es.util.builder.index.EsIndexProperties;
import io.apiman.gateway.engine.DependsOnComponents;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.components.ICacheStoreComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalReadStream;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.engine.storage.model.CacheEntry;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.Map;

import static io.apiman.gateway.engine.storage.util.BackingStoreUtil.JSON_MAPPER;

/**
 * An elasticsearch implementation of a cache store.
 *
 * @author eric.wittmann@redhat.com
 */
@DependsOnComponents({ IBufferFactoryComponent.class })
public class EsCacheStoreComponent extends AbstractEsComponent implements ICacheStoreComponent {
    private IBufferFactoryComponent bufferFactory;

    /**
     * Constructor.
     * @param config the configuration
     */
    public EsCacheStoreComponent(Map<String, String> config) {
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
        CacheEntry entry = new CacheEntry();
        entry.setData(null);
        entry.setExpiresOn(System.currentTimeMillis() + (timeToLive * 1000));
        entry.setHead(JSON_MAPPER.writeValueAsString(entry));

        IndexRequest indexRequest = new IndexRequest(getFullIndexName()).source(JSON_MAPPER.writeValueAsBytes(entry), XContentType.JSON).id(cacheKey);
        try {
            getClient().index(indexRequest, RequestOptions.DEFAULT);
        } catch (Throwable e) {
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#putBinary(java.lang.String, java.lang.Object, long)
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
                        IndexRequest indexRequest = new IndexRequest(getFullIndexName()).source(JSON_MAPPER.writeValueAsBytes(entry), XContentType.JSON).id(cacheKey);
                        getClient().index(indexRequest, RequestOptions.DEFAULT);
                    } catch (Throwable e) {
                    }
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
        try {
            GetResponse response = getClient().get(new GetRequest(getFullIndexName()).id(cacheKey), RequestOptions.DEFAULT);
            if (response.isExists()) {
                String sourceAsString = response.getSourceAsString();
                CacheEntry cacheEntry = JSON_MAPPER.readValue(sourceAsString, CacheEntry.class);
                try {
                    T rval = (T) JSON_MAPPER.reader(type).readValue(cacheEntry.getHead());
                    handler.handle(AsyncResultImpl.create(rval));
                } catch (IOException e) {
                    // TODO log this error.
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
     * @see io.apiman.gateway.engine.components.ICacheStoreComponent#getBinary(java.lang.String, java.lang.Class, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void getBinary(final String cacheKey, final Class<T> type,
            final IAsyncResultHandler<ISignalReadStream<T>> handler) {
        try {
            GetResponse response = getClient().get(new GetRequest(getFullIndexName()).id(cacheKey), RequestOptions.DEFAULT);

            // Did the GET succeed?  If not, return null.
            // TODO log the error
            if (!response.isExists()) {
                handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
                return;
            }

            // Is the cache entry expired?  If so return null.
            String sourceAsString = response.getSourceAsString();
            CacheEntry cacheEntry = JSON_MAPPER.readValue(sourceAsString, CacheEntry.class);
            if (System.currentTimeMillis() > cacheEntry.getExpiresOn() ) {
                // Cache item has expired.  Return null instead of the cached data.
                handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
                return;
            }

            try {
                final T head = (T) JSON_MAPPER.reader(type).readValue(cacheEntry.getHead());
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
                // TODO log this error
                handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
            }
        } catch (Throwable e) {
            handler.handle(AsyncResultImpl.create((ISignalReadStream<T>) null));
        }
    }

    /**
     * @see AbstractEsComponent#getDefaultIndexPrefix()
     */
    @Override
    protected String getDefaultIndexPrefix() {
        return EsConstants.CACHE_INDEX_NAME;
    }

    @Override
    public Map<String, EsIndexProperties> getEsIndices() {
        // This component manages its own indexes
        return Collections.emptyMap();
    }

    /**
     * get index full name for cache entry
     * @return full index name
     */
    private String getFullIndexName() {
        return (getIndexPrefix() + EsConstants.INDEX_CACHE_CACHE_ENTRY).toLowerCase();
    }

}
