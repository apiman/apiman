/*
 * Copyright 2022, Black Parrot Labs Ltd.
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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.AbstractConfiguration;

/**
 * Look up value from environment, but also look up common env var 'synonyms', with upper case and underscores instead of kebab/dot.
 * <p>
 * For example, <code>apiman.manager.api = APIMAN_MANAGER_API</code> or <code>apiman-manager-ui.api = APIMAN_MANAGER_UI_API</code>.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class EnvConfigurationWithSynonyms extends AbstractConfiguration {

    public EnvConfigurationWithSynonyms() {
    }

    @Override
    protected void addPropertyDirect(String key, Object value) {
        throw new IllegalArgumentException("Environment is read-only. Can't add a new property.");
    }

    private static List<String> synonyms(String key) {
        String dotToUnderscore = key.replace(".", "_").replace("-", "_");
        String upperCase = dotToUnderscore.toUpperCase();
        return List.of(key, dotToUnderscore, upperCase);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(String key) {
        return getProperty(key) != null;
    }

    @Override
    public Object getProperty(String key) {
        for (String s : synonyms(key)) {
            Object result = System.getenv(s);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Iterator<String> getKeys() {
        return System.getenv().keySet().iterator();
    }
}
