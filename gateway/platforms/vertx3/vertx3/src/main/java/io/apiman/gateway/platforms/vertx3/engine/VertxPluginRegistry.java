/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.gateway.platforms.vertx3.engine;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.plugin.PluginUtils;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.platforms.vertx3.common.config.InheritingHttpClientOptions;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.engine.proxy.HttpProxy;
import io.apiman.gateway.platforms.vertx3.engine.proxy.JavaSystemPropertiesProxySettings;
import io.apiman.gateway.platforms.vertx3.i18n.Messages;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;

/**
 * A vertx implementation of the API Gateway's plugin registry. This version simply extends the default
 * implementation but provides its own (actually asynchronous) downloading
 *
 * @author eric.wittmann@redhat.com
 */
public class VertxPluginRegistry extends DefaultPluginRegistry {
    private final IApimanLogger LOG = ApimanLoggerFactory.getLogger(VertxPluginRegistry.class);
    private final Vertx vertx;

    private final JavaSystemPropertiesProxySettings httpProxySettings;
    private final JavaSystemPropertiesProxySettings httpsProxySettings;

    private final InheritingHttpClientOptions defaultHttpClientOptions;

    //private final InheritingHttpClientOptions defaultHttpsClientOptions;

    // Maps host to HttpClient
//    private final LRUMap<String, HttpClient> hostToClientCache = new LRUMap<String, HttpClient>(20) {
//        @Override
//        protected void handleRemovedElem(Entry<String, HttpClient> eldest) {
//            eldest.getValue().close();
//        }
//    };

    /**
     * Constructor.
     *
     * @param vertx the vertx
     * @param vxEngineConfig the engine config
     * @param config the plugin config
     */
    public VertxPluginRegistry(Vertx vertx, VertxEngineConfig vxEngineConfig, Map<String, String> config) {
        super(getTempPluginsDir(), PluginUtils.getDefaultMavenRepositories());
        this.vertx = vertx;
        //Get HTTPS Proxy settings (useful for local dev tests and corporate CI)
        this.httpProxySettings = new JavaSystemPropertiesProxySettings("http", 80);
        this.httpsProxySettings = new JavaSystemPropertiesProxySettings("https", 443);

        defaultHttpClientOptions = new InheritingHttpClientOptions();
        //defaultHttpsClientOptions = new InheritingHttpClientOptions();

        // TODO should we split up HTTP and HTTPS?
        JsonObject httpServerOptionsJson = vxEngineConfig.getPluginRegistryConfigJson()
            .getJsonObject("httpClientOptions", new JsonObject());

        // needs generating
        //InheritingHttpClientOptionsConverter.fromJson(httpServerOptionsJson, defaultHttpClientOptions);
        //InheritingHttpClientOptionsConverter.fromJson(httpServerOptionsJson, defaultHttpsClientOptions);

    }

    @SuppressWarnings("nls")
    private static File getTempPluginsDir() {
        try {
            return Files.createTempDirectory("apiman-gateway-plugins-tmp").toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.gateway.engine.impl.DefaultPluginRegistry#downloadArtifactTo(java.net.URL, java.io.File,
     *      io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    protected void downloadArtifactTo(final URL artifactUrl, final File pluginFile,
            final IAsyncResultHandler<File> handler) {
        int port = artifactUrl.getPort();
        boolean isTls = false;

        // Configure http client options following artifact url
        if (artifactUrl.getProtocol().equalsIgnoreCase("https")) {
            // If port is not defined, set to https default port 443
            if (port == -1) port = 443;
            isTls = true;
        } else {
            // If port is not defined, set to http default port 80
            if (port == -1) port = 80;
        }

        HttpClientOptions options = createVertxClientOptions(artifactUrl, isTls, port);

        LOG.trace("Will attempt to download artifact {0} using options {1} to {2}",
            artifactUrl, options, pluginFile);

        HttpClient client = vertx.createHttpClient(options);

        tryDownloadingArtifact(client, port, artifactUrl);
    }
    
    public void tryDownloadingArtifact(HttpClient client, int port, URL artifactUrl) {
        final HttpClientRequest request = client.get(port, artifactUrl.getHost(), artifactUrl.getPath(),
            (Handler<HttpClientResponse>) response -> {

                response.exceptionHandler((Handler<Throwable>) error -> {
                    handler.handle(AsyncResultImpl.create(error, File.class));
                });

                // Body Handler
                response.bodyHandler((Handler<Buffer>) buffer -> {
                    try {
                        //Response status code for request [x] : y
                        LOG.debug(Messages.format("VertxPluginRegistry.ResponseStatusCode",
                            response.request().absoluteURI(),
                            response.statusCode()));

                        // If status code is bad, do not handle the buffer.
                        if (!(response.statusCode() / 100 == 2)) {
                            LOG.warn("Received a bad status code from remote server");
                            handler.handle(AsyncResultImpl.create(null));
                            return;
                        }

                        Files.write(pluginFile.toPath(), buffer.getBytes(), StandardOpenOption.APPEND,
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE);

                        handler.handle(AsyncResultImpl.create(pluginFile));
                    } catch (IOException e) {
                        handler.handle(AsyncResultImpl.create(e, File.class));
                    }
                });
            });

        request.exceptionHandler((Handler<Throwable>) error -> {
            handler.handle(AsyncResultImpl.create(error, File.class));
        });

        request.end();
    }

    private HttpClientOptions createVertxClientOptions(URL artifactUrl, boolean isTls, int port) {
        JavaSystemPropertiesProxySettings propertyProxySettings;
        HttpClientOptions httpClientOptions = new HttpClientOptions(defaultHttpClientOptions);

        if (isTls) {
            propertyProxySettings = httpsProxySettings;
        } else {
            propertyProxySettings = httpProxySettings;
        }

        if (!propertyProxySettings.isNonProxyHost(artifactUrl.getHost())) {
            HttpProxy proxy = propertyProxySettings.getProxy();

            ProxyOptions proxyOptions = new ProxyOptions();
            proxyOptions.setHost(proxy.getHost());
            proxyOptions.setPort(proxy.getPort());
            proxyOptions.setType(ProxyType.HTTP);

            proxy.getCredentials().ifPresent((credentials) -> {
                proxyOptions.setUsername(credentials.getPrinciple());
                proxyOptions.setPassword(credentials.getPasswordAsString());
            });
        }

        return httpClientOptions;
    }
}
