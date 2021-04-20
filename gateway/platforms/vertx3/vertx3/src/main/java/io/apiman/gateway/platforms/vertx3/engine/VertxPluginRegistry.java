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
import io.apiman.common.util.Basic;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.engine.vertx.polling.exceptions.BadResponseCodeError;
import io.apiman.gateway.platforms.vertx3.common.config.InheritingHttpClientOptions;
import io.apiman.gateway.platforms.vertx3.common.config.InheritingHttpClientOptionsConverter;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.engine.proxy.HttpProxy;
import io.apiman.gateway.platforms.vertx3.engine.proxy.JavaSystemPropertiesProxySettings;
import io.apiman.gateway.platforms.vertx3.i18n.Messages;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
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

    private final Map<String, JavaSystemPropertiesProxySettings> proxySettingsMap = new HashMap<>();

    //private final JavaSystemPropertiesProxySettings httpProxySysPropSettings;
    //private final JavaSystemPropertiesProxySettings httpsProxySysPropSettings;

    private final InheritingHttpClientOptions defaultHttpClientOptions = new InheritingHttpClientOptions();

    // TODO(msavy): We could add an LRU cache here that's a combination of URL + proxy info to HttpClient?

    /**
     * Constructor.
     *
     * @param vertx the vertx
     * @param vxEngineConfig the engine config
     * @param config the plugin config
     */
    public VertxPluginRegistry(Vertx vertx, VertxEngineConfig vxEngineConfig, Map<String, String> config) {
        super(config);
        this.vertx = vertx;

        // Get HTTPS Proxy settings (useful for local dev tests and corporate CI)
        setSysPropProxySettings("http", 80);
        setSysPropProxySettings("https", 443);

        // TODO(msavy): should we split up HTTP and HTTPS so they can be configured differently?
        // TODO(msavy): we could add a proxy ignore section into the JSON configuration also if that's
        //  desired rather than using the system property alone.
        JsonObject httpServerOptionsJson = vxEngineConfig.getPluginRegistryConfigJson()
            .getJsonObject("httpClientOptions", new JsonObject());

        // Initialise empty HttpClient options, then we populate it with the converter.
        // This will let users provide custom overrides in the JSON configuration.
        InheritingHttpClientOptionsConverter.fromJson(httpServerOptionsJson, defaultHttpClientOptions);
    }

    private void setSysPropProxySettings(String protocol, int defaultPort) {
        proxySettingsMap.put(protocol, new JavaSystemPropertiesProxySettings(protocol, defaultPort));
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

        HttpClientOptions options = createVertxClientOptions(artifactUrl, isTls);

        LOG.trace("Will attempt to download artifact {0} using options {1} to {2}",
            artifactUrl, options, pluginFile);

        HttpClient client = vertx.createHttpClient(options);

        CircuitBreaker breaker = CircuitBreaker.create(
            "download-plugin-circuit-breaker", vertx,
            new CircuitBreakerOptions()
                .setMaxFailures(2) // number of failure before opening the circuit
                .setTimeout(20000) // consider a failure if the operation does not succeed in time
                .setResetTimeout(10000) // time spent in open state before attempting to re-try
        )
        .retryPolicy(retryCount -> retryCount * 10L); // Increase backoff on each retry

        int finalPort = port;
        breaker.<File>execute(promise -> {
            LOG.info("Will attempt to download plugin from: {0}", artifactUrl);
            tryDownloadingArtifact(client, finalPort, artifactUrl, pluginFile, promise);
        })
        .onSuccess(file -> {
            LOG.debug("Successfully downloaded plugin artifact: {0}", artifactUrl);
            handler.handle(AsyncResultImpl.create(pluginFile));
        })
        .onFailure(failure -> {
            LOG.error("Failed to downloaded plugin artifact", failure);
            handler.handle(AsyncResultImpl.create(failure));
        });
    }
    
    private void tryDownloadingArtifact(HttpClient client, int port, URL artifactUrl, File pluginFile, Promise<File> promise) {
        final HttpClientRequest request = client.get(port, artifactUrl.getHost(), artifactUrl.getPath(),
            (Handler<HttpClientResponse>) response -> {

                response.exceptionHandler((Handler<Throwable>) throwable -> {
                    System.out.println("there was an exception, hmm");
                    promise.fail(throwable);
                });

                // Body Handler. If RAM usage is too high we can change this to write chunks to disk.
                response.bodyHandler((Handler<Buffer>) buffer -> {
                    try {
                        System.out.println("hello");

                        // Response status code for request [x] : y
                        LOG.debug(Messages.format("VertxPluginRegistry.ResponseStatusCode",
                            response.request().absoluteURI(),
                            response.statusCode()));

                        // If status code is bad, do not handle the buffer.
                        if (!(response.statusCode() / 100 == 2)) {
                            LOG.warn("Received a bad status code from remote server");
                            promise.fail(new BadResponseCodeError("Server returned non-200 status code "
                                + response.statusCode()));
                            return;
                        }

                        Files.write(pluginFile.toPath(), buffer.getBytes(), StandardOpenOption.APPEND,
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE);

                        LOG.debug("Successfully wrote artifact to {0} to {1}", artifactUrl, pluginFile);

                        promise.complete(pluginFile);
                    } catch (IOException ioe) {
                        System.out.println("There was an IO Exception " + ioe);
                        ioe.printStackTrace();
                        promise.fail(ioe);
                    }
                });
            });

        // Add the Basic authentication if contained in URL
        if (artifactUrl.getUserInfo() != null) {
            String up = artifactUrl.getUserInfo();
            request.putHeader(
                HttpHeaders.AUTHORIZATION,
                "Basic " + Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.ISO_8859_1))
            );
        }

        request.exceptionHandler((Handler<Throwable>) promise::fail);

        request.end();
    }

    private HttpClientOptions createVertxClientOptions(URL artifactUrl, boolean isTls) {
        JavaSystemPropertiesProxySettings propertyProxySettings;
        HttpClientOptions httpClientOptions = new HttpClientOptions(defaultHttpClientOptions);

        if (isTls) {
            propertyProxySettings = proxySettingsMap.get("https");
            httpClientOptions.setSsl(true);
            System.out.println("Using ssl");
        } else {
            propertyProxySettings = proxySettingsMap.get("http");
        }

        if (propertyProxySettings.getProxy() != null && !propertyProxySettings.isNonProxyHost(artifactUrl.getHost())) {
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
