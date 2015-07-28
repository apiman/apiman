package io.apiman.gateway.platforms.vertx2.verticles;

import io.vertx.core.DeploymentOptions;

public class InitVerticle extends ApimanVerticleBase {

    @Override
    public void start() {
        super.start();

        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setInstances(1); // get from JSON config (4:1 ratio?)
        deploymentOptions.setConfig(config());

//        vertx.deployVerticle(PolicyVerticle.class.getCanonicalName(), deploymentOptions,
//                (AsyncResult<String> event) -> {
//
//                if (event.failed())
//                    throw new RuntimeException(event.cause());
//
//                vertx.deployVerticle(HttpGatewayVerticle.class.getCanonicalName(), deploymentOptions);
//        });

        vertx.deployVerticle(ApiVerticle.class.getCanonicalName());
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.INITIALISER;
    }
}
