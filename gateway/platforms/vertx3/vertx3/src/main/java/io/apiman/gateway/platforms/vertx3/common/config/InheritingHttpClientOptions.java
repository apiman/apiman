package io.apiman.gateway.platforms.vertx3.common.config;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

@DataObject(inheritConverter=true, generateConverter=true)
public class InheritingHttpClientOptions extends HttpClientOptions {

    public InheritingHttpClientOptions() {
        super();
    }

    public InheritingHttpClientOptions(HttpClientOptions other) {
        super(other);
    }

    public InheritingHttpClientOptions(JsonObject json) {
        super(json);
    }

}
