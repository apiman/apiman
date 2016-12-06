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
package io.apiman.gateway.platforms.vertx3.components;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.gateway.engine.components.http.IHttpClientResponse;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.i18n.Messages;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * A Vert.x based implementation of {@link IHttpClientComponent}. Ensure that
 * {@link IHttpClientRequest#end()} is called after writing is finished, or your data may never be sent, and
 * the connection will be left hanging.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HttpClientComponentImpl implements IHttpClientComponent {

    private HttpClient sslClient;
    private HttpClient plainClient;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public HttpClientComponentImpl(Vertx vertx, VertxEngineConfig engineConfig, Map<String, String> componentConfig) {
        this.sslClient = vertx.createHttpClient(new HttpClientOptions().setSsl(true));
        this.plainClient = vertx.createHttpClient(new HttpClientOptions());
    }

    @Override
    public IHttpClientRequest request(String endpoint, HttpMethod method,
            IAsyncResultHandler<IHttpClientResponse> responseHandler) {

        URL pEndpoint = parseEndpoint(endpoint);
        int port = pEndpoint.getPort();
    	String proto = pEndpoint.getProtocol();
    	HttpClient client;

    	// If protocol provided
    	if (port != -1 || proto != null) {
    		if (port == 443 || proto.charAt(proto.length()-1) == 's') {
        		client = sslClient;
        		port = (port == -1) ? 443 : port;
    		} else {
        		client = plainClient;
        		port = (port == -1) ? 80 : port;
    		}
    	} else {
        	client = plainClient;
        	port = 80;
        }

        HttpClientRequest request = client.request(convertMethod(method),
                pEndpoint.getPort(),
                pEndpoint.getHost(),
                pEndpoint.getFile(),
                new HttpClientResponseImpl(responseHandler));

        request.setChunked(true);

        request.exceptionHandler(exception -> {
        	logger.error(exception);
        	responseHandler.handle(AsyncResultImpl.create(exception));
        });

        return new HttpClientRequestImpl(request);
    }

    private static final class HttpClientResponseImpl implements IHttpClientResponse, Handler<HttpClientResponse> {

        private HttpClientResponse response;
        private Buffer body;
		private IAsyncResultHandler<IHttpClientResponse> responseHandler;

        public HttpClientResponseImpl(IAsyncResultHandler<IHttpClientResponse> responseHandler) {
			this.responseHandler = responseHandler;
		}

        @Override
        public void handle(HttpClientResponse response) {
            this.response = response;

            // The interface stipulates accumulating the whole body,
            // And as of 3.2.2 the convenience bodyHandler method doesn't always work reliably for some reason... So a DIY version.
            response.handler((Handler<Buffer>) buff -> {
                if (body == null) {
                	body = buff;
                } else {
                	body.appendBuffer(buff);
                }
            });

            response.endHandler((Handler<Void>) v -> {
                responseHandler.handle(AsyncResultImpl
                        .<IHttpClientResponse> create(HttpClientResponseImpl.this));
            });

            response.exceptionHandler(exception -> {
            	exception.printStackTrace();
            	responseHandler.handle(AsyncResultImpl.create(exception));
            });
        }

        @Override
        public int getResponseCode() {
            return response.statusCode();
        }

        @Override
        public String getResponseMessage() {
            return response.statusMessage();
        }

        @Override
        public String getHeader(String headerName) {
            return response.headers().get(headerName);
        }

        @Override
        public String getBody() {
        	return (body == null) ? null : body.toString();
        }

        @Override
        // Doesn't make sense in async world.
        public void close() {
        }

    }

    class HttpClientRequestImpl implements IHttpClientRequest {

        private boolean finished = false;
        private HttpClientRequest request;

        public HttpClientRequestImpl(HttpClientRequest request) {
            this.request = request;
        }

        @Override
        public void setConnectTimeout(int timeout) {
            this.request.setTimeout(timeout);
        }

        @Override
        public void setReadTimeout(int timeout) {
            // Do nothing - there is only a single timeout in the vertx http client impl.
        }

        @Override
        public void addHeader(String headerName, String headerValue) {
            request.putHeader(headerName, headerValue);
        }

        @Override
        public void removeHeader(String headerName) {
            request.headers().remove(headerName);
        }

        @Override
        public void write(IApimanBuffer buffer) {
        	checkFinished();
        	request.write(getNativeBuffer(buffer));
        }

		@Override
        public void write(byte[] data) {
            checkFinished();
            request.write(Buffer.buffer(data));
        }

        @Override
        public void write(String body, String charsetName) {
            checkFinished();
            request.write(Buffer.buffer(body, charsetName));
        }

        @Override
        public void end() {
            request.end();
            finished = true;
        }

        private void checkFinished() {
        	if (finished) {
                throw new IllegalStateException(Messages.getString("HttpClientComponentImpl.0")); //$NON-NLS-1$
            }
    	}

		private Buffer getNativeBuffer(IApimanBuffer buffer) {
			if (buffer.getNativeBuffer() instanceof Buffer) {
				return (Buffer) buffer.getNativeBuffer();
			} else {
				logger.debug("Received an IApimanBuffer with a non-Vert.x implementation. " //$NON-NLS-1$
						+ "This will function but may require copying and be less efficient."); //$NON-NLS-1$
				return Buffer.buffer(buffer.getBytes());
			}
		}
    }

    private URL parseEndpoint(String endpoint) {
        try {
            return new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private io.vertx.core.http.HttpMethod convertMethod(HttpMethod method) {
    	switch(method) {
		case DELETE:
			return io.vertx.core.http.HttpMethod.DELETE;
		case GET:
			return io.vertx.core.http.HttpMethod.GET;
		case HEAD:
			return io.vertx.core.http.HttpMethod.HEAD;
		case OPTIONS:
			return io.vertx.core.http.HttpMethod.OPTIONS;
		case POST:
			return io.vertx.core.http.HttpMethod.POST;
		case PUT:
			return io.vertx.core.http.HttpMethod.PUT;
		case TRACE:
			return io.vertx.core.http.HttpMethod.TRACE;
		default:
	    	return io.vertx.core.http.HttpMethod.valueOf(method.toString());
    	}
    }

}
