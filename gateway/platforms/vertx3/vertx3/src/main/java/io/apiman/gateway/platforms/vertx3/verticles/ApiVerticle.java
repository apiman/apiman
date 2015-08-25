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
package io.apiman.gateway.platforms.vertx3.verticles;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import io.apiman.gateway.platforms.vertx3.api.ApplicationResourceImpl;
import io.apiman.gateway.platforms.vertx3.api.IRouteBuilder;
import io.apiman.gateway.platforms.vertx3.api.ServiceResourceImpl;
import io.apiman.gateway.platforms.vertx3.api.SystemResourceImpl;
import io.apiman.gateway.platforms.vertx3.common.verticles.VerticleType;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;

/**
 * API verticle provides the Gateway API RESTful service. Config is validated and pushed into the registry
 * component; hence, if a distributed component such as ElasticSearch is used, this is shared across all nodes
 * extending {@link ApimanVerticleWithEngine}.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class ApiVerticle extends ApimanVerticleWithEngine {
    public static final VerticleType VERTICLE_TYPE = VerticleType.API;

    @Override
    public void start() {
        super.start();
        // This will be refactored shortly to factory pattern. This is just a quick testing bodge..
        IRouteBuilder applicationResource = new ApplicationResourceImpl(apimanConfig, engine);
        IRouteBuilder serviceResource = new ServiceResourceImpl(apimanConfig, engine);
        IRouteBuilder systemResource = new SystemResourceImpl(apimanConfig, engine);

        Router router = Router.router(vertx);

        if (apimanConfig.isAuthenticationEnabled()) {
            AuthHandler basicAuthHandler = BasicAuthHandler.create(this::authenticateBasic, apimanConfig.getRealm());
            router.route("/*").handler(basicAuthHandler);
        }

        applicationResource.buildRoutes(router);
        serviceResource.buildRoutes(router);
        systemResource.buildRoutes(router);

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(apimanConfig.getPort(VERTICLE_TYPE));
    }

    @Override
    public VerticleType verticleType() {
        return VERTICLE_TYPE;
    }

    public void authenticateBasic(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
        String username = authInfo.getString("username");
        String password = Base64.encodeBase64String(DigestUtils.sha256(authInfo.getString("password")));
        String storedPassword = apimanConfig.getBasicAuthCredentials().get(username);

        if (storedPassword != null && password.equals(storedPassword)) {
            resultHandler.handle(Future.<User>succeededFuture(null));
        } else {
            resultHandler.handle(Future.<User>failedFuture("Not such user, or password is incorrect."));
        }
    }
}
