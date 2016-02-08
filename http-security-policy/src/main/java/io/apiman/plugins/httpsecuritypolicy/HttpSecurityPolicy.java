/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.plugins.httpsecuritypolicy;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.httpsecuritypolicy.beans.ContentSecurityPolicyBean;
import io.apiman.plugins.httpsecuritypolicy.beans.HttpSecurityBean;
import io.apiman.plugins.httpsecuritypolicy.beans.HttpSecurityBean.FrameOptions;
import io.apiman.plugins.httpsecuritypolicy.beans.HttpSecurityBean.XssProtection;

/**
 * Security-related HTTP headers can be set, such as HSTS, CSP and XSS protection.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HttpSecurityPolicy extends AbstractMappedPolicy<HttpSecurityBean> {

    @Override
    protected Class<HttpSecurityBean> getConfigurationClass() {
        return HttpSecurityBean.class;
    }

    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, HttpSecurityBean config,
            IPolicyChain<ApiRequest> chain) {
        chain.doApply(request);
    }

    @Override
    protected void doApply(ApiResponse response, IPolicyContext context, HttpSecurityBean config,
            IPolicyChain<ApiResponse> chain) {

        setSecurityHeaders(config, response.getHeaders());

        chain.doApply(response);
    }

    @SuppressWarnings("nls")
    private void setSecurityHeaders(HttpSecurityBean config, HeaderMap headers) {
        if (config.getHsts().getEnabled()) {
            headers.put("Strict-Transport-Security", config.getHsts().getHeaderValue());
        }

        if (config.getFrameOptions() != FrameOptions.DISABLED) {
            headers.put("X-Frame-Options", config.getFrameOptions().toString());
        }

        if (config.getXssProtection() != XssProtection.DISABLED) {
            headers.put("X-XSS-Protection", config.getXssProtection().toString());
        }

        if (config.getContentTypeOptions()) {
            headers.put("X-Content-Type-Options", "nosniff");
        }

        ContentSecurityPolicyBean policyBean = config.getContentSecurityPolicy();

        if (policyBean.getMode() != ContentSecurityPolicyBean.Mode.DISABLED) {

            if (policyBean.getMode() == ContentSecurityPolicyBean.Mode.ENABLED) {
                headers.put("Content-Security-Policy", policyBean.getCsp());
            }

            if (policyBean.getMode() == ContentSecurityPolicyBean.Mode.REPORT_ONLY) {
                headers.put("Content-Security-Policy-Report-Only", policyBean.getCsp());
            }
        }

    }
}
