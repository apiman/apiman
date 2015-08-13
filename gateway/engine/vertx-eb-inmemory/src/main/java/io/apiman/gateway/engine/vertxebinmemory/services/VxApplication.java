package io.apiman.gateway.engine.vertxebinmemory.services;

import io.apiman.gateway.engine.beans.Application;
import io.vertx.core.json.Json;

public class VxApplication implements Head {
    private Application application;
    private String action;
    private String uuid;

    public VxApplication(Application app, String action, String uuid) {
        this.application = app;
        this.action = action;
        this.uuid = uuid;
    }

    public Application getApplication() {
        return application;
    }

    @Override
    public String type() {
        return "application";
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public String body() {
        return Json.encode(application);
    }

    @Override
    public String uuid() {
        return uuid;
    }
}
