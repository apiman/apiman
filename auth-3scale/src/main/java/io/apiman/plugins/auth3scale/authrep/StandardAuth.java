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
package io.apiman.plugins.auth3scale.authrep;

import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.AUTHREP_PATH;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.DEFAULT_BACKEND;

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
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.AuthTypeEnum;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.plugins.auth3scale.authrep.apikey.ApiKeyAuthReporter;
import io.apiman.plugins.auth3scale.util.ParameterMap;
import io.apiman.plugins.auth3scale.util.report.AuthResponseHandler;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class StandardAuth extends AbstractAuth<ApiKeyAuthReporter> {
    // TODO Can't remember the place where we put the special exceptions for this...
    private static final AsyncResultImpl<Void> OK_CACHED = AsyncResultImpl.create((Void) null);

    private final Content config;
    private final ApiRequest request;
    private final IPolicyContext context;
    private final IHttpClientComponent httpClient;
    private final IPolicyFailureFactoryComponent failureFactory;
    private final IApimanLogger logger;

    private ICachingAuthenticator authCache;
    private ParameterMap paramMap;
    private Object[] keyElems;
    private long serviceId;
    private IAsyncHandler<PolicyFailure> policyFailureHandler;

    public StandardAuth(Content config,
            ApiRequest request,
            IPolicyContext context) {
        this.config = config;
        this.request = request;
        this.context = context;
        this.httpClient = context.getComponent(IHttpClientComponent.class);
        this.failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
        this.logger = context.getLogger(StandardAuth.class);
        this.serviceId = config.getProxy().getServiceId();
    }

    @Override
    public StandardAuth setKeyElems(Object... keyElems) {
        this.keyElems = keyElems;
        return this;
    }

    @Override
    public StandardAuth setParameterMap(ParameterMap paramMap) {
        this.paramMap = paramMap;
        return this;
    }

    @Override
    public StandardAuth auth(IAsyncResultHandler<Void> resultHandler) {
        // If we have no cache entry, then block. Otherwise, the request can immediately go
        // through and we will resolve the rate limiting status post hoc (various strategies
        // depending on settings).
        if (authCache.isAuthCached(config, request, keyElems)) {
            logger.debug("[ServiceId: {0}] Cached auth on request: {1}", serviceId, request);
            resultHandler.handle(OK_CACHED);
        } else {
            logger.debug("[ServiceId: {0}] Uncached auth on request: {1}", serviceId, request);
            context.setAttribute("3scale.blocking", true); // TODO
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

    private void doBlockingAuthRep(IAsyncResultHandler<Void> resultHandler) {
        IHttpClientRequest get = httpClient.request(DEFAULT_BACKEND + AUTHREP_PATH + paramMap.encode(),
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

    private void flushCache() {
        logger.debug("Invalidating cache");
        authCache.invalidate(config, request, keyElems);
    }

    @Override
    public AuthTypeEnum getType() {
        return AuthTypeEnum.API_KEY;
    }

    @Override
    public StandardAuth policyFailureHandler(IAsyncHandler<PolicyFailure> policyFailureHandler) {
        this.policyFailureHandler = policyFailureHandler;
        return this;
    }

    @Override
    public StandardAuth setAuthCache(ICachingAuthenticator authCache) {
        this.authCache = authCache;
        return this;
    }

}
