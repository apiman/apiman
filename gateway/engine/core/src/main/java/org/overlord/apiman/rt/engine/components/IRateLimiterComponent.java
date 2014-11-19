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
package org.overlord.apiman.rt.engine.components;

import org.overlord.apiman.rt.engine.IComponent;
import org.overlord.apiman.rt.engine.async.IAsyncResultHandler;
import org.overlord.apiman.rt.engine.rates.RateBucketPeriod;

/**
 * A component used to enforce rate limits.  This component is responsible
 * for ensuring that request rates are not exceeded.  Request rates may be 
 * limited by a number of different criteria.  Generally, N number of 
 * requests are allowed within a static time window.  This time window can
 * be of varying sizes.  For example, we might allow only 1000 requests
 * per hour.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IRateLimiterComponent extends IComponent {
    
    /**
     * Adds another request to the given rate bucket.  Sends a true signal if the 
     * request should be accepted or false if it should be rejected.
     * @param bucketId
     * @param limit
     */
    void accept(String bucketId, RateBucketPeriod period, int limit, IAsyncResultHandler<Boolean> handler);

}
