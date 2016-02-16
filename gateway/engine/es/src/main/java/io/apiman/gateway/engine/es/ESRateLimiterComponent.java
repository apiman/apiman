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
import io.searchbox.client.JestResult;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.params.Parameters;

import java.util.Map;

import org.apache.commons.codec.binary.Base64;

/**
 * An elasticsearch implementation of the rate limiter component.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESRateLimiterComponent extends AbstractESComponent implements IRateLimiterComponent {

    /**
     * Constructor.
     * @param config the configuration
     */
    public ESRateLimiterComponent(Map<String, String> config) {
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
            Get get = new Get.Builder(getIndexName(), id).type("rateBucket").build(); //$NON-NLS-1$
            JestResult result = getClient().execute(get);
            RateLimiterBucket bucket = null;
            long version;
            if (result.isSucceeded()) {
                // use the existing bucket
                version = result.getJsonObject().get("_version").getAsLong(); //$NON-NLS-1$
                bucket = result.getSourceAsObject(RateLimiterBucket.class);
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

        Index index = new Index.Builder(bucket).refresh(false).index(getIndexName())
                .setParameter(Parameters.OP_TYPE, "index") //$NON-NLS-1$
                .setParameter(Parameters.VERSION, String.valueOf(version))
                .type("rateBucket").id(id).build(); //$NON-NLS-1$
        try {
            getClient().execute(index);
            handler.handle(AsyncResultImpl.create(rlr));
        } catch (Throwable e) {
            // FIXME need to fix this now that we've switched to jest!
//            if (ESUtils.rootCause(e) instanceof VersionConflictEngineException) {
//                // If we got a version conflict, then it means some other request
//                // managed to update the ES document since we retrieved it.  Therefore
//                // everything we've done is out of date, so we should do it all
//                // over again.
//                accept(bucketId, period, limit, increment, handler);
//            } else {
                handler.handle(AsyncResultImpl.<RateLimitResponse>create(e));
//            }
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
     * @see io.apiman.gateway.engine.es.AbstractESComponent#getIndexName()
     */
    @Override
    protected String getIndexName() {
        return ESConstants.GATEWAY_INDEX_NAME;
    }

}
