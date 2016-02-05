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

import io.apiman.gateway.engine.policies.config.IgnoredResource;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.PolicyFailureCodes;
import io.apiman.gateway.engine.policies.config.IgnoredResourcesConfig;
import io.apiman.gateway.engine.policies.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * A simple policy that causes a failure if the paths of the inbound request
 * matching the configured set of regular expressions.
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
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest,
     *      io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object,
     *      io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, IgnoredResourcesConfig config,
            IPolicyChain<ApiRequest> chain) {
        if (!satisfiesAnyPath(config, request.getDestination(), request.getType())) {
            super.doApply(request, context, config, chain);
        } else {
            IPolicyFailureFactoryComponent ffactory = context
                    .getComponent(IPolicyFailureFactoryComponent.class);
            String msg = Messages.i18n.format("IgnoredResourcesPolicy.PathIgnored", //$NON-NLS-1$
                    request.getDestination());
            PolicyFailure failure = ffactory.createFailure(PolicyFailureType.NotFound,
                    PolicyFailureCodes.PATHS_TO_IGNORE, msg);
            chain.doFailure(failure);
        }
    }

    /**
     * Evaluates whether the destination provided matches any of the configured
     * rules
     * 
     * @param config
     *            The {@link IgnoredResourcesConfig} containing the
     *            rules
     * @param destination
     *            The destination to evaluate
     * @param verb
     *            HTTP verb or '*' for all verbs
     * @return true if any path matches the destination. false otherwise
     */
    private boolean satisfiesAnyPath(IgnoredResourcesConfig config, String destination, String verb) {
        if (destination == null || destination.trim().length() == 0) {
            destination = "/"; //$NON-NLS-1$
        }
        for (IgnoredResource resource : config.getRules()) {
            String resourceVerb = resource.getVerb();
            boolean verbMatches = verb == null || IgnoredResource.VERB_MATCH_ALL.equals(resourceVerb)
                    || verb.equalsIgnoreCase(resourceVerb); // $NON-NLS-1$
            boolean destinationMatches = destination.matches(resource.getPathPattern());
            if (verbMatches && destinationMatches) {
                return true;
            }
        }
        return false;
    }

}
