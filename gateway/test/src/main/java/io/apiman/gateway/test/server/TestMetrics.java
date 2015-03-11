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
package io.apiman.gateway.test.server;

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.metrics.RequestMetric;

import java.util.ArrayList;
import java.util.List;

/**
 * Test version of the IMetrics.
 *
 * @author eric.wittmann@redhat.com
 */
public class TestMetrics implements IMetrics {

    private static List<RequestMetric> metrics = new ArrayList<>();
    private IComponentRegistry registry;

    /**
     * Constructor.
     */
    public TestMetrics() {
        metrics.clear();
    }

    /**
     * @see io.apiman.gateway.engine.IMetrics#record(io.apiman.gateway.engine.metrics.RequestMetric)
     */
    @Override
    public void record(RequestMetric metric) {
        metrics.add(metric);
    }
    
    /**
     * @return the metrics
     */
    public static List<RequestMetric> getMetrics() {
        return metrics;
    }

    @Override
    public void setComponentRegistry(IComponentRegistry registry) {
        this.registry = registry;
    }
    
    public IComponentRegistry getComponentRegistry() {
        return registry;
    }
}
