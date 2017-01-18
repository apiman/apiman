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
package io.apiman.gateway.engine.vertxebinmemory;

import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.engine.vertxebinmemory.apis.EBRegistryProxy;
import io.apiman.gateway.engine.vertxebinmemory.apis.EBRegistryProxyHandler;
import io.vertx.core.Vertx;

import java.util.Map;
import java.util.UUID;

/**
 * In-memory implementation of the {@link IRegistry} using Vert.x 3's event bus to distribute the events to
 * all nodes. This is used for testing purposes only, and isn't sufficiently robust for production.
 *
 * Each node has an event listener; a given node receiving a write/delete-type registry operation distributes
 * the events to all nodes listening, along with an attached node-unique UUID. Any listener ignores messages
 * from their own UUID.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class EBInMemoryRegistry extends InMemoryRegistry implements EBRegistryProxyHandler {
    private Vertx vertx;
    private EBRegistryProxy proxy;
    private final static String ADDRESS = "io.vertx.core.Vertx.registry.EBInMemoryRegistry.event"; //$NON-NLS-1$
    private String registryUuid = UUID.randomUUID().toString();

    public EBInMemoryRegistry(Vertx vertx, IEngineConfig vxConfig, Map<String, String> options) {
        super();

        System.out.println("Starting an EBInMemoryRegistry on UUID " + registryUuid);

        this.vertx = vertx;
        listenProxyHandler();
        this.proxy = new EBRegistryProxy(vertx, address(), registryUuid);
    }

    @Override
    public void getClient(String apiKey, IAsyncResultHandler<Client> handler) {
        super.getClient(apiKey, handler);
    }

    @Override
    public void getContract(String apiOrganizationId, String apiId, String apiVersion, String apiKey,
            IAsyncResultHandler<ApiContract> handler) {
        super.getContract(apiOrganizationId, apiId, apiVersion, apiKey, handler);
    }

    @Override
    public void publishApi(Api api, IAsyncResultHandler<Void> handler) {
        super.publishApi(api, handler);
        proxy.publishApi(api);
        System.out.println("Published a api");
    }

    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        super.retireApi(api, handler);
        proxy.retireApi(api);
    }

    @Override
    public void registerClient(Client client, IAsyncResultHandler<Void> handler) {
        super.registerClient(client, handler);
        proxy.registerClient(client);
    }

    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> handler) {
        super.unregisterClient(client, handler);
        proxy.unregisterClient(client);
    }

    @Override
    public void getApi(String organizationId, String apiId, String apiVersion,
            IAsyncResultHandler<Api> handler) {
        super.getApi(organizationId, apiId, apiVersion, handler);
    }

    @Override
    public String uuid() {
        return registryUuid;
    }

    @Override
    public String address() {
        return ADDRESS;
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }

    // These are called back by the listener

    @Override
    public void publishApi(Api api) {
        System.out.println("Publish api");
        super.publishApi(api, emptyHandler);
    }

    @Override
    public void retireApi(Api api) {
        System.out.println("Retire api");
        super.retireApi(api, emptyHandler);
    }

    @Override
    public void registerClient(Client client) {
        System.out.println("Register client");
        super.registerClient(client, emptyHandler);
    }

    @Override
    public void unregisterClient(Client client) {
        System.out.println("Unregister client");
        super.unregisterClient(client, emptyHandler);
    }

    private EmptyHandler emptyHandler = new EmptyHandler();

    private class EmptyHandler implements IAsyncResultHandler<Void> {

        @Override
        public void handle(IAsyncResult<Void> result) {
            if (result.isError()) {
                System.err.println("Error " + result.getError());
                throw new RuntimeException(result.getError());
            }
        }
    }

}
