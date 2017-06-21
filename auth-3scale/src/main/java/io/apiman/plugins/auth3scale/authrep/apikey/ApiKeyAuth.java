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
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.USER_ID;
import static io.apiman.plugins.auth3scale.util.Auth3ScaleUtils.buildRepMetrics;
import static io.apiman.plugins.auth3scale.util.Auth3ScaleUtils.hasRoutes;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Auth3ScaleBean;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.BackendConfiguration;
import io.apiman.plugins.auth3scale.authrep.AbstractAuth;
import io.apiman.plugins.auth3scale.ratelimit.IAuth;
import io.apiman.plugins.auth3scale.util.Auth3ScaleUtils;

public class ApiKeyAuth implements IAuth {
    private static final AsyncResultImpl<Void> FAIL_PROVIDE_USER_KEY = AsyncResultImpl.create(new RuntimeException("No user apikey provided!")); //$NON-NLS-1$
    private static final AsyncResultImpl<Void> FAIL_NO_ROUTE = AsyncResultImpl.create(new RuntimeException("No valid route")); //$NON-NLS-1$

    private BackendConfiguration config;
    private ApiRequest request;
    private AbstractAuth auth;
    private IAsyncHandler<PolicyFailure> policyFailureHandler;

    public ApiKeyAuth(Auth3ScaleBean auth3ScaleBean,
            ApiRequest request,
            IPolicyContext context,
            AbstractAuth auth) {
        this.config = auth3ScaleBean.getThreescaleConfig().getProxyConfig().getBackendConfig();
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
      ApiKeyReportData data = new ApiKeyReportData()
              .setUserKey(userKey)
              .setServiceToken(config.getBackendAuthenticationValue())
              .setServiceId(serviceId)
              .setUsage(buildRepMetrics(config, request))
              .setReferrer(request.getHeaders().get(REFERRER))
              .setUserId(request.getHeaders().get(USER_ID));

      auth.setKeyElems(userKey);
      auth.setReport(data);
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
        return Auth3ScaleUtils.getUserKey(config, request);
    }
}
