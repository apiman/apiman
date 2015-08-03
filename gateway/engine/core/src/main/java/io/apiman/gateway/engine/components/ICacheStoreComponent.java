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
package io.apiman.gateway.engine.components;

import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.io.ISignalReadStream;
import io.apiman.gateway.engine.io.ISignalWriteStream;

import java.io.IOException;

/**
 * A component that provides a way to cache content. The cache store supports
 * caching of serializeable objects in addition to the binary content associated
 * with it (if any).
 *
 * @author eric.wittmann@redhat.com
 */
public interface ICacheStoreComponent extends IComponent {

    /**
     * Put an entry into the cache.  The entry will not include any binary
     * content - just the JSON object.
     * @param cacheKey
     * @param jsonObject
     * @param timeToLive
     * @throws IOException
     */
    public <T> void put(String cacheKey, T jsonObject, long timeToLive) throws IOException;

    /**
     * Open a cache store entry so that data can be streamed into it.
     * @param cacheKey
     * @param jsonObject
     * @param timeToLive
     * @throws IOException
     */
    public <T> ISignalWriteStream putBinary(String cacheKey, T jsonObject, long timeToLive) throws IOException;

    /**
     * Gets a cache entry.
     * @param cacheKey
     * @param type
     * @throws IOException
     */
    public <T> void get(String cacheKey, Class<T> type, IAsyncResultHandler<T> handler);

    /**
     * Gets a cache entry with its binary data.
     * @param cacheKey
     * @throws IOException
     */
    public <T> void getBinary(String cacheKey, Class<T> type, IAsyncResultHandler<ISignalReadStream<T>> handler);

}
