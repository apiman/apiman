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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policies.config.TransferDirectionType;
import io.apiman.gateway.engine.policies.config.TransferQuotaConfig;
import io.apiman.gateway.engine.policies.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.rates.RateBucketPeriod;

import java.util.Map;

/**
 * Policy that enforces transfer quotas.
 *
 * @author eric.wittmann@redhat.com
 */
public class TransferQuotaPolicy extends AbstractMappedDataPolicy<TransferQuotaConfig> {

    private static final String BUCKET_ID_ATTR = TransferQuotaPolicy.class.getName() + ".bucketId"; //$NON-NLS-1$
    private static final String PERIOD_ATTR = TransferQuotaPolicy.class.getName() + ".period"; //$NON-NLS-1$
    private static final String BYTES_UPLOADED_ATTR = TransferQuotaPolicy.class.getName() + ".bytesUploaded"; //$NON-NLS-1$
    private static final String RATE_LIMIT_RESPONSE_ATTR = TransferQuotaPolicy.class.getName() + ".rateLimitResponse"; //$NON-NLS-1$

    private static final String DEFAULT_LIMIT_HEADER = "X-TransferQuota-Limit"; //$NON-NLS-1$
    private static final String DEFAULT_REMAINING_HEADER = "X-TransferQuota-Remaining"; //$NON-NLS-1$
    private static final String DEFAULT_RESET_HEADER = "X-TransferQuota-Reset"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public TransferQuotaPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.AbstractPolicy#getConfigurationClass()
     */
    @Override
    protected Class<TransferQuotaConfig> getConfigurationClass() {
        return TransferQuotaConfig.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(final ApiRequest request, final IPolicyContext context, final TransferQuotaConfig config,
            final IPolicyChain<ApiRequest> chain) {
        // *************************************************************
        // Step 1:  check to see if we're already in violation of this
        //          policy.  If so, fail fast.
        // *************************************************************
        String bucketId = "XFERQUOTA||" + RateLimitingPolicy.bucketId(request, config); //$NON-NLS-1$
        final RateBucketPeriod period = RateLimitingPolicy.getPeriod(config);

        if (bucketId == RateLimitingPolicy.NO_USER_AVAILABLE) {
            IPolicyFailureFactoryComponent failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
            PolicyFailure failure = failureFactory.createFailure(PolicyFailureType.Other, PolicyFailureCodes.NO_USER_FOR_RATE_LIMITING, Messages.i18n.format("TransferQuotaPolicy.NoUser")); //$NON-NLS-1$
            chain.doFailure(failure);
            return;
        }
        if (bucketId == RateLimitingPolicy.NO_CLIENT_AVAILABLE) {
            IPolicyFailureFactoryComponent failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
            PolicyFailure failure = failureFactory.createFailure(PolicyFailureType.Other, PolicyFailureCodes.NO_APP_FOR_RATE_LIMITING, Messages.i18n.format("TransferQuotaPolicy.NoApp")); //$NON-NLS-1$
            chain.doFailure(failure);
            return;
        }

        context.setAttribute(BUCKET_ID_ATTR, bucketId);
        context.setAttribute(PERIOD_ATTR, period);

        IRateLimiterComponent rateLimiter = context.getComponent(IRateLimiterComponent.class);
        rateLimiter.accept(bucketId, period, config.getLimit(), 0, new IAsyncResultHandler<RateLimitResponse>() {
            @Override
            public void handle(IAsyncResult<RateLimitResponse> result) {
                if (result.isError()) {
                    chain.throwError(result.getError());
                } else {
                    RateLimitResponse rtr = result.getResult();
                    context.setAttribute(RATE_LIMIT_RESPONSE_ATTR, rtr);
                    if (!rtr.isAccepted()) {
                        doQuotaExceededFailure(context, config, chain, rtr);
                    } else {
                        chain.doApply(request);
                    }
                }
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedDataPolicy#requestDataHandler(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    protected IReadWriteStream<ApiRequest> requestDataHandler(final ApiRequest request,
            final IPolicyContext context, final TransferQuotaConfig config) {
        // *************************************************************
        // Step 2:  if upload quotas are enabled, then count all bytes
        //          uploaded to the back-end API
        // *************************************************************
        if (config.getDirection() == TransferDirectionType.upload || config.getDirection() == TransferDirectionType.both) {
            return new AbstractStream<ApiRequest>() {
                private long total = 0;
                @Override
                public ApiRequest getHead() {
                    return request;
                }
                @Override
                protected void handleHead(ApiRequest head) {
                }
                @Override
                public void write(IApimanBuffer chunk) {
                    total += chunk.length();
                    super.write(chunk);
                }
                @Override
                public void end() {
                    context.setAttribute(BYTES_UPLOADED_ATTR, total);
                    super.end();
                }
            };
        } else {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(final ApiResponse response, final IPolicyContext context, final TransferQuotaConfig config,
            final IPolicyChain<ApiResponse> chain) {
        // *************************************************************
        // Step 3:  store the upload count (if appropriate) and fail if
        //          the transfer limit was exceeded
        // *************************************************************

        if (config.getDirection() == TransferDirectionType.upload || config.getDirection() == TransferDirectionType.both) {
            final String bucketId = context.getAttribute(BUCKET_ID_ATTR, (String) null);
            final RateBucketPeriod period = context.getAttribute(PERIOD_ATTR, (RateBucketPeriod) null);
            final long uploadedBytes = context.getAttribute(BYTES_UPLOADED_ATTR, (Long) null);

            IRateLimiterComponent rateLimiter = context.getComponent(IRateLimiterComponent.class);
            rateLimiter.accept(bucketId, period, config.getLimit(), uploadedBytes, new IAsyncResultHandler<RateLimitResponse>() {
                @Override
                public void handle(IAsyncResult<RateLimitResponse> result) {
                    if (result.isError()) {
                        chain.throwError(result.getError());
                    } else {
                        RateLimitResponse rtr = result.getResult();
                        if (!rtr.isAccepted()) {
                            doQuotaExceededFailure(context, config, chain, rtr);
                        } else {
                            Map<String, String> responseHeaders = RateLimitingPolicy.responseHeaders(
                                    config, rtr, defaultLimitHeader(), defaultRemainingHeader(),
                                    defaultResetHeader());
                            response.getHeaders().putAll(responseHeaders);
                            chain.doApply(response);
                        }
                    }
                }
            });
        } else {
            Map<String, String> responseHeaders = RateLimitingPolicy.responseHeaders(config,
                    context.getAttribute(RATE_LIMIT_RESPONSE_ATTR, (RateLimitResponse) null),
                    defaultLimitHeader(), defaultRemainingHeader(), defaultResetHeader());
            response.getHeaders().putAll(responseHeaders);
            chain.doApply(response);
        }
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedDataPolicy#responseDataHandler(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    protected IReadWriteStream<ApiResponse> responseDataHandler(final ApiResponse response,
            final IPolicyContext context, final TransferQuotaConfig config) {
        // *************************************************************
        // Step 4:  if download quotas are enabled, then count all bytes
        //          downloaded from the back-end API and store the count
        // ---
        // Note: we have no easy way to fail the request if the download
        //       quota is exceeded - so we'll pass and then fail on the
        //       next request (see Step 1)
        // *************************************************************
        if (config.getDirection() == TransferDirectionType.download || config.getDirection() == TransferDirectionType.both) {
            return new AbstractStream<ApiResponse>() {
                private long total = 0;
                @Override
                public ApiResponse getHead() {
                    return response;
                }
                @Override
                protected void handleHead(ApiResponse head) {
                }
                @Override
                public void write(IApimanBuffer chunk) {
                    total += chunk.length();
                    super.write(chunk);
                }
                @Override
                public void end() {
                    doFinalApply(context, config, total);
                    super.end();
                }
            };
        } else {
            return null;
        }
    }

    /**
     * Called when everything is done (the last byte is written).  This is used to
     * record the # of bytes downloaded.
     * @param context
     * @param config
     * @param downloadedBytes
     */
    protected void doFinalApply(IPolicyContext context, TransferQuotaConfig config, long downloadedBytes) {
        if (config.getDirection() == TransferDirectionType.download || config.getDirection() == TransferDirectionType.both) {
            final String bucketId = context.getAttribute(BUCKET_ID_ATTR, (String) null);
            final RateBucketPeriod period = context.getAttribute(PERIOD_ATTR, (RateBucketPeriod) null);

            IRateLimiterComponent rateLimiter = context.getComponent(IRateLimiterComponent.class);
            rateLimiter.accept(bucketId, period, config.getLimit(), downloadedBytes, new IAsyncResultHandler<RateLimitResponse>() {
                @Override
                public void handle(IAsyncResult<RateLimitResponse> result) {
                    // No need to handle the response - it's too late to do anything meaningful with the result.
                    // TODO log any error that might have ocurred
                }
            });
        }
    }

    /**
     * Called to send a 'quota exceeded' failure.
     * @param context
     * @param config
     * @param chain
     * @param rtr
     */
    protected void doQuotaExceededFailure(final IPolicyContext context, final TransferQuotaConfig config,
            final IPolicyChain<?> chain, RateLimitResponse rtr) {
        Map<String, String> responseHeaders = RateLimitingPolicy.responseHeaders(config, rtr,
                defaultLimitHeader(), defaultRemainingHeader(), defaultResetHeader());

        IPolicyFailureFactoryComponent failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
        PolicyFailure failure = limitExceededFailure(failureFactory);
        failure.getHeaders().putAll(responseHeaders);
        chain.doFailure(failure);
    }

    /**
     * @param responseHeaders
     * @param failureFactory
     */
    protected PolicyFailure limitExceededFailure(IPolicyFailureFactoryComponent failureFactory) {
        PolicyFailure failure = failureFactory.createFailure(PolicyFailureType.Other,
                PolicyFailureCodes.BYTE_QUOTA_EXCEEDED,
                Messages.i18n.format("TransferQuotaPolicy.RateExceeded")); //$NON-NLS-1$
        failure.setResponseCode(429);
        return failure;
    }

    /**
     * @return the default reset header
     */
    protected String defaultResetHeader() {
        return DEFAULT_RESET_HEADER;
    }

    /**
     * @return the default remaining header
     */
    protected String defaultRemainingHeader() {
        return DEFAULT_REMAINING_HEADER;
    }

    /**
     * @return the default limit header
     */
    protected String defaultLimitHeader() {
        return DEFAULT_LIMIT_HEADER;
    }

}
