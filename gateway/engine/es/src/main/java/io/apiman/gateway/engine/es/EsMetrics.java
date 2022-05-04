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

import io.apiman.common.es.util.AbstractEsComponent;
import io.apiman.common.es.util.ApimanEsClientOptionsParser;
import io.apiman.common.es.util.EsConstants;
import io.apiman.common.es.util.builder.index.EsIndexProperties;
import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.metrics.RequestMetric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import static io.apiman.common.es.util.builder.index.EsIndexUtils.BOOL_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.DATE_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.IP_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.KEYWORD_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.LONG_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.TEXT_AND_KEYWORD_PROP_256;
import static io.apiman.gateway.engine.storage.util.BackingStoreUtil.JSON_MAPPER;

/**
 * An elasticsearch implementation of the {@link IMetrics} interface.
 *
 * @author eric.wittmann@redhat.com
 */
public class EsMetrics extends AbstractEsComponent implements IMetrics {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EsMetrics.class);
    private static final int DEFAULT_QUEUE_SIZE = 10000;
    private static final int DEFAULT_BATCH_SIZE = 1000;

    protected IComponentRegistry componentRegistry;
    private final BlockingQueue<RequestMetric> queue;
    private final int batchSize;

    /**
     * Constructor.
     *
     * @param config map of configuration options
     */
    public EsMetrics(Map<String, String> config) {
        super(config);
        int queueSize = DEFAULT_QUEUE_SIZE;
        String queueSizeConfig = config.get("queue.size"); //$NON-NLS-1$
        if (queueSizeConfig != null) {
            queueSize = Integer.parseInt(queueSizeConfig);
        }
        queue = new LinkedBlockingDeque<>(queueSize);

        int batchSize = DEFAULT_BATCH_SIZE;
        String batchSizeConfig = config.get("batch.size"); //$NON-NLS-1$
        if (batchSizeConfig != null) {
            batchSize = Integer.parseInt(batchSizeConfig);
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
            // Queue#offer returns false if queue is full and unable to accept new records.
            if (!queue.offer(metric)) {
                LOGGER.warn("A metrics entry was dropped because the metrics queue is full. You can try to alter `queue.size` and `batch.size`, but a full buffer "
                                    + "is usually caused by Elasticsearch being slow or unavailable, network problems, or OS network stack configuration issues. "
                                    + "Increasing buffer sizes often just delays the problem, but may be helpful in high traffic and bursting scenarios, or to survive "
                                    + "short periods where the network or Elasticsearch are unavailable. Entry dropped: {0}", metric);
            }
        } catch (Exception e) {
            LOGGER.error(e, "A metrics entry was dropped due to error inserting new record into the metrics queue. "
                                    + "Entry dropped: {0}. Error: {1} ", e.getMessage(), metric);
        }
    }

    /**
     * Starts a thread which will serially pull information off the blocking
     * queue and submit that information to ES metrics.
     */
    private void startConsumerThread() {
        Thread thread = new Thread(() -> {
            while (true) {
                processQueue();
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
            RestHighLevelClient client = getClient();
            Collection<RequestMetric> batch = new ArrayList<>(this.batchSize);
            RequestMetric rm = queue.take();
            batch.add(rm);
            queue.drainTo(batch, this.batchSize - 1);

            BulkRequest request = new BulkRequest();
            for (RequestMetric metric : batch) {
                IndexRequest index = new IndexRequest(getIndexPrefix());
                index.source(JSON_MAPPER.writeValueAsString(metric), XContentType.JSON);
                request.add(index);
            }

            ActionListener<BulkResponse> listener = new ActionListener<>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    if (bulkItemResponses.hasFailures()) {
                        LOGGER.warn("Errors were reported when submitting bulk metrics into Elasticsearch. "
                                            + "This may have resulted in a loss of data: ", bulkItemResponses.buildFailureMessage());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    LOGGER.error("Failed to add metric(s) to ES", e); //$NON-NLS-1$
                }
            };
            client.bulkAsync(request, RequestOptions.DEFAULT, listener);
        } catch (InterruptedException | JsonProcessingException e) {
            LOGGER.error("Failed to add metric(s) to ES", e); //$NON-NLS-1$
        }
    }

    /**
     * @see AbstractEsComponent#getDefaultIndexPrefix()
     */
    @Override
    protected String getDefaultIndexPrefix() {
        return EsConstants.METRICS_INDEX_NAME;
    }

    @Override
    public Map<String, EsIndexProperties> getEsIndices() {
        EsIndexProperties propertiesMap = EsIndexProperties.builder()
            .addProperty(EsConstants.ES_FIELD_API_DURATION, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_API_END, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_API_ID,  KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_API_ORG_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_API_START, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_API_VERSION, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_BYTES_DOWNLOADED, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_BYTES_UPLOADED, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_CLIENT_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_CLIENT_ORG_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_CLIENT_VERSION, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_CONTRACT_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_ERROR, BOOL_PROP)
            .addProperty(EsConstants.ES_FIELD_ERROR_MESSAGE, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_FAILURE, BOOL_PROP)
            .addProperty(EsConstants.ES_FIELD_FAILURE_CODE, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_FAILURE_REASON, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_METHOD, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_PLAN_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_REMOTE_ADDR, IP_PROP)
            .addProperty(EsConstants.ES_FIELD_REQUEST_DURATION, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_REQUEST_END, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_REQUEST_START, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_RESOURCE, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_RESPONSE_CODE, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_RESPONSE_MESSAGE, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_URL, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_USER, TEXT_AND_KEYWORD_PROP_256)
            .build();
        Map<String, EsIndexProperties> indexMap = new HashMap<>();
        indexMap.put("", propertiesMap);
        return indexMap;
    }
}
