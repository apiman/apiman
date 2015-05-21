/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.gateway.engine.auth;

import io.apiman.gateway.engine.beans.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing Authorization types, with aliases.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public enum RequiredAuthType {

    /**
     * Certificate-based mutual TLS/SSL
     */
    MTLS("mtls", "mssl"),
    /**
     * BASIC
     */
    BASIC("basic"),
    /**
     * Default (nothing)
     */
    DEFAULT;

    public static final String ENDPOINT_AUTHORIZATION_TYPE = "authorization.type";

    private static Map<String, RequiredAuthType> constants = new HashMap<>();
    private final String[] aliases;

    static {
        for (RequiredAuthType authType : values()) {
            for (String alias : authType.aliases) {
                constants.put(alias.toLowerCase(), authType);
            }
        }
    }

    RequiredAuthType(String... aliases) {
        this.aliases = aliases;
    }

    /**
     * From string to enum
     *
     * @param value the string
     * @return the enum
     */
    public static RequiredAuthType fromValue(String value) {
        if (value == null)
            return DEFAULT;

        RequiredAuthType constant = constants.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    /**
     * Parse service endpoint properties to retrieve enum value (if any)
     *
     * @param service the service
     * @return the required auth type
     */
    public static RequiredAuthType parseType(Service service) {
        return RequiredAuthType.fromValue(service.getEndpointProperties().get(
                RequiredAuthType.ENDPOINT_AUTHORIZATION_TYPE));
    }
}