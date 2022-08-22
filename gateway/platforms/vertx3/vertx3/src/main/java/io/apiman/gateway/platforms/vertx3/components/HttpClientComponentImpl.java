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

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.gateway.engine.components.http.IHttpClientResponse;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.i18n.Messages;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JdkSSLEngineOptions;

/**
 * A Vert.x based implementation of {@link IHttpClientComponent}. Ensure that
 * {@link IHttpClientRequest#end()} is called after writing is finished, or your data may never be sent, and
 * the connection will be left hanging.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HttpClientComponentImpl implements IHttpClientComponent {

    private final HttpClient sslClient;
    private final HttpClient plainClient;
    private final IApimanLogger logger = ApimanLoggerFactory.getLogger(this.getClass());

    public HttpClientComponentImpl(Vertx vertx, VertxEngineConfig engineConfig, Map<String, String> componentConfig) {
        HttpClientOptions sslOptions = new HttpClientOptions()
                .setSsl(true)
                .setVerifyHost(false)
                .setTrustAll(true); // TODO

        if (JdkSSLEngineOptions.isAlpnAvailable()) {
            sslOptions.setUseAlpn(true);
        }

        this.sslClient = vertx.createHttpClient(sslOptions);
        this.plainClient = vertx.createHttpClient(new HttpClientOptions());
    }

    @Override
    public IHttpClientRequest request(String endpoint, HttpMethod method, IAsyncResultHandler<IHttpClientResponse> responseHandler) {

        URI pEndpoint = URI.create(endpoint);
        int port = pEndpoint.getPort();
    	String proto = pEndpoint.getScheme();
    	HttpClient client;
    	String pathAndQuery = getPathAndQuery(pEndpoint);

    	// If protocol provided
    	if (port != -1 || proto != null) {
    		if (port == 443 || "https".equals(proto)) { //$NON-NLS-1$
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

        Future<HttpClientRequest> requestF = client.request(convertMethod(method), port, pEndpoint.getHost(), pathAndQuery);
        return new HttpClientRequestImpl(requestF, responseHandler);
    }

    private String getPathAndQuery(URI pEndpoint) {
        return pEndpoint.getPath() +
                (pEndpoint.getQuery() == null || pEndpoint.getQuery().isEmpty() ? "" : "?" + pEndpoint.getQuery()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static final class HttpClientResponseImpl implements IHttpClientResponse, Handler<HttpClientResponse> {

        private HttpClientResponse response;
        private Buffer body;
		private final IAsyncResultHandler<IHttpClientResponse> responseHandler;
	    private final Logger logger = LoggerFactory.getLogger(this.getClass());

        HttpClientResponseImpl(IAsyncResultHandler<IHttpClientResponse> responseHandler) {
			this.responseHandler = responseHandler;
		}

        @Override
        public void handle(HttpClientResponse response) {
            this.response = response;

            // The interface stipulates accumulating the whole body,
            // And as of 3.2.2 the convenience bodyHandler method doesn't always work reliably for some reason... So a DIY version.
            response.handler((Handler<Buffer>) buff -> {
                if (body == null) {
                	body = Buffer.buffer(buff.length()).appendBuffer(buff);
                } else {
                	body.appendBuffer(buff);
                }
            });

            response.endHandler((Handler<Void>) v -> {
                responseHandler.handle(AsyncResultImpl
                        .<IHttpClientResponse> create(HttpClientResponseImpl.this));
            });

            response.exceptionHandler(exception -> {
            	logger.error("Exception in HttpClientResponseImpl: {0}", exception.getMessage()); //$NON-NLS-1$
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
        private int timeout;
        private HeaderMap headerMap;
        private ArrayList<Buffer> requestBuffer;

        HttpClientRequestImpl(Future<HttpClientRequest> requestF, IAsyncResultHandler<IHttpClientResponse> clientResponseHandler) {
            requestF.onFailure(exception -> {
                logger.error("Exception in HttpClientRequestImpl: {0}", exception); //$NON-NLS-1$
                clientResponseHandler.handle(AsyncResultImpl.create(exception));
            }).onSuccess(request -> {
                this.request = request;
                request.setChunked(true);
                if (timeout > 0) {
                    request.setTimeout(timeout);
                }
                if (headerMap != null) {
                    for (Entry<String, String> pair : headerMap) {
                        addHeader(pair.getKey(), pair.getValue());
                    }
                }
                if (requestBuffer != null) {
                    requestBuffer.forEach(this::write);
                }
                if (finished) {
                    request.end();
                }
                request.response()
                        .onFailure(exception -> clientResponseHandler.handle(AsyncResultImpl.create(exception)))
                        .onSuccess(new HttpClientResponseImpl(clientResponseHandler));
            });
        }

        @Override
        public void setConnectTimeout(int timeout) {
            if (request != null) {
                request.setTimeout(timeout);
            } else {
                this.timeout = timeout;
            }
        }

        @Override
        public void setReadTimeout(int timeout) {
            // Do nothing - there is only a single timeout in the vertx http client impl.
        }

        @Override
        public void addHeader(String headerName, String headerValue) {
            Objects.requireNonNull(headerName, "Header name must not be null");
            if (request != null) {
                request.putHeader(headerName, headerValue);
            } else {
                if (this.headerMap == null) {
                    this.headerMap = new HeaderMap();
                }
                this.headerMap.add(headerName, headerValue);
            }
        }

        @Override
        public void removeHeader(String headerName) {
            Objects.requireNonNull(headerName, "Header name must not be null");
            if (request != null) {
                request.headers().remove(headerName);
            } else {
                if (this.headerMap != null) {
                    this.headerMap.remove(headerName);
                }
            }
        }

        @Override
        public void write(IApimanBuffer buffer) {
        	checkFinished();
            if (request != null) {
        	    request.write(getNativeBuffer(buffer));
            } else {
                if (this.requestBuffer == null) {
                    this.requestBuffer = new ArrayList<>();
                }
                this.requestBuffer.add(getNativeBuffer(buffer));
            }
        }

		@Override
        public void write(byte[] data) {
            checkFinished();
            if (request != null) {
                request.write(Buffer.buffer(data));
            } else {
                if (this.requestBuffer == null) {
                    this.requestBuffer = new ArrayList<>();
                }
                this.requestBuffer.add(Buffer.buffer(data));
            }
        }

        @Override
        public void write(String body, String charsetName) {
            checkFinished();
            if (request != null) {
                request.write(Buffer.buffer(body, charsetName));
            } else {
                if (this.requestBuffer == null) {
                    this.requestBuffer = new ArrayList<>();
                }
                this.requestBuffer.add(Buffer.buffer(body, charsetName));
            }
        }

        private void write(Buffer buff) {
            checkFinished();
            request.write(buff);
        }

        @Override
        public void end() {
            if (request != null) {
                request.end();
            }
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

    private io.vertx.core.http.HttpMethod convertMethod(HttpMethod method) {
        return io.vertx.core.http.HttpMethod.valueOf(method.name());
    }

}
