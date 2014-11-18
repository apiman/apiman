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
package org.overlord.apiman.rt.engine.policy;

import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.io.IReadWriteStream;

/**
 * A base class for policy impls.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractPolicy implements IPolicy {

    /**
     * Constructor.
     */
    public AbstractPolicy() {
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#apply(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.policy.IPolicyContext, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    public final void apply(ServiceRequest request, IPolicyContext context, IPolicyChain<ServiceRequest> chain) {
        doApply(request, context, chain);
    }

    /**
     * @param request
     * @param chain
     */
    protected void doApply(ServiceRequest request, IPolicyContext context, IPolicyChain<ServiceRequest> chain) {
        chain.doApply(request);
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#apply(org.overlord.apiman.rt.engine.beans.ServiceResponse, org.overlord.apiman.rt.engine.policy.IPolicyContext, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    public final void apply(ServiceResponse response, IPolicyContext context,
            IPolicyChain<ServiceResponse> chain) {
        doApply(response, context, chain);
    }

    /**
     * Apply the policy to the response.
     * @param response
     * @param context
     * @param config
     * @param chain
     */
    protected void doApply(ServiceResponse response, IPolicyContext context, IPolicyChain<ServiceResponse> chain) {
        chain.doApply(response);
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#getRequestHandler()
     */
    @Override
    public IReadWriteStream<ServiceRequest> getRequestHandler() {
        return null;
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#getResponseHandler()
     */
    @Override
    public IReadWriteStream<ServiceResponse> getResponseHandler() {
        return null;
    }

}
