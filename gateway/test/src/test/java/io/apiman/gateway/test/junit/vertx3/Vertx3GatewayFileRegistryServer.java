/*
 * Copyright 2017 JBoss Inc
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
package io.apiman.gateway.test.junit.vertx3;

import io.apiman.gateway.engine.vertx.polling.URILoadingRegistry;
import io.apiman.gateway.platforms.vertx3.verticles.ApiVerticle;
import io.apiman.gateway.platforms.vertx3.verticles.InitVerticle;
import io.apiman.test.common.echo.EchoServer;
import io.apiman.test.common.resttest.IGatewayTestServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A Vert.x 3 version of the gateway test server
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class Vertx3GatewayFileRegistryServer implements IGatewayTestServer {

    protected static final int API_PORT = 9009;
    protected static final int GW_PORT = 8082;
    protected static final int ECHO_PORT = 7654;

    private EchoServer echoServer = new EchoServer(ECHO_PORT);
    private String conf;
    private CountDownLatch gatewayStartLatch;
    private CountDownLatch gatewayStopLatch;
    private Vertx vertx;
    private JsonObject vertxConf;
    private Vertx apiToFileVx;
    private JsonObject apiToFilePushEmulatorConfig;
    private CountDownLatch rewriteCdl = new CountDownLatch(1);
    private CountDownLatch resetLatch = new CountDownLatch(1);
    private CountDownLatch a2fInitLatch = new CountDownLatch(1);

    /**
     * Constructor.
     */
    public Vertx3GatewayFileRegistryServer() {
    }

    @Override
    public void configure(JsonNode nodeConfig) {
        vertxConf = loadJsonObjectFromResources(nodeConfig, "config");
        apiToFilePushEmulatorConfig = loadJsonObjectFromResources(nodeConfig, "configPushEmulator");

        apiToFileVx = Vertx.vertx(new VertxOptions()
                .setBlockedThreadCheckInterval(99999));

        apiToFileVx.deployVerticle(ApiVerticle.class.getCanonicalName(),
                new DeploymentOptions().setConfig(apiToFilePushEmulatorConfig),
                complete -> a2fInitLatch.countDown());

        apiToFileVx.eventBus().consumer("reset").handler(reset -> resetLatch.countDown());
        apiToFileVx.eventBus().consumer("rewrite").handler(rewritten -> rewriteCdl.countDown());

        // Important: before we deploy the verticle, we must ensure that the API-to-File registry
        // has fully initialised. Otherwise the variable may not be substituted yet.
        try {
            a2fInitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private JsonObject loadJsonObjectFromResources(JsonNode nodeConfig, String name) {
        ClassLoader classLoader = getClass().getClassLoader();
        String fPath = nodeConfig.get(name).asText();
        File file = new File(classLoader.getResource(fPath).getFile());
        try {
            conf = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new JsonObject(conf);
    }

    @Override
    public String getApiEndpoint() {
        return "http://localhost:" + API_PORT;
    }

    @Override
    public String getGatewayEndpoint() {
        return "http://localhost:" + GW_PORT;
    }

    @Override
    public String getEchoTestEndpoint() {
        return "http://localhost:" + ECHO_PORT;
    }

    @Override
    public void start() {
        try {
            gatewayStartLatch = new CountDownLatch(1);

            vertx = Vertx.vertx(new VertxOptions()
                    .setBlockedThreadCheckInterval(99999));
            echoServer.start();

            DeploymentOptions options = new DeploymentOptions().
                    setConfig(vertxConf);

            vertx.deployVerticle(InitVerticle.class.getCanonicalName(),
                    options,
                    result -> {
                        System.out.println("Deployed init verticle! " + (result.failed() ? "failed" : "succeeded"));
                        gatewayStartLatch.countDown();
                    });

            gatewayStartLatch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        System.err.println("Stopping main Vert.x");
        gatewayStopLatch = new CountDownLatch(1);
        echoServer.stop();

        apiToFileVx.eventBus().publish(ApiToFileRegistry.class.getCanonicalName(), null,
                new DeliveryOptions().addHeader("action", "reset"));

        URILoadingRegistry.reset();

        vertx.close(result -> {
            gatewayStopLatch.countDown();
        });

        try {
            gatewayStopLatch.await();
            resetLatch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        resetLatch = new CountDownLatch(1);
    }

    @Override
    public void next(String endpoint) {
        try {
            if (URI.create(endpoint).getPort() == API_PORT) {
                CountDownLatch cdl = new CountDownLatch(1);

                System.out.println("awaiting rewrite");
                rewriteCdl.await();

                URILoadingRegistry.reloadData(done -> {
                    cdl.countDown();
                });

                cdl.await();
                System.out.println("Next...");
                rewriteCdl = new CountDownLatch(1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
