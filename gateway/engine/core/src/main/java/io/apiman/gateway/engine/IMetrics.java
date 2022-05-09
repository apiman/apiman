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
package io.apiman.gateway.engine;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.metrics.RequestMetric;

/**
 * An interface used by the engine to report metric information as it
 * processes requests.  Each request is reported to the metrics system
 * so that it can be recorded/aggregated/analyzed.
 *
 * @author eric.wittmann@redhat.com
 * @author marc@blackparrotlabs.io
 */
public interface IMetrics {

    /**
     * Records the metrics for a single request.  Most implementations will likely
     * asynchronously process this information.
     * <p>
     * <strong>Implementors: do not directly store the ApiRequest and ApiResponse objects in your implementations.</strong>
     * Instead, extract the fields you need immediately and store into a minimal local representation.
     * This is important to avoid bloating out heap memory with unnecessary data you will not use, and
     * to ensure the underlying {@link ApiRequest#getRawRequest()} can be garbage collected as soon as possible.
     * <p>
     * For backwards compatibility, this method has a default implementation that calls onto {@link #record(RequestMetric)}
     * and does nothing with <code>apiRequest</code> and <code>apiResponse</code>. Implementors should override this method
     * if they want access to this richer dataset.
     */
    default void record(RequestMetric metric, ApiRequest apiRequest, ApiResponse apiResponse) {
        record(metric);
    }

    /**
     * Records the metrics for a single request.  Most implementations will likely
     * asynchronously process this information.
     * @param metric the request metric
     */
    default void record(RequestMetric metric) {
        // For compatibility
    }

    /**
     * Provides the component registry (before any call to {@link #record(RequestMetric)})
     * is made. Metrics can then access HTTP client components, etc.
     * @param registry the component registry
     */
    void setComponentRegistry(IComponentRegistry registry);
}
