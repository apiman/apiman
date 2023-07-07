package io.apiman.gateway.platforms.vertx3.api.auth;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2Response;

import static io.vertx.ext.auth.oauth2.impl.OAuth2API.makeRequest;

/**
 * Simplified factory to create an {@link io.vertx.ext.auth.oauth2.OAuth2Auth} for OpenID Connect.
 * <p>
 * Important modification by msavy: to allow internal/external distinction without freaking out, we have
 * {@code config.getAllowedIssuers().contains(issuerEndpoint)} with user-provided acceptable issuers.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public interface KeycloakDiscovery {

    class MultiSiteOAuth2ClientOptions extends OAuth2ClientOptions {
        private final Set<String> allowedIssuers = new HashSet<>();

        public MultiSiteOAuth2ClientOptions(JsonObject jso) {
            super(jso);
        }

        @Override
        public OAuth2ClientOptions setSite(String site) {
            if (super.getSite() != null) {
                allowedIssuers.remove(super.getSite());
            }
            allowedIssuers.add(site);
            return super.setSite(site);
        }

        public Set<String> getAllowedIssuers() {
            return allowedIssuers;
        }

        public MultiSiteOAuth2ClientOptions addAllowedIssuer(String allowedIssuer) {
            this.allowedIssuers.add(allowedIssuer);
            return this;
        }
    }

    /**
     * Create a OAuth2Auth provider for OpenID Connect Discovery. The discovery will use the given site in the
     * configuration options and attempt to load the well known descriptor.
     *
     * If the discovered config includes a json web key url, it will be also fetched and the JWKs will be loaded
     * into the OAuth provider so tokens can be decoded.
     *
     * @param vertx the vertx instance
     * @param config the initial config, it should contain a site url
     * @param handler the instantiated Oauth2 provider instance handler
     */
    static void discover(final Vertx vertx,
                         final MultiSiteOAuth2ClientOptions config,
                         final Handler<AsyncResult<OAuth2Auth>> handler) {
        if (config.getSite() == null) {
            handler.handle(Future.failedFuture("issuer cannot be null"));
            return;
        }

        final HttpClientRequest request = makeRequest(vertx, config, HttpMethod.GET, config.getSite() + "/.well-known/openid-configuration", res -> {
            if (res.failed()) {
                handler.handle(Future.failedFuture(res.cause()));
                return;
            }

            final OAuth2Response response = res.result();

            if (response.statusCode() !=  200) {
                handler.handle(Future.failedFuture("Bad Response [" + response.statusCode() + "] " + response.body()));
                return;
            }

            if (!response.is("application/json")) {
                handler.handle(Future.failedFuture("Cannot handle Content-Type: " + response.headers().get("Content-Type")));
                return;
            }

            final JsonObject json = response.jsonObject();

            // issuer validation
            if (config.isValidateIssuer()) {
                String issuerEndpoint = json.getString("issuer");
                if (issuerEndpoint != null) {
                    // the provider is letting the user know the issuer endpoint, so we need to validate
                    // as in vertx oauth the issuer (site config) is a url without the trailing slash we
                    // will compare the received endpoint without the final slash is present
                    if (issuerEndpoint.endsWith("/")) {
                        issuerEndpoint = issuerEndpoint.substring(0, issuerEndpoint.length() - 1);
                    }

                    if (!config.getAllowedIssuers().contains(issuerEndpoint)) {
                        handler.handle(Future.failedFuture("issuer validation failed: received [" + issuerEndpoint + "]"));
                        return;
                    }
                }
            }

            config.setAuthorizationPath(json.getString("authorization_endpoint"));
            config.setTokenPath(json.getString("token_endpoint"));
            config.setIntrospectionPath(json.getString("token_introspection_endpoint"));
            config.setLogoutPath(json.getString("end_session_endpoint"));
            config.setRevocationPath(json.getString("revocation_endpoint"));
            config.setUserInfoPath(json.getString("userinfo_endpoint"));
            config.setJwkPath(json.getString("jwks_uri"));

            final OAuth2Auth oidc = OAuth2Auth.create(vertx, config);

            if (config.getJwkPath() != null) {
                oidc.loadJWK(v -> {
                    if (v.failed()) {
                        handler.handle(Future.failedFuture(v.cause()));
                        return;
                    }

                    handler.handle(Future.succeededFuture(oidc));
                });
            } else {
                handler.handle(Future.succeededFuture(oidc));
            }
        });
        // handle errors
        request.exceptionHandler(t -> handler.handle(Future.failedFuture(t)));
        // we accept JSON as it is the expected response encoding
        request.putHeader("Accept", "application/json");
        // trigger
        request.end();
    }
}
