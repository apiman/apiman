/*
 * Copyright 2018 Pete Cornish
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
package io.apiman.gateway.engine.storage.component;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.rates.RateBucketPeriod;
import io.apiman.gateway.engine.rates.RateLimiterBucket;
import io.apiman.gateway.engine.storage.store.IBackingStoreProvider;

/**
 * Rate limiter component backed by a store.
 *
 * @author Pete Cornish
 */
public abstract class AbstractRateLimiterComponent extends AbstractStorageComponent implements IRateLimiterComponent {
    protected static final String STORE_NAME = "rate-limiter"; //$NON-NLS-1$

    private final Object mutex = new Object();

    /**
     * Constructor.
     */
    public AbstractRateLimiterComponent(IBackingStoreProvider storeProvider) {
        super(storeProvider, STORE_NAME);
    }

    /**
     * @see IRateLimiterComponent#accept(String, RateBucketPeriod, long, long, IAsyncResultHandler)
     */
    @Override
    public void accept(final String bucketId, final RateBucketPeriod period, final long limit,
                       final long increment, final IAsyncResultHandler<RateLimitResponse> handler) {
        RateLimiterBucket bucket;
        synchronized (mutex) {
            bucket = getStore().get(bucketId, RateLimiterBucket.class);
            if (bucket == null) {
                bucket = new RateLimiterBucket();
                getStore().put(bucketId, bucket);
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
            getStore().put(bucketId, bucket);
        }
    }
}
