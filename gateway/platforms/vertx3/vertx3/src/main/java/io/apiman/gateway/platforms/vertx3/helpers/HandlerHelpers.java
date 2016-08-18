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

package io.apiman.gateway.platforms.vertx3.helpers;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface HandlerHelpers {
    static <T> Handler<AsyncResult<T>> translateVoidHandlers(IAsyncResultHandler<Void> apimanResult) {
        return vertxResult -> {
            if (vertxResult.succeeded()) {
                apimanResult.handle(AsyncResultImpl.create((Void) null));
            } else {
                apimanResult.handle(AsyncResultImpl.create(vertxResult.cause()));
            }
        };
    }

    static <T, R> Handler<AsyncResult<T>> translateFailureHandler(IAsyncResultHandler<R> apimanResult) {
        return result -> {
            if (!result.succeeded()) {
                apimanResult.handle(AsyncResultImpl.create(result.cause()));
            }
        };
    }
}
