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
package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.rates.RateBucketPeriod;
import io.apiman.gateway.engine.rates.RateLimiterBucket;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple in-memory implementation of a rate limiter.  This is non-persistent
 * and does not work in a cluster.
 *
 * @author eric.wittmann@redhat.com
 */
public class InMemoryRateLimiterComponent implements IRateLimiterComponent {

    private static final Map<String, RateLimiterBucket> buckets = new HashMap<>();

    /**
     * Constructor.
     */
    public InMemoryRateLimiterComponent() {
    }

    /**
     * @see io.apiman.gateway.engine.components.IRateLimiterComponent#accept(java.lang.String, io.apiman.gateway.engine.rates.RateBucketPeriod, long, long, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void accept(String bucketId, RateBucketPeriod period, long limit, long increment, IAsyncResultHandler<RateLimitResponse> handler) {
        RateLimiterBucket bucket;
        RateLimitResponse response = new RateLimitResponse();
        int reset = 0;
        synchronized (buckets) {
            bucket = buckets.get(bucketId);
            if (bucket == null) {
                bucket = new RateLimiterBucket();
                buckets.put(bucketId, bucket);
            }
            bucket.resetIfNecessary(period);
            if (bucket.getCount() > limit) {
                response.setAccepted(false);
            } else {
                response.setAccepted(bucket.getCount() < limit);
                bucket.setCount(bucket.getCount() + increment);
                bucket.setLast(System.currentTimeMillis());
            }
            reset = (int) (bucket.getResetMillis(period) / 1000L);
        }
        response.setReset(reset);
        response.setRemaining(limit - bucket.getCount());
        handler.handle(AsyncResultImpl.create(response));
    }

}
