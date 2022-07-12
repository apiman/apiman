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

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.Version;
import io.apiman.gateway.platforms.vertx3.common.verticles.VerticleType;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.launcher.commands.VersionCommand;

/**
 * Initialiser verticle
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class InitVerticle extends ApimanVerticleBase {
    private final IApimanLogger log = ApimanLoggerFactory.getLogger(InitVerticle.class);
    private DeploymentOptions base;

    @Override
    public void start(Promise<Void> start) {
        super.start(start);
        base = new DeploymentOptions().setConfig(config());

        @SuppressWarnings({ "rawtypes" }) // CompositeFuture doesn't accept generic type
        List<Future> deployList = new ArrayList<>();

        deploy(ApiVerticle.class.getCanonicalName(), ApiVerticle.VERTICLE_TYPE, deployList);
        deploy(HttpGatewayVerticle.class.getCanonicalName(), HttpGatewayVerticle.VERTICLE_TYPE, deployList);
        deploy(HttpsGatewayVerticle.class.getCanonicalName(), HttpsGatewayVerticle.VERTICLE_TYPE, deployList);

        CompositeFuture.all(deployList).onComplete(compositeResult -> {
            if (compositeResult.failed()) {
                compositeResult.cause().printStackTrace();
                log.warn("Failed to deploy verticles: {0}", compositeResult.cause().getMessage());
                start.fail(compositeResult.cause());
            } else {
                log.info("Apiman Version: {0}", Version.get().getVerbose());
                log.info("Vert.x Version: {0}", VersionCommand.getVersion());

                log.info("Successfully deployed all verticles");
                log.info("Gateway API port: {0}", apimanConfig.getPort(ApiVerticle.VERTICLE_TYPE));

                start.complete();
            }
        });
    }

    private void deploy(String canonicalName, VerticleType verticleType, @SuppressWarnings("rawtypes") List<Future> deployList) {
        log.info("Will deploy {0} of type {1}", apimanConfig.getVerticleCount(verticleType), verticleType);

        if (apimanConfig.getVerticleCount(verticleType) <= 0) {
            return;
        }

        DeploymentOptions deploymentOptions = new DeploymentOptions(base)
                .setInstances(apimanConfig.getVerticleCount(verticleType));
        // Future for this deployment.
        Promise<String> promise = Promise.promise();
        // Do deployment
        vertx.deployVerticle(canonicalName, deploymentOptions, promise);
        // Set the future associated with the deployment so #all can wait for it.
        deployList.add(promise.future());
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.INITIALISER;
    }
}
