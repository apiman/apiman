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

import org.overlord.apiman.engine.policies.config.IPWhitelistConfig;
import org.overlord.apiman.engine.policies.i18n.Messages;
import org.overlord.apiman.rt.engine.beans.PolicyFailureType;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.components.IPolicyFailureFactoryComponent;
import org.overlord.apiman.rt.engine.policy.IPolicyChain;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;

/**
 * A simple policy that causes a failure if the IP address of the inbound
 * request is not included in a specific list of allowed IP addresses.
 *
 * @author eric.wittmann@redhat.com
 */
public class IPWhitelistPolicy extends AbstractPolicy<IPWhitelistConfig> {
    
    /**
     * Constructor.
     */
    public IPWhitelistPolicy() {
    }
    
    /**
     * @see org.overlord.apiman.engine.policies.AbstractPolicy#getConfigClass()
     */
    @Override
    protected Class<IPWhitelistConfig> getConfigClass() {
        return IPWhitelistConfig.class;
    }
    
    /**
     * @see org.overlord.apiman.engine.policies.AbstractPolicy#doApply(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.policy.IPolicyContext, java.lang.Object, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ServiceRequest request, IPolicyContext context, IPWhitelistConfig config,
            IPolicyChain chain) {
        IPWhitelistConfig wc = (IPWhitelistConfig) config;
        if (wc.getIpList().contains(request.getRemoteAddr())) {
            super.doApply(request, context, config, chain);
        } else {
            IPolicyFailureFactoryComponent ffactory = context.getComponent(IPolicyFailureFactoryComponent.class);
            String msg = Messages.i18n.format("IPWhitelistPolicy.NotWhitelisted", request.getRemoteAddr()); //$NON-NLS-1$
            chain.doFailure(ffactory.createFailure(PolicyFailureType.Other, PolicyFailureCodes.IP_NOT_WHITELISTED, msg));
        }
    }

}
