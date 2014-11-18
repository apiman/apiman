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
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.ConfigurationParseException;
import org.overlord.apiman.rt.engine.policy.IPolicy;
import org.overlord.apiman.rt.engine.policy.IPolicyChain;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;

/**
 * A base class for policy impls that use jackson to parse configuration info.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractMappedPolicy<C> implements IPolicy {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    protected C configuration;
    
    /**
     * Constructor.
     */
    public AbstractMappedPolicy() {
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public C parseConfiguration(String jsonConfiguration) throws ConfigurationParseException {
        try {
            return mapper.reader(getConfigurationClass()).readValue(jsonConfiguration);
        } catch (Exception e) {
            throw new ConfigurationParseException(e);
        }
    }

    /**
     * @return the class to use for JSON configuration deserialization
     */
    protected abstract Class<C> getConfigurationClass();

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#apply(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.policy.IPolicyContext, java.lang.Object, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void apply(ServiceRequest request, IPolicyContext context, Object config,
            IPolicyChain<ServiceRequest> chain) {
        doApply(request, context, (C) config, chain);
    }

    /**
     * @param request
     * @param chain
     */
    protected void doApply(ServiceRequest request, IPolicyContext context, C config, IPolicyChain<ServiceRequest> chain) {
        chain.doApply(request);
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#apply(org.overlord.apiman.rt.engine.beans.ServiceResponse, org.overlord.apiman.rt.engine.policy.IPolicyContext, java.lang.Object, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void apply(ServiceResponse response, IPolicyContext context, Object config,
            IPolicyChain<ServiceResponse> chain) {
        doApply(response, context, (C) config, chain);
    }

    /**
     * Apply the policy to the response.
     * @param response
     * @param context
     * @param config
     * @param chain
     */
    protected void doApply(ServiceResponse response, IPolicyContext context, C config, IPolicyChain<ServiceResponse> chain) {
        chain.doApply(response);
    }

}
