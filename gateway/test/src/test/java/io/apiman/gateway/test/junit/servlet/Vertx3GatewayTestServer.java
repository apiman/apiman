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
package io.apiman.gateway.test.junit.servlet;

import io.apiman.gateway.platforms.vertx3.verticles.InitVerticle;
import io.apiman.gateway.test.junit.IGatewayTestServer;
import io.apiman.gateway.test.server.EchoServer;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import org.codehaus.jackson.JsonNode;

/**
 * A servlet version of the gateway test server.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class Vertx3GatewayTestServer implements IGatewayTestServer {

    protected static final int API_PORT = 8081;
    protected static final int GW_PORT = 8082;
    protected static final int ECHO_PORT = 7654;

    private EchoServer echoServer = new EchoServer(ECHO_PORT);
    private String conf;
    private CountDownLatch startLatch;
    private CountDownLatch stopLatch;

    /**
     * Constructor.
     */
    public Vertx3GatewayTestServer() {
    }

    /**
     * @see io.apiman.gateway.test.junit.IGatewayTestServer#configure(org.codehaus.jackson.JsonNode)
     */
    @Override
    public void configure(JsonNode config) {
        String fPath = config.get("config").asText();

        try {
            conf = new String(Files.readAllBytes(Paths.get(fPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * @see io.apiman.gateway.test.junit.IGatewayTestServer#getApiEndpoint()
     */
    @Override
    public String getApiEndpoint() {
        return "http://localhost:" + API_PORT;
    }

    /**
     * @see io.apiman.gateway.test.junit.IGatewayTestServer#getGatewayEndpoint()
     */
    @Override
    public String getGatewayEndpoint() {
        return "http://localhost:" + GW_PORT;
    }

    /**
     * @see io.apiman.gateway.test.junit.IGatewayTestServer#getEchoTestEndpoint()
     */
    @Override
    public String getEchoTestEndpoint() {
        return "http://localhost:" + ECHO_PORT;
    }

    /**
     * @see io.apiman.gateway.test.junit.IGatewayTestServer#start()
     */
    @Override
    public void start() {
        try {
            echoServer.start();

            startLatch = new CountDownLatch(1);

            DeploymentOptions options = new DeploymentOptions();
            options.setConfig(new JsonObject(conf));

            Vertx.vertx().deployVerticle(InitVerticle.class.getCanonicalName(),
                    options, new Handler<AsyncResult<String>>() {

                        @Override
                        public void handle(AsyncResult<String> event) {
                            startLatch.countDown();
                        }
                    });

            startLatch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.gateway.test.junit.IGatewayTestServer#stop()
     */
    @Override
    public void stop() {
        try {
            stopLatch = new CountDownLatch(1);
            echoServer.stop();

            Vertx.vertx().close(new Handler<AsyncResult<Void>>() {

                @Override
                public void handle(AsyncResult<Void> event) {
                    stopLatch.countDown();
                }
            });

            stopLatch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
