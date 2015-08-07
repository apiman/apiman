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

import io.apiman.gateway.platforms.vertx3.http.HttpSpawner;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;

/**
 * A HTTPS gateway verticle
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HttpsGatewayVerticle extends ApimanVerticleBase {
    public static final VerticleType VERTICLE_TYPE = VerticleType.HTTPS;

    @Override
    public void start() {
        super.start();

        HttpServerOptions sslOptions = new HttpServerOptions()
            .setHost(apimanConfig.getHostname())
            .setSsl(true)
            .setKeyStoreOptions(
                    new JksOptions()
                        .setPath(apimanConfig.getKeyStore())
                        .setPassword(apimanConfig.getKeyStorePassword())
                    )
            .setTrustStoreOptions(
                    new JksOptions()
                        .setPath(apimanConfig.getTrustStore())
                        .setPassword(apimanConfig.getTrustStorePassword())
                    );

        vertx.createHttpServer(sslOptions)
            .requestHandler(new HttpSpawner(vertx, log, true))
            .listen(apimanConfig.getPort(VERTICLE_TYPE));
    }

    @Override
    public VerticleType verticleType() {
        return VERTICLE_TYPE;
    }
}