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
package io.apiman.gateway.engine.ispn;

import io.apiman.gateway.engine.impl.InMemoryRegistry;

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

/**
 * An implementation of the Registry that uses infinispan as a storage
 * mechanism.  This is useful because an ISPN cache can be configured
 * in many different ways.
 *
 * @author eric.wittmann@redhat.com
 */
public class InfinispanRegistry extends InMemoryRegistry {
    
    private static final String DEFAULT_CACHE_CONTAINER = "java:jboss/infinispan/container/apiman-gateway"; //$NON-NLS-1$
    private static final String DEFAULT_CACHE = "registry"; //$NON-NLS-1$
    
    private String cacheContainer;
    private String cacheName;
    
    private Cache<Object, Object> cache;
    private Map<String, Object> cacheWrapper;
    
    /**
     * Constructor.
     */
    public InfinispanRegistry() {
        cacheContainer = DEFAULT_CACHE_CONTAINER;
        cacheName = DEFAULT_CACHE;
    }
    
    /**
     * Constructor.
     * @param cacheContainer the cache container
     * @param cacheName the cache name
     */
    public InfinispanRegistry(String cacheContainer, String cacheName) {
        this.cacheContainer = cacheContainer;
        this.cacheName = cacheName;
    }
    
    /**
     * @see io.apiman.gateway.engine.impl.InMemoryRegistry#getMap()
     */
    @Override
    protected Map<String, Object> getMap() {
        if (cacheWrapper == null) {
            cacheWrapper = new CacheMapWrapper(getCache());
        }
        return cacheWrapper;
    }

    /**
     * @return gets the registry cache
     */
    private Cache<Object, Object> getCache() {
        if (cache != null) {
            return cache;
        }
        
        try {
            InitialContext ic = new InitialContext();
            CacheContainer container = (CacheContainer) ic.lookup(cacheContainer);
            cache = container.getCache(cacheName);
            return cache;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

}
