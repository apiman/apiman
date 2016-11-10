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
package io.apiman.manager.api.beans;

import com.ibm.icu.text.Transliterator;

/**
 * Some simple bean utils.
 *
 * @author eric.wittmann@redhat.com
 */
public class BeanUtils {

    private BeanUtils() {
    }

    /**
     * Creates a bean id from the given bean name.
     * @param name the name
     * @return the id
     */
    public static final String idFromName(String name) {
        Transliterator tr = Transliterator.getInstance("Any-Latin");
        return removeNonWord(tr.transliterate(name));
    }

    /**
     * This essentially removes any non "word" characters from the name.
     * @param name the name
     * @return the id
     */
    private static String removeNonWord(String name) {
        return name.replaceAll("[^\\w-\\.]", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Validates that a version string is OK - doesn't contain any
     * illegal characters.
     * @param version the version
     * @return true if valid, else false
     */
    public static final boolean isValidVersion(String version) {
        return removeNonWord(version).equals(version);
    }
}
