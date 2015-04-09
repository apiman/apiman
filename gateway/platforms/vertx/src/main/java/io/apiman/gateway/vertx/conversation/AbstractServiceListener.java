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
import io.apiman.gateway.vertx.i18n.Messages;
import io.apiman.gateway.vertx.io.IResettable;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.impl.Json;
import org.vertx.java.core.logging.Logger;

/**
 * Listen for something.
 *
 * @author Marc Savy <msavy@redhat.com>
 * @param <E> the class to decode to
 */
public abstract class AbstractServiceListener<E> implements IResettable {

    protected EventBus eb;
    protected Logger logger;
    protected String address;
    protected boolean finished = false;

    private Handler<Void> finishHandler;
    private Handler<E> serviceHandler;
    private Handler<Buffer> bodyHandler;
    private Class<E> klazz;
    private Handler<Throwable> errorHandler;

    public AbstractServiceListener(EventBus eb, Logger logger, String address, Class<E> klazz) {
        this.eb = eb;
        this.logger = logger;
        this.address = address;
        this.klazz = klazz;
    }

    protected void listen() {
        eb.registerHandler(address + VertxEngineConfig.API_GATEWAY_HEAD_SUFFIX, new Handler<Message<String>>() {

            @Override
            public void handle(Message<String> message) {

                E serviceObj = Json.<E>decodeValue(message.body(), klazz);

                if (serviceHandler != null)
                    serviceHandler.handle(serviceObj);
            }
        });

        eb.registerHandler(address + VertxEngineConfig.API_GATEWAY_BODY_SUFFIX, new Handler<Message<Buffer>>() {

            @Override
            public void handle(Message<Buffer> event) {
                if(finished) {
                    throw new IllegalStateException(Messages.getString("AbstractServiceListener.0")); //$NON-NLS-1$
                }

                if (bodyHandler != null)
                    bodyHandler.handle(event.body());
            }
        });

        eb.registerHandler(address + VertxEngineConfig.API_GATEWAY_END_SUFFIX, new Handler<Message<Void>>() {

            @Override
            public void handle(Message<Void> signal) {
                logger.debug("Received finish signal in ServiceListener"); //$NON-NLS-1$
                end();
            }
        });

        eb.registerHandler(address + VertxEngineConfig.API_GATEWAY_ERROR_SUFFIX, new Handler<Message<String>>() {

            @Override
            public void handle(Message<String> message) {
                if (errorHandler != null){
                    errorHandler.handle(Json.<Throwable>decodeValue(message.body(), Throwable.class));
                }
                end();
            }
        });
    }

    protected void end() {
        if (finishHandler != null && !finished){
            finished = true;
            finishHandler.handle((Void) null);
        }
    }

    public void serviceHandler(Handler<E> handler) {
        this.serviceHandler = handler;
    }

    public void bodyHandler(Handler<Buffer> bodyHandler) {
        this.bodyHandler = bodyHandler;
    }

    public void endHandler(Handler<Void> finishHandler) {
        this.finishHandler = finishHandler;
    }

    public void errorHandler(Handler<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * @return the finished
     */
    public boolean isFinished() {
        return finished;
    }
}
