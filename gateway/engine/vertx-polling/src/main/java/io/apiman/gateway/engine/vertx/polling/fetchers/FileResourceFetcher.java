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

package io.apiman.gateway.engine.vertx.polling.fetchers;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

import java.net.URI;
import java.util.Map;

/**
 * Fetch a file from the local filesystem.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class FileResourceFetcher implements ResourceFetcher {

    private Vertx vertx;
    private URI uri;
    private Handler<Throwable> exceptionHandler;

    public FileResourceFetcher(Vertx vertx, URI uri, Map<String, String> config) {
        this.vertx = vertx;
        this.uri = uri;
    }

    @Override
    public void fetch(Handler<Buffer> resultHandler) {
        vertx.fileSystem().readFile(uri.getPath(), result -> {
            if (result.succeeded()) {
                resultHandler.handle(result.result());
            } else {
                exceptionHandler.handle(result.cause());
            }
        });
    }

    @Override
    public FileResourceFetcher exceptionHandler(Handler<Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

}
