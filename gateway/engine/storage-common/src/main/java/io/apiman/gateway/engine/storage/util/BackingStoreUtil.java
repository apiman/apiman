/*
 * Copyright 2018 Pete Cornish
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
package io.apiman.gateway.engine.storage.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility methods for backing stores.
 */
public final class BackingStoreUtil {
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    static {
        JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private BackingStoreUtil() {
    }

    /**
     * Parses the String value as a primitive or a String, depending on its type.
     * @param clazz the destination type
     * @param value the value to parse
     * @return the parsed value
     * @throws Exception
     */
    public static Object readPrimitive(Class<?> clazz, String value) throws Exception {
        if (clazz == String.class) {
            return value;
        } else if (clazz == Long.class) {
            return Long.parseLong(value);
        } else if (clazz == Integer.class) {
            return Integer.parseInt(value);
        } else if (clazz == Double.class) {
            return Double.parseDouble(value);
        } else if (clazz == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (clazz == Byte.class) {
            return Byte.parseByte(value);
        } else if (clazz == Short.class) {
            return Short.parseShort(value);
        } else if (clazz == Float.class) {
            return Float.parseFloat(value);
        } else {
            throw new Exception("Unsupported primitive: " + clazz); //$NON-NLS-1$
        }
    }
}
