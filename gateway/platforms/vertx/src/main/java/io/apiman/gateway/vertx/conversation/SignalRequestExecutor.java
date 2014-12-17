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

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.apiman.gateway.vertx.io.IReadyExecute;
import io.apiman.gateway.vertx.io.ISimpleWriteStream;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Container;

/**
 * Send a {@link ServiceRequest} over the {@link EventBus}.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class SignalRequestExecutor<H> extends AbstractServiceExecutor<H> implements IReadyExecute<H, ISimpleWriteStream> {

    public SignalRequestExecutor(Vertx vertx, Container container, String address) {
        super(address, vertx.eventBus(), container.logger());
    }

    // Signals when the other end is ready to receive blocks.
    public void execute(final H service, final Handler<ISimpleWriteStream> readyHandler) {
        logger.debug("Listening for ready on: " + address + VertxEngineConfig.APIMAN_RT_READY_SUFFIX);

        eb.registerHandler(address + VertxEngineConfig.APIMAN_RT_READY_SUFFIX, new Handler<Message<Void>>() {

            @Override
            public void handle(Message<Void> signal) {
                //logger.debug("Got the OK signal!");
                readyHandler.handle(SignalRequestExecutor.this);
            }
        });

        super.execute(service);
    }

    public void reset() {
        super.reset();
    }
}
