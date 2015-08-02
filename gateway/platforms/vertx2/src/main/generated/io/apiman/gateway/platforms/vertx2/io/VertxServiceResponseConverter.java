package io.apiman.gateway.platforms.vertx2.io;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

public class VertxServiceResponseConverter {

  public static void fromJson(JsonObject json, VertxServiceResponse obj) {
    if (json.getValue("code") instanceof Number) {
      obj.setCode(((Number)json.getValue("code")).intValue());
    }
    if (json.getValue("message") instanceof String) {
      obj.setMessage((String)json.getValue("message"));
    }
  }

  public static void toJson(VertxServiceResponse obj, JsonObject json) {
    json.put("code", obj.getCode());
    if (obj.getMessage() != null) {
      json.put("message", obj.getMessage());
    }
  }
}