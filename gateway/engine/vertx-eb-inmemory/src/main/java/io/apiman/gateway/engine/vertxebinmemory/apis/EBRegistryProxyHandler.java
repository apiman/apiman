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
package io.apiman.gateway.engine.vertxebinmemory.apis;

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * Listens for registry events on the event bus. Ignores self-generated events. These arrive as a simple JSON
 * payload, with a header containing the operation type, action and then a marshalled object containing the
 * corresponding object (e.g. Client, Api, etc).
 *
 * Requests are then routed to the appropriate registry method.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface EBRegistryProxyHandler {

    @SuppressWarnings("nls")
    default void listenProxyHandler() {
        System.out.println("Setting up a listener on " + address());

        vertx().eventBus().consumer(address(), (Message<JsonObject> message) -> {
            String uuid = message.body().getString("uuid");

            System.out.println("UUID == " + uuid + " vs " + uuid());

            if (shouldIgnore(uuid))
                return;

            String type = message.body().getString("type");
            String action = message.body().getString("action");
            String body = message.body().getString("body");

            switch (type) {
                case "client":
                    Client app = Json.decodeValue(body, Client.class);

                    if (action == "register") {
                        registerClient(app);
                    } else if (action == "unregister") {
                        unregisterClient(app);
                    }

                    break;
                case "api":
                    Api api = Json.decodeValue(body, Api.class);

                    if (action == "publish") { //$NON-NLS-1$
                        publishApi(api);
                    } else if (action == "retire") {
                        retireApi(api);
                    }

                    break;
                default:
                    throw new IllegalStateException("Unknown type: " + type);
            }

        });
    }

    // Address to subscribe on
    String address();

    // UUID of registry
    String uuid();
    Vertx vertx();
    void publishApi(Api api);
    void retireApi(Api api);
    void registerClient(Client app);
    void unregisterClient(Client app);

    // If *we* sent the message, we shouldn't also digest it, else we'll end in a cycle.
    default boolean shouldIgnore(String uuid) {
        return uuid() == uuid;
    }
}