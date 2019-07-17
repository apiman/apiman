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
package io.apiman.gateway.engine.policies.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration object for the Caching policy.
 *
 * @author rubenrm1@gmail.com
 * @deprecated use {@link CachingResourcesConfig} instead.
 */
@Deprecated
public class CachingConfig {

    private long ttl; // in seconds
    private List<String> statusCodes = new ArrayList<>();
    private boolean includeQueryInKey = false;

    /**
     * Constructor.
     */
    @Deprecated
    public CachingConfig() {
    }

    /**
     * @return the ttl
     */
    @Deprecated
    public long getTtl() {
        return ttl;
    }

    /**
     * @param ttl the ttl to set
     */
    @Deprecated
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    /**
     * @return the status codes
     */
    @Deprecated
    public List<String> getStatusCodes() {
        return statusCodes;
    }

    /**
     * @param statusCodes the statusCodes to set
     */
    @Deprecated
    public void setStatusCodes(List<String> statusCodes) {
        this.statusCodes = statusCodes;
    }

    /**
     * Whether to include query parameters in the cache key.
     * @return {@code true} if query parameters should be included, otherwise, {@code false}
     */
    @Deprecated
    public boolean isIncludeQueryInKey() {
        return includeQueryInKey;
    }

    /**
     * Whether to include query parameters in the cache key.
     * @param includeQueryInKey whether to include query parameters
     */
    @Deprecated
    public void setIncludeQueryInKey(boolean includeQueryInKey) {
        this.includeQueryInKey = includeQueryInKey;
    }
}
