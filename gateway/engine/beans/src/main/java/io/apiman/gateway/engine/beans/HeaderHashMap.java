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
package io.apiman.gateway.engine.beans;

import io.apiman.common.util.SimpleStringUtils;

import java.util.HashMap;

/**
 * Extends the basic {@link HashMap} class in order to provide case insensitive 
 * lookups.
 *
 * @author eric.wittmann@redhat.com
 */
public class HeaderHashMap extends HashMap<String, String> {
    
    private static final long serialVersionUID = -8627183971399152775L;

    private HashMap<String, String> caseInsensitiveIndex = new HashMap<String, String>();
    
    /**
     * Constructor.
     */
    public HeaderHashMap() {
    }
    
    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
        String rval = super.get(key);
        if (rval == null) {
            String trimmedKey = SimpleStringUtils.trim((String)key);
            rval = caseInsensitiveIndex.get(trimmedKey.toLowerCase());
        }
        return rval;
    }
    
    /**
     * @see java.util.HashMap#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        boolean rval = super.containsKey(key);
        if (!rval) {
            String trimmedKey = SimpleStringUtils.trim((String)key);
            rval = caseInsensitiveIndex.containsKey(trimmedKey.toLowerCase());
        }
        return rval;
    }
    
    /**
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public String put(String key, String value) {
        String trimmedKey = SimpleStringUtils.trim(key);
        String trimmedValue = SimpleStringUtils.trim(value);
        String rval = super.put(trimmedKey, trimmedValue);
        caseInsensitiveIndex.put(trimmedKey.toLowerCase(), trimmedValue);
        return rval;
    }
    
    /**
     * @see java.util.HashMap#remove(java.lang.Object)
     */
    @Override
    public String remove(Object key) {
        String trimmedKey = SimpleStringUtils.trim((String)key);
        caseInsensitiveIndex.remove(trimmedKey.toLowerCase());
        return super.remove(trimmedKey);
    }
    
}