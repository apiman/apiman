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
package io.apiman.plugins.auth3scale.util.report.batchedreporter;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.IPeriodicComponent;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.auth3scale.util.report.ReportResponseHandler;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.EvictingQueue;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class BatchedReporter {

    private static final int DEFAULT_REPORTING_INTERVAL = 5000;
    private static final int DEFAULT_INITIAL_WAIT = 5;
    private static final int DEFAULT_RETRY_QUEUE_MAXSIZE = 10000;
    private int reportingInterval = DEFAULT_REPORTING_INTERVAL;

    // Change to RingBuffer?
    private Set<IReporter> reporters = new LinkedHashSet<>();
    private RetryReporter retryReporter = new RetryReporter();

    private IPeriodicComponent periodic;
    private IHttpClientComponent httpClient;
    private long timerId;

    private boolean started = false;
    private volatile boolean sending = false;
    private IApimanLogger logger;

    public BatchedReporter() {
        reporters.add(retryReporter);
    }

    public BatchedReporter start(IPolicyContext context) {
        if (started) {
            throw new IllegalStateException("Already started");
        }

        this.httpClient = context.getComponent(IHttpClientComponent.class);
        this.periodic = context.getComponent(IPeriodicComponent.class);
        this.logger = context.getLogger(BatchedReporter.class);

        this.timerId = periodic.setPeriodicTimer(reportingInterval,
                DEFAULT_INITIAL_WAIT,
                id -> send());
        started = true;
        return this;
    }

    public void stop() {
        periodic.cancelTimer(timerId);
    }

    public boolean isStarted() {
        return started;
    }

    public BatchedReporter setReportingInterval(int millis) {
        this.reportingInterval = millis;
        return this;
    }

    public BatchedReporter addReporter(IReporter reporter) {
        reporter.setFullHandler(isFull -> send());
        reporters.add(reporter);
        return this;
    }

    // Avoid any double sending weirdness.
    private void send() {
        if (!sending) {
            synchronized (this) {
                if (!sending) {
                    sending = true;
                    doSend();
                }
            }
        }
    }

    private volatile int itemsOfWork = 0;

    // speed up / slow down (primitive back-pressure mechanism?)
    private void doSend() {
        for (IReporter reporter : reporters) {
            List<ReportToSend> sendItList = reporter.encode();

            for (final ReportToSend sendIt : sendItList) {
                itemsOfWork++;
                logger.debug("[Report {}] Attempting to send: {}", sendIt.hashCode(), sendIt);
                IHttpClientRequest post = httpClient.request(sendIt.getEndpoint().toString(), // TODO change to broken down components
                        HttpMethod.POST,
                        handleResponse(sendIt));
                post.addHeader("Content-Type", sendIt.getContentType());
                post.write(sendIt.getData(), "UTF-8");
                post.end();
            }
        }
        checkFinishedSending();
    }

    private ReportResponseHandler handleResponse(ReportToSend report) {
        return new ReportResponseHandler(reportResult -> {
            logger.debug("[Report {}] Send result: {}", report.hashCode(), reportResult.getResult());
            // Flush back to allow caller to invalidate cache, etc.
            if (reportResult.isSuccess()) {
                report.flush(reportResult);
            } else { // Retry on failure.
                logger.debug("[Report {}] Will retry: {}", report.hashCode(), report);
                retryReporter.addRetry(report);
            }

            itemsOfWork--;
            checkFinishedSending();
        });
    }

    private void checkFinishedSending() {
        if (itemsOfWork <= 0) {
            itemsOfWork = 0;
            sending = false;
        }
    }

    private static class RetryReporter implements IReporter {
        private Queue<ReportToSend> resendReports = EvictingQueue.create(DEFAULT_RETRY_QUEUE_MAXSIZE);

        @Override
        public List<ReportToSend> encode() {
            List<ReportToSend> copy = new LinkedList<>(resendReports);
            resendReports.clear(); // Some may end up coming back again if retry fails.
            return copy;
        }

        // Never call full; we just evict old records once limit is hit.
        public RetryReporter addRetry(ReportToSend report) {
            resendReports.offer(report);
            return this;
        }

        @Override
        public IReporter setFullHandler(IAsyncHandler<Void> fullHandler) {
            return null;
        }
    }

}
