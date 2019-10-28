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
package io.apiman.gateway.test.junit.vertx3;

import com.fasterxml.jackson.databind.JsonNode;
import io.apiman.common.util.ReflectionUtils;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.verticles.InitVerticle;
import io.apiman.test.common.echo.EchoServer;
import io.apiman.test.common.resttest.IGatewayTestServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

/**
 * A Vert.x 3 version of the gateway test server
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class Vertx3GatewayTestServer implements IGatewayTestServer {

    protected static final int API_PORT = 8081;
    protected static final int GW_PORT = 8082;
    protected static final int ECHO_PORT = 7654;

    private EchoServer echoServer = new EchoServer(ECHO_PORT);
    private CountDownLatch startLatch;
    private CountDownLatch stopLatch;
    private Resetter resetter;
    private Vertx vertx;
    private JsonObject vertxConf;
    private boolean clustered;

    public Vertx3GatewayTestServer(boolean clustered) {
        this.clustered = clustered;
    }

    @Override
    public void configure(JsonNode config) {
        vertxConf = new Vertx3GatewayHelper().loadJsonObjectFromResources(config, "config");
        resetter = getResetter(config.get("resetter").asText());
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
            if (clustered) {
                CountDownLatch clusteredStart = new CountDownLatch(1);
                Vertx.clusteredVertx(new VertxOptions().setClustered(clustered).setClusterHost("localhost").setBlockedThreadCheckInterval(9999999), result -> {
                    if (result.succeeded()) {
                        System.out.println("**** Clustered Vert.x started up successfully! ****");
                        this.vertx = result.result();
                        clusteredStart.countDown();
                    } else {
                        throw new RuntimeException(result.cause());
                    }
                });
                clusteredStart.await();
            } else {
                vertx = Vertx.vertx(new VertxOptions().setBlockedThreadCheckInterval(9999999));
            }
            echoServer.start();

            startLatch = new CountDownLatch(1);

            DeploymentOptions options = new DeploymentOptions();
            options.setConfig(vertxConf);

            vertx.deployVerticle(InitVerticle.class.getCanonicalName(),
                    options, result -> {
                        if (result.succeeded()) {
                            System.out.println("*** Started Non-clustered Vert.x successfully ***");
                        } else {
                            throw new RuntimeException("InitVerticle deployment failed", result.cause());
                        }
                        startLatch.countDown();
                    });

            startLatch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            stopLatch = new CountDownLatch(1);
            echoServer.stop();

            vertx.close(result -> {
                if (result.succeeded()) {
                    System.out.println("**** Shut Vert.x down successfully! ****");
                } else {
                  throw new RuntimeException(result.cause());
                }
                stopLatch.countDown();
            });

            stopLatch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            resetter.reset(); // Also reset at end to avoid leaving pollution in index.
        }
    }

    protected Resetter getResetter(String name) {
        @SuppressWarnings("unchecked")
        Class<Resetter> c = (Class<Resetter>) ReflectionUtils.loadClass(name);
        VertxEngineConfig vxEngineConf = new VertxEngineConfig(vertxConf);

        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                  | SecurityException e) {
            try {
                return c.getConstructor(VertxEngineConfig.class).newInstance(vxEngineConf);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | SecurityException | InvocationTargetException | NoSuchMethodException f) {
                throw new RuntimeException(f);
            }
        }
    }

    @Override
    public void next(String endpoint) {
    }
}
