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
package io.apiman.plugins.cors_policy.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper methods and variables.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HttpHelper {
    // Verbs
    public static final String GET = "GET"; //$NON-NLS-1$
    public static final String HEAD = "HEAD"; //$NON-NLS-1$
    public static final String POST = "POST";//$NON-NLS-1$
    public static final String OPTIONS = "OPTIONS";//$NON-NLS-1$

    // Simple verbs
    private static final Set<String> SIMPLE_METHODS = new HashSet<>(Arrays.asList(
                new String[] { GET, HEAD, POST }
            ));

    // If Content-Type header is requested, then value must be one of following, else requires preflight
    private static final Set<String> SIMPLE_CONTENT_TYPES = new InsensitiveLinkedHashSet(Arrays.asList(
                new String[] { "application/x-www-form-urlencoded", "multipart/form-data", "text/plain" } //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            ));

    // Any header request other than the following requires preflight. Many browsers don't bother asking if from known list.
    private static final Set<String> SIMPLE_HEADER_TYPES = new InsensitiveLinkedHashSet(Arrays.asList(
                new String[] { "Cache-Control", "Content-Language", "Content-Type",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                "Expires", "Last-Modified", "Pragma" } //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            ));

    private HttpHelper() {}

    /**
     * @param method http method
     * @return Whether the given HTTP method is simple as defined by specification
     */
    public static boolean isSimpleMethod(String... method) {
        return containsAll(SIMPLE_METHODS, method);
    }

    public static boolean isSimpleContentType(String... type) {
        return containsAll(SIMPLE_CONTENT_TYPES, type);
    }

    public static boolean isSimpleHeader(String... header) {
        return containsAll(SIMPLE_HEADER_TYPES, header);
    }

    public static boolean containsAll(Set<String> set, String[] values) {
        boolean collect = true;

        for(String entry : values) {
            collect = collect && set.contains(entry);

            if(collect == false)
                break;
        }

        return collect;
    }
}
