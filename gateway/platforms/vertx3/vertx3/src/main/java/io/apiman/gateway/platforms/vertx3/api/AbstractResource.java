/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.platforms.vertx3.api;

import io.apiman.gateway.engine.async.IAsyncResultHandler;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class AbstractResource {

    protected void throwError(Throwable error) {
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        } else {
            throw new RuntimeException(error);
        }
    }

    protected <T> IAsyncResultHandler<T> handlerWithResult(AsyncResponse response) {
        return result -> {
            if (result.isSuccess()) {
                response.resume(Response.ok(result.getResult()).build());
            } else {
                throwError(result.getError());
            }
        };
    }

    protected <T> IAsyncResultHandler<T> handlerWithEmptyResult(AsyncResponse response) {
        return result -> {
            if (result.isSuccess()) {
                response.resume(Response.ok().build());
            } else {
                throwError(result.getError());
            }
        };
    }

    protected <T> IAsyncResultHandler<T> handlerWithEmptyResult() {
        return result -> {
            if (result.isSuccess()) {
                // Do nothing
            } else {
                throwError(result.getError());
            }
        };
    }
}
