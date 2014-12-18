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
package io.apiman.gateway.vertx.conversation;

import io.apiman.gateway.vertx.common.DoubleHandler;
import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.ServiceResponse;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.impl.Json;
import org.vertx.java.platform.Container;

/**
 * Listen for a {@link ServiceResponse}.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class ServiceResponseListener extends AbstractServiceListener<ServiceResponse> {
    protected HttpServerResponse serverResponse;
    private DoubleHandler<PolicyFailure, String> failureHandler;

    public ServiceResponseListener(Vertx vertx, Container container, String address) {
        super(vertx.eventBus(), container.logger(), address, ServiceResponse.class);
    }

    @Override
    public void listen() {
        logger.debug("ServiceResponseListener listening on " + address); //$NON-NLS-1$

        eb.registerHandler(address + VertxEngineConfig.APIMAN_RT_FAILURE_SUFFIX, new Handler<Message<String>>() {

            @Override
            public void handle(Message<String> message) {
                PolicyFailure failure = Json.decodeValue(message.body(), PolicyFailure.class);
                failureHandler.handle(failure, message.body().toString());
            }
        });

        super.listen();
    }

    public void policyFailureHandler(DoubleHandler<PolicyFailure, String> failureHandler) {
        this.failureHandler = failureHandler;
    }

    @Override
    public void reset() {
        finished = false;
    }
}
