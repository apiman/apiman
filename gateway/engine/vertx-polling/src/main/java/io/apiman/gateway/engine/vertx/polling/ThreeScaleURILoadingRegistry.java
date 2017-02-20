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
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.engine.vertx.polling.fetchers.AccessTokenResourceFetcher;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.ProxyConfigRoot;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Service;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.ServicesRoot;
import io.apiman.gateway.platforms.vertx3.common.AsyncInitialize;
import io.apiman.gateway.platforms.vertx3.common.verticles.Json;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
@SuppressWarnings("nls")
public class ThreeScaleURILoadingRegistry extends InMemoryRegistry implements AsyncInitialize {
    // Protected by DCL, use #getUriLoader
    private static volatile OneShotURILoader instance;
    private URI apiUri;
    private Vertx vertx;
    private Map<String, String> options;

    private String requireOpt(String key, String errorMsg) {
        Arguments.require(options.containsKey(key), errorMsg);
        return options.get(key);
    }

    public ThreeScaleURILoadingRegistry(Vertx vertx, IEngineConfig vxConfig, Map<String, String> options) {
        super();
        this.vertx = vertx;
        this.options = options;
        apiUri = URI.create(requireOpt("apiEndpoint", "apiEndpoint is required in configuration"));
    }

    @Override
    public void initialize(IAsyncResultHandler<Void> resultHandler) {
        getURILoader(vertx, apiUri, options).subscribe(this, resultHandler::handle);
    }

    private OneShotURILoader getURILoader(Vertx vertx, URI uri, Map<String, String> options) {
        if (instance == null) {
            synchronized(ThreeScaleURILoadingRegistry.class) {
                if (instance == null) {
                    instance = new OneShotURILoader(vertx, uri, options);
                }
            }
        }
        return instance;
    }

    public static void reloadData(IAsyncHandler<Void> doneHandler) {
        instance.reload(doneHandler);
    }

