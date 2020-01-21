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

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.i18n.Messages;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * A vertx implementation of the API Gateway's plugin registry. This version simply extends the default
 * implementation but provides its own (actually asynchronous) downloading
 *
 * @author eric.wittmann@redhat.com
 */
public class VertxPluginRegistry extends DefaultPluginRegistry {

    private Vertx vertx;
    private ProxyOptions sslProxy;
    private String sslNoProxy;
    private ProxyOptions proxy;
    private String noProxy;


    /**
     * Constructor.
     *
     * @param vertx          the vertx
     * @param vxEngineConfig the engine config
     * @param config         the plugin config
     */
    public VertxPluginRegistry(Vertx vertx, VertxEngineConfig vxEngineConfig, Map<String, String> config) {

        super(config);

        this.vertx = vertx;

        //Get HTTPS Proxy settings (useful for local dev tests and corporate CI)
        String sslProxyHost = System.getProperty("https.proxyHost", "none");
        Integer sslProxyPort = Integer.valueOf(System.getProperty("https.proxyPort", "443"));
        sslNoProxy = System.getProperty("https.nonProxyHosts", "none");

        //Set HTTPS proxy if defined
        if (!"none".equalsIgnoreCase(sslProxyHost)) {
            sslProxy = new ProxyOptions();
            sslProxy.setHost(sslProxyHost);
            sslProxy.setPort(sslProxyPort);
            sslProxy.setType(ProxyType.HTTP);
        }

        //Get HTTP Proxy settings (useful for local dev tests and corporate CI)
        String proxyHost = System.getProperty("http.proxyHost", "none");
        Integer proxyPort = Integer.valueOf(System.getProperty("http.proxyPort", "80"));
        noProxy = System.getProperty("http.nonProxyHosts", "none");

        //Set HTTPS proxy if defined
        if (!"none".equalsIgnoreCase(proxyHost)) {
            proxy = new ProxyOptions();
            proxy.setHost(proxyHost);
            proxy.setPort(proxyPort);
            proxy.setType(ProxyType.HTTP);
        }
    }

    /**
     * @see io.apiman.gateway.engine.impl.DefaultPluginRegistry#downloadArtifactTo(java.net.URL, java.io.File,
     * io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    protected void downloadArtifactTo(final URL artifactUrl, final File pluginFile,
                                      final IAsyncResultHandler<File> handler) {

        HttpClient client;

        int port = artifactUrl.getPort();

        //Configure http client options following artifact url
        if (artifactUrl.getProtocol().equals("https")) {
            //If port is not defined, set to https default port 443
            if (port == -1) port = 443;
        } else {
            //If port is not defined, set to http default port 80
            if (port == -1) port = 80;
        }

        //Create HTTP client with suitable options
        client = vertx.createHttpClient(configureHttpClientOptions(artifactUrl));

        //Try download plugin from repo [y]
        System.out.println(Messages.format("VertxPluginRegistry.TryDownloadFromRepo", artifactUrl.getHost()));

        final HttpClientRequest request = client.get(port, artifactUrl.getHost(), artifactUrl.getPath(),
                (Handler<HttpClientResponse>) response -> {

                    response.exceptionHandler((Handler<Throwable>) error -> {
                        handler.handle(AsyncResultImpl.create(error, File.class));
                    });

                    // Body Handler
                    response.bodyHandler((Handler<Buffer>) buffer -> {
                        try {
                            //Response status code for request [x] : y
                            System.out.println(Messages.format("VertxPluginRegistry.ResponseStatusCode", response.request().absoluteURI(), response.statusCode()));

                            // If status code is bad, do not handle the buffer.
                            if (response.statusCode() != 200) {
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

    private HttpClientOptions configureHttpClientOptions(final URL artifactUrl) {
        HttpClientOptions clientOpts = new HttpClientOptions();
        ArrayList<InetAddress> noProxyAddresses = new ArrayList<>();

        //List all local ip addresses
        try {
            noProxyAddresses.addAll(Arrays.asList(InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())));
        } catch (UnknownHostException e) {
            //Non blocking error while trying to get all local network addresses. Proxy will be always applied if exist.
            e.printStackTrace();
        }

        //Add no proxy hosts from proxy settings
        try {
            if (!"none".equalsIgnoreCase(sslNoProxy)) noProxyAddresses.add(InetAddress.getByName(sslNoProxy));
            if (!"none".equalsIgnoreCase(noProxy)) noProxyAddresses.add(InetAddress.getByName(noProxy));
        } catch (UnknownHostException e) {
            //Non blocking error while trying to get no proxy addresses. Proxy will be always applied if configured.
            e.printStackTrace();
        }

        //Configure http client options following artifact url
        if (artifactUrl.getProtocol().equals("https")) {
            //Enable SSL
            clientOpts.setSsl(true);

            //If artifact host is Loopback address or local ip address
            InetAddress artifactHost = null;
            try {
                artifactHost = InetAddress.getByName(artifactUrl.getHost());

                if (artifactHost.isLoopbackAddress() || noProxyAddresses.contains(artifactHost)) {
                    //Reset proxy options (otherwise Vert.X try to use proxy for local connection)
                    clientOpts.setProxyOptions(null);
                } else {
                    //Set SSL proxy options (if exists)
                    if (sslProxy != null) clientOpts.setProxyOptions(sslProxy);
                }
            } catch (UnknownHostException e) {
                //Non blocking error while trying to check artifact host address. Proxy will be applied if exist.
                e.printStackTrace();

                //Set SSL proxy options (if exists)
                if (sslProxy != null) clientOpts.setProxyOptions(sslProxy);
            }
        } else {
            //Disable SSL
            clientOpts.setSsl(false);

            //If artifact host is Loopback address or local ip address
            InetAddress artifactHost = null;
            try {
                artifactHost = InetAddress.getByName(artifactUrl.getHost());

                if (artifactHost.isLoopbackAddress() || noProxyAddresses.contains(artifactHost)) {
                    //Reset proxy options (otherwise Vert.X try to use proxy for local connection)
                    clientOpts.setProxyOptions(null);
                } else {
                    //Set proxy options (if exists)
                    if (proxy != null) clientOpts.setProxyOptions(proxy);
                }
            } catch (UnknownHostException e) {
                //Non blocking error while trying to check artifact host address. Proxy will be applied if exist.
                e.printStackTrace();

                //Set proxy options (if exists)
                if (proxy != null) clientOpts.setProxyOptions(proxy);
            }
        }

        return clientOpts;
    }
}
