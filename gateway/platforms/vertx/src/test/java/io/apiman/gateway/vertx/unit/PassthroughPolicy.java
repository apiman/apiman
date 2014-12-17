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
package io.apiman.gateway.vertx.unit;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * A pass-through {@link AbstractPolicy} impl for testing purposes.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class PassthroughPolicy implements IPolicy {
    
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
    
    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ServiceRequest request, IPolicyContext context, Object config,
            IPolicyChain<ServiceRequest> chain) {
        chain.doApply(request);
    }
    
    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ServiceResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ServiceResponse response, IPolicyContext context, Object config,
            IPolicyChain<ServiceResponse> chain) {
        chain.doApply(response);
    }

}
