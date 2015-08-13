package io.apiman.gateway.platforms.vertx3.io;

import io.vertx.core.json.JsonObject;

import java.util.Map.Entry;

public class VertxPolicyFailureConverter {

  public static void fromJson(JsonObject json, VertxPolicyFailure obj) {
    if (json.getValue("failureCode") instanceof Number) {
      obj.setFailureCode(((Number)json.getValue("failureCode")).intValue());
    }
    if (json.getValue("message") instanceof String) {
      obj.setMessage((String)json.getValue("message"));
    }
    if (json.getValue("raw") instanceof String) {
      obj.setRaw((String)json.getValue("raw"));
    }
    if (json.getValue("responseCode") instanceof Number) {
      obj.setResponseCode(((Number)json.getValue("responseCode")).intValue());
    }
    if (json.getValue("type") instanceof String) {
      obj.setType(io.apiman.gateway.engine.beans.PolicyFailureType.valueOf((String)json.getValue("type")));
    }
    for (Entry<String, Object> entry : json.getJsonObject("headers").getMap().entrySet()) {
        if (entry.getValue() instanceof String) {
            obj.getHeaders().put(entry.getKey(), (String) entry.getValue());
        }
    }
  }

  public static void toJson(VertxPolicyFailure obj, JsonObject json) {
    json.put("failureCode", obj.getFailureCode());
    if (obj.getMessage() != null) {
      json.put("message", obj.getMessage());
    }
    if (obj.getRaw() != null) {
      json.put("raw", obj.getRaw());
    }
    json.put("responseCode", obj.getResponseCode());
    if (obj.getType() != null) {
      json.put("type", obj.getType().name());
    }
    if (obj.getHeaders() != null) {
        json.put("headers", obj.getHeaders());
      }
  }
}