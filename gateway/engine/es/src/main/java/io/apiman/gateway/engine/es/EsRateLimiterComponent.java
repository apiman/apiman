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

import io.apiman.common.es.util.AbstractEsComponent;
import io.apiman.common.es.util.EsConstants;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.rates.RateBucketPeriod;
import io.apiman.gateway.engine.rates.RateLimiterBucket;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;

import static io.apiman.gateway.engine.storage.util.BackingStoreUtil.JSON_MAPPER;

/**
 * An elasticsearch implementation of the rate limiter component.
 *
 * @author eric.wittmann@redhat.com
 */
public class EsRateLimiterComponent extends AbstractEsComponent implements IRateLimiterComponent {

    /**
     * Constructor.
     * @param config the configuration
     */
    public EsRateLimiterComponent(Map<String, String> config) {
        super(config);
    }

    /**
     * @see io.apiman.gateway.engine.components.IRateLimiterComponent#accept(java.lang.String, io.apiman.gateway.engine.rates.RateBucketPeriod, long, long, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void accept(final String bucketId, final RateBucketPeriod period, final long limit,
            final long increment, final IAsyncResultHandler<RateLimitResponse> handler) {
        final String id = id(bucketId);

        try {
            GetResponse response = getClient().get(new GetRequest(getFullIndexName()).id(id), RequestOptions.DEFAULT);
            RateLimiterBucket bucket;
            long version;
            if (response.isExists()) {
                // use the existing bucket
                version = response.getVersion();
                String sourceAsString = response.getSourceAsString();
                bucket = JSON_MAPPER.readValue(sourceAsString, RateLimiterBucket.class);
            } else {
                // make a new bucket
                version = 0;
                bucket = new RateLimiterBucket();
            }
            bucket.resetIfNecessary(period);

            final RateLimitResponse rlr = new RateLimitResponse();
            if (bucket.getCount() > limit) {
                rlr.setAccepted(false);
            } else {
                rlr.setAccepted(bucket.getCount() < limit);
                bucket.setCount(bucket.getCount() + increment);
                bucket.setLast(System.currentTimeMillis());
            }
            int reset = (int) (bucket.getResetMillis(period) / 1000L);
            rlr.setReset(reset);
            rlr.setRemaining(limit - bucket.getCount());
            updateBucketAndReturn(id, bucket, rlr, version, bucketId, period, limit, increment, handler);
        } catch (Throwable e) {
            handler.handle(AsyncResultImpl.create(e, RateLimitResponse.class));
        }
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
     * @param increment
     * @param handler
     */
    protected void updateBucketAndReturn(final String id, final RateLimiterBucket bucket,
            final RateLimitResponse rlr, final long version, final String bucketId,
            final RateBucketPeriod period, final long limit, final long increment,
            final IAsyncResultHandler<RateLimitResponse> handler) {

        try {
            IndexRequest indexRequest = new IndexRequest(getFullIndexName()).source(JSON_MAPPER.writeValueAsBytes(bucket), XContentType.JSON).id(id);
            IndexResponse response = getClient().index(indexRequest, RequestOptions.DEFAULT);
            // if we got an HTTP 409 conflict status code we try all again
            if(!response.status().equals(RestStatus.CREATED) && !response.status().equals(RestStatus.OK) && response.status().equals(RestStatus.CONFLICT)) {
                accept(bucketId, period, limit, increment, handler);
            } else {
                handler.handle(AsyncResultImpl.create(rlr));
            }

        } catch (Throwable e) {
            handler.handle(AsyncResultImpl.<RateLimitResponse>create(e));
        }
    }

    /**
     * Base64 encode the bucket ID to make an ES-compatible ID.
     * @param bucketId
     */
    private String id(String bucketId) {
        return Base64.encodeBase64String(bucketId.getBytes());
    }

    /**
     * @see AbstractEsComponent#getDefaultIndexPrefix()
     */
    @Override
    protected String getDefaultIndexPrefix() {
        return EsConstants.GATEWAY_INDEX_NAME;
    }

    /**
     * @see AbstractEsComponent#getDefaultIndices()
     * @return default indices
     */
    @Override
    protected List<String> getDefaultIndices() {
        String[] indices = {EsConstants.INDEX_RATE_BUCKET};
        return Arrays.asList(indices);
    }
    /**
     * get index full name for rate bucket
     * @return full index name
     */
    private String getFullIndexName() {
        return getIndexPrefix() + EsConstants.INDEX_RATE_BUCKET;
    }

}
