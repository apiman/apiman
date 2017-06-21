/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.plugins.auth3scale.authrep;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.BackendConfiguration;

import java.util.Arrays;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 * @param <CacheValue> Value being cached
 */
public abstract class AbstractCachingAuthenticator<CacheValue> {
    static final int CAPACITY = 1_000_000;

    protected Cache<CacheKey, CacheValue> lruCache = CacheBuilder.newBuilder()
            .initialCapacity(CAPACITY) // TODO sensible capacity?
            .maximumSize(CAPACITY) // LRU capacity
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .build();

    public int hashMatchedRoutes(BackendConfiguration config, ApiRequest req) {
        return Arrays.hashCode(config.getProxy().match(req.getDestination()));
    }

    public abstract AbstractCachingAuthenticator<CacheValue> invalidate(BackendConfiguration config, ApiRequest serviceRequest, Object... elems);

    public abstract AbstractCachingAuthenticator<CacheValue> cache(BackendConfiguration config, ApiRequest serviceRequest, Object... elems);

    public abstract boolean isAuthCached(BackendConfiguration config, ApiRequest serviceRequest, Object... elems);

    public CacheKey getCacheKey(BackendConfiguration config, ApiRequest req, Object... elems) {
        return new CacheKey(req.getApiId(), elems, config.getProxy().match(req.getDestination()));
    }

    protected static final class CacheKey {
        Object[] keyElems;

        public CacheKey(Object... keyElems) {
            this.keyElems = keyElems;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.deepHashCode(keyElems);
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof CacheKey))
                return false;
            CacheKey other = (CacheKey) obj;
            if (!Arrays.deepEquals(keyElems, other.keyElems))
                return false;
            return true;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("CacheKey [%d]", Arrays.deepHashCode(keyElems)); //$NON-NLS-1$
        }

    }
}
