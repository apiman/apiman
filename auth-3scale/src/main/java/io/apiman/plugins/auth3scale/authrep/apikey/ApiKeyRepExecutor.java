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
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.plugins.auth3scale.authrep.AbstractRepExecutor;
import io.apiman.plugins.auth3scale.authrep.AuthRepConstants;
import io.apiman.plugins.auth3scale.util.ParameterMap;
import io.apiman.plugins.auth3scale.util.report.AuthResponseHandler;

import java.time.OffsetDateTime;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class ApiKeyRepExecutor extends AbstractRepExecutor<ApiKeyAuthReporter> {
    private static final String AUTHORIZE_PATH = "/transactions/authorize.xml?";
    private static final String AUTHREP_PATH = "/transactions/authrep.xml?";

    private ApiKeyAuthReporter reporter;
    private IApimanLogger logger;
    private ApiKeyCachingAuthenticator authCache;

    public ApiKeyRepExecutor(Content config, ApiRequest request, ApiResponse response, IPolicyContext context, ApiKeyAuthReporter reporter, ApiKeyCachingAuthenticator authCache) {
        super(config, request, response, context, reporter, authCache);
        logger = context.getLogger(ApiKeyRepExecutor.class); // TODO think about static
        this.authCache = authCache;
    }

    // Rep seems to require POST with URLEncoding
    @Override
    public ApiKeyRepExecutor rep() {
        doRep();
        return this;
    }

    private void doRep() {
        // If was a blocking request then we already reported, so do nothing.
        if (context.getAttribute("3scale.blocking", false))
            return;

        if (config.getRateLimitingStrategy().isBatched()) {
            doBatchedReport();
        } else {
            doAsyncAuthRep();
        }
    }

    // ApiKeyReportData [endpoint=http://su1.3scale.net:80/transactions.xml, serviceToken=null, userKey=6ade731336760382403649c5d75886ee,
    private void doAsyncAuthRep() {
        // Auth elems
        ParameterMap paramMap = new ParameterMap();
        paramMap.add(AuthRepConstants.USER_KEY, context.getAttribute("3scale.userKey", ""));
        paramMap.add(AuthRepConstants.SERVICE_TOKEN, config.getBackendAuthenticationValue());// maybe use endpoint properties or something. or new properties field.
        paramMap.add(AuthRepConstants.SERVICE_ID, Long.toString(config.getProxy().getServiceId()));
        paramMap.add(AuthRepConstants.USAGE, buildRepMetrics(api));

        setIfNotNull(paramMap, AuthRepConstants.REFERRER, request.getHeaders().get(AuthRepConstants.REFERRER));
        setIfNotNull(paramMap, AuthRepConstants.USER_ID, request.getHeaders().get(AuthRepConstants.USER_ID));

        System.out.println(DEFAULT_BACKEND + AUTHREP_PATH + paramMap.encode());

        IHttpClientRequest get = httpClient.request(DEFAULT_BACKEND + AUTHREP_PATH + paramMap.encode(),
                HttpMethod.GET,
                new AuthResponseHandler(failureFactory)
                .failureHandler(failure -> {
                    // At this point can't do anything but log it.
                    logger.debug("Async AuthRep failure code {0} on: {1}",  failure.getResponseCode(), paramMap);
                    // Flush cache entry for this user.
                    flushCache();
                })
                .resultHandler(result -> {
                    if (result.isError()) {
                        logger.debug("Unexpected error: {0}.", result.getError());
                        flushCache();
                    } else {
                        logger.debug("Reported successfully.");
                    }
                }));

        get.addHeader("Accept-Charset", "UTF-8");
        get.addHeader("X-3scale-User-Client", "apiman");
        get.end();
    }

    private void flushCache() {
        logger.debug("Invalidating cache");
        authCache.invalidate(config, request, context.getAttribute("3scale.userKey", ""));
    }

    // serviceId=2555417735060, timestamp=2016-10-28T22:57:44.273+01:00, userId=null, usage=ParameterMap [data={foo/fooId=1}], log=ParameterMap [data={code=200}]]
    private void doBatchedReport() {
        ApiKeyReportData report = new ApiKeyReportData()
                .setEndpoint(REPORT_ENDPOINT)
                .setServiceToken(config.getBackendAuthenticationValue())
                .setUserKey(getUserKey())
                .setServiceId(Long.toString(config.getProxy().getServiceId()))
                .setTimestamp(OffsetDateTime.now().toString())
                .setUserId(getUserId())
                .setUsage(buildRepMetrics(api))
                .setLog(buildLog());

        logger.debug("Adding a report to batch.");
        reporter.addRecord(report);
    }

    private String getUserId() {
        return request.getHeaders().get(AuthRepConstants.USER_ID);
    }

    private String getUserKey() {
        return getIdentityElement(config, request, "user_key");
    }
}
