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
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.IPolicyProbeRequest;
import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.policies.config.RateLimitingConfig;
import io.apiman.gateway.engine.policies.config.rates.RateLimitingGranularity;
import io.apiman.gateway.engine.policies.config.rates.RateLimitingPeriod;
import io.apiman.gateway.engine.policies.i18n.Messages;
import io.apiman.gateway.engine.policies.probe.RateLimitingProbeConfig;
import io.apiman.gateway.engine.policies.probe.RateLimitingProbeResponse;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.rates.RateBucketPeriod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

/**
 * Policy that enforces rate limits.
 *
 * @author eric.wittmann@redhat.com
 */
public class RateLimitingPolicy extends AbstractMappedPolicy<RateLimitingConfig, RateLimitingProbeConfig> {

    protected static final String NO_USER_AVAILABLE = "";
    protected static final String NO_CLIENT_AVAILABLE = "";

    private static final String DEFAULT_LIMIT_HEADER = "X-RateLimit-Limit"; //$NON-NLS-1$
    private static final String DEFAULT_REMAINING_HEADER = "X-RateLimit-Remaining"; //$NON-NLS-1$
    private static final String DEFAULT_RESET_HEADER = "X-RateLimit-Reset"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public RateLimitingPolicy() {
    }

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

    protected String createBucketId(ApiRequest request, RateLimitingConfig config) {
        BucketIdBuilderContext bucketInfo = new BucketIdBuilderContext()
             .setRateLimitingConfig(config)
             .setApi(request.getApi())
             .setContract(request.getContract())
             .setUserSupplier(() -> {
                          String header = config.getUserHeader();
                          if (!request.getHeaders().containsKey(header)) {
                              return NO_USER_AVAILABLE;
                          }
                          return request.getHeaders().get(header);
              })
             .setRemoteAddr(request.getRemoteAddr());
        return bucketId(config, bucketInfo);
    }

    protected String createBucketId(RateLimitingProbeConfig probeConfig, RateLimitingConfig config) {
        BucketIdBuilderContext bucketInfo = new BucketIdBuilderContext()
             .setRateLimitingConfig(config)
             .setApi(probeConfig.getApi())
             .setContract(probeConfig.getContract())
             .setUserSupplier(probeConfig::getUser)
             .setRemoteAddr(probeConfig.getCallerIp());
        return bucketId(config, bucketInfo);
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
     * Creates the ID of the rate bucket to use.  The ID is composed differently
     * depending on the configuration of the policy.
     */
    protected static String bucketId(RateLimitingConfig config, BucketIdBuilderContext context) {
        Api api = context.getApi();
        StringBuilder builder = new StringBuilder();
        // Public API in this branch
        if (context.getContract() == null) {
            builder.append("PUBLIC||"); //$NON-NLS-1$
            builder.append("||"); //$NON-NLS-1$
            builder.append(api.getOrganizationId());
            builder.append("||"); //$NON-NLS-1$
            builder.append(api.getApiId());
            builder.append("||"); //$NON-NLS-1$
            builder.append(api.getVersion());
            if (config.getGranularity() == RateLimitingGranularity.User) {
                String user = context.getUserSupplier().get();
                builder.append("||"); //$NON-NLS-1$
                builder.append(user);
            } else if (config.getGranularity() == RateLimitingGranularity.Ip) {
                builder.append("||"); //$NON-NLS-1$
                builder.append(context.getRemoteAddr());
            } else if (config.getGranularity() == RateLimitingGranularity.Api) {
            } else {
                return NO_CLIENT_AVAILABLE;
            }
        } else {
            // Have a fully valid contract in this branch.
            ApiContract contract = context.getContract();
            Client client = contract.getClient();
            String apiKey = client.getApiKey();
            builder.append(apiKey);
            if (config.getGranularity() == RateLimitingGranularity.User) {
                String user = context.getUserSupplier().get();
                if (user == null) {
                    return NO_USER_AVAILABLE;
                } else {
                    builder.append("||USER||"); //$NON-NLS-1$
                    builder.append(client.getOrganizationId());
                    builder.append("||"); //$NON-NLS-1$
                    builder.append(client.getClientId());
                    builder.append("||"); //$NON-NLS-1$
                    builder.append(user);
                }
            } else if (config.getGranularity() == RateLimitingGranularity.Client) {
                builder.append(apiKey);
                builder.append("||APP||"); //$NON-NLS-1$
                builder.append(client.getOrganizationId());
                builder.append("||"); //$NON-NLS-1$
                builder.append(client.getClientId());
            } else if (config.getGranularity() == RateLimitingGranularity.Ip) {
                builder.append(apiKey);
                builder.append("||IP||"); //$NON-NLS-1$
                builder.append(client.getOrganizationId());
                builder.append("||"); //$NON-NLS-1$
                builder.append(context.getRemoteAddr());
            } else {
                builder.append(apiKey);
                builder.append("||SERVICE||"); //$NON-NLS-1$
                builder.append(api.getOrganizationId());
                builder.append("||"); //$NON-NLS-1$
                builder.append(api.getApiId());
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


    @Override
    protected Class<RateLimitingProbeConfig> getProbeRequestClass() {
        return RateLimitingProbeConfig.class;
    }

    @Override
    protected void doProbe(RateLimitingProbeConfig probeRequest, RateLimitingConfig policyConfig,
         IPolicyContext context, IAsyncResultHandler<IPolicyProbeResponse> resultHandler) {
        String bucketId = createBucketId(probeRequest, policyConfig);
        IRateLimiterComponent rateLimiter = context.getComponent(IRateLimiterComponent.class);
        rateLimiter.accept(bucketId, getPeriod(policyConfig), policyConfig.getLimit(), 0, rateLimResult -> {
            RateLimitResponse remaining = rateLimResult.getResult();
            var probeResult = new RateLimitingProbeResponse().setRateLimitResponse(remaining);
            resultHandler.handle(AsyncResultImpl.create(probeResult));
        });
    }

    // TODO: make me a record
    private static final class BucketIdBuilderContext {
        private RateLimitingConfig rateLimitingConfig;
        private Api api;
        private ApiContract contract; // Remember, with a public API we might not have a contract!
        private Supplier<String> userSupplier;
        private String remoteAddr;

        public BucketIdBuilderContext() {
        }

        public RateLimitingConfig getRateLimitingConfig() {
            return rateLimitingConfig;
        }

        public BucketIdBuilderContext setRateLimitingConfig(
             RateLimitingConfig rateLimitingConfig) {
            this.rateLimitingConfig = rateLimitingConfig;
            return this;
        }

        public ApiContract getContract() {
            return contract;
        }

        public BucketIdBuilderContext setContract(ApiContract contract) {
            this.contract = contract;
            return this;
        }

        public Supplier<String> getUserSupplier() {
            return userSupplier;
        }

        public BucketIdBuilderContext setUserSupplier(Supplier<String> userSupplier) {
            this.userSupplier = userSupplier;
            return this;
        }

        public String getRemoteAddr() {
            return remoteAddr;
        }

        public BucketIdBuilderContext setRemoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
            return this;
        }

        public Api getApi() {
            return api;
        }

        public BucketIdBuilderContext setApi(Api api) {
            this.api = api;
            return this;
        }
    }

}
