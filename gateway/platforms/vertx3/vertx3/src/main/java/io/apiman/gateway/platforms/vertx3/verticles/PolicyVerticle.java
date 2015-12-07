/*
 * Copyright 2015 JBoss Inc
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

import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.common.verticles.VerticleType;
import io.apiman.gateway.platforms.vertx3.services.InitializeIngestorService;
import io.apiman.gateway.platforms.vertx3.services.impl.InitializeIngestorServiceImpl;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Verticle executes policy engine.
 *
 * Gateway Verticle <-> Policy Verticle <-> Backend
 *
 * The policy verticle receives ApiRequests over the eventbus, which it passes into the apiman core for
 * evaluation. Upon success, a connection is opened up to the backend, and the requester is signalled to begin
 * streaming data (which is also passed through apiman). Failures, either by exception or policy failure are
 * passed back to the requester. As all requests and responses between the policy verticle and any gateway are
 * abstracted, their specific implementation is unimportant. Responses, likewise are evaluated, and streamed
 * over the eventbus.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class PolicyVerticle extends ApimanVerticleWithEngine {
    public static final VerticleType VERTICLE_TYPE = VerticleType.POLICY;

    private InitializeIngestorServiceImpl service;

    @Override
    public void start() {
        super.start();

        service = new InitializeIngestorServiceImpl(vertx, apimanConfig, engine, log);

        // Listen for anyone who wants to initialise a PolicyIngestion connection
        ProxyHelper.registerService(InitializeIngestorService.class, vertx, service,
                VertxEngineConfig.GATEWAY_ENDPOINT_POLICY_INGESTION);
    }

    @Override
    public VerticleType verticleType() {
        return VERTICLE_TYPE;
    }
}