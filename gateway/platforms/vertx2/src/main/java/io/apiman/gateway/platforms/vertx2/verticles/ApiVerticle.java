package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.platforms.vertx2.api.ApplicationResourceImpl;
import io.apiman.gateway.platforms.vertx2.api.IRouteBuilder;
import io.apiman.gateway.platforms.vertx2.api.ServiceResourceImpl;
import io.apiman.gateway.platforms.vertx2.api.SystemResourceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

@SuppressWarnings("nls")
public class ApiVerticle extends ApimanVerticleBase {

    @Override
    public void start() {
        super.start();

        IRouteBuilder applicationResource = new ApplicationResourceImpl(apimanConfig);
        IRouteBuilder serviceResource = new ServiceResourceImpl(apimanConfig);
        IRouteBuilder systemResource = new SystemResourceImpl(apimanConfig);

        Router router = Router.router(vertx);

        if (apimanConfig.isAuthenticationEnabled()) {
            AuthHandler basicAuthHandler = BasicAuthHandler.create(this::authenticateBasic, apimanConfig.getRealm());
            //router.route().handler(CookieHandler.create());
            //router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
            router.route("/*").handler(basicAuthHandler);
        }

        applicationResource.buildRoutes(router);
        serviceResource.buildRoutes(router);
        systemResource.buildRoutes(router);

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(apimanConfig.getPort(VerticleType.API));
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.API;
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
