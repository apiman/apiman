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

package io.apiman.test.common.echo;

import io.apiman.common.util.SimpleStringUtils;
import io.apiman.test.common.mock.EchoResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Vert.x edition of Echo servlet, with view to being more amenable
 * to performance testing.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class EchoServerVertx extends AbstractVerticle {

    private static ObjectMapper jsonMapper = new ObjectMapper();
    private static ObjectMapper xmlMapper = new XmlMapper();
    static {
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private long counter = 0L;

    @Override
    public void start(Future<Void> startFuture)  {
        int port = NumberUtils.toInt(System.getProperty("io.apiman.test.common.echo.port"), 9999);
        vertx.createHttpServer()
            .requestHandler(new EchoHandler())
            .listen(port, result -> {
                if (result.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(result.cause());
                }
            });
        System.out.println("Starting EchoServerVertx on: " + port);
    }

    private static String asXml(EchoResponse obj) {
        try {
            return xmlMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String asJson(EchoResponse obj) {
        try {
            return jsonMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private final class EchoHandler implements Handler<HttpServerRequest>  {
        private long bodyLength = 0L;
        private MessageDigest sha1;

        public EchoHandler() {
            try {
                sha1 = MessageDigest.getInstance("SHA1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handle(HttpServerRequest req) {
            try {
                _handle(req);
            } catch (Exception e) {
                handleError(req.response(), e);
            }
        }

        private void _handle(HttpServerRequest req) {
            HttpServerResponse rep = req.response();

            if (req.headers().contains("X-Echo-ErrorCode")) {
                // Check if number, if not then set to 400.
                int errorCode = Optional.of(req.getHeader("X-Echo-ErrorCode"))
                        .filter(NumberUtils::isNumber)
                        .map(Integer::valueOf)
                        .orElse(400);
                // Get error message, else set to "" to avoid NPE.
                String statusMsg = Optional.ofNullable(req.getHeader("X-Echo-ErrorMessage"))
                            .orElse("");
                // #end writes and flushes the response.
                rep.setStatusCode(errorCode)
                   .setStatusMessage(statusMsg)
                   .end();
                return;
            }

            // If redirect query param set, do a 302.
            String query = req.query();
            if (query != null && query.startsWith("redirectTo=")) {
                String redirectTo = query.substring(11);
                rep.putHeader("Location", redirectTo)
                    .setStatusCode(302)
                    .end();
                return;
            }

            // Determine if explicitly needs XML (else, use JSON).
            boolean isXml = Optional.of(req.getHeader("Accept"))
                .filter(accept -> accept.contains("application/xml"))
                .map(accept -> !(accept.contains("application/json")))
                .orElse(false);

            // Build response
            EchoResponse echo = new EchoResponse();
            echo.setMethod(req.method().toString());
            echo.setResource(normaliseResource(req));
            echo.setUri(req.path());

            req.handler(body -> {
                sha1.update(body.getBytes());
                bodyLength += body.length();
            }).endHandler(end -> {
                // If any body was present, encode digest as Base64.
                if (bodyLength > 0) {
                    echo.setBodyLength(bodyLength);
                    echo.setBodySha1(Base64.getEncoder().encodeToString(sha1.digest()));
                }
                echo.setCounter(++counter);
                echo.setHeaders(multimapToMap(req.headers()));
                rep.putHeader("Response-Counter", echo.getCounter().toString());
                if (isXml) { // XML
                    rep.putHeader("Content-Type", "application/xml");
                    rep.end(asXml(echo));
                } else { // JSON
                    rep.putHeader("Content-Type", "application/json");
                    rep.end(asJson(echo));
                }
            });
        }

        private void handleError(HttpServerResponse rep, Exception e) {
            e.printStackTrace();
            if (!rep.ended()) {
                rep.setStatusCode(500);
                rep.end();
            }
        }

        // IMPORTANT: This is lossy
        private Map<String, String> multimapToMap(MultiMap headers) {
            LinkedHashMap<String, String> out = new LinkedHashMap<>();
            headers.forEach(pair -> out.put(pair.getKey(), pair.getValue()));
            return out;
        }

        private String normaliseResource(HttpServerRequest req) {
            if (req.query() != null) {
                String[] normalisedQueryString = req.query().split("&");
                Arrays.sort(normalisedQueryString);
                return req.path() + "?" + SimpleStringUtils.join("&", normalisedQueryString);
            } else {
                return req.path();
            }
        }
    }
}
