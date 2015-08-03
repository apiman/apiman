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

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

/**
 * Base class for all ISPN component impls.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractInfinispanComponent {

    private String cacheContainer;
    private String cacheName;
    private Cache<Object, Object> cache;

    /**
     * Constructor.
     * @param config the config
     */
    public AbstractInfinispanComponent(Map<String, String> config, String defaultCacheContainer, String defaultCache) {
        cacheContainer = defaultCacheContainer;
        cacheName = defaultCache;

        if (config.containsKey("cache-container")) { //$NON-NLS-1$
            cacheContainer = config.get("cache-container"); //$NON-NLS-1$
        }
        if (config.containsKey("cache")) { //$NON-NLS-1$
            cacheName = config.get("cache"); //$NON-NLS-1$
        }
    }

    /**
     * @return gets the cache
     */
    protected Cache<Object, Object> getCache() {
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
