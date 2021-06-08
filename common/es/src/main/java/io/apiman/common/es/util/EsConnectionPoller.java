/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.common.es.util;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLException;

import org.apache.commons.lang3.Validate;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;

/**
 * Poll Elasticsearch to see whether a successful healthy connection can be established.
 *
 * <p>After construction of the class with a configured {@link RestHighLevelClient},
 * {@link #blockUntilReady()} should be called. This will block the calling thread until Elasticsearch
 * is confirmed as reachable or various error conditions are encountered.
 *
 * <p>Class provides a very simple polling mechanism backed by {@link ScheduledExecutorService}, and attempts
 * to retry connecting and verifying the health of an Elasticsearch cluster. Certain fatal error conditions
 * will result in an immediate propagation of the exception, whilst standard {@link IOException}s result in
 * a retry.
 *
 * @see #blockUntilReady()
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class EsConnectionPoller {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EsConnectionPoller.class);

    private final RestHighLevelClient client;
    private final TimeUnit timeUnit = TimeUnit.SECONDS;
    private final int initialDelaySecs;
    private final int periodSecs;
    private final int maxWaitSecs;
    private final ScheduledExecutorService schedulerService = Executors.newSingleThreadScheduledExecutor();
    private final long startTime;

    private boolean successful = false;
    private RuntimeException latestException;
    private ScheduledFuture<?> future;

    /**
     * Constructor.
     *
     * @param client configured REST client
     * @param initialDelaySecs initial delay before polling (can be zero) in seconds
     * @param periodSecs period between poll attempts in seconds
     * @param maxWaitSecs the maximum time to block in seconds
     */
    EsConnectionPoller(
        RestHighLevelClient client,
        int initialDelaySecs,
        int periodSecs,
        int maxWaitSecs
    ) {
        this.client = Objects.requireNonNull(client);
        this.initialDelaySecs = initialDelaySecs;
        this.periodSecs = periodSecs;
        this.maxWaitSecs = maxWaitSecs;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Block calling thread until Elasticsearch can be reached successfully, the timeout period is exceeded,
     * or a fatal error occurs.
     *
     * @throws FailedPollingException if Elasticsearch can not be reached, which wraps a range of checked
     *      and unchecked exceptions. {@link FailedPollingException#getCause()} contains the wrapped
     *      exception.
     */
    public void blockUntilReady() {
        future = schedulerService
            .scheduleAtFixedRate(this::pollElasticSearch, initialDelaySecs, periodSecs, timeUnit);
        try {
            future.get(maxWaitSecs, timeUnit);
            if (!successful) {
                throw new FailedPollingException(latestException);
            }
            cancelQuietly();
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            if (!successful) {
                throw new FailedPollingException(e);
            }
        } catch (TimeoutException toe) {
            LOGGER.error(toe, "Reached polling timeout limit of {0} secs without "
                + "successfully connecting to server {1}", maxWaitSecs, client);
            throw new FailedPollingException(toe);
        }
    }

    private void pollElasticSearch() {
        try {
            LOGGER.debug("Attempting to connect to Elasticsearch via: {0}", client);
            // Do Health request
            final ClusterHealthRequest healthRequest = new ClusterHealthRequest();
            // Health request will time out after 5s, and we will try again after the scheduled period via
            // the schedulerService.
            healthRequest.timeout(new TimeValue(periodSecs, TimeUnit.SECONDS));
            final ClusterHealthResponse healthResponse = client.cluster()
                .health(healthRequest, RequestOptions.DEFAULT);

            if (!healthResponse.isTimedOut()) {
                long pollingTimeMeasure = System.currentTimeMillis() - startTime;
                LOGGER.debug("Took {0} milliseconds to successfully poll Elasticsearch", pollingTimeMeasure);
                cancelImmediately(true);
            }
        } catch (ElasticsearchException e) {
            LOGGER.error("Fatal error when attempting to connect to Elasticsearch", e);
            latestException = e;
            cancelImmediately(false);
        } catch (SSLException ssle) {
            LOGGER.error(ssle, "Fatal SSL/TLS connection occurred when connecting to Elasticsearch. "
                + "Underlying SSL/TLS config will likely need to be resolved before retrying): {0}",
                ssle.getMessage());
            latestException = new UncheckedIOException(ssle);
            cancelImmediately(false);
        } catch (IOException ioe) {
            LOGGER.info("Unable to reach Elasticsearch (with error: {0}). "
                    + "Retry will be attempted in {1} seconds.",
                ioe.getMessage(), periodSecs);
            latestException = new UncheckedIOException(ioe);
        }
    }

    private void cancelQuietly() {
        try {
            future.cancel(false);
        } catch (CancellationException ignored) {
            // Ignored
        }
    }

    private void cancelImmediately(boolean successful) {
        this.successful = successful;
        future.cancel(false);
    }

    /**
     * Wrapper for exceptions that may occur during a polling failure.
     */
    private static final class FailedPollingException extends RuntimeException {

        /**
         * Constructor.
         *
         * @param cause the exception to wrap.
         */
        FailedPollingException(Exception cause) {
            super("Failed while attempting to poll for Elasticsearch " + cause.getMessage(), cause);
        }
    }

}
