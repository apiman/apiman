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

import io.apiman.gateway.engine.IRequiresInitialization;
import io.apiman.gateway.engine.hazelcast.config.HazelcastInstanceManager;

import java.util.Map;

/**
 * Common base class for components backed by a Hazelcast Map.
 *
 * @author Pete Cornish
 */
abstract class AbstractHazelcastComponent implements IRequiresInitialization {
    public static final String CONFIG_EAGER_INIT = "eager-init";

    private final HazelcastInstanceManager instanceManager;
    private final Map<String, String> componentConfig;
    private final String storeName;

    /**
     * Constructor.
     */
    public AbstractHazelcastComponent(Map<String, String> componentConfig, String storeName) {
        this(HazelcastInstanceManager.DEFAULT_MANAGER, componentConfig, storeName);
    }

    /**
     * Constructor.
     */
    public AbstractHazelcastComponent(HazelcastInstanceManager instanceManager, Map<String, String> componentConfig, String storeName) {
        this.instanceManager = instanceManager;
        this.componentConfig = componentConfig;
        this.storeName = storeName;
    }

    @Override
    public void initialize() {
        if (Boolean.valueOf(componentConfig.get(CONFIG_EAGER_INIT))) {
            getMap();
        }
    }

    /**
     * Returns an instance of the Map for the current {@link #storeName}.
     *
     * @param <T> the value type
     * @return the Map
     */
    protected <T> Map<String, T> getMap() {
        return instanceManager.getHazelcastMap(storeName);
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
}
