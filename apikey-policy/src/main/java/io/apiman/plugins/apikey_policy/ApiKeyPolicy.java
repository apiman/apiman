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
package io.apiman.plugins.apikey_policy;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * A policy that simply adds a header to the inbound http request.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiKeyPolicy extends AbstractMappedPolicy<ApiKeyConfigBean> {

    /**
     * Constructor.
     */
    public ApiKeyPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<ApiKeyConfigBean> getConfigurationClass() {
        return ApiKeyConfigBean.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, ApiKeyConfigBean config,
            IPolicyChain<ApiRequest> chain) {
        if (request.getApiKey() != null) {
            String header = config.getRequestHeader();
            request.getHeaders().put(header, request.getApiKey());
            // Ensure that connector does not strip the header out (e.g. X-Api-Key)
            context.getConnectorConfiguration().permitRequestHeader(header);
        }
        super.doApply(request, context, config, chain);
    }

}
