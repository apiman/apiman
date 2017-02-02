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

package io.apiman.gateway.engine.vertx.polling;

import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.platforms.vertx3.common.verticles.Json;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
@SuppressWarnings("nls")
public class URILoadingRegistry extends InMemoryRegistry {
    // Protected by DCL, use #getUriLoader
    private static volatile OneShotURILoader instance;

    // TODO: Authentication for HTTP(S).
    public URILoadingRegistry(Vertx vertx, IEngineConfig vxConfig, Map<String, String> options) {
        super();
        Arguments.require(options.containsKey("configUri"), "configUri is required in configuration");
        URI uri = URI.create(options.get("configUri"));
        getURILoader(vertx, uri, options).subscribe(this, result -> {
            // For now just throw any exception as it should successfully propagate at this phase
            // In future we should add an initialise method with a result handler (e.g. via interface).
            if (result.isError())
                throw new RuntimeException(result.getError());
        });
    }

    private OneShotURILoader getURILoader(Vertx vertx, URI uri, Map<String, String> options) {
        if (instance == null) {
            synchronized(URILoadingRegistry.class) {
                if (instance == null) {
                    instance = new OneShotURILoader(vertx, uri, options);
                }
            }
        }
        return instance;
    }

    @Override
    public void publishApi(Api api, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerClient(Client client, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    protected void publishApiInternal(Api api, IAsyncResultHandler<Void> handler) {
        super.publishApi(api, handler);
    }

    protected void registerClientInternal(Client client, IAsyncResultHandler<Void> handler) {
        super.registerClient(client, handler);
    }

    private static final class OneShotURILoader {
        private Vertx vertx;
        private Deque<URILoadingRegistry> awaiting = new ArrayDeque<>();
        private List<IAsyncResultHandler<Void>> failureHandlers = new ArrayList<>();
        private URI uri;
        private Buffer rawData;
        private boolean dataProcessed = false;
        private List<Client> clients;
        private List<Api> apis;
        private Map<String, String> options;
        private Logger log = LoggerFactory.getLogger(OneShotURILoader.class);

        public OneShotURILoader(Vertx vertx, URI uri, Map<String, String> options) {
            this.options = options;
            this.vertx = vertx;
            this.uri = uri;
            loadData();
        }

        private void loadData() {
            switch (uri.getScheme().toLowerCase()) {
            case "http":
                fetchHttp(false);
                break;
            case "https":
                fetchHttp(true);
                break;
            case "file":
                fetchFile();
                break;
            default:
                throw new UnsupportedProtocolException(String.format("%s is not supported. Available: http, https and file.", uri.getScheme()));
            }
        }

        private void fetchFile() {
            vertx.fileSystem().readFile(uri.getPath(), result -> {
                if (result.succeeded()) {
                    rawData = result.result();
                    processData();
                } else {
                    failAll(result.cause());
                }
            });
        }

        private void fetchHttp(boolean isHttps) {
            int port = uri.getPort();
            if (port == -1) {
                if (isHttps) {
                    port = 443;
                } else {
                    port = 80;
                }
            }

            vertx.createHttpClient(new HttpClientOptions().setSsl(isHttps))
                .get(port, uri.getHost(), uri.getPath(), clientResponse -> {
                    if (clientResponse.statusCode() / 100 == 2) {
                        clientResponse.handler(data -> {
                            if (rawData == null) {
                                rawData = data;
                            } else {
                                rawData.appendBuffer(data);
                            }
                        })
                        .endHandler(end -> processData())
                        .exceptionHandler(this::failAll);
                    } else {
                        failAll(new BadResponseCodeError("Unexpected response code when trying to retrieve config: "
                                + clientResponse.statusCode()));
                    }
                })
                .exceptionHandler(this::failAll)
                .end();
        }

        private void processData() {
            try {
                JsonObject json = rawData.toJsonObject();
                log.trace("Processing JSON: {0}", json);
                clients = requireJsonArray("clients", json, Client.class);
                apis = requireJsonArray("apis", json, Api.class);
                dataProcessed = true;
                checkQueue();
            } catch (DecodeException e) {
                failAll(e);
            }
        }

        @SuppressWarnings("unchecked")
        private <T, K> List<T> requireJsonArray(String keyName, JsonObject json, Class<K> klazz) {
            // Contains key.
            Arguments.require(json.containsKey(keyName),
                    String.format("Must provide array of %s objects for key '%s'", StringUtils.capitalize(keyName), keyName));
            // Is of type array.
            Arguments.require(json.getValue(keyName) instanceof JsonArray,
                    String.format("'%s' must be a Json array", keyName));
            return Json.decodeValue(json.getJsonArray(keyName).encode(), List.class, klazz);
        }


        public synchronized void subscribe(URILoadingRegistry registry, IAsyncResultHandler<Void> failureHandler) {
            Objects.requireNonNull(registry, "registry must be non-null.");
            Objects.requireNonNull(failureHandler, "failure handler must be non-null.");
            failureHandlers.add(failureHandler);
            awaiting.add(registry);
            vertx.runOnContext(action -> checkQueue());
        }

        private void checkQueue() {
            if (dataProcessed && awaiting.size()>0) {
                loadDataIntoRegistries();
            }
        }

        private void loadDataIntoRegistries() {
            URILoadingRegistry reg = null;
            while ((reg = awaiting.poll()) != null) {
                for (Api api : apis) {
                    reg.publishApiInternal(api, handleAnyFailure());
                    log.debug("Publishing {0}: ", api);
                }
                for (Client client : clients) {
                    reg.registerClientInternal(client, handleAnyFailure());
                    log.debug("Registering {0}: ", client);
                }
            }
        }

        private IAsyncResultHandler<Void> handleAnyFailure() {
            return result -> {
                if (result.isError()) {
                    failAll(result.getError());
                    throw new RuntimeException(result.getError());
                }
            };
        }

        private void failAll(Throwable cause) {
            AsyncResultImpl<Void> failure = AsyncResultImpl.create(cause);
            failureHandlers.stream().forEach(failureHandler -> {
                vertx.runOnContext(run -> failureHandler.handle(failure));
            });
        }
    }

}
