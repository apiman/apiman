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
import io.apiman.gateway.engine.Version;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.engine.vertx.polling.fetchers.AccessTokenResourceFetcher;
import io.apiman.gateway.engine.vertx.polling.fetchers.FileResourceFetcher;
import io.apiman.gateway.engine.vertx.polling.fetchers.HttpResourceFetcher;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * URI loading registry that pulls configuration from a specified 3scale backend, this is
 * mapped to the internal apiman data model and
 * <ul>
 *   <li>accessToken: 3scale access token</li>
 *   <li>apiEndpoint: 3scale API endpoint</li>
 *   <li>environment: which environment (e.g. production, staging)</li>
 *   <li>policyConfigUri: apiman policy config to load as JSON from file
 *   ({@link FileResourceFetcher}) or HTTP/S ({@link HttpResourceFetcher}).
 *   See the corresponding fetcher for additional options.</li>
 * </ul>
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 * @see FileResourceFetcher
 * @see HttpResourceFetcher
 * @see AccessTokenResourceFetcher
 */
@SuppressWarnings("nls")
public class ThreeScaleURILoadingRegistry extends InMemoryRegistry implements AsyncInitialize {
    private static volatile OneShotURILoader instance;
    private Vertx vertx;
    private Map<String, String> options;
    public static final String DEFAULT_NS = "DEFAULT";

    /**
     * @param vertx the vertx instance
     * @param vxConfig the engine config
     * @param options the options
     */
    public ThreeScaleURILoadingRegistry(Vertx vertx, IEngineConfig vxConfig, Map<String, String> options) {
        super();
        this.vertx = vertx;
        this.options = options;
    }

    public ThreeScaleURILoadingRegistry(Map<String, String> options) {
        this(Vertx.vertx(), null, options);
    }

    @Override
    public void initialize(IAsyncResultHandler<Void> resultHandler) {
        getURILoader(vertx, options).subscribe(this, resultHandler::handle);
    }

    private OneShotURILoader getURILoader(Vertx vertx, Map<String, String> options) {
        if (instance == null) {
            synchronized(ThreeScaleURILoadingRegistry.class) {
                if (instance == null) {
                    instance = new OneShotURILoader(vertx, options);
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
        private URI policyConfigUri;
        private Map<String, String> config;
        private List<IAsyncResultHandler<Void>> failureHandlers = new ArrayList<>();
        private Deque<ThreeScaleURILoadingRegistry> awaiting = new ArrayDeque<>();
        private List<ThreeScaleURILoadingRegistry> allRegistries = new ArrayList<>();
        private boolean dataProcessed = false;
        private List<ProxyConfigRoot> configs = new ArrayList<>();
        private List<Client> clients = new ArrayList<>();
        private List<Api> apis = new ArrayList<>();
        private Logger log = LoggerFactory.getLogger(OneShotURILoader.class);
        private IAsyncHandler<Void> reloadHandler;
        private List<Api> policyConfigApis = Collections.emptyList();
        private String environment;

        public OneShotURILoader(Vertx vertx, Map<String, String> config) {
            this.config = config;
            this.vertx = vertx;


            apiUri = URI.create(requireOpt("apiEndpoint", "apiEndpoint is required in configuration"));
            environment = config.getOrDefault("environment", "production");
            if (config.containsKey("policyConfigUri"))
                policyConfigUri = URI.create(config.get("policyConfigUri")); // Can be null.

            fetchResource();
        }

        private String requireOpt(String key, String errorMsg) {
            Arguments.require(config.containsKey(key), errorMsg);
            return config.get(key);
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
            log.debug("Fetching 3scale services...");
            // Fetch list of all services
            getServicesRoot(servicesRoot -> {
                List<Service> services = servicesRoot.getServices();
                // Get all service IDs
                List<Long> sids = services.stream()
                        .map(service -> service.getService().getId())
                        .collect(Collectors.toList());
                // Get all configs for given service IDs.
                @SuppressWarnings("rawtypes")
                List<Future> configFutures = sids.stream()
                    .map(this::getConfig)
                    .collect(Collectors.toList());

                // If policyConfigUri is provided, then load API policy config.
                // NB: THESE ARE API BEANSs WITH *ONLY* POLICY CONFIG!
                if (policyConfigUri != null) {
                    configFutures.add(fetchPolicyConfig());
                }

                CompositeFuture.all(configFutures)
                    .setHandler(result -> {
                        if (result.succeeded()) {
                            processData();
                        } else {
                            failAll(result.cause());
                        }
                    });
           });
        }

        private Future<List<Api>> fetchPolicyConfig() {
            log.debug("Loading policy configuration from {0}...", policyConfigUri);
            Future<List<Api>> apiResultFuture = Future.future();
            new PolicyConfigLoader(vertx, policyConfigUri, config)
                .setApiResultHandler(apis -> {
                    this.policyConfigApis = apis;
                    apiResultFuture.complete();
                })
                .setExceptionHandler(apiResultFuture::fail)
                .load();
            return apiResultFuture;
        }

        @SuppressWarnings("rawtypes")
        private Future getConfig(long id) {
            Future future = Future.future();
            String path = String.format("/admin/api/services/%d/proxy/configs/%s/latest.json", id, environment);
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
                // Naive version initially.
                for (ProxyConfigRoot root : configs) {
                    // Reflects the remote data structure.
                    Content config = root.getProxyConfig().getContent();
                    Api api = new Api();
                    api.setApiId(config.getSystemName());
                    api.setOrganizationId(DEFAULT_NS);
                    api.setEndpoint(config.getProxy().getApiBackend());
                    api.setEndpointContentType("text/json"); // don't think there is an equivalent of this in 3scale
                    api.setEndpointType("rest"); //don't think there is an equivalent of this in 3scale
                    api.setParsePayload(false); // can let user override this?
                    api.setPublicAPI(true); // is there an equivalent of this?
                    api.setVersion(DEFAULT_NS); // don't think this is relevant anymore
                    setPolicies(api, root);

                    log.info("Processing - {0}: ", config);
                    log.info("Creating API - {0}: ", api);
                    apis.add(api);
                }

                dataProcessed = true;
                checkQueue();
            } catch (DecodeException e) {
                failAll(e);
            }
        }

        private void setPolicies(Api api, ProxyConfigRoot config) { // FIXME optimise
            // Add 3scale policy
            Policy pol = new Policy();
            pol.setPolicyImpl(determinePolicyImpl()); // TODO get version? Hmm! Env?
            pol.setPolicyJsonConfig(Json.encode(config));
            api.getApiPolicies().add(pol);
            // Add any policies user specified in remote config.
            policyConfigApis.stream()
                .filter(skeleton -> skeleton.getApiId().equals(api.getApiId()))
                // Apply policies from skeleton to 3scale API.
                .forEach(skeleton -> api.getApiPolicies().addAll(skeleton.getApiPolicies()));
        }

        private String determinePolicyImpl() {
            String version = config.getOrDefault("pluginVersion", Version.get().getVersionString());
            return "plugin:io.apiman.plugins:apiman-plugins-3scale-auth:" +
                    version +
                   ":war/io.apiman.plugins.auth3scale.Auth3Scale";
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
            if (dataProcessed && awaiting.size() > 0) {
                loadDataIntoRegistries();
            }
        }

        private void loadDataIntoRegistries() {
            ThreeScaleURILoadingRegistry reg = null;
            while ((reg = awaiting.poll()) != null) {
                log.debug("Loading data into registry {0}: ", reg);
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

}
