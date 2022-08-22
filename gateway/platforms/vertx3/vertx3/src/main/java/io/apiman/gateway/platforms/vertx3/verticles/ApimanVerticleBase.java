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

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.SimpleStringUtils;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.common.verticles.VerticleType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
/**
 * Standard base for all apiman verticles.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public abstract class ApimanVerticleBase extends AbstractVerticle {

    protected VertxEngineConfig apimanConfig;
    protected String uuid = SimpleStringUtils.join(".", UUID.randomUUID().toString(), verticleType().name());
    protected IApimanLogger log = ApimanLoggerFactory.getLogger(this.getClass());
    protected HashSet<String> allowedCorsOrigins = new HashSet<>();

    @Override
    public void start(Promise<Void> startPromise) {
        apimanConfig = getEngineConfig();
        log.info("Starting verticle: {0}. UUID: {1}.", verticleType(), uuid);
        setAllowedCorsOrigins();
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

    protected IApimanLogger getLogger() {
        return log;
    }

    /**
     * Adds allowed TLS versions to the verticle configuration if special TLS versions are configured
     * @param httpsServerOptions
     */
    public void addAllowedSslTlsProtocols(HttpServerOptions httpsServerOptions) {
        String[] allowedProtocols = apimanConfig.getSslTlsAllowedProtocols();

        if (!allowedProtocols[0].isEmpty()) {
            // remove unsecure protocols
            httpsServerOptions.removeEnabledSecureTransportProtocol("TLSv1");
            httpsServerOptions.removeEnabledSecureTransportProtocol("TLSv1.1");

            for (String protocol : allowedProtocols) {
                httpsServerOptions.addEnabledSecureTransportProtocol(protocol);
            }
        }
    }

    /**
     * Read system property and set allowed CORS Headers
     */
    private void setAllowedCorsOrigins() {
        String corsOrigins = System.getProperty("allowed_cors_origins");
        if (corsOrigins != null) {
            allowedCorsOrigins = new HashSet<>(Arrays.asList(corsOrigins.trim().split(",")));
        }
    }
}
