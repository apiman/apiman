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
package io.apiman.common.config.options;

import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Base class for all options.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractOptions {

    public AbstractOptions() {
    }

    /**
     * Constructor. Parses options immediately.
     * @param options the options
     */
    public AbstractOptions(Map<String, String> options) {
        parse(options);
    }

    /**
     * Called to parse a map into a set of specific options.
     * @param options the option map
     */
    protected abstract void parse(Map<String, String> options);

    protected static String getVar(Map<String, String> optionsMap, String varName) {
        if(optionsMap.get(varName) == null || optionsMap.get(varName).isEmpty()) {
            return null;
        }
        return optionsMap.get(varName);
    }

    protected static String[] split(String str, char splitter) {
        if (str == null)
            return null;

        String[] splitStr = StringUtils.split(str, splitter);

        String[] out = new String[splitStr.length];

        for (int i = 0; i < splitStr.length; i++) {
            out[i] = StringUtils.trim(splitStr[i]);
        }

        return out;
    }

    protected static int parseInt(Map<String, String> optionsMap, String key, int defaultValue) {
        if (optionsMap.containsKey(key)) {
            return Integer.valueOf(optionsMap.get(key));
        }
        return defaultValue;
    }

    protected static boolean parseBool(Map<String, String> optionsMap, String key) {
        return parseBool(optionsMap, key, false);
    }

    protected static boolean parseBool(Map<String, String> optionsMap, String key, boolean defaultValue) {
        String value = optionsMap.get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return BooleanUtils.toBoolean(value);
        }
    }
}
