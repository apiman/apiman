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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.apiman.common.es.util.AbstractEsComponent;
import io.apiman.common.es.util.EsConstants;
import io.apiman.common.logging.DefaultDelegateFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.metrics.RequestMetric;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static io.apiman.gateway.engine.storage.util.BackingStoreUtil.JSON_MAPPER;

/**
 * An elasticsearch implementation of the {@link IMetrics} interface.
 *
 * @author eric.wittmann@redhat.com
 */
public class EsMetrics extends AbstractEsComponent implements IMetrics {

    private static final int DEFAULT_QUEUE_SIZE = 10000;
    private static final int DEFAULT_BATCH_SIZE = 1000;

    protected IComponentRegistry componentRegistry;
    private final BlockingQueue<RequestMetric> queue;
    private final int batchSize;

    private IApimanLogger logger = new DefaultDelegateFactory().createLogger(PollCachingEsRegistry.class);

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public EsMetrics(Map<String, String> config) {
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
        }, "EsMetricsConsumer"); //$NON-NLS-1$
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

            BulkRequest request = new BulkRequest();
            for (RequestMetric metric : batch) {
                IndexRequest index = new IndexRequest(getDefaultIndexPrefix());
                index.source(JSON_MAPPER.writeValueAsString(metric), XContentType.JSON);
                request.add(index);
            }

            ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    // Do nothing
                }

                @Override
                public void onFailure(Exception e) {
                    logger.error("Failed to add metric(s) to ES", e); //$NON-NLS-1$
                }
            };
            getClient().bulkAsync(request, RequestOptions.DEFAULT, listener);
        } catch (InterruptedException | JsonProcessingException e) {
            logger.error("Failed to add metric(s) to ES", e); //$NON-NLS-1$
        }
    }

    /**
     * @see AbstractEsComponent#getDefaultIndexPrefix()
     */
    @Override
    protected String getDefaultIndexPrefix() {
        return EsConstants.METRICS_INDEX_NAME;
    }

    /**
     * @see AbstractEsComponent#getDefaultIndices()
     * @return default indices
     */
    @Override
    protected List<String> getDefaultIndices() {
        String[] indices = {""};
        return Arrays.asList(indices);
    }
}
