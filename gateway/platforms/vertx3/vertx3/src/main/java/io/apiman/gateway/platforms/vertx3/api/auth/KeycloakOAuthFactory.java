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

import io.apiman.common.util.Basic;
import io.apiman.gateway.platforms.vertx3.api.auth.KeycloakDiscovery.MultiSiteOAuth2ClientOptions;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.verticles.ApiVerticle;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.auth.oauth2.rbac.KeycloakRBAC;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import org.apache.commons.lang3.EnumUtils;
import org.apache.http.client.utils.URIBuilder;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class KeycloakOAuthFactory {
    private static final Logger log = LoggerFactory.getLogger(KeycloakOAuthFactory.class);

    public static AuthHandler create(Vertx vertx, Router router, VertxEngineConfig apimanConfig, JsonObject authConfig) {
        OAuth2FlowType flowType = toEnum(authConfig.getString("flowType"));
        String role = authConfig.getString("requiredRole");

        Objects.requireNonNull(flowType, String.format("flowType must be specified and valid. Flows: %s.", Arrays.asList(OAuth2FlowType.values())));
        Objects.requireNonNull(role, "requiredRole must be non-null.");

        if (flowType != OAuth2FlowType.AUTH_CODE) {
            return directGrant(vertx, apimanConfig, authConfig, flowType, role);
        } else {
            return standardAuth(vertx, router, apimanConfig, authConfig, flowType);
        }
    }

    private static OAuth2AuthHandler standardAuth(Vertx vertx, Router router, VertxEngineConfig apimanConfig, JsonObject authConfig, OAuth2FlowType flowType)  {
        String proto = apimanConfig.isSSL() ? "https://" : "http://";
        int port = apimanConfig.getPort(ApiVerticle.VERTICLE_TYPE);
        String hostname = Optional.of(apimanConfig.getPublicEndpoint()).orElse(apimanConfig.getHostname());
        String redirect = proto + hostname + ":" + port; // Redirect back here to *after* auth.
        // Set up KC OAuth2 Authentication
        OAuth2AuthHandler auth = OAuth2AuthHandler.create(KeycloakAuth.create(vertx, flowType, authConfig), redirect);
        // Callback can be anything (as long as it's not already used by something else).
        auth.setupCallback(router.get("/callback"));
        return auth;
    }

    private static AuthHandler directGrant(Vertx vertx, VertxEngineConfig apimanConfig, JsonObject authConfig,
            OAuth2FlowType flowType, String role) {
        return new AuthHandler() {

            @Override
            public void handle(RoutingContext context) {
                try {
                    String[] auth = Basic.decodeWithScheme(context.request().getHeader("Authorization"));
                    doBasic2Oauth(context, role, auth[0], auth[1]);
                } catch (RuntimeException e) {
                    handle400(context, e.getMessage());
                }
            }

            private void doBasic2Oauth(RoutingContext context, String role, String username, String password) {
                JsonObject params = new JsonObject()
                        .put("username", username)
                        .put("password", password);

                MultiSiteOAuth2ClientOptions options = buildClientOptions(authConfig);
                KeycloakDiscovery.discover(vertx, options, (AsyncResult<OAuth2Auth> result) -> {
                    if (result.succeeded()) {
                        OAuth2Auth oauth2 = result.result();
                        oauth2.rbacHandler(KeycloakRBAC.create(options));
                        oauth2.authenticate(params, tokenResult -> {
                            if (tokenResult.succeeded()) {
                                log.debug("OAuth2 Keycloak exchange succeeded.");
                                AccessToken token = (AccessToken) tokenResult.result();
                                token.isAuthorised(role, res -> {
                                    if (res.result()) {
                                        context.next();
                                    } else {
                                        String message = MessageFormat.format("User {0} does not have required role: {1}.", username, role);
                                        log.error(message);
                                        handle403(context, "insufficient_scope", message);
                                    }
                                });
                            } else {
                                String message = tokenResult.cause().getMessage();
                                log.error("Access Token Error: {0}.", message);
                                handle401(context, "invalid_token", message);
                            }
                        });
                    } else {
                        log.error("Failure during discovery", result.cause());
                        handle400(context, result.cause().getMessage());
                    }
                });
            }

            private void handle400(RoutingContext context, String message) {
                if (message != null) context.response().setStatusMessage(message);
                context.fail(400);
            }

            private void handle401(RoutingContext context, String error, String message) {
                String value = MessageFormat.format("Basic realm=\"{0}\" error=\"{1}\" error_message=\"{2}\"", "apiman-gw", error, message);
                context.response().putHeader("WWW-Authenticate", value);
                context.fail(401);
            }

            private void handle403(RoutingContext context, String error, String message) {
                String value = MessageFormat.format("Basic realm=\"{0}\" error=\"{1}\" error_message=\"{2}\"", "apiman-gw", error, message);
                context.response().putHeader("WWW-Authenticate", value);
                context.fail(403);
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
            public void parseCredentials(RoutingContext routingContext, Handler<AsyncResult<JsonObject>> handler) {}

            @Override
            public void authorize(User user, Handler<AsyncResult<Void>> handler) {}
        };
    }

    private static OAuth2FlowType toEnum(String flowType) {
        return EnumUtils.getEnum(OAuth2FlowType.class, flowType.toUpperCase());
    }

    private static MultiSiteOAuth2ClientOptions buildClientOptions(JsonObject config) {
        MultiSiteOAuth2ClientOptions options = new MultiSiteOAuth2ClientOptions();
        String authServer = config.getString("auth-server-url");
        String authServerPublic = config.getString("auth-server-public-url");
        String realmName = config.getString("realm");
        String clientId = config.getString("resource");

        Objects.requireNonNull(authServer, "Must provide auth-server-url");
        Objects.requireNonNull(realmName, "Must provide realm");
        Objects.requireNonNull(clientId, "Must provide resource (also known as Client ID)");

        URI uri;
        try {
            uri = new URIBuilder(authServer)
                          .setPathSegments(Stream.concat(new URIBuilder(authServer).getPathSegments().stream(), Stream.of("realms", realmName)).collect(Collectors.toList()))
                          .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        options.setFlow(OAuth2FlowType.PASSWORD);
        options.setSite(uri.toString());
        options.addAllowedIssuer(authServerPublic);
        options.setClientID(clientId);

        if (config.containsKey("credentials") && config.getJsonObject("credentials").containsKey("secret")) {
            options.setClientSecret(config.getJsonObject("credentials").getString("secret"));
        }

        if (config.containsKey("public-client") && config.getBoolean("public-client", false)) {
            options.setUseBasicAuthorizationHeader(true);
        }

        // HTTP Client Options
        if (config.containsKey("ssl-required")) {
            if (!config.getString("ssl-required").equalsIgnoreCase("none")) {
                options.setSsl(true);
            }
        }

        if (config.containsKey("allow-any-hostname")) {
            Object value = config.getValue("allow-any-hostname");
            if (value.equals(true) || value.equals("true")) {
                options.setVerifyHost(false);
            }
        }

        if (config.containsKey("disable-trust-manager")) {
            Object value = config.getValue("disable-trust-manager");
            if (value.equals(true) || value.equals("true")) {
                options.setTrustAll(true);
            }
        }

        if (config.containsKey("truststore") && config.containsKey("truststore-password")) {
            options.setTrustStoreOptions(new JksOptions().setPath(config.getString("truststore")).setPassword(
                    config.getString("truststore-password")));
        }

        if (config.containsKey("client-keystore") && config.containsKey("client-keystore-password")) {
            options.setTrustStoreOptions(new JksOptions().setPath(config.getString("client-keystore")).setPassword(
                    config.getString("client-keystore-password")));
        }

        return options;
    }

}
