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
package io.apiman.gateway.platforms.vertx3.verticles;

import io.apiman.common.util.SimpleStringUtils;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.common.verticles.VerticleType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.UUID;
/**
 * Standard base for all apiman verticles.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public abstract class ApimanVerticleBase extends AbstractVerticle {

    protected VertxEngineConfig apimanConfig;
    protected String uuid = SimpleStringUtils.join(".", UUID.randomUUID().toString(), verticleType().name());
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start(Future<Void> startFuture) {
        apimanConfig = getEngineConfig();
        log.info("Starting verticle: {0}. UUID: {1}.", verticleType(), uuid);
    }

    /**
     * Maps to config.
     * @return Verticle's type
     */
    public abstract VerticleType verticleType();

    // Override this for verticle specific config & testing.
    protected VertxEngineConfig getEngineConfig() {
        if (config().isEmpty()) {
            throw new IllegalStateException("No configuration provided!");
        }
        return new VertxEngineConfig(config());
    }

    protected String getUuid() {
        return uuid;
    }

    protected Logger getLogger() {
        return log;
    }
}
