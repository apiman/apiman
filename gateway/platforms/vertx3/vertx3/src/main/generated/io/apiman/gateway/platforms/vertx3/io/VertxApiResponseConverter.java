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

import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.vertx.core.json.JsonObject;

/**
 * Converter for {@link io.apiman.gateway.platforms.vertx3.io.VertxApiResponse}.
 *
 * NOTE: This class has been automatically generated from the {@link io.apiman.gateway.platforms.vertx3.io.VertxApiResponse} original class using Vert.x codegen.
 */
public class VertxApiResponseConverter {

  public static void fromJson(JsonObject json, VertxApiResponse obj) {
    if (json.getValue("attributes") instanceof JsonObject) {
      java.util.Map<String, java.lang.Object> map = new java.util.LinkedHashMap<>();
      json.getJsonObject("attributes").forEach(entry -> {
        if (entry.getValue() instanceof Object)
          map.put(entry.getKey(), entry.getValue());
      });
      obj.setAttributes(map);
    }
    if (json.getValue("code") instanceof Number) {
      obj.setCode(((Number)json.getValue("code")).intValue());
    }
    if (json.getValue("headers") instanceof JsonObject) {
      HeaderMap map = new HeaderMap();
      json.getJsonObject("headers").forEach(entry -> {
        if (entry.getValue() instanceof String)
          map.put(entry.getKey(), (String)entry.getValue());
      });
      obj.setHeaders(map);
    }
    if (json.getValue("message") instanceof String) {
      obj.setMessage((String)json.getValue("message"));
    }
  }

  public static void toJson(VertxApiResponse obj, JsonObject json) {
    if (obj.getAttributes() != null) {
      JsonObject map = new JsonObject();
      obj.getAttributes().forEach((key,value) -> map.put(key, value));
      json.put("attributes", map);
    }
    json.put("code", obj.getCode());
    if (obj.getHeaders() != null) {
      JsonObject map = new JsonObject();
      obj.getHeaders().forEach((pair) -> map.put(pair.getKey(), pair.getValue()));
      json.put("headers", map);
    }
    if (obj.getMessage() != null) {
      json.put("message", obj.getMessage());
    }
  }
}