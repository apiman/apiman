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

import io.apiman.gateway.platforms.vertx3.common.config.InheritingHttpServerOptions;
import io.apiman.gateway.platforms.vertx3.common.config.InheritingHttpServerOptionsConverter;
import io.apiman.gateway.platforms.vertx3.common.verticles.VerticleType;
import io.apiman.gateway.platforms.vertx3.http.HttpApiFactory;
import io.apiman.gateway.platforms.vertx3.http.HttpPolicyAdapter;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

/**
 * A HTTP gateway verticle
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HttpGatewayVerticle extends ApimanVerticleWithEngine {
    static final VerticleType VERTICLE_TYPE = VerticleType.HTTP;

    @Override
    public void start(Future<Void> startFuture) {
        super.start(startFuture);

        HttpApiFactory.init(engine.getApiRequestPathParser());

        InheritingHttpServerOptions httpServerOptions = new InheritingHttpServerOptions();
        httpServerOptions.setHost(apimanConfig.getHostname());

        // Load any provided configuration into the HttpServerOptions.
        JsonObject httpServerOptionsJson = apimanConfig.getVerticleConfig(verticleType().name())
                .getJsonObject("httpServerOptions", new JsonObject()); //$NON-NLS-1$
        InheritingHttpServerOptionsConverter.fromJson(httpServerOptionsJson, httpServerOptions);

        vertx.createHttpServer(httpServerOptions)
            .requestHandler(this::requestHandler)
            .listen(apimanConfig.getPort(VERTICLE_TYPE), apimanConfig.getHost(VERTICLE_TYPE));
    }

    private void requestHandler(HttpServerRequest req) {
        new HttpPolicyAdapter(req, policyFailureWriter, policyErrorWriter, engine, false).execute();
    }

    @Override
    public VerticleType verticleType() {
        return VERTICLE_TYPE;
    }
}
