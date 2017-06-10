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
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.REPORT_URI;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.USER_ID;
import static io.apiman.plugins.auth3scale.util.IMetricsBuilder.buildLog;
import static io.apiman.plugins.auth3scale.util.IMetricsBuilder.buildRepMetrics;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.plugins.auth3scale.authrep.AbstractRep;
import io.apiman.plugins.auth3scale.authrep.AuthRepConstants;
import io.apiman.plugins.auth3scale.ratelimit.IRep;

public class ApiKeyRep implements IRep {
    private final Content config;
    private final ApiRequest request;
    private final ApiResponse response;
    private final AbstractRep<?> rep;

    public ApiKeyRep(Content config,
            ApiRequest request,
            ApiResponse response,
            IPolicyContext context,
            AbstractRep<?> rep) {
                this.config = config;
                this.request = request;
                this.response = response;
                this.rep = rep;
    }

    @Override
    public IRep rep() {
        // Otherwise build report to be encoded.
        ApiKeyReportData report = new ApiKeyReportData()
                .setEndpoint(REPORT_URI)
                .setReferrer(request.getHeaders().get(REFERRER))
                .setServiceToken(config.getBackendAuthenticationValue())
                .setUserKey(getUserKey())
                .setServiceId(Long.toString(config.getProxy().getServiceId()))
                .setUserId(getUserId())
                .setUsage(buildRepMetrics(config, request))
                .setLog(buildLog(response));

        rep.setKeyElems(getUserKey());
        rep.setReport(report);
        rep.rep();
        return this;
    }

    private String getUserId() {
        return request.getHeaders().get(USER_ID);
    }

    private String getUserKey() {
        return getIdentityElement(config, request, AuthRepConstants.USER_KEY);
    }

    protected String getIdentityElement(Content config, ApiRequest request, String canonicalName)  {
        // Manual for now as there's no mapping in the config.
        String keyFieldName = config.getProxy().getAuthUserKey();
        if (config.getProxy().getCredentialsLocation().equalsIgnoreCase("query")) { //$NON-NLS-1$
            return request.getQueryParams().get(keyFieldName);
        } else { // Else let's assume header
            return request.getHeaders().get(keyFieldName);
        }
    }

}
