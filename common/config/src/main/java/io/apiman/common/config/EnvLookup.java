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
package io.apiman.common.config;

import org.apache.commons.lang.text.StrLookup;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Allows users to AesEncrypt their properties in apiman.properties.
 *
 * @author eric.wittmann@redhat.com
 */
public class EnvLookup extends StrLookup {

    /**
     * @see org.apache.commons.lang.text.StrLookup#lookup(java.lang.String)
     */
    @Override
    public String lookup(String key) {
        // Emulates Microprofile prop -> env lookup
        for (String s : synonyms(key)) {
            if (s != null) {
                return s;
            }
        }
        return "";
    }

    private List<String> synonyms(String key) {
        String dotToUnderscore = key.replace(".", "_").replace("-", "_");
        String upperCase = dotToUnderscore.toUpperCase();
        return Stream.of(System.getenv(dotToUnderscore), System.getenv(upperCase))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

}
