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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.config.IgnoredResourcesConfig;
import io.apiman.gateway.engine.policies.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * A simple policy that causes a failure if the paths of the inbound
 * request matching the configured set of regular expressions.
 *
 * @author rubenrm1@gmail.com
 */
public class IgnoredResourcesPolicy extends AbstractMappedPolicy<IgnoredResourcesConfig> {
    
    /**
     * Constructor.
     */
    public IgnoredResourcesPolicy() {
    }
    
    /**
     * @see io.apiman.gateway.engine.policy.AbstractPolicy#getConfigurationClass()
     */
    @Override
    protected Class<IgnoredResourcesConfig> getConfigurationClass() {
        return IgnoredResourcesConfig.class;
    }
    
    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ServiceRequest request, IPolicyContext context, IgnoredResourcesConfig config,
            IPolicyChain<ServiceRequest> chain) {
        if (!satisfiesAnyPath(config, request.getDestination())) {
            super.doApply(request, context, config, chain);
        } else {
            IPolicyFailureFactoryComponent ffactory = context.getComponent(IPolicyFailureFactoryComponent.class);
            String msg = Messages.i18n.format("IgnoredResourcesPolicy.PathsToIgnore", request.getDestination()); //$NON-NLS-1$
            PolicyFailure failure = ffactory.createFailure(PolicyFailureType.NotFound, PolicyFailureCodes.PATHS_TO_IGNORE, msg);
            chain.doFailure(failure);
        }
    }
    
    /**
     * Evaluates whether the destination provided matches any of the configured pathsToIgnore
     * 
     * @param config The {@link IgnoredResourcesConfig} containing the pathsToIgnore
     * @param destination The destination to evaluate
     * @return true if any path matches the destination. false otherwise
     */
    private boolean satisfiesAnyPath(IgnoredResourcesConfig config, String destination) {
        for(String pathToIgnore : config.getPathsToIgnore()) {
            if(destination.matches(pathToIgnore)) {
                return true;
            }
        }
        return false;
    }

}
