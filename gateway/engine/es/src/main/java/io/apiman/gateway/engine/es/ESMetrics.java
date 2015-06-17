/*
 * Copyright 2013 JBoss Inc
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

package io.apiman.gateway.engine.es;

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.metrics.RequestMetric;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Index;

import java.util.Map;

/**
 * An elasticsearch implementation of the {@link IMetrics} interface.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESMetrics extends AbstractESComponent implements IMetrics {

    protected IComponentRegistry componentRegistry;

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public ESMetrics(Map<String, String> config) {
        super(config);
    }

    /**
     * @see io.apiman.gateway.engine.IMetrics#setComponentRegistry(io.apiman.gateway.engine.IComponentRegistry)
     */
    @Override
    public void setComponentRegistry(IComponentRegistry registry) {
        componentRegistry = registry;
    }

    /**
     * @see io.apiman.gateway.engine.IMetrics#record(io.apiman.gateway.engine.metrics.RequestMetric)
     */
    @Override
    public void record(RequestMetric metric) {
        try {
            Index index = new Index.Builder(metric).refresh(false)
                    .index(ESConstants.METRICS_INDEX_NAME)
                    .type("request").build(); //$NON-NLS-1$
            getClient().executeAsync(index, new JestResultHandler<JestResult>() {
                @Override
                public void completed(JestResult result) {
                    if (!result.isSucceeded()) {
                        System.err.println("Failed to add metric to ES:"); //$NON-NLS-1$
                        System.err.println("/t" + result.getErrorMessage()); //$NON-NLS-1$
                    }
                }
                @Override
                public void failed(Exception e) {
                    System.out.println("Error adding metric to ES:"); //$NON-NLS-1$
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
