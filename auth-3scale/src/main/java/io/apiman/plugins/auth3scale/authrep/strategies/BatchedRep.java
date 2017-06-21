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

import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.AUTHREP_PATH;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.BLOCKING_FLAG;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Auth3ScaleBean;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.BackendConfiguration;
import io.apiman.plugins.auth3scale.authrep.AbstractRep;
import io.apiman.plugins.auth3scale.util.ParameterMap;
import io.apiman.plugins.auth3scale.util.Status;
import io.apiman.plugins.auth3scale.util.report.AuthResponseHandler;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.BatchedReportData;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.ReportData;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.Reporter;

@SuppressWarnings("nls")
public class BatchedRep extends AbstractRep {

    private final String backendUri;
    private final BackendConfiguration config;
    private final ApiRequest request;
    private final Reporter<BatchedReportData> reporter;
    private final IPolicyContext context;
    private Object[] keyElems;
    private ReportData report;
    private BatchedAuthCache heuristicCache;
    private StandardAuthCache authCache;
    private IHttpClientComponent httpClient;
    private IPolicyFailureFactoryComponent failureFactory;
    private IApimanLogger logger;

    public BatchedRep(Auth3ScaleBean auth3ScaleBean,
            ApiRequest request,
            ApiResponse response,
            IPolicyContext context,
            Reporter<BatchedReportData> reporter,
            StandardAuthCache authCache,
            BatchedAuthCache heuristicCache) {
        this.config = auth3ScaleBean.getThreescaleConfig().getProxyConfig().getBackendConfig();
        this.backendUri = auth3ScaleBean.getBackendEndpoint();
        this.request = request;
        this.context = context;
        this.reporter = reporter;
        this.authCache = authCache;
        this.heuristicCache = heuristicCache;
        this.httpClient = context.getComponent(IHttpClientComponent.class);
        this.failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
        this.logger = context.getLogger(BatchedRep.class);
    }

    @Override
    public BatchedRep rep() {
        // If was a blocking request then we already reported, so do nothing.
        if (context.getAttribute(BLOCKING_FLAG, false))
            return this;

        // If the heuristic indicates that we should be doing standard async authrep, then... do that!
        if (heuristicCache.shouldForceAsyncAuthRep(config, request, keyElems)) {
            doAsyncRep();
            // Decrement so that we don't continue doing standard async authrep indefinitely -- TODO could use time instead?
            heuristicCache.decrement(config, request, keyElems);
        } else { // Otherwise just do batching.
            reporter.addRecord(new BatchedReportDataWrapper(config, report, request, keyElems));
        }
        return this;
    }

    private void doAsyncRep() {
        IHttpClientRequest get = httpClient.request(backendUri + AUTHREP_PATH + report.encode(),
                HttpMethod.GET,
                new AuthResponseHandler(failureFactory)
                .failureHandler(failure -> {
                    // At this point can't do anything but log it.
                    logger.debug("Async AuthRep failure code {0} on: {1}",  failure.getResponseCode(), report);
                })
                .exceptionHandler(AsyncResultImpl::create)
                .statusHandler(status -> {
                    if (!status.isAuthorized() || rateLimitReached(status)) {
                        flushCache();
                    }
                }));

        get.addHeader("Accept-Charset", "UTF-8");
        get.addHeader("X-3scale-User-Client", "apiman");
        get.end();
    }

    private boolean rateLimitReached(Status status) {
        return status.getUsageReports()
            .stream()
            .filter(report -> report.getCurrentValue() == report.getMaxValue())
            .filter(report -> config.getProxy().match(request.getDestination(), report.getMetric()))
            .findFirst()
            .isPresent();
    }

    private void flushCache() {
        logger.debug("Invalidating caches");
        authCache.invalidate(config, request, keyElems);
        heuristicCache.invalidate(config, request, keyElems);
    }

    @Override
    public BatchedRep setKeyElems(Object... keyElems) {
        this.keyElems = keyElems;
        return this;
    }

    @Override
    public BatchedRep setReport(ReportData report) {
        this.report = report;
        return this;
    }

    // TODO quite heavy-weight carrying around lots of req objects. Think of how to cleanly optimise.
    private static final class BatchedReportDataWrapper implements BatchedReportData {
        private final BackendConfiguration config;
        private final ReportData reportData;
        private final ApiRequest request;
        private final Object[] keyElems;

        public BatchedReportDataWrapper(BackendConfiguration config, ReportData reportData, ApiRequest request, Object... keyElems) {
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
        public BackendConfiguration getConfig() {
            return config;
        }
    }

}
