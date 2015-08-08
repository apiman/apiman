package io.apiman.gateway.engine.vertxebinmemory.services;

import io.vertx.core.json.JsonObject;

public interface Head {
    String UUID = "uuid";
    String HEAD =  "head";
    String ACTION = "action";
    String BODY = "body";
    String TYPE = "type";

    String uuid();
    String type();
    String action();
    String body();

    default JsonObject asJson() {
        return new JsonObject().put("type", type()).put("action", action()).put("body", body())
                .put("uuid", uuid());
    }
}
