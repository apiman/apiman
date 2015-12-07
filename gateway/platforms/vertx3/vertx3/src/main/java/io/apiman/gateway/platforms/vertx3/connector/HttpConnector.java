/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.platforms.vertx3.connector;

import io.apiman.common.config.options.BasicAuthOptions;
import io.apiman.common.config.options.TLSOptions;
import io.apiman.common.util.Basic;
import io.apiman.gateway.engine.IApiConnection;
import io.apiman.gateway.engine.IApiConnectionResponse;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalReadStream;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.platforms.vertx3.http.HttpClientOptionsFactory;
import io.apiman.gateway.platforms.vertx3.http.HttpApiFactory;
import io.apiman.gateway.platforms.vertx3.i18n.Messages;
import io.apiman.gateway.platforms.vertx3.io.VertxApimanBuffer;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A vert.x-based HTTP connector; implementing both {@link ISignalReadStream} and {@link ISignalWriteStream}.
 *
 * Its {@link ISignalWriteStream} elements are valid immediately and its {@link ISignalReadStream} is sent as
 * an event to the provided {@link #resultHandler} when once it has reached a valid state. Hence, it is safe
 * to return instances immediately after the constructor has returned.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
class HttpConnector implements IApiConnectionResponse, IApiConnection {

    private static final Set<String> SUPPRESSED_HEADERS = new HashSet<>();
    static {
        SUPPRESSED_HEADERS.add("Transfer-Encoding");
        SUPPRESSED_HEADERS.add("Content-Length");
        SUPPRESSED_HEADERS.add("X-API-Key");
        SUPPRESSED_HEADERS.add("Host");
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ApiRequest apiRequest;
    private ApiResponse apiResponse;

    private IAsyncResultHandler<IApiConnectionResponse> resultHandler;
    private IAsyncHandler<IApimanBuffer> bodyHandler;
    private IAsyncHandler<Void> endHandler;
    private ExceptionHandler exceptionHandler;

    private boolean inboundFinished = false;
    private boolean outboundFinished = false;

    private Api api;
    private String apiPath;
    private String apiHost;
    private String destination;
    private int apiPort;
    private boolean isHttps;
    private RequiredAuthType authType;
    private BasicAuthOptions basicOptions;

    private HttpClient client;
    private HttpClientRequest clientRequest;
    private HttpClientResponse clientResponse;

    private URL apiEndpoint;

    /**
     * Construct an {@link HttpConnector} instance. The {@link #resultHandler} must remain exclusive to a
     * given instance.
     *
     * @param vertx a vertx
     * @param api a api
     * @param request a request with fields filled
     * @param authType the required auth type
     * @param tlsOptions the tls options
     * @param resultHandler a handler, called when reading is permitted
     */
    public HttpConnector(Vertx vertx, Api api, ApiRequest request, RequiredAuthType authType,
            TLSOptions tlsOptions, IAsyncResultHandler<IApiConnectionResponse> resultHandler) {
       this.api = api;
       this.apiRequest = request;
       this.authType = authType;
       this.resultHandler = resultHandler;
       this.exceptionHandler = new ExceptionHandler();

       apiEndpoint = parseApiEndpoint(api);

       isHttps = apiEndpoint.getProtocol().equals("https");
       apiHost = apiEndpoint.getHost();
       apiPort = getPort();
       apiPath = apiEndpoint.getPath().isEmpty() || apiEndpoint.getPath().equals("/") ? "" : apiEndpoint.getPath();
       destination = apiRequest.getDestination() == null ? "/" : apiRequest.getDestination();

       HttpClientOptions clientOptions = HttpClientOptionsFactory.parseOptions(tlsOptions, apiEndpoint);
       this.client = vertx.createHttpClient(clientOptions);
       verifyConnection();
       doConnection();
    }

    private int getPort() {
        if (apiEndpoint.getPort() != -1)
            return apiEndpoint.getPort();

        return isHttps ? 443 : 80;
    }

    private void verifyConnection() {
        switch (authType) {
        case BASIC:
            basicOptions = new BasicAuthOptions(api.getEndpointProperties());
            if (!isHttps && basicOptions.isRequireSSL())
                throw new ConnectorException("Endpoint security requested (BASIC auth) but endpoint is not secure (SSL).");
            break;
        case MTLS:
            if (!isHttps)
                throw new ConnectorException("Mutual TLS specified, but endpoint is not HTTPS.");
            break;
        case DEFAULT:
            break;
        }
    }

    private void doConnection() {
        String endpoint = apiPath + destination + queryParams(apiRequest.getQueryParams());
        logger.debug(String.format("Connecting to %s | port: %d verb: %s path: %s", apiHost, apiPort,
                HttpMethod.valueOf(apiRequest.getType()), endpoint));

        clientRequest = client.request(HttpMethod.valueOf(apiRequest.getType()),
                apiPort,
                apiHost,
                endpoint,
                new Handler<HttpClientResponse>() {

            @Override
            public void handle(final HttpClientResponse vxClientResponse) {
                clientResponse = vxClientResponse;

                // Pause until we're given permission to xfer the response.
                vxClientResponse.pause();

                apiResponse = HttpApiFactory.buildResponse(vxClientResponse, SUPPRESSED_HEADERS);

                vxClientResponse.handler((Handler<Buffer>) chunk -> {
                    bodyHandler.handle(new VertxApimanBuffer(chunk));
                });

                vxClientResponse.endHandler((Handler<Void>) v -> {
                    endHandler.handle((Void) null);
                });

                vxClientResponse.exceptionHandler(exceptionHandler);

                // The response is only ever returned when vxClientResponse is valid.
                resultHandler.handle(AsyncResultImpl
                        .create((IApiConnectionResponse) HttpConnector.this));
            }
        });

        clientRequest.exceptionHandler(exceptionHandler);
        clientRequest.setChunked(true);
        clientRequest.headers().addAll(apiRequest.getHeaders());
        addMandatoryRequestHeaders(clientRequest.headers());

        if (authType == RequiredAuthType.BASIC) {
            clientRequest.putHeader("Authorization", Basic.encode(basicOptions.getUsername(), basicOptions.getPassword()));
        }
    }

    private void addMandatoryRequestHeaders(MultiMap headers) {
        String port = apiEndpoint.getPort() == -1 ? "" : ":" + apiEndpoint.getPort();
        headers.add("Host", apiEndpoint.getHost() + port);
    }

    @Override
    public ApiResponse getHead() {
        return apiResponse;
    }

    @Override
    public void transmit() {
        logger.debug("Resuming");
        clientResponse.resume();
    }

    @Override
    public void abort() {
        bodyHandler(null);

        if(clientRequest != null) {
           clientRequest.end();
        }

        if(clientResponse != null) {
            clientResponse.netSocket().close(); //TODO verify
        }
    }

    @Override
    public void bodyHandler(IAsyncHandler<IApimanBuffer> bodyHandler) {
        this.bodyHandler = bodyHandler;
    }

    @Override
    public void endHandler(IAsyncHandler<Void> endHandler) {
        this.endHandler = endHandler;
    }

    @Override
    public void write(IApimanBuffer chunk) {
        if (inboundFinished) {
            throw new IllegalStateException(Messages.getString("HttpConnector.0"));
        }

        if (chunk.getNativeBuffer() instanceof Buffer) {
            clientRequest.write((Buffer) chunk.getNativeBuffer());
        } else {
            throw new IllegalArgumentException(Messages.getString("HttpConnector.1"));
        }
    }

    @Override
    public void end() {
        clientRequest.end();
        inboundFinished = true;
    }

    @Override
    public boolean isFinished() {
        return inboundFinished && outboundFinished;
    }

    /**
     * @see io.apiman.gateway.engine.IApiConnection#isConnected()
     */
    @Override
    public boolean isConnected() {
        return !isFinished();
    }

    private URL parseApiEndpoint(Api api) {
        try {
            return new URL(api.getEndpoint());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String queryParams(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.size() == 0)
            return "";

        StringBuilder sb = new StringBuilder(queryParams.size() * 2 * 10);
        String joiner = "?";

        try {
            for (Entry<String, String> entry : queryParams.entrySet()) {
                sb.append(joiner);
                sb.append(entry.getKey());
                if (entry.getValue() != null) {
                    sb.append("=");
                    sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                joiner = "&";
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private class ExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable error) {
            resultHandler.handle(AsyncResultImpl
                    .<IApiConnectionResponse> create(error));
        }
    }
}
