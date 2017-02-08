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

package io.apiman.gateway.test.junit.vertx3;

import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("nls")
public class ApiToFileRegistry extends InMemoryRegistry {

    private FileSystem fileSystem;
    private File file;
    private JsonArray clients = new JsonArray();
    private JsonArray apis = new JsonArray();
    private JsonObject root = new JsonObject(); private void linkRoot() {
        root.put("clients", clients);
        root.put("apis", apis);
    }
    private Map<Client, JsonObject> clientMap = new LinkedHashMap<>();
    private Map<Api, JsonObject> apiMap = new LinkedHashMap<>();
    private EventBus eb;

    public ApiToFileRegistry(Vertx vertx, IEngineConfig foo, Map<String, String> config) {
        super();
        this.eb = vertx.eventBus();
        this.fileSystem = vertx.fileSystem();
        linkRoot();
        createTempFile();
        createResetListener();
    }

    private void createResetListener() {
        eb.consumer(ApiToFileRegistry.class.getCanonicalName(), msg -> {
           if (msg.headers().get("action").equals("reset")) {
               System.out.println("Resetting!");
               reset();
           } else {
               throw new RuntimeException("Unknown action in " + msg.headers());
           }
        });
    }

    private void createTempFile()  {
        try {
            file = File.createTempFile("apiman", "fs");
            System.setProperty("CONFIG_FILE_PATH", "file:///" + file.getAbsolutePath());
            System.out.println("file path file:///" + file.getAbsolutePath());
            file.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        fileSystem.deleteBlocking(file.getAbsolutePath());
        super.getMap().clear();
        apis.clear();
        clients.clear();
        clientMap.clear();
        apiMap.clear();
        root.clear();
        linkRoot();
    }

    private void rewrite() {
        fileSystem.writeFileBlocking(file.getAbsolutePath(), Buffer.buffer(root.encodePrettily()));
        restartReader();
    }

    private void restartReader() {
        eb.publish("restartReader", "reset!");
    }

    private void publish(Api api) {
        System.out.println("WANT TO PUBLISH " + api);
        JsonObject apiJson = new JsonObject(Json.encode(api));
        apis.add(apiJson);
        apiMap.put(api, apiJson);
        rewrite();
    }

    private void retire(Api api) {
        JsonObject removeJson = apiMap.remove(api);
        apis.remove(removeJson);
        rewrite();
    }

    private void register(Client client) {
        JsonObject clientJson = new JsonObject(Json.encode(client));
        clients.add(clientJson);
        clientMap.put(client, clientJson);
        rewrite();
    }

    private void unregister(Client lookup) {
        JsonObject removeJson = clientMap.remove(lookup);
        clients.remove(removeJson);
        rewrite();
    }

    @Override
    public void publishApi(Api api, IAsyncResultHandler<Void> handler) {
        super.publishApi(api, result -> {
           if (result.isSuccess()) {
               publish(api);
           }
           handler.handle(result);
        });
    }

    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        super.retireApi(api, result -> {
            if (result.isSuccess()) {
                retire(api);
            }
            handler.handle(result);
         });
    }

    @Override
    public void registerClient(Client client, IAsyncResultHandler<Void> handler) {
        super.registerClient(client, result -> {
            if (result.isSuccess()) {
                register(client);
            }
            handler.handle(result);
         });
    }

    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> handler) {
        super.unregisterClient(client, result -> {
            if (result.isSuccess()) {
                unregister(client);
            }
            handler.handle(result);
         });
    }

    @Override
    public void getApi(String organizationId, String apiId, String apiVersion, IAsyncResultHandler<Api> handler) {
        super.getApi(organizationId, apiId, apiVersion, handler);
    }

    @Override
    public void getClient(String apiKey, IAsyncResultHandler<Client> handler) {
        super.getClient(apiKey, handler);
    }

    @Override
    public void getContract(String apiOrganizationId, String apiId, String apiVersion, String apiKey, IAsyncResultHandler<ApiContract> handler) {
        super.getContract(apiOrganizationId, apiId, apiVersion, apiKey, handler);
    }

}
