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
package io.apiman.gateway.engine.ispn;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.infinispan.Cache;

/**
 * Wraps a cache.
 *
 * @author eric.wittmann@redhat.com
 */
public class CacheMapWrapper implements Map<String, Object> {
    
    private Cache<Object,Object> cache;
    
    /**
     * Constructor.
     * 
     * @param cache the cache
     */
    public CacheMapWrapper(Cache<Object,Object> cache) {
        this.cache = cache;
    }

    /**
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return cache.size();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return cache.containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return cache.containsValue(value);
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        return cache.get(key);
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(String key, Object value) {
        return cache.put(key, value);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key) {
        return cache.remove(key);
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        cache.putAll(m);
    }

    /**
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        cache.clear();
    }

    /**
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        throw new RuntimeException("Unsupported operation."); //$NON-NLS-1$
    }

    /**
     * @see java.util.Map#values()
     */
    @Override
    public Collection<Object> values() {
        return cache.values();
    }

    /**
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        throw new RuntimeException("Unsupported operation."); //$NON-NLS-1$
    }

}
