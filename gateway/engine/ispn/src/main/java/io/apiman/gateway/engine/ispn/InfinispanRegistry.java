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
import io.apiman.gateway.engine.ispn.io.RegistryCacheMapWrapper;

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
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
    
    private static final String DEFAULT_CACHE_CONTAINER = "java:jboss/infinispan/apiman"; //$NON-NLS-1$
    private static final String DEFAULT_CACHE = "registry"; //$NON-NLS-1$
    
    private String cacheContainer;
    private String cacheName;
    
    private Cache<Object, Object> cache;
    private Map<String, Object> cacheWrapper;
    
    /**
     * Constructor.
     * @param config
     */
    public InfinispanRegistry(Map<String, String> config) {
        cacheContainer = config.get("cache.container"); //$NON-NLS-1$
        cacheName = config.get("cache.name"); //$NON-NLS-1$
        
        if (StringUtils.isEmpty(cacheContainer)) {
            cacheContainer = DEFAULT_CACHE_CONTAINER;
        }
        if (StringUtils.isEmpty(cacheName)) {
            cacheName = DEFAULT_CACHE;
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.impl.InMemoryRegistry#getMap()
     */
    @Override
    public Map<String, Object> getMap() {
        if (cacheWrapper == null) {
            cacheWrapper = new RegistryCacheMapWrapper(getCache());
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
