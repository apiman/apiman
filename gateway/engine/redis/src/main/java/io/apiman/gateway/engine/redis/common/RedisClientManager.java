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

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Manages Redis clients to reduce the amount of initialisation work on each I/O operation.
 *
 * @author Pete Cornish
 */
public final class RedisClientManager {
    public static final RedisClientManager DEFAULT_MANAGER = new RedisClientManager();

    private final Object mutex = new Object();
    private Config overrideConfig;
    private File configFile;
    private RedissonClient client;

    /**
     * Generally, you don't want to initialise your own manager - instead, use the {@link #DEFAULT_MANAGER}.
     */
    public RedisClientManager() {
    }

    /**
     * Override the default Redis configuration discovery strategy.
     */
    public void setConfigFile(Path configFilePath) {
        this.configFile = configFilePath.toFile();
    }

    /**
     * Override the default Redis configuration discovery strategy.
     */
    public void setOverrideConfig(Config overrideConfig) {
        this.overrideConfig = overrideConfig;
    }

    /**
     * @param storeName a name to associate with the instance
     * @return a new or existing Redis client for the given store name
     */
    public RedisBackingStore getRedis(String storeName) {
        if (isNull(client)) {
            synchronized (mutex) {
                if (isNull(client)) { // double-guard
                    final Config config;
                    if (nonNull(overrideConfig)) {
                        config = overrideConfig;
                    } else if (nonNull(configFile)) {
                        config = loadConfigFromFile();
                    } else {
                        throw new IllegalStateException("No Redisson configuration provided");
                    }
                    config.setCodec(new StringCodec());
                    client = Redisson.create(config);
                }
            }
        }
        return new RedisBackingStore(client, storeName);
    }

    private Config loadConfigFromFile() {
        if (configFile.exists()) {
            try {
                return Config.fromJSON(configFile);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Error reading Redisson configuration file: %s", configFile), e);
            }
        } else {
            throw new RuntimeException(String.format("Redisson configuration file: '%s' does not exist", configFile));
        }
    }

    /**
     * Shut down the Redis client.
     */
    public void reset() {
        if (nonNull(client)) {
            synchronized (mutex) {
                if (nonNull(client)) { // double-guard
                    client.shutdown();
                    client = null;
                }
            }
        }
    }
}
