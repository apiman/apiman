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
package io.apiman.gateway.vertx.engine;

import io.apiman.common.plugin.PluginUtils;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

/**
 * A vertx implementation of the API Gateway's plugin registry.  This version
 * simply extends the default implementation but provides its own (actually
 * asynchronous) downloading
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

    private Vertx vertx;

    /**
     * Constructor.
     * @param vertx
     */
    public VertxPluginRegistry(Vertx vertx) {
        super(getTempPluginsDir(), PluginUtils.getDefaultMavenRepositories());

        this.vertx = vertx;
    }

    /**
     * @see io.apiman.gateway.engine.impl.DefaultPluginRegistry#downloadArtifactTo(java.net.URL, java.io.File, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @SuppressWarnings("nls")
    @Override
    protected void downloadArtifactTo(final URL artifactUrl, final File pluginFile, final IAsyncResultHandler<File> handler) {
        int port = artifactUrl.getPort();
        if (port == -1) {
            port = 80;
        }
        HttpClient client = vertx.createHttpClient().
                setHost(artifactUrl.getHost()).
                setPort(port);
        final HttpClientRequest request = client.request("GET", artifactUrl.getPath(), new Handler<HttpClientResponse>() {
            @Override
            public void handle(HttpClientResponse response) {
                response.exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable error) {
                        handler.handle(AsyncResultImpl.create(error, File.class));
                    }
                });
                response.dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer) {
                        try {
                            Files.write(pluginFile.toPath(), buffer.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        } catch (IOException e) {
                            handler.handle(AsyncResultImpl.create(e, File.class));
                        }
                    }
                });
                response.endHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                        handler.handle(AsyncResultImpl.create(pluginFile));
                    }
                });
            }
        });
        request.exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable error) {
                handler.handle(AsyncResultImpl.create(error, File.class));
            }
        });
        request.end();
    }

}
