/*
 * Copyright 2017 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.gateway.engine.vertx.polling.fetchers.auth;

import io.apiman.gateway.engine.vertx.polling.exceptions.OAuth2Exception;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
public abstract class AbstractOAuth2Base {
    // Umm :-)
    protected static JsonObject mapToJson(Map<String, String> map) {
        JsonObject root = new JsonObject();
        for (Entry<String, String> entry : map.entrySet()) {
            String[] split = StringUtils.split(entry.getKey(), '.');
            ArrayDeque<String> dq = new ArrayDeque<>(Arrays.asList(split));
            createOrDescend(root, dq, entry.getValue());
        }
        return root;
    }

    protected static void createOrDescend(JsonObject root, Deque<String> keyPath, String value) {
        // If there are still key-path elements remaining to traverse.
        if (keyPath.size() > 1) {
            // If there's no object already at this key-path create a new JsonObject.
            if (root.getJsonObject(keyPath.peek()) == null) {
                JsonObject newJson = new JsonObject();
                String val = keyPath.pop();
                root.put(val, newJson);
                createOrDescend(newJson, keyPath, value);
            } else { // If there's already an existing object on key-path, grab it and traverse.
                createOrDescend(root.getJsonObject(keyPath.pop()), keyPath, value);
            }
        } else { // Set the value.
            Boolean boolObj = BooleanUtils.toBooleanObject(value);
            if (boolObj != null) {
                root.put(keyPath.pop(), boolObj);
            } else if (StringUtils.isNumeric(value)) {
                root.put(keyPath.pop(), Long.parseLong(value));
            } else {
                root.put(keyPath.pop(), value);
            }
        }
    }

    @SuppressWarnings("nls")
    protected OAuth2FlowType getFlowType(String flowAsString) {
        switch(flowAsString.toUpperCase()) {
        case "AUTH_CODE":
        case "AUTHCODE":
            return OAuth2FlowType.AUTH_CODE;
        case "CLIENT":
            return OAuth2FlowType.CLIENT;
        case "PASSWORD":
            return OAuth2FlowType.PASSWORD;
        }
        throw new OAuth2Exception("Unrecognised OAuth2FlowType " + flowAsString);
    }
}
