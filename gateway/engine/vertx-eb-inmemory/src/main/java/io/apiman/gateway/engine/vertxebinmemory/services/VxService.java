package io.apiman.gateway.engine.vertxebinmemory.services;

import io.apiman.gateway.engine.beans.Service;
import io.vertx.core.json.Json;

public class VxService implements Head {

    private Service service;
    private String action;
    private String uuid;

    public VxService(Service service, String action, String uuid) {
        this.service = service;
        this.action = action;
        this.uuid = uuid;
    }

    public Service getService() {
        return service;
    }

    @Override
    public String type() {
        return "service";
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public String body() {
        return Json.encode(service);
    }

    @Override
    public String uuid() {
        return uuid;
    }
}
