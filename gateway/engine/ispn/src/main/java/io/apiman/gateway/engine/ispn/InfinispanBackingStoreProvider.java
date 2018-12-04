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

import io.apiman.gateway.engine.storage.store.IBackingStoreProvider;

import java.util.Map;

/**
 * Store provider for components backed by an Infinispan cache.
 *
 * @author Pete Cornish
 */
public class InfinispanBackingStoreProvider extends AbstractInfinispanComponent
        implements IBackingStoreProvider<InfinispanBackingStore> {

    /**
     * Constructor.
     *
     * @param config                the config
     * @param defaultCacheContainer
     * @param defaultCache
     */
    public InfinispanBackingStoreProvider(Map<String, String> config,
                                          String defaultCacheContainer,
                                          String defaultCache) {
        super(config, defaultCacheContainer, defaultCache);
    }

    @Override
    public InfinispanBackingStore get(String storeName) {
        return new InfinispanBackingStore(getCache());
    }
}
