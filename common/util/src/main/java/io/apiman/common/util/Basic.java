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

import org.apache.commons.codec.binary.Base64;

@SuppressWarnings("nls")
public class Basic {

    private Basic() {
    }

    public static String encode(String username, String password) {
        String up = username + ':' + password;
        StringBuilder builder = new StringBuilder();
        builder.append("Basic ");
        builder.append(Base64.encodeBase64String(up.getBytes()));
        return builder.toString();
    }
}
