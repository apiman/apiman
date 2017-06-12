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

package io.apiman.plugins.auth3scale.authrep.appid;

import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.APP_ID;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.APP_KEY;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.REFERRER;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.SERVICE_ID;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.SERVICE_TOKEN;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.USAGE;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.USER_ID;
import static io.apiman.plugins.auth3scale.util.Auth3ScaleUtils.buildRepMetrics;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.plugins.auth3scale.authrep.AbstractAuth;
import io.apiman.plugins.auth3scale.ratelimit.IAuth;
import io.apiman.plugins.auth3scale.util.Auth3ScaleUtils;
import io.apiman.plugins.auth3scale.util.ParameterMap;

@SuppressWarnings("nls")
public class AppIdAuth implements IAuth {
    private static final AsyncResultImpl<Void> FAIL_NO_ROUTE = AsyncResultImpl.create(new RuntimeException("No valid route"));
    private static final AsyncResultImpl<Void> FAIL_PROVIDE_APP_ID = AsyncResultImpl.create(new RuntimeException("No user app id provided")); // TODO mirror 3scale errors
    private static final AsyncResultImpl<Void> FAIL_PROVIDE_APP_KEY = AsyncResultImpl.create(new RuntimeException("No user app key provided")); // TODO mirror 3scale errors

    private Content config;
    private ApiRequest request;
    private AbstractAuth<?> auth;
    private IAsyncHandler<PolicyFailure> policyFailureHandler;

    public AppIdAuth(Content config,
            ApiRequest request,
            IPolicyContext context,
            AbstractAuth<?> auth) {
        this.config = config;
        this.request = request;
        this.auth = auth;
    }

    @Override
    public IAuth auth(IAsyncResultHandler<Void> resultHandler) {
        String appId = AppIdUtils.getAppId(config, request);
        String appKey = AppIdUtils.getAppKey(config, request);

        if (appId == null) {
            resultHandler.handle(FAIL_PROVIDE_APP_ID);
            return this;
        }

        if (appKey == null) {
            resultHandler.handle(FAIL_PROVIDE_APP_KEY);
            return this;
        }

        if (!Auth3ScaleUtils.hasRoutes(config, request)) {
            resultHandler.handle(FAIL_NO_ROUTE);
            return this;
        }

        ParameterMap paramMap = new ParameterMap();
        paramMap.add(APP_ID, appId);
        paramMap.add(APP_KEY, appKey); // TODO possibly optional according to API docs, but not 100% clear
        paramMap.add(SERVICE_TOKEN, config.getBackendAuthenticationValue());
        paramMap.add(SERVICE_ID, Long.toString(config.getProxy().getServiceId()));
        paramMap.add(USAGE, buildRepMetrics(config, request));

        Auth3ScaleUtils.setIfNotNull(paramMap, REFERRER, request.getHeaders().get(REFERRER));
        Auth3ScaleUtils.setIfNotNull(paramMap, USER_ID, request.getHeaders().get(USER_ID));

        auth.setKeyElems(appId, appKey);
        auth.setParameterMap(paramMap);
        auth.policyFailureHandler(policyFailureHandler::handle);
        auth.auth(resultHandler);
        return this;
    }

    @Override
    public AppIdAuth policyFailureHandler(IAsyncHandler<PolicyFailure> policyFailureHandler) {
        this.policyFailureHandler = policyFailureHandler;
        return this;
    }
}
