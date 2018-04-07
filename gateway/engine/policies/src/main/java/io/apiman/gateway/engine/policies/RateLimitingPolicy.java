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

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.policies.config.RateLimitingConfig;
import io.apiman.gateway.engine.policies.config.rates.RateLimitingGranularity;
import io.apiman.gateway.engine.policies.config.rates.RateLimitingPeriod;
import io.apiman.gateway.engine.policies.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.rates.RateBucketPeriod;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Policy that enforces rate limits.
 *
 * @author eric.wittmann@redhat.com
 */
public class RateLimitingPolicy extends AbstractMappedPolicy<RateLimitingConfig> {

    protected static final String NO_USER_AVAILABLE = new String();
    protected static final String NO_CLIENT_AVAILABLE = new String();

    private static final String DEFAULT_LIMIT_HEADER = "X-RateLimit-Limit"; //$NON-NLS-1$
    private static final String DEFAULT_REMAINING_HEADER = "X-RateLimit-Remaining"; //$NON-NLS-1$
    private static final String DEFAULT_RESET_HEADER = "X-RateLimit-Reset"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public RateLimitingPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.AbstractPolicy#getConfigurationClass()
     */
    @Override
    protected Class<RateLimitingConfig> getConfigurationClass() {
        return RateLimitingConfig.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(final ApiRequest request, final IPolicyContext context, final RateLimitingConfig config,
            final IPolicyChain<ApiRequest> chain) {
        String bucketId = createBucketId(request, config);
        final RateBucketPeriod period = getPeriod(config);

        if (bucketId == NO_USER_AVAILABLE) {
            IPolicyFailureFactoryComponent failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
            PolicyFailure failure = failureFactory.createFailure(PolicyFailureType.Other, PolicyFailureCodes.NO_USER_FOR_RATE_LIMITING, Messages.i18n.format("RateLimitingPolicy.NoUser")); //$NON-NLS-1$
            chain.doFailure(failure);
            return;
        }
        if (bucketId == NO_CLIENT_AVAILABLE) {
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
     * @param request
     * @param config
     */
    protected String createBucketId(ApiRequest request, RateLimitingConfig config) {
        return bucketId(request, config);
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiResponse response, IPolicyContext context, RateLimitingConfig config,
            IPolicyChain<ApiResponse> chain) {
        Map<String, String> headers = context.getAttribute("rate-limit-response-headers", null); //$NON-NLS-1$
        if (headers != null) {
            response.getHeaders().putAll(headers);
        }
        super.doApply(response, context, config, chain);
    }

    /**
     * Creates the ID of the rate bucket to use.  The ID is composed differently
     * depending on the configuration of the policy.
     *
     * @param request
     * @param config
     */
    protected static String bucketId(ApiRequest request, RateLimitingConfig config) {
        StringBuilder builder = new StringBuilder();
        if (request.getContract() == null) {
            builder.append("PUBLIC||"); //$NON-NLS-1$
            builder.append("||"); //$NON-NLS-1$
            builder.append(request.getApiOrgId());
            builder.append("||"); //$NON-NLS-1$
            builder.append(request.getApiId());
            builder.append("||"); //$NON-NLS-1$
            builder.append(request.getApiVersion());
            if (config.getGranularity() == RateLimitingGranularity.User) {
                String header = config.getUserHeader();
                if (!request.getHeaders().containsKey(header)) {
                    return NO_USER_AVAILABLE;
                }
                String user = request.getHeaders().get(header);
                builder.append("||"); //$NON-NLS-1$
                builder.append(user);
            } else if (config.getGranularity() == RateLimitingGranularity.Ip) {
                builder.append("||"); //$NON-NLS-1$
                builder.append(request.getRemoteAddr());
            } else if (config.getGranularity() == RateLimitingGranularity.Api) {
            } else {
                return NO_CLIENT_AVAILABLE;
            }
        } else {
            builder.append(request.getApiKey());
            if (config.getGranularity() == RateLimitingGranularity.User) {
                String header = config.getUserHeader();
                String user = request.getHeaders().get(header);
                if (user == null) {
                    return NO_USER_AVAILABLE;
                } else {
                    builder.append("||USER||"); //$NON-NLS-1$
                    builder.append(request.getContract().getClient().getOrganizationId());
                    builder.append("||"); //$NON-NLS-1$
                    builder.append(request.getContract().getClient().getClientId());
                    builder.append("||"); //$NON-NLS-1$
                    builder.append(user);
                }
            } else if (config.getGranularity() == RateLimitingGranularity.Client) {
                builder.append(request.getApiKey());
                builder.append("||APP||"); //$NON-NLS-1$
                builder.append(request.getContract().getClient().getOrganizationId());
                builder.append("||"); //$NON-NLS-1$
                builder.append(request.getContract().getClient().getClientId());
            } else if (config.getGranularity() == RateLimitingGranularity.Ip) {
                builder.append(request.getApiKey());
                builder.append("||IP||"); //$NON-NLS-1$
                builder.append(request.getContract().getClient().getOrganizationId());
                builder.append("||"); //$NON-NLS-1$
                builder.append(request.getRemoteAddr());
            } else {
                builder.append(request.getApiKey());
                builder.append("||SERVICE||"); //$NON-NLS-1$
                builder.append(request.getContract().getApi().getOrganizationId());
                builder.append("||"); //$NON-NLS-1$
                builder.append(request.getContract().getApi().getApiId());
            }
        }
        return builder.toString();
    }

    /**
     * Gets the appropriate bucket period from the config.
     * @param config
     */
    protected static RateBucketPeriod getPeriod(RateLimitingConfig config) {
        RateLimitingPeriod period = config.getPeriod();
        switch (period) {
        case Second:
            return RateBucketPeriod.Second;
        case Day:
            return RateBucketPeriod.Day;
        case Hour:
            return RateBucketPeriod.Hour;
        case Minute:
            return RateBucketPeriod.Minute;
        case Month:
            return RateBucketPeriod.Month;
        case Year:
            return RateBucketPeriod.Year;
        default:
            return RateBucketPeriod.Month;
        }
    }

    /**
     * @param config
     * @param rtr
     * @param defaultLimitHeader
     * @param defaultRemainingHeader
     * @param defaultResetHeader
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
     * @param responseHeaders
     * @param failureFactory
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

}
