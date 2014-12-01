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
package io.apiman.manager.api.ispn;

import io.apiman.common.auth.ISharedSecretSource;

import java.util.UUID;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

/**
 * A shared secret source that stores the shared secret in an 
 * Infinispan cache.
 *
 * @author eric.wittmann@redhat.com
 */
public class InfinispanSharedSecretSource implements ISharedSecretSource {

    private static final String DEFAULT_CACHE_CONTAINER = "java:jboss/infinispan/container/apiman-manager"; //$NON-NLS-1$
    private static final String DEFAULT_CACHE = "auth"; //$NON-NLS-1$

    private static final String SHARED_SECRET_KEY = "AUTH_SHARED_SECRET"; //$NON-NLS-1$

    private String cacheContainer;
    private String cacheName;
    
    private Cache<Object, Object> cache;
    
    private String secret;
    private Object mutex = new Object();

    /**
     * Constructor.
     */
    public InfinispanSharedSecretSource() {
        cacheContainer = System.getProperty("apiman.manager.auth.cache-container", DEFAULT_CACHE_CONTAINER); //$NON-NLS-1$
        cacheName = System.getProperty("apiman.manager.auth.cache", DEFAULT_CACHE); //$NON-NLS-1$
    }
    
    /**
     * @see io.apiman.common.auth.ISharedSecretSource#getSharedSecret()
     */
    @Override
    public String getSharedSecret() {
        if (secret != null) {
            return secret;
        }
        synchronized (mutex) {
            if (secret == null) {
                if (getCache().containsKey(SHARED_SECRET_KEY)) {
                    secret = String.valueOf(getCache().get(SHARED_SECRET_KEY));
                } else {
                    secret = UUID.randomUUID().toString();
                    getCache().put(SHARED_SECRET_KEY, secret);
                }
            }
            return secret;
        }
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
