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

import org.overlord.apiman.engine.policies.config.IPBlacklistConfig;
import org.overlord.apiman.engine.policies.i18n.Messages;
import org.overlord.apiman.rt.engine.beans.PolicyFailureType;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.components.IPolicyFailureFactoryComponent;
import org.overlord.apiman.rt.engine.policy.IPolicyChain;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;

/**
 * A simple policy that fails the inbound request if its IP address is
 * included in the list of dis-allowed IPs.
 *
 * @author eric.wittmann@redhat.com
 */
public class IPBlacklistPolicy extends AbstractMappedPolicy<IPBlacklistConfig> {
    
    /**
     * Constructor.
     */
    public IPBlacklistPolicy() {
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.AbstractPolicy#getConfigurationClass()
     */
    @Override
    protected Class<IPBlacklistConfig> getConfigurationClass() {
        return IPBlacklistConfig.class;
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.AbstractPolicy#doApply(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.policy.IPolicyContext, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ServiceRequest request, IPolicyContext context, IPolicyChain<ServiceRequest> chain) {
        if (getConfiguration().getIpList().contains(request.getRemoteAddr())) {
            IPolicyFailureFactoryComponent ffactory = context.getComponent(IPolicyFailureFactoryComponent.class);
            String msg = Messages.i18n.format("IPBlacklistPolicy.NotBlacklisted", request.getRemoteAddr()); //$NON-NLS-1$
            chain.doFailure(ffactory.createFailure(PolicyFailureType.Other, PolicyFailureCodes.IP_BLACKLISTED, msg));
        } else {
            super.doApply(request, context, chain);
        }
    }

}
