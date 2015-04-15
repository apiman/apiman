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

import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.apiman.gateway.vertx.io.ISimpleWriteStream;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.impl.Json;
import org.vertx.java.core.logging.Logger;

/**
 * Execute a service
 *
 * @author Marc Savy <msavy@redhat.com>
 * @param <H> the head type
 */
public abstract class AbstractServiceExecutor<H> implements ISimpleWriteStream {

    protected EventBus eb;
    protected Logger logger;
    protected String address;

    protected boolean finished;

    public AbstractServiceExecutor(String address, EventBus eb, Logger logger) {
        this.address = address;
        this.eb = eb;
        this.logger = logger;
        this.finished = false;
    }

    // Send head
    protected void execute(H service) {
        eb.send(address + VertxEngineConfig.API_GATEWAY_HEAD_SUFFIX, Json.encode(service));
    }

    @Override
    public void write(Buffer chunk) {
        eb.send(address + VertxEngineConfig.API_GATEWAY_BODY_SUFFIX, chunk);
    }

    @Override
    public void end() {
        eb.send(address + VertxEngineConfig.API_GATEWAY_END_SUFFIX, (Void) null);
        finished = true;
    }

    /**
     * @return the finished
     */
    public boolean isFinished() {
        return finished;
    }

    public void reset() {
        this.finished = false;
    }
}
