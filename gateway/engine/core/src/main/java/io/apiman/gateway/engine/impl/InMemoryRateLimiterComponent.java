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
import io.apiman.gateway.engine.rates.RateBucketPeriod;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple in-memory implementation of a rate limiter.  This is non-persistent
 * and does not work in a cluster.
 *
 * @author eric.wittmann@redhat.com
 */
public class InMemoryRateLimiterComponent implements IRateLimiterComponent {
    
    private Map<String, RateLimiterBucket> buckets = new HashMap<String, RateLimiterBucket>();

    /**
     * Constructor.
     */
    public InMemoryRateLimiterComponent() {
    }

    /**
     * @see io.apiman.gateway.engine.components.IRateLimiterComponent#accept(java.lang.String, io.apiman.gateway.engine.rates.RateBucketPeriod, int, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void accept(String bucketId, RateBucketPeriod period, int limit, IAsyncResultHandler<Boolean> handler) {
        RateLimiterBucket bucket = null;
        synchronized (buckets) {
            bucket = buckets.get(bucketId);
            if (bucket == null) {
                bucket = new RateLimiterBucket();
                buckets.put(bucketId, bucket);
            }
        }
        
        synchronized (bucket.mutex) {
            bucket.resetIfNecessary(period);
            if (bucket.count >= limit) {
                handler.handle(AsyncResultImpl.<Boolean>create(Boolean.FALSE));
            } else {
                bucket.count++;
                bucket.last = System.currentTimeMillis();
                handler.handle(AsyncResultImpl.<Boolean>create(Boolean.TRUE));
            }
        }
    }
    
    private static class RateLimiterBucket {
        public int count = 0;
        public long last = System.currentTimeMillis();
        public Object mutex = new Object();
        
        /**
         * Resets the count if the period boundary has been crossed.
         * @param period
         */
        public void resetIfNecessary(RateBucketPeriod period) {
            long periodBoundary = getLastPeriodBoundary(period);
            if (System.currentTimeMillis() >= periodBoundary) {
                count = 0;
            }
        }

        /**
         * Gets the period boundary for the period bounding the last
         * request.
         * @param period
         */
        private long getLastPeriodBoundary(RateBucketPeriod period) {
            Calendar lastCal = Calendar.getInstance();
            lastCal.setTimeInMillis(last);
            switch (period) {
            case Second:
                lastCal.set(Calendar.MILLISECOND, 0);
                lastCal.add(Calendar.SECOND, 1);
                return lastCal.getTimeInMillis();
            case Minute:
                lastCal.set(Calendar.MILLISECOND, 0);
                lastCal.set(Calendar.SECOND, 0);
                lastCal.add(Calendar.MINUTE, 1);
                return lastCal.getTimeInMillis();
            case Hour:
                lastCal.set(Calendar.MILLISECOND, 0);
                lastCal.set(Calendar.SECOND, 0);
                lastCal.set(Calendar.MINUTE, 0);
                lastCal.add(Calendar.HOUR_OF_DAY, 1);
                return lastCal.getTimeInMillis();
            case Day:
                lastCal.set(Calendar.MILLISECOND, 0);
                lastCal.set(Calendar.SECOND, 0);
                lastCal.set(Calendar.MINUTE, 0);
                lastCal.set(Calendar.HOUR_OF_DAY, 0);
                lastCal.add(Calendar.DAY_OF_YEAR, 1);
                return lastCal.getTimeInMillis();
            case Month:
                lastCal.set(Calendar.MILLISECOND, 0);
                lastCal.set(Calendar.SECOND, 0);
                lastCal.set(Calendar.MINUTE, 0);
                lastCal.set(Calendar.HOUR_OF_DAY, 0);
                lastCal.add(Calendar.DAY_OF_MONTH, 1);
                return lastCal.getTimeInMillis();
            case Year:
                lastCal.set(Calendar.MILLISECOND, 0);
                lastCal.set(Calendar.SECOND, 0);
                lastCal.set(Calendar.MINUTE, 0);
                lastCal.set(Calendar.HOUR_OF_DAY, 0);
                lastCal.set(Calendar.DAY_OF_YEAR, 0);
                lastCal.add(Calendar.YEAR, 1);
                return lastCal.getTimeInMillis();
            }
            return Long.MAX_VALUE;
        }
    }

}
