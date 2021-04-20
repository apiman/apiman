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
package io.apiman.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

/**
 * Basic BASIC utilities
 */
public class Basic {

    private Basic() {
    }

    /**
     * Encode username and password in BASIC format
     */
    public static String encode(String username, String password) {
        String up = username + ':' + password;
        return "Basic " + Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     * Decode BASIC value that includes the {@code Basic } scheme segment.
     */
    public static String[] decodeWithScheme(String input) {
        String[] split = StringUtils.split(input, null);
        if ("Basic".equalsIgnoreCase(split[0])) {
            if (split.length == 2) {
                return decode(split[1]);
            } else {
                throw new IllegalArgumentException("Authorization header invalid for BASIC");
            }
        } else {
            throw new IllegalArgumentException("Auth scheme was not BASIC");
        }
    }

    /**
     * Decode BASIC value (without scheme segment).
     */
    public static String[] decode(String input) {
        String decoded = new String(Base64.getDecoder().decode(input));
        return StringUtils.split(decoded, ":");
    }

}
