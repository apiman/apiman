/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.apiman.dt.api.security.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Message bundle accessor for i18n.
 *
 * @author eric.wittmann@redhat.com
 */
public class Messages {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(Messages.class.getPackage().getName() + ".messages"); //$NON-NLS-1$

    /**
     * Constructor.
     */
    private Messages() {
    }

    /**
     * Gets a string from the bundle.
     * @param key
     * @return the resolved string or !key! if missing
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
