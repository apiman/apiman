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
package io.apiman.gateway.engine.ispn.io;

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.infinispan.Cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

/**
 * Wraps a cache.  Stores serialized versions of the objects
 * rather than the objects themselves.  This is to avoid 
 * classloader problems between the Gateway API and the 
 * Gateway.
 *
 * @author eric.wittmann@redhat.com
 */
public class RegistryCacheMapWrapper implements Map<String, Object> {

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.setDateFormat(new ISO8601DateFormat());
    }

    private Cache<Object,Object> cache;
    
    /**
     * Constructor.
     * 
     * @param cache the cache
     */
    public RegistryCacheMapWrapper(Cache<Object,Object> cache) {
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
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        Object value = cache.get(key);
        if (value != null) {
            try {
                if (key.toString().startsWith("API::")) { //$NON-NLS-1$
                    value = unmarshalAs(value.toString(), Api.class);
                } else {
                    value = unmarshalAs(value.toString(), Client.class);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return value;
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(String key, Object value) {
        try {
            value = mapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cache.put(key, value);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key) {
        Object value = cache.remove(key);
        if (value != null) {
            try {
                if (key.toString().startsWith("API::")) { //$NON-NLS-1$
                    value = unmarshalAs(value.toString(), Api.class);
                } else if (key.toString().startsWith("CLIENT::")) { //$NON-NLS-1$
                    value = unmarshalAs(value.toString(), Client.class);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return value;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Map#values()
     */
    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unmarshall the given type of object.
     * @param valueAsString
     * @param asClass
     * @throws IOException 
     */
    private <T> T unmarshalAs(String valueAsString, Class<T> asClass) throws IOException {
        return mapper.reader(asClass).readValue(valueAsString);
    }

}
