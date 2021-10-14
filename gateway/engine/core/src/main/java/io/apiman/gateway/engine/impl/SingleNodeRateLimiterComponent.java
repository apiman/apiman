/*
 * Copyright 2016 JBoss Inc
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

import io.apiman.common.config.ConfigDirectoryFinder;
import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.rates.RateBucketPeriod;
import io.apiman.gateway.engine.rates.RateLimiterBucket;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of the rate limiter component that is optimized for use
 * in a single node environment.  
 * @author eric.wittmann@gmail.com
 */
public class SingleNodeRateLimiterComponent implements IRateLimiterComponent {

    private final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(SingleNodeRateLimiterComponent.class);
    private final boolean isPersistent;

    private final Map<String, RateLimiterBucket> buckets = new HashMap<>();
    private File savedRates;
    private long lastSavedOn;
    private long lastModifiedOn;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    
    /**
     * Constructor.
     */
    public SingleNodeRateLimiterComponent() {
        isPersistent = false;
    }
    
    /**
     * Constructor.
     * @param config
     */
    public SingleNodeRateLimiterComponent(Map<String, String> config) {
        isPersistent = "true".equals(config.get("persistence.enabled"));  //$NON-NLS-1$//$NON-NLS-2$
        if (isPersistent) {
            String ratesPathRaw = config.get("persistence.file"); //$NON-NLS-1$
            Path ratesFilePath;
            if (ratesPathRaw == null || ratesPathRaw.isBlank()) {
                ratesFilePath = ConfigDirectoryFinder.getDataDirectory().resolve("apiman-single-node-rate-limiting.properties");
            } else {
                ratesFilePath = Paths.get(ratesPathRaw);
            }
            LOGGER.info("Rate limiting data will be loaded from {0}", ratesFilePath);
            savedRates = ratesFilePath.toFile();
            if (savedRates.isFile()) {
                loadBuckets();
            }
            startBucketSavingThread(config);
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.IRateLimiterComponent#accept(java.lang.String, io.apiman.gateway.engine.rates.RateBucketPeriod, long, long, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void accept(String bucketId, RateBucketPeriod period, long limit, long increment,
            IAsyncResultHandler<RateLimitResponse> handler) {
        RateLimiterBucket bucket;
        RateLimitResponse response = new RateLimitResponse();
        synchronized (buckets) {
            bucket = buckets.get(bucketId);
            if (bucket == null) {
                bucket = new RateLimiterBucket();
                buckets.put(bucketId, bucket);
            }
            boolean rateModified = bucket.resetIfNecessary(period);
            if (bucket.getCount() > limit) {
                response.setAccepted(false);
            } else {
                response.setAccepted(bucket.getCount() < limit);
                bucket.setCount(bucket.getCount() + increment);
                bucket.setLast(System.currentTimeMillis());
                rateModified = true;
            }
            int reset = (int) (bucket.getResetMillis(period) / 1000L);
            response.setReset(reset);
            if (rateModified) {
                lastModifiedOn = System.currentTimeMillis();
            }
        }
        response.setRemaining(limit - bucket.getCount());
        handler.handle(AsyncResultImpl.create(response));
    }

    /**
     * Save the buckets to file (persist them) from time to time.  This is done 
     * in a thread so that it does not impact performance of the rate limits.
     * @param config
     */
    private void startBucketSavingThread(Map<String, String> config) {
        String delay = config.get("persistence.delay"); //$NON-NLS-1$
        if (delay == null) {
            delay = "5"; //$NON-NLS-1$
        }
        String period = config.get("persistence.period"); //$NON-NLS-1$
        if (period == null) {
            period = "10"; //$NON-NLS-1$
        }
        executor.scheduleAtFixedRate(this::saveBuckets, Long.parseLong(delay), Long.parseLong(period), TimeUnit.SECONDS);
    }

    /**
     * Saves the current list of buckets into a file.
     */
    private void saveBuckets() {
        if (lastModifiedOn > lastSavedOn) {
            LOGGER.info("Persisting current rates to: {0}", savedRates); //$NON-NLS-1$
            Properties props = new Properties();
            for (Entry<String, RateLimiterBucket> entry : buckets.entrySet()) {
                String value = entry.getValue().getCount() + "|" + entry.getValue().getLast(); //$NON-NLS-1$
                props.setProperty(entry.getKey(), value);
            }
            try (FileWriter writer = new FileWriter(savedRates)) {
                props.store(writer, "All Apiman rate limits"); //$NON-NLS-1$
            } catch (IOException e) {
                e.printStackTrace();
            }
            lastSavedOn = System.currentTimeMillis();
        }
    }

    /**
     * Loads the saved rates from a file.  Done only on startup.
     */
    private void loadBuckets() {
        Properties props = new Properties();
        try (FileReader reader = new FileReader(savedRates)) {
            props.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        for (Entry<Object, Object> entry : props.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            String [] split = value.split("\\|"); //$NON-NLS-1$
            long count = Long.parseLong(split[0]);
            long last = Long.parseLong(split[1]);
            RateLimiterBucket bucket = new RateLimiterBucket();
            bucket.setCount(count);
            bucket.setLast(last);
            buckets.put(key, bucket);
        }
    }

}
