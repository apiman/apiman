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

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.logging.Logger;

/**
 * A worker which will register itself with queue at a given endpoint
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class WorkerHelper {
    private String endpoint;
    private String uuid;
    private EventBus eb;

    public WorkerHelper(String endpoint, String uuid, EventBus eb, Logger logger) {
        this.endpoint = endpoint;
        this.uuid = uuid;
        this.eb = eb;
    }

    public String getUuid() {
        return uuid;
    }

    /**
     * Register with a gateway (TODO consider how we handle registration with
     * many different gw types).
     */
    public void register(AsyncResultHandler<Message<String>> handler) {
        eb.sendWithTimeout(endpoint, uuid, eb.getDefaultReplyTimeout(), handler);
    }
}
