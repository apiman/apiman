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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.apiman.common.config.options.TLSOptions;
import io.apiman.gateway.engine.IApiConnector;
import io.apiman.gateway.engine.IConnectorConfig;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.platforms.vertx3.http.HttpClientOptionsFactory;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Create Vert.x connectors to the enable apiman to connect to a backend API.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ConnectorFactory implements IConnectorFactory {

    private static final Set<String> SUPPRESSED_HEADERS = new HashSet<>();
    static {
        SUPPRESSED_HEADERS.add("Transfer-Encoding"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("X-API-Key"); //$NON-NLS-1$
    }

    private Vertx vertx;
    private TLSOptions tlsOptions;
    private Map<String, String> config;
    private LoadingCache<ApimanHttpConnectorOptions, HttpClient> clientCache = CacheBuilder.newBuilder()
                // TODO make this tuneable.
                .maximumSize(2000)
                // Close any evicted connections.
                .<ApimanHttpConnectorOptions, HttpClient>removalListener(eviction -> eviction.getValue().close())
                // Either grab from cache or build new (which will be cached automatically).
                .build(new CacheLoader<ApimanHttpConnectorOptions, HttpClient>() {

                    @Override
                    public HttpClient load(ApimanHttpConnectorOptions opts) throws Exception {
                        HttpClientOptions vxClientOptions = HttpClientOptionsFactory.parseTlsOptions(opts.getTlsOptions(), opts.getUri())
                                .setConnectTimeout(opts.getConnectionTimeout())
                                .setIdleTimeout(opts.getIdleTimeout())
                                .setKeepAlive(opts.isKeepAlive())
                                .setTryUseCompression(opts.isTryUseCompression());
                        return vertx.createHttpClient(vxClientOptions);
                    }
                });

    /**
     * Constructor
     * @param vertx a vertx instance
     * @param config the config
     */
    public ConnectorFactory(Vertx vertx, Map<String, String> config) {
        this.vertx = vertx;
        this.config = config;
        this.tlsOptions = new TLSOptions(config);
    }

    // In the future we can switch to different back-end implementations here!
    @Override
    public IApiConnector createConnector(ApiRequest req, Api api, RequiredAuthType authType, boolean hasDataPolicy, IConnectorConfig connectorConfig) {
        return (request, resultHandler) -> {
            // Apply options from config as our base case
            ApimanHttpConnectorOptions httpOptions = new ApimanHttpConnectorOptions(config)
                    .setHasDataPolicy(hasDataPolicy)
                    .setRequiredAuthType(authType)
                    .setTlsOptions(tlsOptions)
                    .setUri(parseApiEndpoint(api))
                    .setSsl(api.getEndpoint().toLowerCase().startsWith("https")); //$NON-NLS-1$
            // If API has endpoint properties indicating timeouts, then override config.
            setAttributesFromApiEndpointProperties(api, httpOptions);
            // Get from cache
            HttpClient client = clientFromCache(httpOptions);
            return new HttpConnector(vertx, client, request, api, httpOptions, connectorConfig, resultHandler).connect();
         };
    }

    private HttpClient clientFromCache(ApimanHttpConnectorOptions key) {
        try {
            return clientCache.get(key);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private URI parseApiEndpoint(Api api) {
        try {
            return new URI(api.getEndpoint());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * If the endpoint properties includes a read timeout override, then
     * set it here.
     * @param connection
     */
    private void setAttributesFromApiEndpointProperties(Api api, ApimanHttpConnectorOptions options) {
        try {
            Map<String, String> endpointProperties = api.getEndpointProperties();
            if (endpointProperties.containsKey("timeouts.read")) { //$NON-NLS-1$
                int connectTimeoutMs = Integer.parseInt(endpointProperties.get("timeouts.read")); //$NON-NLS-1$
                options.setRequestTimeout(connectTimeoutMs);
            }
            if (endpointProperties.containsKey("timeouts.connect")) { //$NON-NLS-1$
                int connectTimeoutMs = Integer.parseInt(endpointProperties.get("timeouts.connect")); //$NON-NLS-1$
                options.setConnectionTimeout(connectTimeoutMs);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IConnectorConfig createConnectorConfig(ApiRequest request, Api api) {
        return new VertxConnectorConfig();
    }
}
