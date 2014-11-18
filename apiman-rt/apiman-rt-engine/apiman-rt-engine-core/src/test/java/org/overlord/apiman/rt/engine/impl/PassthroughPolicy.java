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
package org.overlord.apiman.rt.engine.impl;

import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.ConfigurationParseException;
import org.overlord.apiman.rt.engine.policy.AbstractPolicy;
import org.overlord.apiman.rt.engine.policy.IPolicyChain;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;

/**
 * A pass-through {@link AbstractPolicy} impl for testing purposes.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class PassthroughPolicy extends AbstractPolicy {
    
    public static final String QUALIFIED_NAME = "class:" + PassthroughPolicy.class.getCanonicalName();
    private Object config;
    private String name;
    
    public PassthroughPolicy(){}

    public PassthroughPolicy(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public Object parseConfiguration(String jsonConfiguration) throws ConfigurationParseException {
        this.config = (Object) jsonConfiguration;
        return config;
    }

    @Override
    public Object getConfiguration() {
        return config;
    }

    @Override
    public void setConfiguration(Object config) {
        this.config = config;
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.AbstractPolicy#doApply(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.policy.IPolicyContext, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ServiceRequest request, IPolicyContext context, IPolicyChain<ServiceRequest> chain) {
        chain.doApply(request);
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.AbstractPolicy#doApply(org.overlord.apiman.rt.engine.beans.ServiceResponse, org.overlord.apiman.rt.engine.policy.IPolicyContext, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ServiceResponse response, IPolicyContext context,
            IPolicyChain<ServiceResponse> chain) {
        chain.doApply(response);
    }

}
