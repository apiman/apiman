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

import java.util.Map;
import java.util.UUID;

import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.engine.vertxebinmemory.services.EBRegistryProxy;
import io.apiman.gateway.engine.vertxebinmemory.services.EBRegistryProxyHandler;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.vertx.core.Vertx;

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

    public EBInMemoryRegistry(Vertx vertx, VertxEngineConfig vxConfig, Map<String, String> options) {
        super();

        System.out.println("Starting an EBInMemoryRegistry on UUID " + registryUuid);

        this.vertx = vertx;
        listenProxyHandler();
        this.proxy = new EBRegistryProxy(vertx, address(), registryUuid);
    }

    @Override
    public void getContract(ServiceRequest request, IAsyncResultHandler<ServiceContract> handler) {
        super.getContract(request, handler);
    }

    @Override
    public void publishService(Service service, IAsyncResultHandler<Void> handler) {
        super.publishService(service, handler);
        proxy.publishService(service);
        System.out.println("Published a service");
    }

    @Override
    public void retireService(Service service, IAsyncResultHandler<Void> handler) {
        super.retireService(service, handler);
        proxy.retireService(service);
    }

    @Override
    public void registerApplication(Application application, IAsyncResultHandler<Void> handler) {
        super.registerApplication(application, handler);
        proxy.registerApplication(application);
    }

    @Override
    public void unregisterApplication(Application application, IAsyncResultHandler<Void> handler) {
        super.unregisterApplication(application, handler);
        proxy.unregisterApplication(application);
    }

    @Override
    public void getService(String organizationId, String serviceId, String serviceVersion,
            IAsyncResultHandler<Service> handler) {
        super.getService(organizationId, serviceId, serviceVersion, handler);
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
    public void publishService(Service service) {
        System.out.println("Publish service");
        super.publishService(service, emptyHandler);
    }

    @Override
    public void retireService(Service service) {
        System.out.println("Retire service");
        super.retireService(service, emptyHandler);
    }

    @Override
    public void registerApplication(Application application) {
        System.out.println("Register application");
        super.registerApplication(application, emptyHandler);
    }

    @Override
    public void unregisterApplication(Application application) {
        System.out.println("Unregister application");
        super.unregisterApplication(application, emptyHandler);
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
