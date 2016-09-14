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
package io.apiman.plugins.auth3scale.authrep.apikey;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.AuthTypeEnum;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.plugins.auth3scale.authrep.AbstractAuthExecutor;
import io.apiman.plugins.auth3scale.authrep.AuthRepConstants;
import io.apiman.plugins.auth3scale.util.report.AuthResponseHandler;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ApiKeyAuthExecutor extends AbstractAuthExecutor<ApiKeyAuthReporter> {
    private static final String AUTHORIZE_PATH = "/transactions/authorize.xml?";
    private static final String AUTHREP_PATH = "/transactions/authrep.xml?";

    // TODO Can't remember the place where we put the special exceptions for this...
    private static final AsyncResultImpl<Void> OK_CACHED = AsyncResultImpl.create((Void) null);
    private static final AsyncResultImpl<Void> FAIL_PROVIDE_USER_KEY = AsyncResultImpl.create(new RuntimeException("No user apikey provided!"));
    private static final AsyncResultImpl<Void> FAIL_NO_ROUTE = AsyncResultImpl.create(new RuntimeException("No valid route"));

    private final IApimanLogger logger;
    private ApiKeyCachingAuthenticator authCache;

    ApiKeyAuthExecutor(Content config, ApiRequest request, IPolicyContext context, ApiKeyCachingAuthenticator authCache) {
        super(config, request, context);
        this.authCache = authCache;
        logger = context.getLogger(ApiKeyAuthExecutor.class);
    }

    @Override
    public ApiKeyAuthExecutor auth(IAsyncResultHandler<Void> resultHandler) {
        doAuth(resultHandler);
        return this;
    }

    @Override
    public AuthTypeEnum getType() {
        return AuthTypeEnum.API_KEY;
    }

    private void doAuth(IAsyncResultHandler<Void> resultHandler) {
        String userKey = getUserKey();
        if (userKey == null) {
            resultHandler.handle(FAIL_PROVIDE_USER_KEY);
            return;
        }

        if (!hasRoutes(request)) { // TODO Optimise
            resultHandler.handle(FAIL_NO_ROUTE);
            return;
        }

        // If we have no cache entry, then block. Otherwise, the request can immediately go
        // through and we will resolve the rate limiting status post hoc (various strategies
        // depending on settings).
        if (authCache.isAuthCached(config, request, userKey)) {
            logger.debug("Cached auth on request " + request);
            resultHandler.handle(OK_CACHED);
            context.setAttribute("3scale.userKey", userKey);
        } else {
            logger.debug("Uncached auth on request " + request);
            context.setAttribute("3scale.blocking", true); // TODO
            doBlockingAuthRep(userKey, result -> {
                logger.debug("Blocking auth success: {0}", result.isSuccess());
                // Only cache if successful
                if (result.isSuccess()) {
                    authCache.cache(config, request, userKey);
                }
                // Pass result up.
                resultHandler.handle(result);
            });
        }
    }

    @SuppressWarnings("nls")
    private void doBlockingAuthRep(String userKey, IAsyncResultHandler<Void> resultHandler) {
        paramMap.add(AuthRepConstants.USER_KEY, userKey);
        paramMap.add(AuthRepConstants.SERVICE_TOKEN, config.getBackendAuthenticationValue());// maybe use endpoint properties or something. or new properties field.
        paramMap.add(AuthRepConstants.SERVICE_ID, Long.toString(config.getProxy().getServiceId()));
        paramMap.add(AuthRepConstants.USAGE, buildRepMetrics(api));

        setIfNotNull(paramMap, AuthRepConstants.REFERRER, request.getHeaders().get(AuthRepConstants.REFERRER));
        setIfNotNull(paramMap, AuthRepConstants.USER_ID, request.getHeaders().get(AuthRepConstants.USER_ID));

        IHttpClientRequest get = httpClient.request(DEFAULT_BACKEND + AUTHREP_PATH + paramMap.encode(),
                HttpMethod.GET,
                new AuthResponseHandler(failureFactory)
                    .failureHandler(policyFailureHandler)
                    .resultHandler(resultHandler));

        get.addHeader("Accept-Charset", "UTF-8");
        get.addHeader("X-3scale-User-Client", "apiman");
        get.end();
    }

    private String getUserKey() {
        return getIdentityElement(config, request, AuthRepConstants.USER_KEY);
    }
}
