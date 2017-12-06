/*
 * Copyright 2017 Pete Cornish
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
package io.apiman.gateway.engine.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

/**
 * Common base class for components backed by a Hazelcast Map.
 *
 * @author Pete Cornish
 */
abstract class AbstractHazelcastComponent {
    public static final String CONFIG_EAGER_INIT = "eager-init";
    private final String storeName;
    private final Config config;
    private final Object mutex = new Object();

    /**
     * Access this via {@link #getHazelcastInstance()}.
     */
    private HazelcastInstance hazelcastInstance;

    /**
     * Constructor.
     */
    public AbstractHazelcastComponent(Map<String, String> componentConfig, String storeName) {
        this(componentConfig, storeName, null);
    }

    /**
     * Constructor.
     *
     * @param config the config
     */
    public AbstractHazelcastComponent(Map<String, String> componentConfig, String storeName, Config config) {
        this.storeName = storeName;
        this.config = config;

        if (Boolean.valueOf(componentConfig.get(CONFIG_EAGER_INIT))) {
            getHazelcastInstance();
        }
    }

    /**
     * @return a new or existing Hazelcast instance
     */
    private HazelcastInstance getHazelcastInstance() {
        if (null == hazelcastInstance) {
            synchronized (mutex) {
                if (null == hazelcastInstance) {
                    hazelcastInstance = Hazelcast.newHazelcastInstance(config);
                }
            }
        }
        return hazelcastInstance;
    }

    /**
     * Returns an instance of the Map for the current {@link #storeName}.
     *
     * @param <T> the value type
     * @return the Map
     */
    protected <T> Map<String, T> getMap() {
        return getHazelcastInstance().getMap(storeName);
    }

    /**
     * Builds a key derived from the namespace.
     *
     * @param namespace    the namespace
     * @param propertyName the property name
     * @return the namespaced key
     */
    protected String buildNamespacedKey(String namespace, String propertyName) {
        return namespace + "." + propertyName;
    }

    /**
     * Shut down the Hazelcast instance.
     */
    public void reset() {
        if (null != hazelcastInstance) {
            synchronized (mutex) {
                hazelcastInstance.shutdown();
                hazelcastInstance = null;
            }
        }
    }
}
