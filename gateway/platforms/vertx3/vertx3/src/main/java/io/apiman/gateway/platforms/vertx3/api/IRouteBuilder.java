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
package io.apiman.gateway.platforms.vertx3.api;

import io.apiman.common.util.MediaType;
import io.apiman.common.util.SimpleStringUtils;
import io.apiman.gateway.engine.beans.exceptions.IStatusCode;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Route building helper
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public interface IRouteBuilder {

    void buildRoutes(Router router);

    String getPath();

    static String join(String... args) {
        return ":" + SimpleStringUtils.join("/:", args);
    }

    default String buildPath(String path) {
        return "/" + (path.length() == 0 ? getPath() : getPath() + "/" + path);
    }

    default void error(RoutingContext context, Throwable error) {
        HttpServerResponse response = context.response();

        response.putHeader("X-API-Gateway-Error", "true");

        if (error != null) {
            if (error instanceof IStatusCode) {
                response.setStatusCode(((IStatusCode)error).getStatusCode());
                response.setStatusMessage(error.getMessage());
            } else {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                response.setStatusMessage(error.getMessage() != null ? error.getMessage() : HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
            }

            JsonObject errorResponse = new JsonObject()
                    .put("errorType", error.getClass().getSimpleName())
                    .put("message", error.getMessage());

            response.setChunked(true)
            .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .end(errorResponse.toString(), "UTF-8");
        } else {
            response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            response.setStatusMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
            response.end();
        }
    }

    default <T> void writeBody(RoutingContext context, T object) {
        context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .setChunked(true)
            .setStatusCode(HttpResponseStatus.OK.code())
            .end(Json.encode(object), "UTF-8");
    }

    default void end(RoutingContext context, HttpResponseStatus statusCode) {
        context.response().setStatusCode(statusCode.code()).setStatusMessage(statusCode.reasonPhrase()).end();
    }
}
