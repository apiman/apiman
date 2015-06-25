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
import org.jboss.security.vault.SecurityVaultUtil;

/**
 * Allows users to lookup strings from the vault and use them in the
 * apiman.properties file.
 *
 * @author eric.wittmann@redhat.com
 */
public class VaultLookup extends StrLookup {

    /**
     * @see org.apache.commons.lang.text.StrLookup#lookup(java.lang.String)
     */
    @Override
    public String lookup(String key) {
        try {
            return SecurityVaultUtil.getValueAsString(key);
        } catch (Throwable t) {
            // Eat it - if something goes wrong, too bad - we're probably not running in jboss
        }
        return null;
    }

}
