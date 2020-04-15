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

/**
 * Common operations for backing stores.
 */
public interface IBackingStore {
    /**
     * Insert an item into the store.
     *
     * @param key   the item's key
     * @param value the item's value
     */
    default void put(String key, Object value) {
        put(key, value, Integer.MAX_VALUE);
    }

    /**
     * Insert an item into the store, with a given TTL.
     *
     * @param key   the item's key
     * @param value the item's value
     * @param ttl   the TTL in seconds
     */
    void put(String key, Object value, long ttl);

    /**
     * Fetch an item from the store.
     *
     * @param key  the item's key
     * @param type the item's class
     * @param <T>  the item's type
     * @return the item, or {@code null}
     */
    <T> T get(String key, Class<T> type);

    /**
     * Remove an item from the store.
     *
     * @param key the item's key
     */
    void remove(String key);

    /**
     * Determine whether the store contains an item.
     *
     * @param key the item's key
     * @return {@code true} if the item is in the store, otherwise {@code false}
     */
    boolean containsKey(String key);
}
