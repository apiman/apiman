/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.gateway.platforms.vertx3.components;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.async.IAsyncFuture;
import io.apiman.gateway.engine.components.IExecuteBlockingComponent;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Map;

/**
 * Vert.x implementation of {@link IExecuteBlockingComponent}.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ExecuteBlockingComponentImpl implements IExecuteBlockingComponent {
    private Vertx vertx;

    public ExecuteBlockingComponentImpl(Vertx vertx, VertxEngineConfig engineConfig, Map<String, String> componentConfig) {
        this.vertx = vertx;
    }

    @Override
    public <T> void executeBlocking(IAsyncHandler<IAsyncFuture<T>> blockingCode, IAsyncResultHandler<T> resultHandler) {
        vertx.<T>executeBlocking(future -> {
            blockingCode.handle(wrapFuture(future));
        }, result -> {
            resultHandler.handle(wrapResult(result));
        });
    }

    private <T> IAsyncResult<T> wrapResult(AsyncResult<T> result) {
        if (result.succeeded()) {
            return AsyncResultImpl.<T>create(result.result());
        } else {
            return AsyncResultImpl.<T>create(result.cause());
        }
    }

    private <T> IAsyncFuture<T> wrapFuture(Future<T> future) {
        return IAsyncFuture.<T>create().setActionHandler(action -> {
            if (action.isSuccess()) {
                future.complete(action.getResult());
            } else {
                future.fail(action.getError());
            }
        });
    }
}
