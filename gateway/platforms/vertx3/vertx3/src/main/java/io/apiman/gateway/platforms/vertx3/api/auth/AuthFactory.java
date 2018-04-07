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

import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;

import java.util.Arrays;

import org.apache.commons.lang3.EnumUtils;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class AuthFactory {
    public static enum AuthType {
        NONE, BASIC, KEYCLOAK;

        public static AuthType getType(String name) {
            return EnumUtils.getEnum(AuthType.class, name.toUpperCase());
        }

        public static String all() {
            return Arrays.toString(AuthType.values());
        }
    }

    /**
     * Creates an auth handler of the type indicated in the `auth` section of config.
     *
     * @param vertx the vert.x instance
     * @param router the vert.x web router to protect
     * @param apimanConfig the apiman config
     * @return an auth handler
     */
    public static AuthHandler getAuth(Vertx vertx, Router router, VertxEngineConfig apimanConfig) {
        String type = apimanConfig.getAuth().getString("type", "NONE");
        JsonObject authConfig = apimanConfig.getAuth().getJsonObject("config", new JsonObject());

        switch(AuthType.getType(type)) {
        case BASIC:
            return BasicAuth.create(authConfig);
        case NONE:
            return NoneAuth.create();
        case KEYCLOAK:
            return KeycloakOAuthFactory.create(vertx, router, apimanConfig, authConfig);
        default:
            return NoneAuth.create();
        }
    }

}
