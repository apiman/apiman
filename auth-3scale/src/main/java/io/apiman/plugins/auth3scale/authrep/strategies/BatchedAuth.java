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
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Auth3ScaleBean;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.BackendConfiguration;
import io.apiman.plugins.auth3scale.authrep.AbstractAuth;
import io.apiman.plugins.auth3scale.authrep.AbstractAuthRepBase;
import io.apiman.plugins.auth3scale.util.report.AuthResponseHandler;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.ReportData;

/**
 *  First leg is the same as {@link StandardAuth}.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class BatchedAuth extends AbstractAuth {
    private static final AsyncResultImpl<Void> OK_CACHED = AsyncResultImpl.create((Void) null);
    private final String backendUri;
    private final StandardAuthCache authCache;
    private final BatchedAuthCache heuristicCache;
    private BackendConfiguration config;
    private ApiRequest request;
    private Object[] keyElems;
    private IAsyncHandler<PolicyFailure> policyFailureHandler;
    private final IApimanLogger logger;
    private ReportData report;
    private long serviceId;
    private IPolicyContext context;
    private IHttpClientComponent httpClient;
    private IPolicyFailureFactoryComponent failureFactory;

    public BatchedAuth(Auth3ScaleBean auth3ScaleBean,
            ApiRequest request,
            IPolicyContext context,
            StandardAuthCache authCache,
            BatchedAuthCache heuristicCache) {
        this.backendUri = auth3ScaleBean.getBackendEndpoint();
        this.config = auth3ScaleBean.getThreescaleConfig().getProxyConfig().getBackendConfig();
        this.request = request;
        this.context = context;
        this.authCache = authCache;
        this.heuristicCache = heuristicCache;
        this.logger = context.getLogger(BatchedAuth.class);
        this.serviceId = config.getProxy().getServiceId();
        this.httpClient = context.getComponent(IHttpClientComponent.class);
        this.failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
    }

    @Override
    public BatchedAuth setKeyElems(Object... keyElems) {
        this.keyElems = keyElems;
        return this;
    }

    @Override
    public BatchedAuth auth(IAsyncResultHandler<Void> resultHandler) {
        // If we have no cache entry, then block. Otherwise, the request can immediately go
        // through and we will resolve the rate limiting status post hoc (various strategies
        // depending on settings).
        if (authCache.isAuthCached(config, request, keyElems)) {
            logger.debug("[ServiceId: {0}] Cached auth on request: {1}", serviceId, request);
            resultHandler.handle(OK_CACHED);
        } else {
            logger.debug("[ServiceId: {0}] Uncached auth on request: {1}", serviceId, request);
            context.setAttribute(BLOCKING_FLAG, true);
            doBlockingAuthRep(result -> {
                logger.debug("Blocking auth success?: {0}", result.isSuccess());
                // Only cache if successful
                if (result.isSuccess()) {
                    authCache.cache(config, request, keyElems);
                }
                // Pass result up.
                resultHandler.handle(result);
            });
        }
        return this;
    }

    protected void doBlockingAuthRep(IAsyncResultHandler<Void> resultHandler) {
        IHttpClientRequest get = httpClient.request(backendUri + AUTHREP_PATH + report.encode(),
                HttpMethod.GET,
                new AuthResponseHandler(failureFactory)
                .failureHandler(failure -> {
                    logger.debug("[ServiceId: {0}] Blocking AuthRep failure: {1}", serviceId, failure.getResponseCode());
                    policyFailureHandler.handle(failure);
                })
                .exceptionHandler(exception -> resultHandler.handle(AsyncResultImpl.create(exception)))
                .statusHandler(status -> {
                    logger.debug("[ServiceId: {0}] Backend status: {1}", serviceId, status);
                    if (!status.isAuthorized()) {
                        flushCache();
                    } else {
                        resultHandler.handle(OK_CACHED);
                    }
                }));

        get.addHeader("Accept-Charset", "UTF-8");
        get.addHeader("X-3scale-User-Client", "apiman");
        get.end();
    }

    protected void flushCache() {
        logger.debug("Invalidating cache");
        authCache.invalidate(config, request, keyElems);
        heuristicCache.invalidate(config, request, keyElems);
    }

    @Override
    public BatchedAuth policyFailureHandler(IAsyncHandler<PolicyFailure> policyFailureHandler) {
        this.policyFailureHandler = policyFailureHandler;
        return this;
    }

    @Override
    public AbstractAuthRepBase setReport(ReportData report) {
        this.report = report;
        return this;
    }
}
