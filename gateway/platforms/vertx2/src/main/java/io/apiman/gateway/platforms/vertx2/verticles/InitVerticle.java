package io.apiman.gateway.platforms.vertx2.verticles;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;

public class InitVerticle extends ApimanVerticleBase {

    @Override
    public void start() {
        super.start();

        DeploymentOptions base = new DeploymentOptions().setConfig(config());
        DeploymentOptions policy = buildDeploymentOptions(base, PolicyVerticle.VERTICLE_TYPE);
        DeploymentOptions api = buildDeploymentOptions(base, ApiVerticle.VERTICLE_TYPE);
        DeploymentOptions http = buildDeploymentOptions(base, HttpGatewayVerticle.VERTICLE_TYPE);

        vertx.deployVerticle(PolicyVerticle.class.getCanonicalName(), policy,
                (AsyncResult<String> event) -> {

                if (event.failed())
                    throw new RuntimeException(event.cause());

                vertx.deployVerticle(HttpGatewayVerticle.class.getCanonicalName(), http, this::failureHandler);
                vertx.deployVerticle(ApiVerticle.class.getCanonicalName(), api, this::failureHandler);
        });
    }

    private void failureHandler(AsyncResult<String> result) {
        if (result.failed())
            throw new RuntimeException("Failed to deploy verticle", result.cause()); //$NON-NLS-1$
    }

    private DeploymentOptions buildDeploymentOptions(DeploymentOptions base, VerticleType type) {
        return new DeploymentOptions(base).setInstances(apimanConfig.getVerticleCount(type));
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.INITIALISER;
    }
}
