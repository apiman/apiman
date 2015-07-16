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
package io.apiman.gateway.vertx.engine;

import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.beans.exceptions.ComponentNotFoundException;
import io.apiman.gateway.engine.impl.ConfigDrivenComponentRegistry;
import io.apiman.gateway.vertx.config.VertxEngineConfig;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.vertx.java.core.Vertx;

/**
 * Extends {@link ConfigDrivenComponentRegistry} to allow components to be constructed with a {@link Vertx}
 * instance; else the standard mechanisms are fallen back on.
 *
 * @see ConfigDrivenComponentRegistry
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class VertxConfigDrivenComponentRegistry extends ConfigDrivenComponentRegistry {

    private VertxEngineConfig engineConfig;
    private Vertx vertx;
    private IPluginRegistry pluginRegistry;

    public VertxConfigDrivenComponentRegistry(Vertx vertx, VertxEngineConfig engineConfig,
            IPluginRegistry pluginRegistry) {
        super(engineConfig, pluginRegistry);
        this.engineConfig = engineConfig;
        this.vertx = vertx;
        this.pluginRegistry = pluginRegistry;
    }

    @Override
    public <T extends IComponent> T createAndRegisterComponent(Class<T> componentType)
            throws ComponentNotFoundException {
        try {
            Class<T> componentClass = engineConfig.getComponentClass(componentType, pluginRegistry);
            Map<String, String> componentConfig = engineConfig.getComponentConfig(componentType);
            T component = createWithVertx(componentClass, engineConfig, componentConfig);
            super.addComponentMapping(componentType, component);
            return component;
        } catch (Exception e) {
            throw new ComponentNotFoundException(componentType.getName(), e);
        }
    }

    /**
     * Creates components, but allows a {@link #vertx} instance to be passed in.
     *
     * Note that we can't have a static {@link #vertx} object, so we can't override the super method.
     *
     * @param type component type
     * @param config configuration (if necessary).
     * @return instance of Class<T>
     */
    protected <T> T createWithVertx(Class<T> type, VertxEngineConfig engineConfig, Map<String, String> mapConfig) {
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
