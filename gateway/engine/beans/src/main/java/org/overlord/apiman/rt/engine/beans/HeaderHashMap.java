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
package org.overlord.apiman.rt.engine.beans;

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
            rval = caseInsensitiveIndex.get(((String) key).toLowerCase());
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
            rval = caseInsensitiveIndex.containsKey(((String) key).toLowerCase());
        }
        return rval;
    }
    
    /**
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public String put(String key, String value) {
        String rval = super.put(key, value);
        caseInsensitiveIndex.put(key.toLowerCase(), value);
        return rval;
    }
    
    /**
     * @see java.util.HashMap#remove(java.lang.Object)
     */
    @Override
    public String remove(Object key) {
        caseInsensitiveIndex.remove(((String) key).toLowerCase());
        return super.remove(key);
    }
    
}