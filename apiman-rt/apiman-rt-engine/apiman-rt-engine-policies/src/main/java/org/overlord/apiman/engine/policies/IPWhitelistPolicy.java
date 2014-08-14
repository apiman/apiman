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
package org.overlord.apiman.engine.policies;

import org.codehaus.jackson.map.ObjectMapper;
import org.overlord.apiman.engine.policies.i18n.Messages;
import org.overlord.apiman.rt.engine.IPolicyFailureFactoryComponent;
import org.overlord.apiman.rt.engine.beans.PolicyFailureType;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.policy.IPolicy;
import org.overlord.apiman.rt.engine.policy.IPolicyChain;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;

/**
 * A simple policy that 
 *
 * @author eric.wittmann@redhat.com
 */
public class IPWhitelistPolicy implements IPolicy {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Constructor.
     */
    public IPWhitelistPolicy() {
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     * 
     * Configuration format:
     * <pre>
     *   {
     *     "ipList" : [ "1.2.3.4", "3.4.5.6", "10.0.0.1" ]
     *   }
     * </pre>
     */
    @Override
    public Object parseConfiguration(String jsonConfiguration) {
        try {
            return mapper.reader(IPWhitelistConfig.class).readValue(jsonConfiguration);
        } catch (Exception e) {
            // TODO configuration parsing exceptions should be checked exceptions on the IPolicy interface
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#apply(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.policy.IPolicyContext, java.lang.Object, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ServiceRequest request, IPolicyContext context, Object config, IPolicyChain chain) {
        IPWhitelistConfig wc = (IPWhitelistConfig) config;
        if (wc.getIpList().contains(request.getRemoteAddr())) {
            chain.doApply(request);
        } else {
            IPolicyFailureFactoryComponent ffactory = context.getComponent(IPolicyFailureFactoryComponent.class);
            String msg = Messages.i18n.format("IPWhitelistPolicy.NotWhitelisted", request.getRemoteAddr()); //$NON-NLS-1$
            chain.doFailure(ffactory.createFailure(PolicyFailureType.Other, FailureCodes.IP_NOT_WHITELISTED, msg));
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#apply(org.overlord.apiman.rt.engine.beans.ServiceResponse, org.overlord.apiman.rt.engine.policy.IPolicyContext, java.lang.Object, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ServiceResponse response, IPolicyContext context, Object config, IPolicyChain chain) {
        chain.doApply(response);
    }

}