    public static void reset() {
        synchronized(ThreeScaleURILoadingRegistry.class) {
            instance = null;
        }
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
        private URI apiUri;
        private Map<String, String> config;
        private List<IAsyncResultHandler<Void>> failureHandlers = new ArrayList<>();
        private Deque<ThreeScaleURILoadingRegistry> awaiting = new ArrayDeque<>();
        private List<ThreeScaleURILoadingRegistry> allRegistries = new ArrayList<>(); // TODO for testing, perhaps can get rid of?
        private boolean dataProcessed = false;
        private List<Client> clients = Collections.emptyList();
        private List<Api> apis = Collections.emptyList();
        private Logger log = LoggerFactory.getLogger(OneShotURILoader.class);
        private IAsyncHandler<Void> reloadHandler;


        public OneShotURILoader(Vertx vertx, URI uri, Map<String, String> config) {
            this.config = config;
            this.vertx = vertx;
            this.apiUri = uri;
            fetchResource();
        }

        // Clear all registries and add back to processing queue.
        public synchronized void reload(IAsyncHandler<Void> reloadHandler) {
            this.reloadHandler = reloadHandler;
            awaiting.addAll(allRegistries);
            apis.clear();
            clients.clear();
            failureHandlers.clear();
            allRegistries.stream()
                .map(ThreeScaleURILoadingRegistry::getMap)
                .forEach(Map::clear);
            dataProcessed = false;
            // Load again from scratch.
            fetchResource();
        }

        private void fetchResource() {
            System.out.println("Fetching resources");
            getServicesRoot(servicesRoot -> {
                List<Service> services = servicesRoot.getServices();
                // Get all service IDs
                List<Long> sids = services.stream()
                        .map(service -> service.getService().getId())
                        .collect(Collectors.toList());
                // Get all configs.
                List<Future> futureList = sids.stream()
                    .map(this::getConfig)
                    .collect(Collectors.toList());

                CompositeFuture.all(futureList)
                    .setHandler(result -> {
                        if (result.succeeded()) {
                            processData();
                        } else {
                            failAll(result.cause());
                        }
                    });
            });

        }

        // https://ewittman-admin.3scale.net/admin/api/services/2555417735060/proxy/configs/production/latest.json
        // ?access_token=914e2f81d22b0c1baf62e75250d3daab9bec675318ecb555b8e39f91877ed5a8
        private List<ProxyConfigRoot> configs = new ArrayList<>();

        private Future getConfig(long id) {
            Future future = Future.future();
            String path = String.format("/admin/api/services/%d/proxy/configs/production/latest.json", id);
            new AccessTokenResourceFetcher(vertx, config, joinPath(path))
                .exceptionHandler(future::fail)
                .fetch(buffer -> {
                    if (buffer.length() > 0) {
                        ProxyConfigRoot pc = Json.decodeValue(buffer.toString(), ProxyConfigRoot.class);
                        log.debug("Received Proxy Config: {0}", pc);
                        configs.add(pc);
                    }
                    future.complete();
                });
            return future;
        }

        private void getServicesRoot(Handler<ServicesRoot> resultHandler) {
            new AccessTokenResourceFetcher(vertx, config, joinPath("/admin/api/services.json"))
                .exceptionHandler(this::failAll)
                .fetch(buffer -> {
                    ServicesRoot sr = Json.decodeValue(buffer.toString(), ServicesRoot.class);
                    System.out.println("Received buffer");
                    //log.debug("Received Services: {0}", sr);
                    resultHandler.handle(sr);
                });
        }

        // Bit messy, refactor
        private URI joinPath(String path) {
            try {
                return new URL(apiUri.toURL(), path).toURI();
            } catch (MalformedURLException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        private void processData() {
            if (configs.size() == 0) {
                log.warn("File loaded into registry was empty. No entities created.");
                return;
            }
            try {
                //JsonObject json = new JsonObject(rawData.toString("UTF-8").trim());
                //log.trace("Processing JSON: {0}", json);
                //clients = requireJsonArray("clients", json, Client.class);
                //apis = requireJsonArray("apis", json, Api.class);

                // Naive version initially.
                for (ProxyConfigRoot root : configs) {
                    // Reflects the remote data structure.
                    Content config = root.getProxyConfig().getContent();
                    Api api = new Api();
                    api.setEndpoint(config.getProxy().getEndpoint());
                    api.setPublicAPI(true);

                    log.info("Processing - {0}: ", config);
                    log.info("Creating API - {0}: ", api);

//                    api.setApiId(config.getId());
                    //api.setApiId(config);
                }




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
            // Transform into List<T>.
            return Json.decodeValue(json.getJsonArray(keyName).encode(), List.class, klazz);
        }

        public synchronized void subscribe(ThreeScaleURILoadingRegistry registry, IAsyncResultHandler<Void> failureHandler) {
            Objects.requireNonNull(registry, "registry must be non-null.");
            Objects.requireNonNull(failureHandler, "failure handler must be non-null.");
            failureHandlers.add(failureHandler);
            allRegistries.add(registry);
            awaiting.add(registry);
            vertx.runOnContext(action -> checkQueue());
        }

        private void checkQueue() {
            if (dataProcessed && awaiting.size()>0) {
                loadDataIntoRegistries();
            }
        }

        private void loadDataIntoRegistries() {
            ThreeScaleURILoadingRegistry reg = null;
            while ((reg = awaiting.poll()) != null) {
                log.debug("Loading data into registry {0}:", reg);
                for (Api api : apis) {
                    reg.publishApiInternal(api, handleAnyFailure());
                    log.debug("Publishing: {0} ", api);
                }
                for (Client client : clients) {
                    reg.registerClientInternal(client, handleAnyFailure());
                    log.debug("Registering: {0} ", client);
                }
            }
            if (reloadHandler != null)
                reloadHandler.handle((Void) null);
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

    public static void main(String[] args) {
        Map<String, String> opts = new HashMap<>();
        opts.put("apiEndpoint", "https://ewittman-admin.3scale.net/");
        opts.put("accessToken", "914e2f81d22b0c1baf62e77250d3daab9bec675318ebb555b8e39f91877ed5a8");
        ThreeScaleURILoadingRegistry reg = new ThreeScaleURILoadingRegistry(Vertx.vertx(), null, opts);
        reg.initialize(res -> {
            if (res.isError()) {
                throw new RuntimeException(res.getError());
            }
        });
    }

}
