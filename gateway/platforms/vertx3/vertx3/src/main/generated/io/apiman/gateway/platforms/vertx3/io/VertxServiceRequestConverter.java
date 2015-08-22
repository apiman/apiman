package io.apiman.gateway.platforms.vertx3.io;

import io.vertx.core.json.JsonObject;

import java.util.Map.Entry;

public class VertxServiceRequestConverter {

  public static void fromJson(JsonObject json, VertxServiceRequest obj) {
    if (json.getValue("apiKey") instanceof String) {
      obj.setApiKey((String)json.getValue("apiKey"));
    }
    if (json.getValue("destination") instanceof String) {
      obj.setDestination((String)json.getValue("destination"));
    }
    if (json.getValue("remoteAddr") instanceof String) {
      obj.setRemoteAddr((String)json.getValue("remoteAddr"));
    }
    if (json.getValue("serviceId") instanceof String) {
      obj.setServiceId((String)json.getValue("serviceId"));
    }
    if (json.getValue("serviceOrgId") instanceof String) {
      obj.setServiceOrgId((String)json.getValue("serviceOrgId"));
    }
    if (json.getValue("serviceVersion") instanceof String) {
      obj.setServiceVersion((String)json.getValue("serviceVersion"));
    }
    if (json.getValue("transportSecure") instanceof Boolean) {
      obj.setTransportSecure((Boolean)json.getValue("transportSecure"));
    }
    if (json.getValue("type") instanceof String) {
      obj.setType((String)json.getValue("type"));
    }
    for (Entry<String, Object> entry : json.getJsonObject("headers").getMap().entrySet()) {
        if (entry.getValue() instanceof String) {
            obj.getHeaders().put(entry.getKey(), (String) entry.getValue());
        }
    }
    for (Entry<String, Object> entry : json.getJsonObject("queryParams").getMap().entrySet()) {
        if (entry.getValue() instanceof String) {
            obj.getQueryParams().put(entry.getKey(), (String) entry.getValue());
        }
    }
  }

  public static void toJson(VertxServiceRequest obj, JsonObject json) {
    if (obj.getApiKey() != null) {
      json.put("apiKey", obj.getApiKey());
    }
    if (obj.getDestination() != null) {
      json.put("destination", obj.getDestination());
    }
    if (obj.getRemoteAddr() != null) {
      json.put("remoteAddr", obj.getRemoteAddr());
    }
    if (obj.getServiceId() != null) {
      json.put("serviceId", obj.getServiceId());
    }
    if (obj.getServiceOrgId() != null) {
      json.put("serviceOrgId", obj.getServiceOrgId());
    }
    if (obj.getServiceVersion() != null) {
      json.put("serviceVersion", obj.getServiceVersion());
    }
    json.put("transportSecure", obj.isTransportSecure());
    if (obj.getType() != null) {
      json.put("type", obj.getType());
    }
    if (obj.getHeaders() != null) {
      json.put("headers", obj.getHeaders());
    }
    if (obj.getQueryParams() != null) {
        json.put("queryParams", obj.getQueryParams());
    }
  }
}