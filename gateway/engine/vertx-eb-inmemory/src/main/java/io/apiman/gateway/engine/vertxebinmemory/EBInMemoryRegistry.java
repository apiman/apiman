package io.apiman.gateway.engine.vertxebinmemory;

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.engine.vertxebinmemory.services.EBRegistryProxy;
import io.apiman.gateway.engine.vertxebinmemory.services.EBRegistryProxyHandler;
import io.apiman.gateway.platforms.vertx3.config.VertxEngineConfig;
import io.vertx.core.Vertx;

import java.util.Map;
import java.util.UUID;

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
