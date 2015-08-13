package io.apiman.gateway.engine.vertxebinmemory.services;

import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Service;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;

public class EBRegistryProxy {
    Vertx vertx;
    DeliveryOptions options;
    String address;
    private String uuid;

    public static final String REGISTER = "register";
    public static final String UNREGISTER = "unregister";
    public static final String PUBLISH = "publish";
    public static final String RETIRE = "retire";

    public EBRegistryProxy(Vertx vertx, String address, String uuid) {
        this.vertx = vertx;
        this.address = address;
        this.uuid = uuid;
    }

    public void registerApplication(Application application) {
        vertx.eventBus().publish(address, new VxApplication(application, REGISTER, uuid).asJson());
    }

    public void unregisterApplication(Application application) {
        vertx.eventBus().publish(address, new VxApplication(application, UNREGISTER, uuid).asJson());
    }

    public void publishService(Service service) {
        System.out.println("publishing service on " + address);
        vertx.eventBus().publish(address, new VxService(service, PUBLISH, uuid).asJson());
    }

    public void retireService(Service service) {
        vertx.eventBus().publish(address, new VxService(service, RETIRE, uuid).asJson());
    }
}
