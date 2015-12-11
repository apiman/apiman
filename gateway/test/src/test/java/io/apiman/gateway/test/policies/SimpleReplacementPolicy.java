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
package io.apiman.gateway.test.policies;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.test.policies.beans.SimpleReplacementBean;

/**
 * @author eric.wittmann@redhat.com
 */
public class SimpleReplacementPolicy extends AbstractMappedPolicy<SimpleReplacementBean> {

    /**
     * Constructor.
     */
    public SimpleReplacementPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<SimpleReplacementBean> getConfigurationClass() {
        return SimpleReplacementBean.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @SuppressWarnings("nls")
    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, SimpleReplacementBean config,
            IPolicyChain<ApiRequest> chain) {
        request.getHeaders().put("X-Field-1", config.getField1());
        request.getHeaders().put("X-Field-2", config.getField2());
        request.getHeaders().put("X-Field-3", config.getField3());
        super.doApply(request, context, config, chain);
    }

}
