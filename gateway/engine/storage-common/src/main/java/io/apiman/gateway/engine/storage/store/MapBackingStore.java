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
package io.apiman.gateway.engine.storage.store;

import java.util.Map;

/**
 * A backing store that uses a {@link Map}.
 *
 * Note: this implementation never evicts entries from its map, meaning that it will grow infinitely.
 */
public class MapBackingStore implements IBackingStore {
    private final Map<String, Object> map;

    public MapBackingStore(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public void put(String key, Object value, long ttl) {
        map.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, Class<T> type) {
        return (T) map.get(key);
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }
}
