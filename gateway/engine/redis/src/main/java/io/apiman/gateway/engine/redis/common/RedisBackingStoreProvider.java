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
package io.apiman.gateway.engine.redis.common;

import io.apiman.gateway.engine.storage.store.IBackingStoreProvider;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Map;

/**
 * Store provider for components backed by Redis.
 *
 * @author Pete Cornish
 */
public class RedisBackingStoreProvider implements IBackingStoreProvider<RedisBackingStore> {
    public static final String CONFIG_FILE = "config.file";

    private final RedisClientManager clientManager;

    /**
     * Constructor.
     */
    public RedisBackingStoreProvider(Map<String, String> componentConfig) {
        this(RedisClientManager.DEFAULT_MANAGER, componentConfig);
    }

    /**
     * Constructor.
     */
    public RedisBackingStoreProvider(RedisClientManager clientManager, Map<String, String> componentConfig) {
        this.clientManager = clientManager;
        final String configFilePath = componentConfig.get(CONFIG_FILE);
        if (StringUtils.isNotBlank(configFilePath)) {
            clientManager.setConfigFile(Paths.get(configFilePath));
        }
    }

    /**
     * Returns a namespaced instance of the store for the store name.
     *
     * @return the Map
     */
    @Override
    public RedisBackingStore get(String storeName) {
        return clientManager.getRedis(storeName);
    }
}
