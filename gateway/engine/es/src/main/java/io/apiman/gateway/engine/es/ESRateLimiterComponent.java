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

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.rates.RateBucketPeriod;
import io.apiman.gateway.engine.rates.RateLimiterBucket;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Base64;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.engine.VersionConflictEngineException;

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
    public void accept(final String bucketId, final RateBucketPeriod period, final int limit,
            final IAsyncResultHandler<RateLimitResponse> handler) {
        final String id = id(bucketId);
        getClient().prepareGet(ESConstants.INDEX_NAME, "rateBucket", id) //$NON-NLS-1$
            .setFetchSource(true)
            .execute(new ActionListener<GetResponse>() {
                @Override
                public void onResponse(GetResponse response) {
                    RateLimiterBucket bucket = null;
                    long version;
                    if (response.isExists()) {
                        // use the existing bucket
                        version = response.getVersion();
                        bucket = readBucket(response);
                    } else {
                        // make a new bucket
                        version = 0;
                        bucket = new RateLimiterBucket();
                    }
                    bucket.resetIfNecessary(period);

                    final RateLimitResponse rlr = new RateLimitResponse();
                    if (bucket.count >= limit) {
                        rlr.setAccepted(false);
                    } else {
                        bucket.count++;
                        bucket.last = System.currentTimeMillis();
                        rlr.setAccepted(true);
                    }
                    int reset = (int) (bucket.getResetMillis(period) / 1000L);
                    rlr.setReset(reset);
                    rlr.setRemaining(limit - bucket.count);
                    updateBucketAndReturn(id, bucket, rlr, version, bucketId, period, limit, handler);
                }
                @Override
                public void onFailure(Throwable e) {
                    handler.handle(AsyncResultImpl.create(e, RateLimitResponse.class));
                }
            });
    }

    /**
     * Unmarshal a rate limiter bucket from the information in ES.
     * @param response
     */
    protected RateLimiterBucket readBucket(GetResponse response) {
        RateLimiterBucket bucket = new RateLimiterBucket();
        Map<String, Object> source = response.getSourceAsMap();
        bucket.count = ((Number) source.get("count")).intValue(); //$NON-NLS-1$
        bucket.last = ((Number) source.get("last")).longValue(); //$NON-NLS-1$
        return bucket;
    }

    /**
     * Update the bucket in ES and then return the rate limit response to the
     * original handler.  If the update fails because we have a stale version,
     * then try the whole thing again (because we conflicted with another
     * request).
     * @param id
     * @param bucket
     * @param rlr
     * @param version
     * @param limit 
     * @param period 
     * @param bucketId 
     * @param handler
     */
    protected void updateBucketAndReturn(final String id, final RateLimiterBucket bucket,
            final RateLimitResponse rlr, final long version, final String bucketId,
            final RateBucketPeriod period, final int limit,
            final IAsyncResultHandler<RateLimitResponse> handler) {
        Map<String,Object> source = new HashMap<>();
        source.put("count", bucket.count); //$NON-NLS-1$
        source.put("last", bucket.last); //$NON-NLS-1$
        getClient().prepareIndex(ESConstants.INDEX_NAME, "rateBucket", id) //$NON-NLS-1$
            .setVersion(version)
            .setContentType(XContentType.JSON)
            .setCreate(false)
            .setSource(source)
            .execute(new ActionListener<IndexResponse>() {
                @Override
                public void onResponse(IndexResponse response) {
                    handler.handle(AsyncResultImpl.create(rlr));
                }
                @Override
                public void onFailure(Throwable e) {
                    if (ESUtils.rootCause(e) instanceof VersionConflictEngineException) {
                        // If we got a version conflict, then it means some other request
                        // managed to update the ES document since we retrieved it.  Therefore
                        // everything we've done is out of date, so we should do it all
                        // over again.
                        accept(bucketId, period, limit, handler);
                    } else {
                        handler.handle(AsyncResultImpl.<RateLimitResponse>create(e));
                    }
                }
            });
    }

    /**
     * Base64 encode the bucket ID to make an ES-compatible ID.
     * @param bucketId
     */
    private String id(String bucketId) {
        return Base64.encodeBytes(bucketId.getBytes());
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
