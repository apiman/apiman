/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.apiman.gateway.platforms.vertx3.io;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link io.apiman.gateway.platforms.vertx3.io.VertxServiceRequest}.
 *
 * NOTE: This class has been automatically generated from the {@link io.apiman.gateway.platforms.vertx3.io.VertxServiceRequest} original class using Vert.x codegen.
 */
public class VertxServiceRequestConverter {

  public static void fromJson(JsonObject json, VertxServiceRequest obj) {
    if (json.getValue("apiKey") instanceof String) {
      obj.setApiKey((String)json.getValue("apiKey"));
    }
    if (json.getValue("destination") instanceof String) {
      obj.setDestination((String)json.getValue("destination"));
    }
    if (json.getValue("headers") instanceof JsonObject) {
      java.util.Map<String, java.lang.String> map = new java.util.LinkedHashMap<>();
      json.getJsonObject("headers").forEach(entry -> {
        if (entry.getValue() instanceof String)
          map.put(entry.getKey(), (String)entry.getValue());
      });
      obj.setHeaders(map);
    }
    if (json.getValue("queryParams") instanceof JsonObject) {
      java.util.Map<String, java.lang.String> map = new java.util.LinkedHashMap<>();
      json.getJsonObject("queryParams").forEach(entry -> {
        if (entry.getValue() instanceof String)
          map.put(entry.getKey(), (String)entry.getValue());
      });
      obj.setQueryParams(map);
    }
    if (json.getValue("rawRequest") instanceof Object) {
      obj.setRawRequest(json.getValue("rawRequest"));
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
  }

  public static void toJson(VertxServiceRequest obj, JsonObject json) {
    if (obj.getApiKey() != null) {
      json.put("apiKey", obj.getApiKey());
    }
    if (obj.getDestination() != null) {
      json.put("destination", obj.getDestination());
    }
    if (obj.getHeaders() != null) {
      JsonObject map = new JsonObject();
      obj.getHeaders().forEach((key,value) -> map.put(key, value));
      json.put("headers", map);
    }
    if (obj.getQueryParams() != null) {
      JsonObject map = new JsonObject();
      obj.getQueryParams().forEach((key,value) -> map.put(key, value));
      json.put("queryParams", map);
    }
    if (obj.getRawRequest() != null) {
      json.put("rawRequest", obj.getRawRequest());
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
  }
}