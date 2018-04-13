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
import io.apiman.gateway.engine.beans.EngineErrorResponse;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.test.common.mock.EchoResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerOptionsConverter;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JdkSSLEngineOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.streams.WriteStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * <p>
 * Vert.x edition of Echo servlet, with view to being more amenable
 * to performance testing.
 * </p>
 * <p>
 * Can be run directly with: <tt>vertx run EchoServerVertx.java</tt>
 * </p>
 * <p>
 * To set port, use the property -Dio.apiman.test.common.echo.port=1234
 * </p>
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class EchoServerVertx extends AbstractVerticle {
    private Logger log = LoggerFactory.getLogger(EchoServerVertx.class);

    private static ObjectMapper jsonMapper = new ObjectMapper();
    static {
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private static JAXBContext jaxbContext;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(EngineErrorResponse.class, EchoResponse.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private long counter = 0L;
    private int toStart = 2;

    @Override
    public void start(Future<Void> startFuture)  {
        int port = NumberUtils.toInt(System.getProperty("io.apiman.test.common.echo.port"), 9998);
        HttpServerOptions httpServerOptions = getHttpServerOptions();
        HttpServerOptions httpsServerOptions = getHttpsServerOptions()
                .setSsl(true)
                .setKeyStoreOptions(getKeystore())
                .setTrustStoreOptions(getTrustStore());

        // Plain HTTP server
        vertx.createHttpServer(httpServerOptions)
            .requestHandler(new EchoHandler())
            .listen(port, result -> {
                if (result.succeeded()) {
                    checkSuccess(startFuture, result);
                } else {
                    startFuture.fail(result.cause());
                }
            });
        // HTTPS server
        vertx.createHttpServer(httpsServerOptions)
            .requestHandler(new EchoHandler())
            .listen(port+1, result -> {
                if (result.succeeded()) {
                    checkSuccess(startFuture, result);
                } else {
                    startFuture.fail(result.cause());
                }
            });

        log.info("*** Starting EchoServerVertx on HTTP: {0} HTTPS: {1}", port, port+1);
    }

    private void checkSuccess(Future<Void> startFuture, AsyncResult<HttpServer> result) {
        toStart--;
        if (toStart == 0) startFuture.complete();
    }

    private HttpServerOptions getHttpServerOptions() {
        return getHttpServerOptions("http");
    }

    private HttpServerOptions getHttpsServerOptions() {
        return getHttpServerOptions("https");
    }

    private HttpServerOptions getHttpServerOptions(String name) {
        HttpServerOptions options = new HttpServerOptions();
        HttpServerOptionsConverter.fromJson(config().getJsonObject(name, new JsonObject()), options);
        if (JdkSSLEngineOptions.isAlpnAvailable()) {
            options.setUseAlpn(true);
        }
        return options;
    }

    // Writes buffered chunks directly to the response and then calls #end.
    private static void writeXmlAndEnd(HttpServerResponse rep, EchoResponse echo) {
        try (BufferOutputStream bufferOutputStream = new BufferOutputStream(500, rep)) {
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal(echo, bufferOutputStream);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // Writes buffered chunks directly to the response and then calls #end.
    private static void writeJsonAndEnd(HttpServerResponse rep, EchoResponse echo) {
        try (BufferOutputStream bufferOutputStream = new BufferOutputStream(500, rep)) {
            jsonMapper.writeValue(bufferOutputStream, echo);
        } catch (Exception e) {
            e.printStackTrace();
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
                req.exceptionHandler(ex -> {
                    handleError(req.response(), ex);
                });
                _handle(req);
            } catch (Exception ex) {
                handleError(req.response(), ex);
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
                rep.setChunked(true);
                if (isXml) { // XML
                    rep.putHeader("Content-Type", "application/xml");
                    writeXmlAndEnd(rep, echo);
                } else { // JSON
                    rep.putHeader("Content-Type", "application/json");
                    writeJsonAndEnd(rep, echo);
                }
            });
        }

        private void handleError(HttpServerResponse rep, Throwable e) {
            log.error(e);
            if (!rep.ended()) {
                rep.setStatusCode(500);
                rep.end();
            }
        }

        private HeaderMap multimapToMap(MultiMap headers) {
            HeaderMap map = new HeaderMap();
            headers.forEach(pair -> map.add(pair.getKey(), pair.getValue()));
            return map;
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

    private static final class BufferOutputStream extends java.io.OutputStream {
        private Buffer vxBuffer;
        private int sizeHint;
        private WriteStream<Buffer> writeStream;
        private boolean ended = false;

        public BufferOutputStream(int sizeHint, WriteStream<Buffer> writeStream) {
            this.sizeHint = sizeHint;
            this.writeStream = writeStream;
            vxBuffer = Buffer.buffer(sizeHint);
        }

        @Override
        public void write(int b) throws IOException {
            checkFlush(1);
            vxBuffer.appendByte((byte) b);
        }

        @Override
        public void write(byte b[]) throws IOException {
            checkFlush(b.length);
            vxBuffer.appendBytes(b);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            checkFlush(len);
            vxBuffer.appendBytes(b, off, len);
        }

        private void checkFlush(int len) {
            if (vxBuffer.length() + len >= sizeHint) {
                flush();
            }
        }

        @Override
        public void flush() {
            writeStream.write(vxBuffer);
            vxBuffer.getByteBuf().clear();
        }

        @Override
        public void close() {
            if (!ended) {
                if (vxBuffer.length() > 0) {
                    writeStream.end(vxBuffer);
                } else {
                    writeStream.end();
                }
                ended = true;
            }
        }
    }

    private JksOptions getKeystore() {
        return getJksOptions("keystore", "jks/keystore.jks");
    }

    private JksOptions getTrustStore() {
        return getJksOptions("trustStore", "jks/truststore.ts");
    }

    private JksOptions getJksOptions(String key, String defaultResource) {
        JsonObject config = config()
                .getJsonObject(key, new JsonObject());
        JksOptions jksOptions = new JksOptions()
                .setPassword(config.getString("password", "secret"))
                .setValue(getResource(config.getString("resourceName", defaultResource)));
        return jksOptions;
    }

    private Buffer getResource(String fPath) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fPath).getFile());
        Buffer buff;
        try {
            buff = Buffer.buffer(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buff;
    }



}
