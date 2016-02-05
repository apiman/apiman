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
package io.apiman.plugins.simpleheaderpolicy;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.simpleheaderpolicy.beans.AddHeaderBean;
import io.apiman.plugins.simpleheaderpolicy.beans.AddHeaderBean.ApplyTo;
import io.apiman.plugins.simpleheaderpolicy.beans.SimpleHeaderPolicyDefBean;

import java.util.Map;

/**
 * Set, overwrite and/or delete headers on request, response or both, with pattern matching available.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class SimpleHeaderPolicy extends AbstractMappedPolicy<SimpleHeaderPolicyDefBean> {

    @Override
    protected Class<SimpleHeaderPolicyDefBean> getConfigurationClass() {
        return SimpleHeaderPolicyDefBean.class;
    }

    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, SimpleHeaderPolicyDefBean config,
            IPolicyChain<ApiRequest> chain) {
        setHeaders(request.getHeaders(), config, ApplyTo.REQUEST);
        stripHeaders(request.getHeaders(), config);
        chain.doApply(request);
    }

    @Override
    protected void doApply(ApiResponse response, IPolicyContext context,
            SimpleHeaderPolicyDefBean config, IPolicyChain<ApiResponse> chain) {
        setHeaders(response.getHeaders(), config, ApplyTo.RESPONSE);
        stripHeaders(response.getHeaders(), config);
        chain.doApply(response);
    }

    private void setHeaders(HeaderMap headers, SimpleHeaderPolicyDefBean config, ApplyTo applyTo) {
        for (AddHeaderBean header : config.getAddHeaders()) {
            if ((header.getApplyTo() == applyTo || header.getApplyTo() == ApplyTo.BOTH)) {
                if (header.getOverwrite() || !headers.containsKey(header.getHeaderName())) {
                    headers.put(header.getHeaderName(), header.getResolvedHeaderValue());
                }
            }
        }
    }

    private void stripHeaders(HeaderMap headers, SimpleHeaderPolicyDefBean config) {
        for (Map.Entry<String, String> header : headers) {
            if (config.getKeyRegex().matcher(header.getKey()).matches()) {
                headers.remove(header.getKey());
            }

            if (config.getValueRegex().matcher(header.getValue()).matches()) {
                headers.remove(header.getKey());
            }
        }
    }
}
