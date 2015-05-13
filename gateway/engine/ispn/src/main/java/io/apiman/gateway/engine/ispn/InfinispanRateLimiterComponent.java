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

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.rates.RateBucketPeriod;
import io.apiman.gateway.engine.rates.RateLimiterBucket;

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

/**
 * Rate limiter component backed by an Infinispan cache.  This allows rate limiting
 * to be done across nodes in a cluster of gateways.
 *
 * @author eric.wittmann@redhat.com
 */
public class InfinispanRateLimiterComponent implements IRateLimiterComponent {

    private static final String DEFAULT_CACHE_CONTAINER = "java:jboss/infinispan/container/apiman-gateway"; //$NON-NLS-1$
    private static final String DEFAULT_CACHE = "rate-limiter"; //$NON-NLS-1$

    private String cacheContainer;
    private String cacheName;
    
    private Cache<Object, Object> cache;
    private Object mutex = new Object();
    
    /**
     * Constructor.
     */
    public InfinispanRateLimiterComponent() {
        cacheContainer = DEFAULT_CACHE_CONTAINER;
        cacheName = DEFAULT_CACHE;
    }

    /**
     * Constructor.
     * @param config the config
     */
    public InfinispanRateLimiterComponent(Map<String, String> config) {
        cacheContainer = DEFAULT_CACHE_CONTAINER;
        cacheName = DEFAULT_CACHE;
        
        if (config.containsKey("cache-container")) { //$NON-NLS-1$
            cacheContainer = config.get("cache-container"); //$NON-NLS-1$
        }
        if (config.containsKey("cache")) { //$NON-NLS-1$
            cacheName = config.get("cache"); //$NON-NLS-1$
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

    /**
     * @see io.apiman.gateway.engine.components.IRateLimiterComponent#accept(java.lang.String, io.apiman.gateway.engine.rates.RateBucketPeriod, int, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void accept(String bucketId, RateBucketPeriod period, int limit,
            IAsyncResultHandler<RateLimitResponse> handler) {
        RateLimiterBucket bucket = null;
        synchronized (mutex) {
            bucket = (RateLimiterBucket) getCache().get(bucketId);
            if (bucket == null) {
                bucket = new RateLimiterBucket();
                getCache().put(bucketId, bucket);
            }
            bucket.resetIfNecessary(period);
            
            RateLimitResponse response = new RateLimitResponse();
            if (bucket.getCount() >= limit) {
                response.setAccepted(false);
            } else {
                bucket.setCount(bucket.getCount() + 1);
                bucket.setLast(System.currentTimeMillis());
                response.setAccepted(true);
            }
            int reset = (int) (bucket.getResetMillis(period) / 1000L);
            response.setReset(reset);
            response.setRemaining(limit - bucket.getCount());
            handler.handle(AsyncResultImpl.<RateLimitResponse>create(response));
            getCache().put(bucketId, bucket);
        }
    }
}
