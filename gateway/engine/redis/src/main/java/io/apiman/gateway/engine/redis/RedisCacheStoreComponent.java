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
package io.apiman.gateway.engine.redis;

import io.apiman.gateway.engine.DependsOnComponents;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.redis.common.RedisBackingStoreProvider;
import io.apiman.gateway.engine.redis.common.RedisClientManager;
import io.apiman.gateway.engine.storage.component.AbstractCacheStoreComponent;

import java.util.Map;

/**
 * A Redis implementation of a cache store.
 *
 * @author Pete Cornish
 */
@DependsOnComponents({IBufferFactoryComponent.class})
public class RedisCacheStoreComponent extends AbstractCacheStoreComponent {
    /**
     * Constructor.
     */
    public RedisCacheStoreComponent(Map<String, String> componentConfig) {
        this(RedisClientManager.DEFAULT_MANAGER, componentConfig);
    }

    /**
     * Constructor.
     */
    public RedisCacheStoreComponent(RedisClientManager poolManager, Map<String, String> componentConfig) {
        super(new RedisBackingStoreProvider(poolManager, componentConfig));
    }
}
