package io.apiman.gateway.platforms.vertx2.services;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

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
  }
}