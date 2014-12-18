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
package io.apiman.gateway.vertx.api;

import io.apiman.gateway.vertx.i18n.Messages;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

/**
 * A handler that copes with a variety of errors, responding appropriate on the implementor's behalf.
 *
 * @author Marc Savy <msavy@redhat.com>
 *
 * @param <T> Type of message
 */
public abstract class ApiCatchHandler<T> implements Handler<Message<T>> {

    private static final JsonObject OK_STATUS = new JsonObject().putBoolean("status", true); //$NON-NLS-1$

    @Override
    public final void handle(Message<T> message) {
        try {
            handleApi(message);
        } catch(DecodeException e) {
            replyError(message, new GenericError(HttpResponseStatus.UNPROCESSABLE_ENTITY.code(),
                    e.getLocalizedMessage(), e));
        }  catch(Exception e) {
            replyError(message, new GenericError(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                    Messages.getString("ApiCatchHandler.0") + e.getLocalizedMessage(), e)); //$NON-NLS-1$
        }
    }

    protected void replyError(Message<T> message, GenericError genericException) {
        JsonObject error = new JsonObject().
                putBoolean("status", false). //$NON-NLS-1$
                putObject("error", new JsonObject(Json.encode(genericException))); //$NON-NLS-1$

        message.reply(error);
    }

    protected void replyOk(Message<T> message) {
        message.reply(OK_STATUS);
    }

    protected abstract void handleApi(Message<T> message);
}
