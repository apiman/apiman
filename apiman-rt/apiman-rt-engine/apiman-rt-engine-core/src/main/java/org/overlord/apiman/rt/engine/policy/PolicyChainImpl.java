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

import java.util.List;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;


public class PolicyChainImpl implements IPolicyChain {
    
    private IPolicyChainHandler chainHandler;
    private List<IPolicy> policies;
    private IPolicyContext context;
    private int inboundPolicyIndex;
    private int outboundPolicyIndex;

    public PolicyChainImpl(List<IPolicy> policies, IPolicyContext context, IPolicyChainHandler chainHandler) {
        this.policies = policies;
        this.inboundPolicyIndex = 0;
        this.outboundPolicyIndex = policies.size() - 1;
        this.context = context;
        this.chainHandler = chainHandler;
    }
    
    public void doApply(ServiceRequest request) {
        if (inboundPolicyIndex < policies.size()) {
            try {
                IPolicy policy = policies.get(inboundPolicyIndex++);
                Object policyConfig = null; //TODO get policy config.
                policy.apply(request, this.context, policyConfig, this);
            } catch (Throwable error) {
                this.chainHandler.onError(error);
            }
        } else {
            this.chainHandler.onInboundComplete(request);
        }
    }
    
    public void doApply(ServiceResponse response) {
        if (outboundPolicyIndex >= 0) {
            try {
                IPolicy policy = policies.get(outboundPolicyIndex--);
                Object policyConfig = null; //TODO get policy config.
                policy.apply(response, this.context, policyConfig, this);
            } catch (Throwable error) {
                this.chainHandler.onError(error);
            }
        } else {
            this.chainHandler.onOutboundComplete(response);
        }
    }

    public void doFailure(PolicyFailure failure) {
        this.chainHandler.onFailure(failure);
    }
}
