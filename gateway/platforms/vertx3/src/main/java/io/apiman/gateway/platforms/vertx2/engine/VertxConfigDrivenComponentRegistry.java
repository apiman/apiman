/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.platforms.vertx2.engine;

import io.apiman.gateway.engine.impl.ConfigDrivenComponentRegistry;
import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.vertx.core.Vertx;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Extends {@link ConfigDrivenComponentRegistry} to allow components to be constructed with a {@link Vertx}
 * instance; else the standard mechanisms are fallen back on.
 *
 * @see ConfigDrivenComponentRegistry
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class VertxConfigDrivenComponentRegistry extends ConfigDrivenComponentRegistry {

    private VertxEngineConfig engineConfig;
    private Vertx vertx;

    public VertxConfigDrivenComponentRegistry(Vertx vertx, VertxEngineConfig engineConfig) {
        super(engineConfig);
        this.engineConfig = engineConfig;
        this.vertx = vertx;
    }

    /**
     * Creates components, but allows a {@link #vertx} instance to be passed in.
     *
     * Note that we can't have a static {@link #vertx} object, so we can't override the super method.
     *
     * @param type component type
     * @param apimanConfig configuration (if necessary).
     * @return instance of Class<T>
     */
    @Override
    protected <T> T create(Class<T> type, Map<String, String> mapConfig) {
        try {
            Constructor<T> constructor = type.getConstructor(Vertx.class, VertxEngineConfig.class, Map.class);
            return constructor.newInstance(vertx, engineConfig, mapConfig);
        } catch (Exception e) {
        }
        try {
            Constructor<T> constructor = type.getConstructor(Map.class);
            return constructor.newInstance(mapConfig);
        } catch (Exception e) {
        }
        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
