/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.vertx.worker;

import java.util.UUID;

import io.apiman.gateway.vertx.http.HttpGatewayStreamer;
import io.apiman.gateway.vertx.verticles.PolicyVerticle;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A queue of workers ready to handle the execution of some task(s).
 *
 * ServiceWorkers are expected to register themselves via the {@link EventBus} at the stated endpoint.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class ServiceWorkerQueue extends WorkerQueue<HttpGatewayStreamer> {

    private Container container;
    private Vertx vertx;
    private String stripString;

    public ServiceWorkerQueue(Vertx vertx, Container container, String registrationTopic, String stripString) {
        super(registrationTopic, vertx.eventBus(), container.logger());
        this.stripString = stripString;
        this.vertx = vertx;
        this.container = container;
    }

    @Override
    protected void collectRegistrations() {
        eb.registerHandler(registrationTopic, new Handler<Message<String>>() {

            @Override
            public void handle(Message<String> policyVerticleUuid) {
                logger.debug("New registrant on " + registrationTopic + ": " + policyVerticleUuid.body()); //$NON-NLS-2$
                add(createStreamer(policyVerticleUuid.body()));
            }
        });
    }

    // If a registrant is available, return it, else fire a new one up.
    public void poll(final Handler<HttpGatewayStreamer> workerHandler) {
        HttpGatewayStreamer gatewayStreamer = super.poll();

        if (gatewayStreamer != null) {
            workerHandler.handle(gatewayStreamer);
        } else {
            final String uuid = UUID.randomUUID().toString();
            JsonObject launchConfig = container.config().copy();
            launchConfig.putBoolean("skip_registration", true); //$NON-NLS-1$
            launchConfig.putString("uuid", uuid); //$NON-NLS-1$

            container.deployVerticle(PolicyVerticle.class.getCanonicalName(), launchConfig,
                    new Handler<AsyncResult<String>>() {

                @Override
                public void handle(AsyncResult<String> result) {
                    if (result.succeeded()) {
                        logger.info("Didn't have enough PolicyVerticle, so I deployed a new one! " + uuid);
                        workerHandler.handle(createStreamer(uuid)); // User must return it.
                    } else {
                        throw new RuntimeException(result.cause());
                    }
                }
            });
        }
    }

    private HttpGatewayStreamer createStreamer(String uuid) {
        return new HttpGatewayStreamer(vertx, container, uuid, stripString);
    }

    @Override
    public void add(HttpGatewayStreamer worker) {
        super.add(worker);
    }
}
