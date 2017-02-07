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

package io.apiman.gateway.engine.vertx.polling.fetchers.auth;

import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;

import java.util.Map;
import java.util.Objects;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
@SuppressWarnings("nls")
public class KeycloakOAuth2Client extends AbstractOAuth2Base implements Authenticator {

    private Logger log = LoggerFactory.getLogger(KeycloakOAuth2Client.class);

    public KeycloakOAuth2Client() {
    }

    @Override
    public Authenticator validateConfig(Map<String, String> config) {
        Objects.requireNonNull(config.get("flowType"), "must provide OAuth2 flow type 'flowType'.");
        return this;
    }

    @Override
    public Authenticator authenticate(Vertx vertx, Map<String, String> config, MultiMap headerMap, AsyncResultHandler<Void> resultHandler) {

        OAuth2FlowType flowType = getFlowType(config.get("flowType"));
        JsonObject params = new JsonObject();
        if (config.get("username") != null) {
            params.put("username", config.get("username"));
        }
        if (config.get("password") != null) {
            params.put("password", config.get("password"));
        }

        OAuth2Auth oauth2 = OAuth2Auth.createKeycloak(vertx, flowType, mapToJson(config));

        oauth2.getToken(params, tokenResult -> {
            if (tokenResult.succeeded()) {
                log.debug("OAuth2 Keycloak exchange succeeded.");
                AccessToken token = tokenResult.result();
                headerMap.set("Authorization", "Bearer " + token.principal().getString("access_token"));
                resultHandler.handle(Future.succeededFuture());
            } else {
                log.error("Access Token Error: {0}.", tokenResult.cause().getMessage());
                resultHandler.handle(Future.failedFuture(tokenResult.cause()));
            }
          });
        return this;
    }

}
