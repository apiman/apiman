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



/**
 * Configuration object for the Caching policy.
 *
 * @author rubenrm1@gmail.com
 */
public class CachingConfig {

    private long ttl;

    /**
     * Constructor.
     */
    public CachingConfig() {
    }

    /**
     * @return the ttl
     */
    public long getTtl() {
        return ttl;
    }

    /**
     * @param ttl the ttl to set
     */
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

}
