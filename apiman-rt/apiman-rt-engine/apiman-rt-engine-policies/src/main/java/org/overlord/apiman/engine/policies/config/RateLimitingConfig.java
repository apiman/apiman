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
package org.overlord.apiman.engine.policies.config;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.overlord.apiman.engine.policies.config.rates.RateLimitingGranularity;
import org.overlord.apiman.engine.policies.config.rates.RateLimitingPeriod;

/**
 * Configuration object for the rate limiting policy.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class RateLimitingConfig {
    
    private int limit;
    private RateLimitingGranularity granularity;
    private RateLimitingPeriod period;
    private String userHeader;
    
    /**
     * Constructor.
     */
    public RateLimitingConfig() {
    }

    /**
     * @return the granularity
     */
    public RateLimitingGranularity getGranularity() {
        return granularity;
    }

    /**
     * @param granularity the granularity to set
     */
    public void setGranularity(RateLimitingGranularity granularity) {
        this.granularity = granularity;
    }

    /**
     * @return the period
     */
    public RateLimitingPeriod getPeriod() {
        return period;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod(RateLimitingPeriod period) {
        this.period = period;
    }

    /**
     * @return the userHeader
     */
    public String getUserHeader() {
        return userHeader;
    }

    /**
     * @param userHeader the userHeader to set
     */
    public void setUserHeader(String userHeader) {
        this.userHeader = userHeader;
    }

    /**
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

}
