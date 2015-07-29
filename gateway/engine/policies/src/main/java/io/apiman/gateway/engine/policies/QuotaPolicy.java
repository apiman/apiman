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

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.config.RateLimitingConfig;
import io.apiman.gateway.engine.policies.i18n.Messages;

/**
 * Similar to the rate limiting policy, but less granular.  Useful primarily
 * so that both a quota and a rate limit can be active at the same time.
 *
 * @author eric.wittmann@redhat.com
 */
public class QuotaPolicy extends RateLimitingPolicy {

    private static final String DEFAULT_LIMIT_HEADER = "X-Quota-Limit"; //$NON-NLS-1$
    private static final String DEFAULT_REMAINING_HEADER = "X-Quota-Remaining"; //$NON-NLS-1$
    private static final String DEFAULT_RESET_HEADER = "X-Quota-Reset"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public QuotaPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policies.RateLimitingPolicy#limitExceededFailure(io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent)
     */
    @Override
    protected PolicyFailure limitExceededFailure(IPolicyFailureFactoryComponent failureFactory) {
        PolicyFailure failure = failureFactory.createFailure(PolicyFailureType.Other, PolicyFailureCodes.REQUEST_QUOTA_EXCEEDED, Messages.i18n.format("QuotaPolicy.QuotaExceeded")); //$NON-NLS-1$
        failure.setResponseCode(429);
        return failure;
    }

    /**
     * @see io.apiman.gateway.engine.policies.RateLimitingPolicy#defaultLimitHeader()
     */
    @Override
    protected String defaultLimitHeader() {
        return DEFAULT_LIMIT_HEADER;
    }

    /**
     * @see io.apiman.gateway.engine.policies.RateLimitingPolicy#defaultRemainingHeader()
     */
    @Override
    protected String defaultRemainingHeader() {
        return DEFAULT_REMAINING_HEADER;
    }

    /**
     * @see io.apiman.gateway.engine.policies.RateLimitingPolicy#defaultResetHeader()
     */
    @Override
    protected String defaultResetHeader() {
        return DEFAULT_RESET_HEADER;
    }

    /**
     * @see io.apiman.gateway.engine.policies.RateLimitingPolicy#createBucketId(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policies.config.RateLimitingConfig)
     */
    @Override
    protected String createBucketId(ServiceRequest request, RateLimitingConfig config) {
        return "QUOTA||" + super.createBucketId(request, config); //$NON-NLS-1$
    }

}
