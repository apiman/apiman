package io.apiman.gateway.engine.vertxebinmemory.services;

import io.vertx.core.json.JsonObject;

public interface Head {
    String uuid();
    String type();
    String action();
    String body();

    default JsonObject asJson() {
        return new JsonObject().put("type", type()).put("action", action()).put("body", body())
                .put("uuid", uuid());
    }
}
