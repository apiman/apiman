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
package io.apiman.gateway.engine.ispn;

import io.apiman.gateway.engine.storage.component.AbstractRateLimiterComponent;

import java.util.Collections;
import java.util.Map;

/**
 * Rate limiter component backed by an Infinispan cache.  This allows rate limiting
 * to be done across nodes in a cluster of gateways.
 *
 * @author eric.wittmann@redhat.com
 */
public class InfinispanRateLimiterComponent extends AbstractRateLimiterComponent {

    private static final String DEFAULT_CACHE_CONTAINER = "java:jboss/infinispan/apiman"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public InfinispanRateLimiterComponent() {
        this(Collections.emptyMap());
    }

    /**
     * Constructor.
     *
     * @param config the config
     */
    public InfinispanRateLimiterComponent(Map<String, String> config) {
        super(new InfinispanBackingStoreProvider(config, DEFAULT_CACHE_CONTAINER, STORE_NAME));
    }
}
