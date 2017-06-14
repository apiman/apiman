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

package io.apiman.plugins.auth3scale.authrep.strategies;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.plugins.auth3scale.authrep.AbstractAuthRepBase;
import io.apiman.plugins.auth3scale.authrep.AbstractRep;
import io.apiman.plugins.auth3scale.util.ParameterMap;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.BatchedReportData;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.ReportData;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.Reporter;

public class BatchedRep extends AbstractRep {
    private final Content config;
    private final ApiRequest request;
    private final Reporter<BatchedReportData> reporter;
    private final IPolicyContext context;
    private Object[] keyElems;
    private ReportData report;

    public BatchedRep(Content config,
            ApiRequest request,
            ApiResponse response,
            IPolicyContext context,
            Reporter<BatchedReportData> reporter) {
        super();
        this.config = config;
        this.request = request;
        this.context = context;
        this.reporter = reporter;
    }

    @Override
    public AbstractRep rep() {
        // If was a blocking request then we already reported, so do nothing.
        System.out.println("context.getAttribute(\"3scale.blocking\", false) " + context.getAttribute("3scale.blocking", false));
        if (context.getAttribute("3scale.blocking", false)) //$NON-NLS-1$
            return this;

        reporter.addRecord(new BatchedReportDataWrapper(config, report, request, keyElems));
        return this;
    }

    @Override
    public AbstractAuthRepBase setKeyElems(Object... keyElems) {
        this.keyElems = keyElems;
        return this;
    }

    @Override
    public AbstractAuthRepBase setReport(ReportData report) {
        this.report = report;
        return this;
    }

    // TODO quite heavy-weight carrying around lots of req objects. Think of how to cleanly optimise.
    private static final class BatchedReportDataWrapper implements BatchedReportData {
        private final Content config;
        private final ReportData reportData;
        private final ApiRequest request;
        private final Object[] keyElems;

        public BatchedReportDataWrapper(Content config, ReportData reportData, ApiRequest request, Object... keyElems) {
            this.config = config;
            this.reportData = reportData;
            this.request = request;
            this.keyElems = keyElems;
        }

        @Override
        public BatchedReportDataWrapper setTimestamp(String timestamp) {
            reportData.setTimestamp(timestamp);
            return this;
        }

        @Override
        public ParameterMap getUsage() {
            return reportData.getUsage();
        }

        @Override
        public ParameterMap getLog() {
            return reportData.getLog();
        }

        @Override
        public int bucketId() {
            return reportData.bucketId();
        }

        @Override
        public String getServiceToken() {
            return reportData.getServiceToken();
        }

        @Override
        public String getServiceId() {
            return reportData.getServiceId();
        }

        @Override
        public String encode() {
            return reportData.encode();
        }

        @Override
        public ParameterMap toParameterMap() {
            return reportData.toParameterMap();
        }

        @Override
        public ApiRequest getRequest() {
            return request;
        }

        @Override
        public Object[] getKeyElems() {
            return keyElems;
        }

        @Override
        public Content getConfig() {
            return config;
        }
    }

}
