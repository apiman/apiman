// /*
//  * Copyright 2017 JBoss Inc
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  *      http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */
//
// package io.apiman.gateway.engine.vertx.polling.fetchers;
//
// import io.apiman.gateway.engine.vertx.polling.exceptions.BadResponseCodeError;
// import io.apiman.gateway.engine.vertx.polling.fetchers.auth.AuthType;
// import io.apiman.gateway.engine.vertx.polling.fetchers.auth.Authenticator;
// import io.vertx.core.Handler;
// import io.vertx.core.Vertx;
// import io.vertx.core.buffer.Buffer;
// import io.vertx.core.http.HttpClientOptions;
// import io.vertx.core.http.HttpClientRequest;
// import io.vertx.core.http.HttpMethod;
// import io.vertx.core.impl.Arguments;
//
// import java.net.URI;
// import java.util.Map;
//
// import org.apache.commons.lang3.EnumUtils;
//
// /**
//  * Fetch HTTP and HTTPS resources, with Auth options including BASIC and various OAuth2
//  * permutations.
//  *
//  * <ul>
//  *   <li>auth: auth type, one of {@link AuthType}. Otherwise <tt>None</tt> by default.<li>
//  * </ul>
//  *
//  * Refer to {@link AuthType} for available options, such as BASIC, OAuth2, Keycloak, etc.
//  *
//  * @see AuthType
//  * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
//  */
// @SuppressWarnings("nls")
// public class HttpResourceFetcher implements ResourceFetcher {
//
//     private final URI uri;
//     private final boolean isHttps;
//     private final Vertx vertx;
//     private final Buffer rawData = Buffer.buffer();
//     private Handler<Throwable> exceptionHandler;
//     private final Map<String, String> config;
//     private final Authenticator authenticator;
//
//     public HttpResourceFetcher(Vertx vertx, URI uri, Map<String, String> config, boolean isHttps) {
//         this.vertx = vertx;
//         this.uri = uri;
//         this.isHttps = isHttps;
//         this.config = config;
//
//         String authString = config.getOrDefault("auth", "NONE").toUpperCase();
//         Arguments.require(EnumUtils.isValidEnum(AuthType.class, authString), "auth must be one of: " + AuthType.all());
//         authenticator = AuthType.valueOf(authString).getAuthenticator();
//         authenticator.validateConfig(config);
//     }
//
//     @Override
//     public void fetch(Handler<Buffer> resultHandler) {
//         int port = uri.getPort();
//         if (port == -1) {
//             if (isHttps) {
//                 port = 443;
//             } else {
//                 port = 80;
//             }
//         }
//
//         vertx.createHttpClient(new HttpClientOptions().setSsl(isHttps))
//             .request(HttpMethod.GET, port, uri.getHost(), uri.getPath()), clientResponse -> {
//                 if (clientResponse.statusCode() / 100 == 2) {
//                     clientResponse.handler(data -> {
//                         rawData.appendBuffer(data);
//                     })
//                     .endHandler(end -> resultHandler.handle(rawData))
//                     .exceptionHandler(exceptionHandler);
//                 } else {
//                     exceptionHandler.handle(new BadResponseCodeError("Unexpected response code when trying to retrieve config: " //$NON-NLS-1$
//                             + clientResponse.statusCode()));
//                 }
//             })
//             .exceptionHandler(exceptionHandler);
//
//         authenticator.authenticate(vertx, config, httpClientRequest.headers(), authResult -> {
//             if (authResult.succeeded()) {
//                 // The client request is executed when HttpClientRequest#end is invoked.
//                 httpClientRequest.end();
//             } else {
//                 exceptionHandler.handle(authResult.cause());
//             }
//         });
//     }
//
//     @Override
//     public HttpResourceFetcher exceptionHandler(Handler<Throwable> exceptionHandler) {
//         this.exceptionHandler = exceptionHandler;
//         return this;
//     }
// }
