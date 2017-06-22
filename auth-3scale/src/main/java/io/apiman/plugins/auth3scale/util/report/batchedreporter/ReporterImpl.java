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

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.plugins.auth3scale.Auth3ScaleConstants;
import io.apiman.plugins.auth3scale.util.ParameterMap;
import io.apiman.plugins.auth3scale.util.report.ReportResponseHandler.ReportResponse;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 * @param <T> extends ReportData
 */
public class ReporterImpl<T extends BatchedReportData> implements Reporter {
    private final ReporterOptions options;
    private IAsyncHandler<Void> fullHandler;
    private IAsyncResultHandler<List<BatchedReportData>> flushHandler;

    private final Map<Integer, ArrayBlockingQueue<T>> reportBuckets = new ConcurrentHashMap<>();

    public ReporterImpl(ReporterOptions options) {
        this.options = options;
    }

    @Override
    public List<EncodedReport> encode() {
        List<EncodedReport> encodedReports = new ArrayList<>(reportBuckets.size());
        // For each bucket
        for (ArrayBlockingQueue<T> bucket : reportBuckets.values()) {
            if (bucket.isEmpty()) {
                continue;
            }
            // Drain TODO Small chance of brief blocking; can rework easily if this becomes a problem.
            List<BatchedReportData> reports = new ArrayList<>(bucket.size());
            bucket.drainTo(reports);
            encodedReports.add(new ReportToSendImpl(options.getReportEndpoint(), reports, flushHandler));
        }
        return encodedReports;
    }

    public ReporterImpl<T> addRecord(T record) {
        ArrayBlockingQueue<T> reportGroup = reportBuckets.computeIfAbsent(record.bucketId(), k -> new ArrayBlockingQueue<>(options.getInitialBucketCapacity()));
        reportGroup.add(record);
        // This is just approximate, we don't care whether it's somewhat out.
        if (reportGroup.size() >= options.getBucketFullTriggerSize()) {
            full();
        }
        return this;
    }

    public ReporterImpl<T> flushHandler(IAsyncResultHandler<List<BatchedReportData>> flushHandler) {
        this.flushHandler = flushHandler;
        return this;
    }

    @Override
    public ReporterImpl<T> setFullHandler(IAsyncHandler<Void> fullHandler) {
        this.fullHandler = fullHandler;
        return this;
    }

    protected void full() {
        fullHandler.handle((Void) null);
    }

    private static final class ReportToSendImpl implements EncodedReport {
        private final URI endpoint;
        private final List<BatchedReportData> reports;
        private IAsyncResultHandler<List<BatchedReportData>> flushHandler;

        public ReportToSendImpl(URI endpoint,
                List<BatchedReportData> reports,
                IAsyncResultHandler<List<BatchedReportData>> flushHandler) {
            this.endpoint = endpoint;
            this.reports = reports;
            this.flushHandler = flushHandler;
        }

        @Override
        public String getData() {
            // 2 mandatory top-level items
            ParameterMap data = new ParameterMap();
            data.add(Auth3ScaleConstants.SERVICE_TOKEN, reports.get(0).getServiceToken());
            data.add(Auth3ScaleConstants.SERVICE_ID, reports.get(0).getServiceId());
            List<ParameterMap> transactions = new ArrayList<>();
            // Build transactions list.
            reports.stream().forEach(report -> transactions.add(report.toParameterMap()));
            // Get array representation.
            data.add(Auth3ScaleConstants.TRANSACTIONS, transactions.toArray(new ParameterMap[0]));
            return data.encode();
        }

        @Override
        public String getContentType() {
            return "application/x-www-form-urlencoded"; //$NON-NLS-1$
        }

        @Override
        public URI getEndpoint() {
            return endpoint;
        }

        @Override
        public void flush(IAsyncResult<ReportResponse> reportResponse) {
            if (reportResponse.isSuccess()) {
                flushHandler.handle(AsyncResultImpl.create(reports));
            } else { // Flushing failed! Likely same result -- want to flush from cache somewhere.
                flushHandler.handle(new IAsyncResult<List<BatchedReportData>>() {

                    @Override
                    public boolean isSuccess() {
                        return false;
                    }

                    @Override
                    public boolean isError() {
                        return true;
                    }

                    @Override
                    public List<BatchedReportData> getResult() {
                        return reports;
                    }

                    @Override
                    public Throwable getError() {
                        return new RuntimeException("Reporting failed; see #getResult for failed entries."); //$NON-NLS-1$
                    }
                });
            }
        }

        @Override
        public String toString() {
            final int maxLen = 10;
            return String.format("Report [endpoint=%s, reports=%s]", endpoint, //$NON-NLS-1$
                    reports != null ? reports.subList(0, Math.min(reports.size(), maxLen)) : null);
        }
    }

}
