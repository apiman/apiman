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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.policies.config.RateLimitingConfig;
import io.apiman.gateway.engine.policies.i18n.Messages;
import io.apiman.gateway.engine.policies.limiting.BucketFactory;
import io.apiman.gateway.engine.policies.limiting.BucketFactory.BucketIdBuilderContext;
import io.apiman.gateway.engine.policies.probe.RateLimitingProbeConfig;
import io.apiman.gateway.engine.policies.probe.RateLimitingProbeResponse;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.policy.IPolicyProbe;
import io.apiman.gateway.engine.policy.ProbeContext;
import io.apiman.gateway.engine.rates.RateBucketPeriod;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Policy that enforces rate limits.
 *
 * @author eric.wittmann@redhat.com
 */
public class RateLimitingPolicy extends AbstractMappedPolicy<RateLimitingConfig>
     implements IPolicyProbe<RateLimitingConfig, RateLimitingProbeConfig> {

    private static final String DEFAULT_LIMIT_HEADER = "X-RateLimit-Limit"; //$NON-NLS-1$
    private static final String DEFAULT_REMAINING_HEADER = "X-RateLimit-Remaining"; //$NON-NLS-1$
    private static final String DEFAULT_RESET_HEADER = "X-RateLimit-Reset"; //$NON-NLS-1$

    private final BucketFactory bucketFactory = new BucketFactory();

    /**
     * Constructor.
     */
    public RateLimitingPolicy() {
    }

    @Override
    public Class<RateLimitingConfig> getConfigurationClass() {
        return RateLimitingConfig.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(final ApiRequest request, final IPolicyContext context, final RateLimitingConfig config,
            final IPolicyChain<ApiRequest> chain) {
        String bucketId = bucketId(request, config);
        final RateBucketPeriod period = bucketFactory.getPeriod(config);

        if (bucketId.equals(BucketFactory.NO_USER_AVAILABLE)) {
            IPolicyFailureFactoryComponent failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
            PolicyFailure failure = failureFactory.createFailure(PolicyFailureType.Other, PolicyFailureCodes.NO_USER_FOR_RATE_LIMITING, Messages.i18n.format("RateLimitingPolicy.NoUser")); //$NON-NLS-1$
            chain.doFailure(failure);
            return;
        }
        if (bucketId.equals(BucketFactory.NO_CLIENT_AVAILABLE)) {
            IPolicyFailureFactoryComponent failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
            PolicyFailure failure = failureFactory.createFailure(PolicyFailureType.Other, PolicyFailureCodes.NO_APP_FOR_RATE_LIMITING, Messages.i18n.format("RateLimitingPolicy.NoApp")); //$NON-NLS-1$
            chain.doFailure(failure);
            return;
        }

        IRateLimiterComponent rateLimiter = context.getComponent(IRateLimiterComponent.class);
        rateLimiter.accept(bucketId, period, config.getLimit(), 1, new IAsyncResultHandler<RateLimitResponse>() {
            @Override
            public void handle(IAsyncResult<RateLimitResponse> result) {
                if (result.isError()) {
                    chain.throwError(result.getError());
                } else {
                    RateLimitResponse rtr = result.getResult();

                    Map<String, String> responseHeaders = responseHeaders(config, rtr,
                            defaultLimitHeader(), defaultRemainingHeader(), defaultResetHeader());

                    if (rtr.isAccepted()) {
                        context.setAttribute("rate-limit-response-headers", responseHeaders); //$NON-NLS-1$
                        chain.doApply(request);
                    } else {
                        IPolicyFailureFactoryComponent failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
                        PolicyFailure failure = limitExceededFailure(failureFactory);
                        failure.getHeaders().putAll(responseHeaders);
                        chain.doFailure(failure);
                    }
                }
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(final ApiResponse response, final IPolicyContext context, final RateLimitingConfig config,
            final IPolicyChain<ApiResponse> chain) {
        Map<String, String> headers = context.getAttribute("rate-limit-response-headers", null); //$NON-NLS-1$
        if (headers != null) {
            response.getHeaders().putAll(headers);
        }
        super.doApply(response, context, config, chain);
    }

    /**
     * Set response headers
     */
    protected static Map<String, String> responseHeaders(RateLimitingConfig config,
            RateLimitResponse rtr, String defaultLimitHeader, String defaultRemainingHeader,
            String defaultResetHeader) {
        Map<String, String> responseHeaders = new HashMap<>();
        String limitHeader = config.getHeaderLimit();
        if (StringUtils.isEmpty(limitHeader)) {
            limitHeader = defaultLimitHeader;
        }
        String remainingHeader = config.getHeaderRemaining();
        if (StringUtils.isEmpty(remainingHeader)) {
            remainingHeader = defaultRemainingHeader;
        }
        String resetHeader = config.getHeaderReset();
        if (StringUtils.isEmpty(resetHeader)) {
            resetHeader = defaultResetHeader;
        }
        responseHeaders.put(limitHeader, String.valueOf(config.getLimit()));
        responseHeaders.put(remainingHeader, String.valueOf(rtr.getRemaining()));
        responseHeaders.put(resetHeader, String.valueOf(rtr.getReset()));
        return responseHeaders;
    }

    /**
     * Generate a rate limit exceeded policy failure.
     *
     * @param failureFactory failure factory
     * @return a limit exceeded policy failure
     */
    protected PolicyFailure limitExceededFailure(IPolicyFailureFactoryComponent failureFactory) {
        PolicyFailure failure = failureFactory.createFailure(PolicyFailureType.Other, PolicyFailureCodes.RATE_LIMIT_EXCEEDED, Messages.i18n.format("RateLimitingPolicy.RateExceeded")); //$NON-NLS-1$
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

    protected String bucketId(ApiRequest request, RateLimitingConfig config) {
        return bucketFactory.bucketId(request, config);
    }

    protected String bucketId(RateLimitingConfig config, BucketIdBuilderContext context) {
        return bucketFactory.bucketId(config, context);
    }

    @Override
    public Class<RateLimitingProbeConfig> getProbeRequestClass() {
        return RateLimitingProbeConfig.class;
    }

    @Override
    public void probe(RateLimitingProbeConfig probeRequest, RateLimitingConfig policyConfig, ProbeContext probeContext,
         IPolicyContext context, IAsyncResultHandler<IPolicyProbeResponse> resultHandler) {
        String bucketId = bucketFactory.bucketId(probeRequest, probeContext, policyConfig);
        IRateLimiterComponent rateLimiter = context.getComponent(IRateLimiterComponent.class);
        // Ask for rate limit, but don't actually decrement the counter.
        rateLimiter.accept(bucketId, bucketFactory.getPeriod(policyConfig), policyConfig.getLimit(), 0, rateLimResult -> {
            RateLimitResponse remaining = rateLimResult.getResult();
            var probeResult = new RateLimitingProbeResponse()
                    .setStatus(remaining)
                    .setConfig(policyConfig);
            resultHandler.handle(AsyncResultImpl.create(probeResult));
        });
    }
}
