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
package org.overlord.apiman.dt.ui.client.local.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Simple utility for dealing with Multimaps.
 *
 * @author eric.wittmann@redhat.com
 */
public class MultimapUtil {
    
    /**
     * Creates a multimap from a key and value.
     * @param key
     * @param value
     */
    public static final Multimap<String, String> singleItemMap(String key, String value) {
        HashMultimap<String, String> multimap = HashMultimap.create();
        multimap.put(key, value);
        return multimap;
    }
    
    /**
     * Creates a multimap out of a variable number of keys and values.  This should
     * always be called with an even number of arguments, otherwise an {@link ArrayIndexOutOfBoundsException}
     * is likely to occur.
     * @param data
     */
    public static final Multimap<String, String> fromMultiple(String ... data) {
        HashMultimap<String, String> multimap = HashMultimap.create();
        if (data == null)
            return multimap;
        for (int i = 0; i < data.length; i+=2) {
            String key = data[i];
            String val = data[i+1];
            if (val != null) {
                multimap.put(key, val);
            }
        }
        return multimap;
    }

}
