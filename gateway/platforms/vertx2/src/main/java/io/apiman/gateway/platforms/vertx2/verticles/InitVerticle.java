package io.apiman.gateway.platforms.vertx2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;

public class InitVerticle extends AbstractVerticle {

    @Override
    public void start() {
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setInstances(5); // get from JSON config (4:1 ratio?)

        vertx.deployVerticle(PolicyVerticle.class.getCanonicalName(), deploymentOptions,
                (AsyncResult<String> event) -> {
                vertx.deployVerticle(HttpGatewayVerticle.class.getCanonicalName(), deploymentOptions);
        });
    }
}
