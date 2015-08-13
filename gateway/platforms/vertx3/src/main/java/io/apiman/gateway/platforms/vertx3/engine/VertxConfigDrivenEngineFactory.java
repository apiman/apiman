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
package io.apiman.gateway.platforms.vertx3.engine;

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.impl.ConfigDrivenEngineFactory;
import io.apiman.gateway.platforms.vertx3.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.connector.ConnectorFactory;
import io.vertx.core.Vertx;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * A configuration driven engine specifically for Vert.x
 *
 * @see ConfigDrivenEngineFactory
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class VertxConfigDrivenEngineFactory extends ConfigDrivenEngineFactory {

    private Vertx vertx;
    private VertxEngineConfig vxConfig;

    public VertxConfigDrivenEngineFactory(Vertx vertx, VertxEngineConfig config) {
        super(config);
        this.vertx  = vertx;
        this.vxConfig = config;
    }

    @Override
    protected IConnectorFactory createConnectorFactory(IPluginRegistry pluginRegistry) {
        return new ConnectorFactory(vertx, vxConfig.getConnectorFactoryConfig());
    }

    @Override
    protected IComponentRegistry createComponentRegistry(IPluginRegistry pluginRegistry) {
        return new VertxConfigDrivenComponentRegistry(pluginRegistry, vertx, vxConfig);
    }

    @Override
    @SuppressWarnings("nls")
    protected <T> T create(Class<T> type, Map<String, String> mapConfig) {
        try {
            Constructor<T> constructor = type.getConstructor(Vertx.class, VertxEngineConfig.class, Map.class);
            return constructor.newInstance(vertx, vxConfig, mapConfig);
        } catch (Exception e) {
        }
        try {
            Constructor<T> constructor = type.getConstructor(Map.class);
            return constructor.newInstance(mapConfig);
        } catch (Exception e) {
        }
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(String.format(
                    "Could not instantiate %s. Verify class has valid constructor parameters: %s", type,
                    e.getMessage()), e);
        }
    }
}
