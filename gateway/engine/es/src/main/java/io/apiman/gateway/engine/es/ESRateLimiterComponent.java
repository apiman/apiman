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
package io.apiman.gateway.engine.es;

import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.rates.RateBucketPeriod;

import java.util.Map;

import org.elasticsearch.client.Client;

/**
 * An elasticsearch implementation of the rate limiter component.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESRateLimiterComponent implements IRateLimiterComponent {
    
    private Map<String, String> config;
    private Client esClient;

    /**
     * Constructor.
     * @param config
     */
    public ESRateLimiterComponent(Map<String, String> config) {
        this.config = config;
    }

    /**
     * @see io.apiman.gateway.engine.components.IRateLimiterComponent#accept(java.lang.String, io.apiman.gateway.engine.rates.RateBucketPeriod, int, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void accept(String bucketId, RateBucketPeriod period, int limit,
            IAsyncResultHandler<RateLimitResponse> handler) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @return the esClient
     */
    public synchronized Client getClient() {
        if (esClient == null) {
            esClient = ESClientFactory.createClient(config);
        }
        return esClient;
    }

}
