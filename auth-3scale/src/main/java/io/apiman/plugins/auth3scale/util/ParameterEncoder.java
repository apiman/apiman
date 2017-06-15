/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.plugins.auth3scale.util;

/**
 * Encodes a ParameterMap as a string suitable for sending as part of an HTML request.
 */
@SuppressWarnings("nls")
public class ParameterEncoder {

    /**
     * Takes the parameter map and returns an encoded string.
     *
     * @param params Parameter map to encode
     * @return Encoded string
     */
    public String encode(ParameterMap params) {
        StringBuilder result = new StringBuilder();

        int index = 0;
        for (String mapKey : params.getKeys()) {
            if (index != 0) result.append("&");
            switch (params.getType(mapKey)) {
                case STRING:
                    result.append(emitNormalValue(mapKey, params.getStringValue(mapKey)));
                    break;
                case MAP:
                    result.append(emitNormalMap(mapKey, params.getMapValue(mapKey)));
                    break;
                case ARRAY:
                    result.append(emitNormalArray(mapKey, params.getArrayValue(mapKey)));
                    break;
                case LONG:
                    result.append(emitNormalValue(mapKey, Long.toString(params.getLongValue(mapKey))));
                    break;
                default:
                    break;
            }
            index++;
        }

        return substitueCharacters(result.toString());
    }

    private String emitNormalArray(String mapKey, ParameterMap[] mapValue) {
        StringBuilder b = new StringBuilder();
        int index = 0;

        for (ParameterMap arrayMap : mapValue) {
            if (index != 0) b.append("&");
            b.append(emitArray(mapKey, arrayMap, index));
            index++;
        }
        return b.toString();
    }

    private String emitArray(String mapKey, ParameterMap arrayMap, int arrayIndex) {
        StringBuilder b = new StringBuilder();
        int index = 0;

        for (String key : arrayMap.getKeys()) {
            switch (arrayMap.getType(key)) {
                case STRING:
                    if (index != 0) b.append("&");
                    b.append(mapKey).append("[").append(arrayIndex).append("]");
                    b.append("[").append(key).append("]=").append(arrayMap.getStringValue(key));
                    index++;
                    break;
                case MAP:
                    ParameterMap map = arrayMap.getMapValue(key);
                    for (String itemKey : map.getKeys()) {
                        if (index != 0) b.append("&");
                        b.append(emitArrayValue(mapKey, key, itemKey, map.getStringValue(itemKey), arrayIndex));
                        index++;
                    }
                    break;
                case ARRAY:
                    // TODO does ARRAY need to be handled?
                    break;
                case LONG:
                    break;
                default:
                    break;
            }
        }
        return b.toString();
    }

    private String emitArrayValue(String mapKey, String key, String itemKey, String stringValue, int index) {
        StringBuilder b = new StringBuilder();
        b.append(mapKey).append("[").append(index).append("]");
        b.append("[").append(key).append("]");
        b.append("[").append(itemKey).append("]=").append(stringValue);
        return b.toString();
    }

    private String emitNormalMap(String mapKey, ParameterMap mapValue) {
        StringBuilder b = new StringBuilder();
        int index = 0;
        for (String key : mapValue.getKeys()) {
            if (index != 0) b.append("&");
            switch (mapValue.getType(key)) {
                case LONG:
                    b.append(emitMapValue(mapKey, key, Long.toString(mapValue.getLongValue(key))));
                    break;
                case STRING:
                    b.append(emitMapValue(mapKey, key, mapValue.getStringValue(key)));
                    break;
                case MAP:
                    // TODO does MAP need to be handled?
                    break;
                case ARRAY:
                    // TODO does ARRAY need to be handled?
                    break;
            }
            index++;
        }
        return b.toString();
    }

    private String emitMapValue(String mapKey, String key, String stringValue) {
        StringBuilder b = new StringBuilder();
        b.append(mapKey).append("[").append(key).append("]=").append(stringValue);
        return b.toString();
    }

    private String emitNormalValue(String key, String value) {
        StringBuilder b = new StringBuilder();
        b.append(key).append("=").append(value);
        return b.toString();
    }

    private String substitueCharacters(String input) {
        return input.replace(" ", "%20").replace("[", "%5B").replace("]", "%5D").replace("#", "%23").replace(":", "%3A");
    }
}
