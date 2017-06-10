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

package io.apiman.plugins.auth3scale.authrep.apikey;

import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.REFERRER;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.SERVICE_ID;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.SERVICE_TOKEN;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.USAGE;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.USER_ID;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.USER_KEY;
import static io.apiman.plugins.auth3scale.util.IMetricsBuilder.buildRepMetrics;
import static io.apiman.plugins.auth3scale.util.IMetricsBuilder.hasRoutes;
import static io.apiman.plugins.auth3scale.util.IMetricsBuilder.setIfNotNull;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.plugins.auth3scale.authrep.AbstractAuth;
import io.apiman.plugins.auth3scale.authrep.AuthRepConstants;
import io.apiman.plugins.auth3scale.ratelimit.IAuth;
import io.apiman.plugins.auth3scale.util.ParameterMap;

public class ApiKeyAuth implements IAuth {
    private static final AsyncResultImpl<Void> FAIL_PROVIDE_USER_KEY = AsyncResultImpl.create(new RuntimeException("No user apikey provided!"));
    private static final AsyncResultImpl<Void> FAIL_NO_ROUTE = AsyncResultImpl.create(new RuntimeException("No valid route"));

    private Content config;
    private ApiRequest request;
    private AbstractAuth<?> auth;
    private IAsyncHandler<PolicyFailure> policyFailureHandler;

    public ApiKeyAuth(Content config,
            ApiRequest request,
            IPolicyContext context,
            AbstractAuth<?> auth) {
        this.config = config;
        this.request = request;
        this.auth = auth;
    }

    @Override
    public IAuth auth(IAsyncResultHandler<Void> resultHandler) {
      String userKey = getUserKey();
      if (userKey == null) {
          resultHandler.handle(FAIL_PROVIDE_USER_KEY);
          return this;
      }

      if (!hasRoutes(config, request)) {
          resultHandler.handle(FAIL_NO_ROUTE);
          return this;
      }

      String serviceId = Long.toString(config.getProxy().getServiceId());
      ParameterMap paramMap = new ParameterMap();
      paramMap.add(USER_KEY, userKey);
      paramMap.add(SERVICE_TOKEN, config.getBackendAuthenticationValue());
      paramMap.add(SERVICE_ID, serviceId);
      paramMap.add(USAGE, buildRepMetrics(config, request));

      setIfNotNull(paramMap, REFERRER, request.getHeaders().get(REFERRER));
      setIfNotNull(paramMap, USER_ID, request.getHeaders().get(USER_ID));

      auth.setKeyElems(userKey);
      auth.setParameterMap(paramMap);
      auth.policyFailureHandler(policyFailureHandler::handle);
      auth.auth(resultHandler);
      return this;
    }

    @Override
    public ApiKeyAuth policyFailureHandler(IAsyncHandler<PolicyFailure> policyFailureHandler) {
        this.policyFailureHandler = policyFailureHandler;
        return this;
    }

    private String getUserKey() {
        return getIdentityElement(config, request, AuthRepConstants.USER_KEY);
    }

    protected String getIdentityElement(Content config, ApiRequest request, String canonicalName)  {
        // Manual for now as there's no mapping in the config.
        String keyFieldName = config.getProxy().getAuthUserKey();
        if (config.getProxy().getCredentialsLocation().equalsIgnoreCase("query")) {
            return request.getQueryParams().get(keyFieldName);
        } else { // Else let's assume header
            return request.getHeaders().get(keyFieldName);
        }
    }

}
