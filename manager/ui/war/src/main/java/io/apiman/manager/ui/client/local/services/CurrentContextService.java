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
package io.apiman.manager.ui.client.local.services;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

/**
 * Provides global application context.  Allows components (e.g. pages) to
 * be loosely coupled but still pass information back and forth.  Also allows
 * a page to remember its previous state, should that be useful.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class CurrentContextService {
    
    private Map<String, Object> context = new HashMap<String, Object>();
    
    /**
     * Constructor.
     */
    public CurrentContextService() {
    }
    
    /**
     * Gets an attribute.
     * @param key
     */
    public Object getAttribute(String key) {
        return context.get(key);
    }

    /**
     * Gets an attribute (defaultValue is returned if not found).
     * @param key
     * @param defaultValue
     */
    public Object getAttribute(String key, Object defaultValue) {
        Object rval = getAttribute(key);
        if (rval == null)
            rval = defaultValue;
        return rval;
    }

    /**
     * Sets an attribute.
     * @param key
     * @param value
     */
    public void setAttribute(String key, Object value) {
        context.put(key, value);
    }
    
    /**
     * Removes an attribute.
     * @param key
     */
    public void removeAttribute(String key) {
        context.remove(key);
    }
}
