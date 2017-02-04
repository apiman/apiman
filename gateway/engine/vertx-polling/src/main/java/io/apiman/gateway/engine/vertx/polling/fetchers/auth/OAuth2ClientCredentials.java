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
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;

import java.util.Map;
import java.util.Objects;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
@SuppressWarnings("nls")
public class OAuth2ClientCredentials implements Authenticator {

    public OAuth2ClientCredentials() {
    }

    @Override
    public Authenticator validateConfig(Map<String, String> config) {
        Objects.requireNonNull(config.get("clientId"), "must provide clientId");
        Objects.requireNonNull(config.get("clientSecret"), "must provide clientSecret");
        Objects.requireNonNull(config.get("uri"), "must provide OAuth URI 'uri'.");
        return this;
    }

    @Override
    public Authenticator authenticate(Vertx vertx, Map<String, String> config, MultiMap headerMap, AsyncResultHandler<Void> resultHandler) {
        OAuth2ClientOptions credentials = new OAuth2ClientOptions()
                .setClientID(config.get("clientId"))
                .setClientSecret(config.get("clientSecret"))
                .setSite(config.get("uri"));

        OAuth2Auth oauth2 = OAuth2Auth.create(vertx, OAuth2FlowType.CLIENT, credentials);

        oauth2.getToken(new JsonObject(), tokenResult -> {
          if (tokenResult.succeeded()) {
              AccessToken token = tokenResult.result();
              headerMap.set("Authorization", "Bearer " + token.principal().getString("access_token"));
              resultHandler.handle(Future.succeededFuture());
          } else {
              resultHandler.handle(Future.failedFuture(tokenResult.cause()));
          }
        });
        return this;
    }

}
