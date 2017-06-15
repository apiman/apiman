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
package io.apiman.plugins.auth3scale.authrep.strategies;

import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.AUTHREP_PATH;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.BLOCKING_FLAG;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.DEFAULT_BACKEND;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.plugins.auth3scale.authrep.AbstractRep;
import io.apiman.plugins.auth3scale.util.Status;
import io.apiman.plugins.auth3scale.util.report.AuthResponseHandler;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.ReportData;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class StandardRep extends AbstractRep {
    private final Content config;
    private final ApiRequest request;
    private final IHttpClientComponent httpClient;
    private final IPolicyFailureFactoryComponent failureFactory;
    private final IApimanLogger logger;

    private StandardAuthCache authCache;
    private Object[] keyElems;
    private ReportData report;
    private IPolicyContext context;

    public StandardRep(Content config, ApiRequest request, ApiResponse response, IPolicyContext context, StandardAuthCache authCache) {
        this.config = config;
        this.request = request;
        this.context = context;
        this.httpClient = context.getComponent(IHttpClientComponent.class);
        this.failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
        this.logger = context.getLogger(StandardRep.class);
        this.authCache = authCache;
    }

    @Override
    public StandardRep rep() {
        // If was a blocking request then we already reported, so do nothing.
        if (context.getAttribute(BLOCKING_FLAG, false))
            return this;

        IHttpClientRequest get = httpClient.request(DEFAULT_BACKEND + AUTHREP_PATH + report.encode(),
                HttpMethod.GET,
                new AuthResponseHandler(failureFactory)
                    // At this point can't do anything but log it.
                    .failureHandler(failure -> logger.debug("Async AuthRep failure code {0} on: {1}", failure.getResponseCode(), report))
                    .exceptionHandler(AsyncResultImpl::create)
                    .statusHandler(status -> {
                        if (!status.isAuthorized() || rateLimitReached(status)) {
                            flushCache();
                        }
                    }));

        get.addHeader("Accept-Charset", "UTF-8");
        get.addHeader("X-3scale-User-Client", "apiman");
        get.end();
        return this;
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
        logger.debug("Invalidating cache");
        authCache.invalidate(config, request, keyElems);
    }

    @Override
    public StandardRep setKeyElems(Object... keyElems) {
        this.keyElems = keyElems;
        return this;
    }

    @Override
    public StandardRep setReport(ReportData report) {
        this.report = report;
        return this;
    }

}
