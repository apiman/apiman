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

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages Hazelcast instances to reduce the amount of initialisation work on each I/O operation.
 *
 * @author Pete Cornish
 */
public final class HazelcastInstanceManager {
    public static final HazelcastInstanceManager DEFAULT_MANAGER = new HazelcastInstanceManager();

    private final Object mutex = new Object();
    private final Map<String, Map<String, ?>> stores = Collections.synchronizedMap(new HashMap<>());
    private Config overrideConfig;
    private HazelcastInstance hazelcastInstance;

    /**
     * Generally, you don't want to initialise your own manager - instead, use the {@link #DEFAULT_MANAGER}.
     */
    public HazelcastInstanceManager() {
    }

    /**
     * Override the default Hazelcast configuration discovery strategy.
     */
    public void setOverrideConfig(Config overrideConfig) {
        this.overrideConfig = overrideConfig;
    }

    /**
     * @param storeName a name to associate with the instance
     * @return a new or existing Hazelcast instance for the given store name
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getHazelcastMap(String storeName) {
        Map<String, ?> hzMap = stores.get(storeName);
        if (null == hzMap) {
            synchronized (mutex) {
                if (null == hazelcastInstance) {
                    hazelcastInstance = Hazelcast.newHazelcastInstance(overrideConfig);
                }
                if (null == stores.get(storeName)) {
                    hzMap = hazelcastInstance.getMap(storeName);
                    stores.put(storeName, hzMap);
                }
            }
        }
        return (Map<String, T>) hzMap;
    }

    /**
     * Shut down the Hazelcast instance and clear stores references.
     */
    public void reset() {
        if (null != hazelcastInstance) {
            synchronized (mutex) {
                if (null == hazelcastInstance) {
                    hazelcastInstance.shutdown();
                    hazelcastInstance = null;
                }
            }
        }
        if (!stores.isEmpty()) {
            synchronized (mutex) {
                stores.clear();
            }
        }
    }
}
