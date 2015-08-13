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

import io.apiman.common.plugin.PluginUtils;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.platforms.vertx3.config.VertxEngineConfig;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * A vertx implementation of the API Gateway's plugin registry. This version simply extends the default
 * implementation but provides its own (actually asynchronous) downloading
 *
 * @author eric.wittmann@redhat.com
 */
public class VertxPluginRegistry extends DefaultPluginRegistry {

    @SuppressWarnings("nls")
    private static File getTempPluginsDir() {
        try {
            File tempDir = File.createTempFile("_apiman", "plugins").getParentFile();
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            if (!tempDir.isDirectory()) {
                throw new IOException("Invalid temporary directory: " + tempDir);
            }
            File tempPluginsDir = new File(tempDir, "api-gateway-plugins");
            if (!tempPluginsDir.exists()) {
                tempPluginsDir.mkdirs();
            }
            return tempPluginsDir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpClient client;


    /**
     * Constructor.
     *
     * @param vertx the vertx
     * @param vxEngineConfig the engine config
     * @param config the plugin config
     */
    public VertxPluginRegistry(Vertx vertx, VertxEngineConfig vxEngineConfig, Map<String, String> config) {
        super(getTempPluginsDir(), PluginUtils.getDefaultMavenRepositories());
        this.client = vertx.createHttpClient();
    }

    /**
     * @see io.apiman.gateway.engine.impl.DefaultPluginRegistry#downloadArtifactTo(java.net.URL, java.io.File,
     *      io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    protected void downloadArtifactTo(final URL artifactUrl, final File pluginFile,
            final IAsyncResultHandler<File> handler) {
        int port = artifactUrl.getPort();
        if (port == -1) {
            port = 80;
        }

        final HttpClientRequest request = client.get(port, artifactUrl.getHost(), artifactUrl.getPath(),
                (Handler<HttpClientResponse>) response -> {

            response.exceptionHandler((Handler<Throwable>) error -> {
                handler.handle(AsyncResultImpl.create(error, File.class));
            });

            // Body Handler
            response.handler((Handler<Buffer>) buffer -> {
                try {
                    Files.write(pluginFile.toPath(), buffer.getBytes(), StandardOpenOption.APPEND,
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                } catch (IOException e) {
                    handler.handle(AsyncResultImpl.create(e, File.class));
                }
            });

            response.endHandler((Handler<Void>) event -> {
                handler.handle(AsyncResultImpl.create(pluginFile));
            });
        });

        request.exceptionHandler((Handler<Throwable>) error -> {
            handler.handle(AsyncResultImpl.create(error, File.class));
        });

        request.end();
    }
}
