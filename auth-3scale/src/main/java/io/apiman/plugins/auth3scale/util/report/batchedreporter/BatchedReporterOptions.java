/*
 * Copyright 2017 JBoss Inc
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

public class BatchedReporterOptions {
    public static final int DEFAULT_REPORTING_INTERVAL = 5000;
    public static final int DEFAULT_INITIAL_WAIT = 5;
    public static final int DEFAULT_RETRY_QUEUE_MAX_SIZE = 10000;

    private int reportingInterval = DEFAULT_REPORTING_INTERVAL;
    private int initialWait = DEFAULT_INITIAL_WAIT;
    private int retryQueueMaxSize = DEFAULT_RETRY_QUEUE_MAX_SIZE;

    public int getReportingInterval() {
        return reportingInterval;
    }

    public BatchedReporterOptions setReportingInterval(int reportingInterval) {
        this.reportingInterval = reportingInterval;
        return this;
    }

    public int getInitialWait() {
        return initialWait;
    }

    public BatchedReporterOptions setInitialWait(int initialWait) {
        this.initialWait = initialWait;
        return this;
    }

    public int getRetryQueueMaxSize() {
        return retryQueueMaxSize;
    }

    public BatchedReporterOptions setRetryQueueMaxSize(int retryQueueMaxSize) {
        this.retryQueueMaxSize = retryQueueMaxSize;
        return this;
    }

}
