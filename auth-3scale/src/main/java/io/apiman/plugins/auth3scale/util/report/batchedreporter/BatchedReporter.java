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

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.IPeriodicComponent;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.plugins.auth3scale.util.report.ReportResponseHandler;
import io.apiman.plugins.auth3scale.util.report.ReportResponseHandler.ReportResponse;

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
    private static final int DEFAULT_REPORTING_INTERVAL = 10;
    private static final int DEFAULT_INITIAL_WAIT = 10;
    private static final int DEFAULT_RETRY_QUEUE_MAXSIZE = 1000;
    private int reportingInterval = DEFAULT_REPORTING_INTERVAL;

    // Change list to RingBuffer?
    private Set<AbstractReporter<? extends ReportData>> reporters = new LinkedHashSet<>();
    private RetryReporter retryReporter = new RetryReporter();
    private IPeriodicComponent periodic;
    private IHttpClientComponent httpClient;
    private long timerId;

    private boolean started = false;
    private volatile boolean sending = false;

    public BatchedReporter() {
        reporters.add(retryReporter);
    }

    public boolean isStarted() {
        return started;
    }

    public BatchedReporter setReportingInterval(int millis) {
        this.reportingInterval = millis;
        return this;
    }

    public BatchedReporter addReporter(AbstractReporter<? extends ReportData> reporter) {
        reporter.setFullHandler(isFull -> {
            send();
        });

        reporters.add(reporter);
        return this;
    }

    public BatchedReporter start(IPeriodicComponent periodic, IHttpClientComponent httpClient) {
        if (started)
            throw new IllegalStateException("Already started");
        this.httpClient = httpClient;
        this.periodic = periodic;

        this.timerId = periodic.setPeriodicTimer(reportingInterval, DEFAULT_INITIAL_WAIT, id -> {
            //System.out.println("tick! " + id + System.currentTimeMillis());
            send();
        });
        started = true;
        return this;
    }

    public void stop() {
        periodic.cancelTimer(timerId);
    }

    // Avoid any double sending weirdness.
    private void send() {
        //System.out.println("calling send " + itemsOfWork + " and sending is " + sending);
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
        for (AbstractReporter<? extends ReportData> reporter : reporters) {
            List<ReportToSend> sendItList = reporter.encode(); // doSend? also need to consider there may be too much left

            for (final ReportToSend sendIt : sendItList) {
                itemsOfWork++;
                System.out.println("Sending :" + itemsOfWork);

                IHttpClientRequest post = httpClient.request(sendIt.getEndpoint().toString(), // TODO change to broken down components
                        HttpMethod.POST,
                        new ReportResponseHandler(reportResult -> {
                            retryIfFailure(reportResult, sendIt);
                            // TODO IMPORTANT: invalidate any bad credentials!
                            itemsOfWork--;
                            System.out.println("Attempted to send report: Report was successful? " + reportResult.getResult().success() + " " + itemsOfWork );
                            checkFinishedSending();
                        }));
                post.addHeader("Content-Type", sendIt.getEncoding()); // TODO change to contentType
                System.out.println("Writing the following:" + sendIt.getData());
                post.write(sendIt.getData(), "UTF-8");
                post.end();
            }
        }
        checkFinishedSending();
    }

    private void retryIfFailure(IAsyncResult<ReportResponse> reportResult, ReportToSend report) {
        if (reportResult.isError()) {
            // if (reportResult.getResult().isNonFatal()) {
            retryReporter.addRetry(report);
        }
    }

    private void checkFinishedSending() {
        if (itemsOfWork<=0) {
            itemsOfWork=0;
            sending = false;
        }
    }

    private static class RetryReporter extends AbstractReporter<ReportData> {
        private Queue<ReportToSend> resendReports = EvictingQueue.create(DEFAULT_RETRY_QUEUE_MAXSIZE);

        @Override
        public List<ReportToSend> encode() {
            List<ReportToSend> copy = new LinkedList<>(resendReports);
            resendReports.clear(); // Some may end up coming back again if retry fails.
            return copy;
        }

        @Override
        public AbstractReporter<ReportData> addRecord(ReportData record) {
            throw new UnsupportedOperationException("Should not call #addRecord on special retry BatchedReporter"); //$NON-NLS-1$
        }

        // Notice that super.full() is never triggered, we just evict old records once limit is hit.
        public AbstractReporter<ReportData> addRetry(ReportToSend report) {
            resendReports.offer(report);
            return this;
        }
    }

}
