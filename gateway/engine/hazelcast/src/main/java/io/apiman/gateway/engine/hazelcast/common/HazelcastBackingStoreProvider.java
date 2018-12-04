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
package io.apiman.gateway.engine.hazelcast.common;

import io.apiman.gateway.engine.storage.store.IBackingStoreProvider;
import io.apiman.gateway.engine.storage.store.MapBackingStore;

/**
 * Store provider for components backed by a Hazelcast Map.
 *
 * @author Pete Cornish
 */
public class HazelcastBackingStoreProvider implements IBackingStoreProvider<MapBackingStore> {
    public static final String CONFIG_EAGER_INIT = "eager-init";

    private final HazelcastInstanceManager instanceManager;

    /**
     * Constructor.
     */
    public HazelcastBackingStoreProvider() {
        this(HazelcastInstanceManager.DEFAULT_MANAGER);
    }

    /**
     * Constructor.
     */
    public HazelcastBackingStoreProvider(HazelcastInstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    /**
     * Returns an instance of the store for the store name.
     *
     * @return the store
     */
    @Override
    public MapBackingStore get(String storeName) {
        return new MapBackingStore(instanceManager.getHazelcastMap(storeName));
    }
}
