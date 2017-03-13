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

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.impl.BasicAuthHandlerImpl;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class BasicAuth extends BasicAuthHandlerImpl {

    public BasicAuth(AuthProvider authProvider, String realm) {
        super(authProvider, realm);
    }

    public static AuthHandler create(JsonObject apimanConfig) {
        return new BasicAuth(authenticateBasic(apimanConfig), apimanConfig.getString("realm", "apiman-gateway"));
    }

    private static AuthProvider authenticateBasic(JsonObject apimanConfig) {
        return (authInfo, resultHandler) -> {
            String storedUsername = apimanConfig.getString("username");
            String storedPassword = apimanConfig.getString("password");

            if (storedUsername == null || storedPassword == null) {
                resultHandler.handle(Future.failedFuture("Credentials not set in configuration."));
                return;
            }

            String username = authInfo.getString("username");
            String password = StringUtils.chomp(authInfo.getString("password"));

            if (storedUsername.equals(username) && storedPassword.equals(password)) {
                resultHandler.handle(Future.succeededFuture());
            } else {
                resultHandler.handle(Future.failedFuture("No such user, or password incorrect."));
            }
        };
    }

}
