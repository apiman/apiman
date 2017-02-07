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
import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.impl.AbstractEngineFactory;
import io.apiman.gateway.engine.impl.ConfigDrivenEngineFactory;
import io.apiman.gateway.platforms.vertx3.common.AsyncInitialize;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
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
    private IAsyncResultHandler<Void> handler;
    private int started = 0;
    private static final int ELEMENTS_TO_START = 6;


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
    protected <T> T create(Class<T> type, Map<String, String> mapConfig) {
        started +=1;
        T instance = getInstance(type, mapConfig);
        if (instance instanceof AsyncInitialize) {
            ((AsyncInitialize) instance).initialize(initResult -> {
                if (initResult.isError()) {
                    handler.handle(initResult);
                }
            });
        }
        if (started == ELEMENTS_TO_START) {
            handler.handle(AsyncResultImpl.create((Void) null));
        }
        return instance;
    }

    @SuppressWarnings("nls")
    private <T> T getInstance(Class<T> type, Map<String, String> mapConfig) {
        try {
            Constructor<T> constructor = type.getConstructor(Vertx.class, VertxEngineConfig.class, Map.class);
            return constructor.newInstance(vertx, vxConfig, mapConfig);
        } catch (NoSuchMethodException e) { // If doesn't have constructor, try other method.
        } catch (Exception e ) {
            throw new RuntimeException(e);
        }
        try {
            Constructor<T> constructor = type.getConstructor(Vertx.class, IEngineConfig.class, Map.class);
            return constructor.newInstance(vertx, vxConfig, mapConfig);
        } catch (NoSuchMethodException e) { // If doesn't have constructor, try other method.
        } catch (Exception e ) {
            throw new RuntimeException(e);
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

    public AbstractEngineFactory setHandler(IAsyncResultHandler<Void> handler) {
        this.handler = handler;
        return this;
    }
}
