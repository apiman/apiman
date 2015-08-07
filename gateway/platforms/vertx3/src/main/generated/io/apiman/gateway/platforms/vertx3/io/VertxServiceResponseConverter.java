package io.apiman.gateway.platforms.vertx3.io;

import io.vertx.core.json.JsonObject;

import java.util.Map.Entry;

public class VertxServiceResponseConverter {

  public static void fromJson(JsonObject json, VertxServiceResponse obj) {
    if (json.getValue("code") instanceof Number) {
      obj.setCode(((Number)json.getValue("code")).intValue());
    }
    if (json.getValue("message") instanceof String) {
      obj.setMessage((String)json.getValue("message"));
    }
    for (Entry<String, Object> entry : json.getJsonObject("headers").getMap().entrySet()) {
        if (entry.getValue() instanceof String) {
            obj.getHeaders().put(entry.getKey(), (String) entry.getValue());
        }
    }

  }

  public static void toJson(VertxServiceResponse obj, JsonObject json) {
    json.put("code", obj.getCode());
    if (obj.getMessage() != null) {
      json.put("message", obj.getMessage());
    }
    if (obj.getHeaders() != null) {
        json.put("headers", obj.getHeaders());
    }
  }
}