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

import io.vertx.core.json.JsonObject;

/**
 * A standard head object containing the UUID (given registry's unique identity), the action (e.g. register,
 * retire), the type (e.g. client, api), and the body (real object).
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public interface Head {
    String UUID = "uuid";
    String HEAD =  "head";
    String ACTION = "action";
    String BODY = "body";
    String TYPE = "type";

    String uuid();
    String type();
    String action();
    String body();

    default JsonObject asJson() {
        return new JsonObject().put("type", type()).put("action", action()).put("body", body())
                .put("uuid", uuid());
    }
}
