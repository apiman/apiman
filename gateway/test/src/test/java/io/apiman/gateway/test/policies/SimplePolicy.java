/*
 * Copyright 2014 JBoss Inc
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
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * A simple policy used for testing.
 *
 * @author eric.wittmann@redhat.com
 */
public class SimplePolicy implements IPolicy {

    public static int inboundCallCounter = 0;
    public static int outboundCallCounter = 0;
    public static void reset() {
        inboundCallCounter = 0;
        outboundCallCounter = 0;
    }

    /**
     * Constructor.
     */
    public SimplePolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public Object parseConfiguration(String jsonConfiguration) {
        return new Object();
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(final ApiRequest request, final IPolicyContext context, final Object config,
            final IPolicyChain<ApiRequest> chain) {
        inboundCallCounter++;
        request.getHeaders().put("X-Test-InboundCallCounter", String.valueOf(inboundCallCounter)); //$NON-NLS-1$
        chain.doApply(request);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ApiResponse response, IPolicyContext context, Object config,
            IPolicyChain<ApiResponse> chain) {
        outboundCallCounter++;
        response.getHeaders().put("X-Test-OutboundCallCounter", String.valueOf(outboundCallCounter)); //$NON-NLS-1$
        chain.doApply(response);
    }

}
