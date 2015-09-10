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
 * Converter for {@link io.apiman.gateway.platforms.vertx3.io.VertxPolicyFailure}.
 *
 * NOTE: This class has been automatically generated from the {@link io.apiman.gateway.platforms.vertx3.io.VertxPolicyFailure} original class using Vert.x codegen.
 */
public class VertxPolicyFailureConverter {

  public static void fromJson(JsonObject json, VertxPolicyFailure obj) {
    if (json.getValue("failureCode") instanceof Number) {
      obj.setFailureCode(((Number)json.getValue("failureCode")).intValue());
    }
    if (json.getValue("headers") instanceof JsonObject) {
      java.util.Map<String, java.lang.String> map = new java.util.LinkedHashMap<>();
      json.getJsonObject("headers").forEach(entry -> {
        if (entry.getValue() instanceof String)
          map.put(entry.getKey(), (String)entry.getValue());
      });
      obj.setHeaders(map);
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
    if (obj.getHeaders() != null) {
      JsonObject map = new JsonObject();
      obj.getHeaders().forEach((key,value) -> map.put(key, value));
      json.put("headers", map);
    }
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