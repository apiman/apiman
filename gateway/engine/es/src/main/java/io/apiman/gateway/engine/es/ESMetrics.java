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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.metrics.RequestMetric;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.BulkResult;
import io.searchbox.core.Index;

/**
 * An elasticsearch implementation of the {@link IMetrics} interface.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESMetrics extends AbstractESComponent implements IMetrics {

    private static final int DEFAULT_QUEUE_SIZE = 10000;
    private static final int DEFAULT_BATCH_SIZE = 1000;
    
    protected IComponentRegistry componentRegistry;
    private final BlockingQueue<RequestMetric> queue;
    private final int batchSize;

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public ESMetrics(Map<String, String> config) {
        super(config);

        int queueSize = DEFAULT_QUEUE_SIZE;
        String queueSizeConfig = config.get("queue.size"); //$NON-NLS-1$
        if (queueSizeConfig != null) {
            queueSize = new Integer(queueSizeConfig);
        }
        queue = new LinkedBlockingDeque<>(queueSize);
        
        int batchSize = DEFAULT_BATCH_SIZE;
        String batchSizeConfig = config.get("batch.size"); //$NON-NLS-1$
        if (batchSizeConfig != null) {
            batchSize = new Integer(batchSizeConfig);
        }
        this.batchSize = batchSize;
        
        startConsumerThread();
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
            queue.put(metric);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts a thread which will serially pull information off the blocking
     * queue and submit that information to hawkular metrics.
     */
    private void startConsumerThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (Boolean.TRUE) {
                    processQueue();
                }
            }
        }, "ESMetricsConsumer"); //$NON-NLS-1$
        thread.setDaemon(true);
        thread.start();        
    }

    /**
     * Process the next item in the queue.
     */
    protected void processQueue() {
        try {
            Collection<RequestMetric> batch = new ArrayList<>(this.batchSize);
            RequestMetric rm = queue.take();
            batch.add(rm);
			queue.drainTo(batch, this.batchSize - 1);
			
			Builder builder = new Bulk.Builder();
			for (RequestMetric metric : batch) {
	            Index index = new Index.Builder(metric).refresh(false)
	                    .index(getIndexName())
	                    .type("request").build(); //$NON-NLS-1$
				builder.addAction(index);
			}
            
            BulkResult result = getClient().execute(builder.build());
            if (!result.isSucceeded()) {
                System.err.println("Failed to add metric(s) to ES: " + result.getErrorMessage()); //$NON-NLS-1$
            }
        } catch (Exception e) {
            // TODO better logging of this unlikely error
            System.err.println("Error adding metric to ES:"); //$NON-NLS-1$
            e.printStackTrace();
            return;
        }
    }

    /**
     * @see io.apiman.gateway.engine.es.AbstractESComponent#getDefaultIndexName()
     */
    @Override
    protected String getDefaultIndexName() {
        return ESConstants.METRICS_INDEX_NAME;
    }
}
