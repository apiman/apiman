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

/**
 * Similar to the rate limiting policy.
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

}
