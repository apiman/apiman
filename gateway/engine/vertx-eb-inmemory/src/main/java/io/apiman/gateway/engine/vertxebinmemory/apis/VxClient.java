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

import io.apiman.gateway.engine.beans.Client;
import io.vertx.core.json.Json;

/**
 * An {@link io.apiman.gateway.engine.beans.Client} with a {@link Head}.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class VxClient implements Head {
    private Client client;
    private String action;
    private String uuid;

    public VxClient(Client app, String action, String uuid) {
        this.client = app;
        this.action = action;
        this.uuid = uuid;
    }

    public Client getClient() {
        return client;
    }

    @Override
    public String type() {
        return "client";
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public String body() {
        return Json.encode(client);
    }

    @Override
    public String uuid() {
        return uuid;
    }
}
