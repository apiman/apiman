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
package io.apiman.plugins.config_policy;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * A policy that simply adds a header to the inbound http request.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConfigPolicy extends AbstractMappedPolicy<ConfigBean> {

    /**
     * Constructor.
     */
    public ConfigPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<ConfigBean> getConfigurationClass() {
        return ConfigBean.class;
    }
    
    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ServiceRequest request, IPolicyContext context, ConfigBean config,
            IPolicyChain<ServiceRequest> chain) {
        request.getHeaders().put(config.getRequestHeader(), "true"); //$NON-NLS-1$
        super.doApply(request, context, config, chain);
    }
    
    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ServiceResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ServiceResponse response, IPolicyContext context, ConfigBean config,
            IPolicyChain<ServiceResponse> chain) {
        response.getHeaders().put(config.getResponseHeader(), "true"); //$NON-NLS-1$
        super.doApply(response, context, config, chain);
    }

}
