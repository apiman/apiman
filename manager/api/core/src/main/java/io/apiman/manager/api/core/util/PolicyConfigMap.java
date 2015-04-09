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
package io.apiman.manager.api.core.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper around a map so that mvel doesn't fail when a property is missing.  Instead it
 * will get a 'null' value.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyConfigMap implements Map<String, Object> {
    
    private Map<String, Object> delegate;
    
    /**
     * Constructor.
     * @param delegate the delegate map
     */
    public PolicyConfigMap(Map<String, Object> delegate) {
        this.delegate = delegate;
    }

    /**
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return true;
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        if (delegate.containsKey(key)) {
            return delegate.get(key);
        } else {
            return null;
        }
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(String key, Object value) {
        return delegate.put(key, value);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        delegate.putAll(m);
    }

    /**
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        delegate.clear();
    }

    /**
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    /**
     * @see java.util.Map#values()
     */
    @Override
    public Collection<Object> values() {
        return delegate.values();
    }

    /**
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return delegate.entrySet();
    }

}
