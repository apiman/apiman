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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.apiman.gateway.engine.storage.store.IBackingStore;
import io.apiman.gateway.engine.storage.util.BackingStoreUtil;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.function.Consumer;
import java.util.function.Function;

import static io.apiman.gateway.engine.storage.util.BackingStoreUtil.JSON_MAPPER;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Provides a Redis backing store, whose keys are namespaced with a prefix.
 * This is implemented using a {@link RedissonClient}.
 */
public class RedisBackingStore implements IBackingStore {
    private final RedissonClient client;
    private final String prefix;

    public RedisBackingStore(RedissonClient client, String prefix) {
        this.client = client;
        this.prefix = prefix;
    }

    /**
     * Pass an {@link RMap} to the function and return its result.
     *
     * @param func the function to apply using the {@link RMap}
     * @return the result
     */
    private <T> T onMap(Function<RMap<String, String>, T> func) {
        final RMap<String, String> map = client.getMap(prefix);
        return func.apply(map);
    }

    /**
     * Pass an {@link RMap} to the consumer.
     *
     * @param consumer the consumer of the {@link RMap}
     */
    private void withMap(Consumer<RMap<String, String>> consumer) {
        final RMap<String, String> map = client.getMap(prefix);
        consumer.accept(map);
    }

    @Override
    public void put(String key, Object value) {
        withMap(map -> {
            try {
                final String raw;
                if (isNull(value)) {
                    raw = null;
                } else if (value.getClass().isPrimitive() || value instanceof String) {
                    raw = value.toString();
                } else {
                    raw = JSON_MAPPER.writeValueAsString(value);
                }
                map.put(key, raw);

            } catch (JsonProcessingException e) {
                throw new RuntimeException(String.format("Error setting value for key '%s'", key), e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, Class<T> type) {
        return onMap(map -> {
            try {
                final String raw = map.get(key);
                if (isNull(raw)) {
                    return null;
                } else if (type.isPrimitive() || type.isAssignableFrom(String.class)) {
                    return (T) BackingStoreUtil.readPrimitive(type, raw);
                } else {
                    return JSON_MAPPER.readValue(raw, type);
                }
            } catch (Exception e) {
                throw new RuntimeException(String.format("Error reading value for key '%s'", key), e);
            }
        });
    }

    @Override
    public void remove(String key) {
        withMap(map -> {
            try {
                map.remove(key);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Error removing value for key '%s'", key), e);
            }
        });
    }

    @Override
    public boolean containsKey(String key) {
        return onMap(map -> nonNull(map.get(key)));
    }
}
