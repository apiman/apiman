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

import io.apiman.gateway.platforms.vertx3.common.verticles.VerticleType;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Initialiser verticle
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class InitVerticle extends ApimanVerticleBase {
    private Logger log = LoggerFactory.getLogger(InitVerticle.class);
    private DeploymentOptions base;

    @Override
    public void start(Future<Void> start) {
        super.start(start);
        base = new DeploymentOptions().setConfig(config());

        @SuppressWarnings({ "rawtypes" }) // CompositeFuture doesn't accept generic type
        List<Future> deployList = new ArrayList<>();

        deploy(ApiVerticle.class.getCanonicalName(), ApiVerticle.VERTICLE_TYPE, deployList);
        deploy(HttpGatewayVerticle.class.getCanonicalName(), HttpGatewayVerticle.VERTICLE_TYPE, deployList);
        deploy(HttpsGatewayVerticle.class.getCanonicalName(), HttpsGatewayVerticle.VERTICLE_TYPE, deployList);

        CompositeFuture.all(deployList).setHandler(compositeResult -> {
            if (compositeResult.failed()) {
                compositeResult.cause().printStackTrace();
                log.fatal("Failed to deploy verticles: " + compositeResult.cause().getMessage()); //$NON-NLS-1$
                start.fail(compositeResult.cause());
            } else {
                log.info("Successfully deployed all verticles"); //$NON-NLS-1$
                start.complete();
            }
        });
    }

    private void deploy(String canonicalName, VerticleType verticleType, @SuppressWarnings("rawtypes") List<Future> deployList) {
        log.info("Will deploy {0} of type {1}", apimanConfig.getVerticleCount(verticleType), verticleType); //$NON-NLS-1$

        if (apimanConfig.getVerticleCount(verticleType) <= 0) {
            return;
        }

        DeploymentOptions deploymentOptions = new DeploymentOptions(base)
                .setInstances(apimanConfig.getVerticleCount(verticleType));
        // Future for this deployment.
        Future<String> future = Future.future();
        // Do deployment
        vertx.deployVerticle(canonicalName, deploymentOptions, future.completer());
        // Set the future associated with the deployment so #all can wait for it.
        deployList.add(future);
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.INITIALISER;
    }
}
