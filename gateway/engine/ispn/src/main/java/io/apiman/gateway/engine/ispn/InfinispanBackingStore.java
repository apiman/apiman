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
package io.apiman.gateway.engine.ispn;

import io.apiman.gateway.engine.storage.store.IBackingStore;
import org.infinispan.Cache;

/**
 * Store provider for components backed by an Infinispan cache.
 */
public class InfinispanBackingStore implements IBackingStore {
    private final Cache<Object, Object> cache;

    public InfinispanBackingStore(Cache<Object, Object> cache) {
        this.cache = cache;
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, Class<T> type) {
        return (T) cache.get(key);
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }
}
