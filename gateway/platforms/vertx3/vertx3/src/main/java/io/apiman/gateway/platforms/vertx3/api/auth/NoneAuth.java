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

package io.apiman.gateway.platforms.vertx3.api.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;

import java.util.Set;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class NoneAuth implements AuthHandler {

    @Override
    public void handle(RoutingContext context) {
        context.response().setStatusMessage("WARNING: NoneAuth Auth selected. Always returns 401."); //$NON-NLS-1$
        context.fail(401);
    }

    @Override
    public AuthHandler addAuthority(String authority) {
        return this;
    }

    @Override
    public AuthHandler addAuthorities(Set<String> authorities) {
        return this;
    }

    @Override
    public void parseCredentials(RoutingContext routingContext, Handler<AsyncResult<JsonObject>> handler) {

    }

    @Override
    public void authorize(User user, Handler<AsyncResult<Void>> handler) {

    }

    public static AuthHandler create() {
        return new NoneAuth();
    }

}
