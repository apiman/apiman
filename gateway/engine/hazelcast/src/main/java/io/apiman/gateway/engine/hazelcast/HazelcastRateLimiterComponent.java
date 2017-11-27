/*
 * Copyright 2017 Pete Cornish
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
package io.apiman.gateway.engine.hazelcast;

import com.hazelcast.config.Config;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.rates.RateBucketPeriod;
import io.apiman.gateway.engine.rates.RateLimiterBucket;

/**
 * Rate limiter component backed by a Hazelcast Map. This allows rate limiting
 * to be done across nodes in a cluster of gateways.
 *
 * @author Pete Cornish
 */
public class HazelcastRateLimiterComponent extends AbstractHazelcastComponent implements IRateLimiterComponent {
    private static final String STORE_NAME = "rate-limiter"; //$NON-NLS-1$

    private final Object mutex = new Object();

    /**
     * Constructor.
     */
    public HazelcastRateLimiterComponent() {
        super(STORE_NAME);
    }

    /**
     * Constructor.
     *
     * @param config the config
     */
    public HazelcastRateLimiterComponent(Config config) {
        super(STORE_NAME, config);
    }

    /**
     * @see io.apiman.gateway.engine.components.IRateLimiterComponent#accept(java.lang.String, io.apiman.gateway.engine.rates.RateBucketPeriod, long, long, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void accept(final String bucketId, final RateBucketPeriod period, final long limit,
                       final long increment, final IAsyncResultHandler<RateLimitResponse> handler) {
        RateLimiterBucket bucket;
        synchronized (mutex) {
            bucket = (RateLimiterBucket) getMap().get(bucketId);
            if (bucket == null) {
                bucket = new RateLimiterBucket();
                getMap().put(bucketId, bucket);
            }
            bucket.resetIfNecessary(period);

            RateLimitResponse response = new RateLimitResponse();
            if (bucket.getCount() > limit) {
                response.setAccepted(false);
            } else {
                response.setAccepted(bucket.getCount() < limit);
                bucket.setCount(bucket.getCount() + increment);
                bucket.setLast(System.currentTimeMillis());
            }
            int reset = (int) (bucket.getResetMillis(period) / 1000L);
            response.setReset(reset);
            response.setRemaining(limit - bucket.getCount());
            handler.handle(AsyncResultImpl.create(response));
            getMap().put(bucketId, bucket);
        }
    }
}
