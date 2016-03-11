/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.gateway.engine.hawkular;

import io.apiman.common.config.options.HttpConnectorOptions;
import io.apiman.common.net.hawkular.HawkularMetricsClient;
import io.apiman.common.net.hawkular.beans.MetricLongBean;
import io.apiman.common.net.hawkular.beans.MetricType;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.metrics.RequestMetric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * An implementation of the apiman {@link IMetrics} interface that pushes metrics
 * data to a Hawkular Metrics server.  For more information about Hawkular Metrics,
 * see:  http://www.hawkular.org/docs/components/metrics/index.html
 * 
 * @author eric.wittmann@gmail.com
 */
public class HawkularMetrics implements IMetrics {
    
    private static final int DEFAULT_QUEUE_SIZE = 10000;
    
    private final HawkularMetricsClient client;
    private final BlockingQueue<QueueItem> queue;

    /**
     * Constructor.
     * @param config
     */
    @SuppressWarnings("nls")
    public HawkularMetrics(Map<String, String> config) {
        String endpoint = config.get("hawkular.endpoint");
        if (endpoint == null) {
            throw new RuntimeException("Missing configuration property: apiman-gateway.metrics.hawkular.endpoint");
        }
        Map<String, String> httpOptions = new HashMap<>();
        httpOptions.put("http.timeouts.read", config.get("http.timeouts.read"));
        httpOptions.put("http.timeouts.write", config.get("http.timeouts.write"));
        httpOptions.put("http.timeouts.connect", config.get("http.timeouts.connect"));
        httpOptions.put("http.followRedirects", config.get("http.followRedirects"));
        client = new HawkularMetricsClient(endpoint, new HttpConnectorOptions(httpOptions));

        int queueSize = DEFAULT_QUEUE_SIZE;
        String queueSizeConfig = config.get("hawkular.queueSize");
        if (queueSizeConfig != null) {
            queueSize = new Integer(queueSizeConfig);
        }
        queue = new LinkedBlockingDeque<>(queueSize);
        startConsumerThread();
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
        }, "HawkularMetricsConsumer"); //$NON-NLS-1$
        thread.setDaemon(true);
        thread.start();        
    }

    /**
     * Process the next item in the queue.
     */
    protected void processQueue() {
        try {
            QueueItem item = queue.take();
            client.addMultipleCounterDataPoints(item.tenantId, item.data);
        } catch (InterruptedException e) {
            // TODO better logging of this unlikely error
            e.printStackTrace();
            return;
        }
    }

    /**
     * @see io.apiman.gateway.engine.IMetrics#record(io.apiman.gateway.engine.metrics.RequestMetric)
     */
    @Override
    public void record(RequestMetric metric) {
        // Record data points (potentially) for the following metrics:
        // 1) # of total requests (always)
        // 2) # of failures (only when a failure)
        // 3) # of errors (only when an error)
        String tenantId = metric.getApiOrgId();
        List<MetricLongBean> data = new ArrayList<>();
        
        // # of total requests
        MetricLongBean totalRequests = new MetricLongBean();
        totalRequests.addDataPoint(metric.getRequestStart(), 1);
        totalRequests.setId("apis." + metric.getApiId() + "." + metric.getApiVersion()); //$NON-NLS-1$ //$NON-NLS-2$
        totalRequests.setType(MetricType.counter);
        data.add(totalRequests);

        // # of failures
        if (metric.isFailure()) {
            MetricLongBean failedRequests = new MetricLongBean();
            failedRequests.addDataPoint(metric.getRequestStart(), 1);
            failedRequests.setId("apis." + metric.getApiId() + "." + metric.getApiVersion() + ".Failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            failedRequests.setType(MetricType.counter);
            data.add(failedRequests);
        }

        // # of errors
        if (metric.isError()) {
            MetricLongBean erroredRequests = new MetricLongBean();
            erroredRequests.addDataPoint(metric.getRequestStart(), 1);
            erroredRequests.setId("apis." + metric.getApiId() + "." + metric.getApiVersion() + ".Errored"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            erroredRequests.setType(MetricType.counter);
            data.add(erroredRequests);
        }

        try {
            queue.put(new QueueItem(tenantId, data));
        } catch (InterruptedException e) {
            // TODO better logging of this unlikely error
            e.printStackTrace();
        }
    }

    /**
     * @see io.apiman.gateway.engine.IMetrics#setComponentRegistry(io.apiman.gateway.engine.IComponentRegistry)
     */
    @Override
    public void setComponentRegistry(IComponentRegistry registry) {
    }
    
    
    private static class QueueItem {
        public String tenantId;
        public List<MetricLongBean> data;

        /**
         * Constructor.
         */
        public QueueItem(String tenantId, List<MetricLongBean> data) {
            this.tenantId = tenantId;
            this.data = data;
        }
    }
}
