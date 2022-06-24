/*
 * Copyright 2022. Black Parrot Labs Ltd
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

package io.apiman.gateway.engine.metrics;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.async.IAsyncHandler;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;

/**
 * Batched metrics consumer. Uses a single spin-wait daemon thread to poll a {@link ManyToOneConcurrentArrayQueue}. Calls to {@link #offer(Object)} are thread safe.
 * <p>
 * This implementation will flush to the batchHandler as fast as it can, with a maximum batch size specified.
 * <p>
 * It is generally acceptable for implementors to use the metric processing thread to perform whatever actions are required to dispatch their metrics; new metrics will
 * be queued up until the thread becomes available again, as long as this is not an excessively long period of time (i.e. allowing the buffer to overflow).
 * <p>
 * Important: The List<M> provided to batchHandler is cleared after the callback returns, so don't hold onto it.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class BatchedMetricsConsumer<M> implements Closeable {
    private final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(BatchedMetricsConsumer.class);
    private final String metricConsumerName;
    private final ManyToOneConcurrentArrayQueue<M> metricQueue;
    private final int maxBatchSize;
    private final ArrayList<M> batchBuffer;
    private final IAsyncHandler<List<M>> batchHandler;
    private boolean running = false;

    public BatchedMetricsConsumer(String metricConsumerName,
                                  int queueCapacity,
                                  int maxBatchSize,
                                  IAsyncHandler<List<M>> batchHandler) {
        Objects.requireNonNull(metricConsumerName, "Must provide metric consumer name");
        Objects.requireNonNull(batchHandler, "Must provide handler to drain metrics to");
        this.metricConsumerName = metricConsumerName;
        this.metricQueue = new ManyToOneConcurrentArrayQueue<>(queueCapacity);
        this.maxBatchSize = maxBatchSize;
        this.batchBuffer = new ArrayList<>(maxBatchSize);
        this.batchHandler = batchHandler;
    }

    public void start() {
        this.running = true;
        var metricsConsumerThread = new Thread(() -> {
            LOGGER.info("Starting metrics consumer: {0}...", metricConsumerName);
            while (running) {
                while (metricQueue.isEmpty()) {
                    Thread.onSpinWait();
                }
                try {
                    consumeBatch();
                } catch (RuntimeException rte) {
                    LOGGER.error("An error occurred when consuming a batch of metrics; this will be ignored so that the metrics subsystem can continue functioning. "
                                         + "Implementors should handle any errors in their code to prevent entire batches being dropped.", rte);
                    LOGGER.warn("Dropping {0} metrics", batchBuffer.size());
                }
                // Must always clear batch buffer, even if exception occurs.
                batchBuffer.clear();
            }
        }, metricConsumerName);
        metricsConsumerThread.setDaemon(true);
        metricsConsumerThread.start();
    }

    public boolean offer(M requestMetric) {
        return metricQueue.offer(requestMetric);
    }

    private void consumeBatch() {
        int elemsDrained = metricQueue.drainTo(batchBuffer, maxBatchSize);
        if (elemsDrained > 0) {
            LOGGER.debug("Draining {0} metrics to handler", elemsDrained);
            batchHandler.handle(batchBuffer);
        }
    }

    @Override
    public void close() {
        LOGGER.info("Stopping metrics consumer: {0}...", metricConsumerName);
        this.running = false;
        metricQueue.clear();
    }
}
