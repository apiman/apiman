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

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.impl.ConfigDrivenEngineFactory;
import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.apiman.gateway.vertx.connector.ConnectorFactory;

/**
 * A configuration driven engine specifically for Vert.x
 * 
 * @see ConfigDrivenEngineFactory
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class VertxConfigDrivenEngineFactory extends ConfigDrivenEngineFactory {

    private Vertx vertx;
    private Container container;
    private VertxEngineConfig vxConfig;

    public VertxConfigDrivenEngineFactory(Vertx vertx, Container container, VertxEngineConfig config) {
        super(config);
        this.vertx  = vertx;
        this.container = container;
        this.vxConfig = config;
    }
    
    @Override
    protected IConnectorFactory createConnectorFactory() {
        return new ConnectorFactory(vertx, container);
    }
    
    @Override
    protected IComponentRegistry createComponentRegistry() {
        return new VertxConfigDrivenComponentRegistry(vertx, vxConfig);
    }
   
}
