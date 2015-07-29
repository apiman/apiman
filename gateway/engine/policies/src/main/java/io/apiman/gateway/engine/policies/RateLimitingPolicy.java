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
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
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

/**
 * Policy that enforces rate limits.
 *
 * @author eric.wittmann@redhat.com
 */
public class RateLimitingPolicy extends AbstractMappedPolicy<RateLimitingConfig> {

    protected static final String NO_USER_AVAILABLE = new String();
    protected static final String NO_APPLICATION_AVAILABLE = new String();

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
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(final ServiceRequest request, final IPolicyContext context, final RateLimitingConfig config,
            final IPolicyChain<ServiceRequest> chain) {
        String bucketId = createBucketId(request, config);
        final RateBucketPeriod period = getPeriod(config);

        if (bucketId == NO_USER_AVAILABLE) {
            IPolicyFailureFactoryComponent failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
            PolicyFailure failure = failureFactory.createFailure(PolicyFailureType.Other, PolicyFailureCodes.NO_USER_FOR_RATE_LIMITING, Messages.i18n.format("RateLimitingPolicy.NoUser")); //$NON-NLS-1$
            chain.doFailure(failure);
            return;
        }
        if (bucketId == NO_APPLICATION_AVAILABLE) {
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
    protected String createBucketId(ServiceRequest request, RateLimitingConfig config) {
        return bucketId(request, config);
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ServiceResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ServiceResponse response, IPolicyContext context, RateLimitingConfig config,
            IPolicyChain<ServiceResponse> chain) {
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
    protected static String bucketId(ServiceRequest request, RateLimitingConfig config) {
        StringBuilder builder = new StringBuilder();
        if (request.getContract() == null) {
            builder.append("PUBLIC||"); //$NON-NLS-1$
            builder.append("||"); //$NON-NLS-1$
            builder.append(request.getServiceOrgId());
            builder.append("||"); //$NON-NLS-1$
            builder.append(request.getServiceId());
            builder.append("||"); //$NON-NLS-1$
            builder.append(request.getServiceVersion());
            if (config.getGranularity() == RateLimitingGranularity.User) {
                String header = config.getUserHeader();
                if (!request.getHeaders().containsKey(header)) {
                    return NO_USER_AVAILABLE;
                }
                String user = request.getHeaders().get(header);
                builder.append("||"); //$NON-NLS-1$
                builder.append(user);
            } else if (config.getGranularity() == RateLimitingGranularity.Service) {
            } else {
                return NO_APPLICATION_AVAILABLE;
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
                    builder.append(request.getContract().getApplication().getOrganizationId());
                    builder.append("||"); //$NON-NLS-1$
                    builder.append(request.getContract().getApplication().getApplicationId());
                    builder.append("||"); //$NON-NLS-1$
                    builder.append(user);
                }
            } else if (config.getGranularity() == RateLimitingGranularity.Application) {
                builder.append(request.getApiKey());
                builder.append("||APP||"); //$NON-NLS-1$
                builder.append(request.getContract().getApplication().getOrganizationId());
                builder.append("||"); //$NON-NLS-1$
                builder.append(request.getContract().getApplication().getApplicationId());
            } else {
                builder.append(request.getApiKey());
                builder.append("||SERVICE||"); //$NON-NLS-1$
                builder.append(request.getContract().getService().getOrganizationId());
                builder.append("||"); //$NON-NLS-1$
                builder.append(request.getContract().getService().getServiceId());
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
        if (limitHeader == null) {
            limitHeader = defaultLimitHeader;
        }
        String remainingHeader = config.getHeaderRemaining();
        if (remainingHeader == null) {
            remainingHeader = defaultRemainingHeader;
        }
        String resetHeader = config.getHeaderReset();
        if (resetHeader == null) {
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
