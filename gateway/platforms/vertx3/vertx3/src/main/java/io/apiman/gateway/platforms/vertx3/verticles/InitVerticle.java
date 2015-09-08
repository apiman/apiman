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
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Initialiser verticle
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class InitVerticle extends ApimanVerticleBase {

    class ApimanDeployment {
        public DeploymentOptions deploymentOptions;
        public String className;

        public ApimanDeployment(DeploymentOptions deploymentOptions, String className) {
            this.deploymentOptions = deploymentOptions;
            this.className = className;
        }
    }

    private boolean failed = false;
    private int ctr;

    @Override
    public void start(Future<Void> start) {
        super.start();

        DeploymentOptions base = new DeploymentOptions().setConfig(config());

        @SuppressWarnings("serial")
        List<ApimanDeployment> deployList = new ArrayList<ApimanDeployment>() {{
            add(buildDeploymentOptions(base, PolicyVerticle.class.getCanonicalName(), PolicyVerticle.VERTICLE_TYPE));
            add(buildDeploymentOptions(base, ApiVerticle.class.getCanonicalName(), ApiVerticle.VERTICLE_TYPE));
            add(buildDeploymentOptions(base, HttpGatewayVerticle.class.getCanonicalName(), HttpGatewayVerticle.VERTICLE_TYPE));
            add(buildDeploymentOptions(base, HttpsGatewayVerticle.class.getCanonicalName(), HttpsGatewayVerticle.VERTICLE_TYPE));
        }};

        ctr = deployList.size();

        deployList.forEach(deployment -> {
            if (!failed) {
                vertx.deployVerticle(deployment.className, deployment.deploymentOptions, result -> {
                    checkAndSetStatus(result, start);
                });
            }
        });
    }

    private void checkAndSetStatus(AsyncResult<String> result, Future<Void> start) {
        ctr--;
        if (result.failed()) {
            start.fail(result.cause());
            failed = true;
            printError(result.cause());
        } else if (ctr == 0) {
            start.complete();
        }
    }

    private void printError(Throwable cause) {
        StringWriter errorTrace = new StringWriter();
        cause.printStackTrace(new PrintWriter(errorTrace));
        System.err.println("Failed to deploy verticles: " + cause.getMessage()); //$NON-NLS-1$
        System.err.println(errorTrace);
    }

    private ApimanDeployment buildDeploymentOptions(DeploymentOptions base, String className, VerticleType type) {
        DeploymentOptions deploymentOptions = new DeploymentOptions(base).setInstances(apimanConfig.getVerticleCount(type));
        return new ApimanDeployment(deploymentOptions, className);
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.INITIALISER;
    }
}
