package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.platforms.vertx2.http.HttpExecutor;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;

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
            .requestHandler(new HttpExecutor(vertx, log, true))
            .listen(apimanConfig.getPort(VERTICLE_TYPE));
    }

    @Override
    public VerticleType verticleType() {
        return VERTICLE_TYPE;
    }
}