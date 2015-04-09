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
package io.apiman.gateway.engine.util;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policy.IDataPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * A pass-through {@link IDataPolicy} impl for testing purposes.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class PassthroughDataPolicy implements IDataPolicy {
    
    public static final String QUALIFIED_NAME = "class:" + PassthroughDataPolicy.class.getCanonicalName();
    private Object config;
    private String name;
    private IReadWriteStream<ServiceRequest> dataRequestHandler;

    private IReadWriteStream<ServiceResponse> dataResponseHandler;
    
    public PassthroughDataPolicy(){}

    public PassthroughDataPolicy(String name) {
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

    @Override
    public IReadWriteStream<ServiceRequest> getRequestDataHandler(ServiceRequest request,
            IPolicyContext context) {
        return dataRequestHandler;
    }

    @Override
    public IReadWriteStream<ServiceResponse> getResponseDataHandler(ServiceResponse response,
            IPolicyContext context) {
        return dataResponseHandler;
    }
    
    /**
     * @return the dataRequestHandler
     */
    public IReadWriteStream<ServiceRequest> getDataRequestHandler() {
        return dataRequestHandler;
    }

    /**
     * @param dataRequestHandler the dataRequestHandler to set
     */
    public void setDataRequestHandler(IReadWriteStream<ServiceRequest> dataRequestHandler) {
        this.dataRequestHandler = dataRequestHandler;
    }

}
