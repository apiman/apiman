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
    private final String storeName;
    private final HazelcastInstance hazelcastInstance;

    /**
     * Constructor.
     */
    public AbstractHazelcastComponent(String storeName) {
        this(storeName, new Config());
    }

    /**
     * Constructor.
     *
     * @param config the config
     */
    public AbstractHazelcastComponent(String storeName, Config config) {
        this.storeName = storeName;
        hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    }

    /**
     * Returns an instance of the shared state.
     *
     * @param <T> the value type
     * @return the shared state
     */
    protected <T> Map<String, T> getSharedState() {
        return hazelcastInstance.getMap(storeName);
    }

    /**
     * Builds a key derived from the namespace.
     *
     * @param namespace the namespace
     * @param propertyName the property name
     * @return the namespaced key
     */
    protected String buildNamespacedKey(String namespace, String propertyName) {
        return namespace + "." + propertyName;
    }
}
