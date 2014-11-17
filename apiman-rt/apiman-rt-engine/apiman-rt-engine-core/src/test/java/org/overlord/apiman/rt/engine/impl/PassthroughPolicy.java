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

import org.overlord.apiman.rt.engine.ApimanBuffer;
import org.overlord.apiman.rt.engine.async.AbstractStream;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.ConfigurationParseException;
import org.overlord.apiman.rt.engine.policy.Chain;
import org.overlord.apiman.rt.engine.policy.AbstractPolicy;
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
    private ServiceResponse serviceResponse;
    private ServiceRequest serviceRequest;
    
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
    public Object getConfig() {
        return config;
    }

    @Override
    public void setConfig(Object config) {
        this.config = config;
    }

    @Override
    public void request(ServiceRequest request, IPolicyContext context,
            Chain<ServiceRequest> chain) {
        this.serviceRequest = request;
        chain.doApply(request);
    }

    @Override
    public AbstractStream<ServiceRequest> getRequestHandler() {
        return requestHandler;
    }

    @Override
    public void response(ServiceResponse response, IPolicyContext context,
            Chain<ServiceResponse> chain) {
        this.serviceResponse = response;
        chain.doApply(response);
    }

    @Override
    public AbstractStream<ServiceResponse> getResponseHandler() {
        return responseHandler;
    }
    
    private AbstractStream<ServiceRequest> requestHandler = new AbstractStream<ServiceRequest>() {

        @Override
        public void write(ApimanBuffer chunk) {
            handleBody(chunk);
        }

        @Override
        public void end() {
            handleEnd();
        }

        @Override
        protected void handleHead(ServiceRequest head) {
            handleHead(head);
        }

        @Override
        public ServiceRequest getHead() {
            return serviceRequest;
        }
        
    };
    
    private AbstractStream<ServiceResponse> responseHandler = new AbstractStream<ServiceResponse>() {

        @Override
        public void write(ApimanBuffer chunk) {
            handleBody(chunk);
        }

        @Override
        public void end() {
            handleEnd();
        }

        @Override
        protected void handleHead(ServiceResponse head) {
            handleHead(head);
        }

        @Override
        public ServiceResponse getHead() {
            return serviceResponse;
        }
        
    };

    @Override
    protected ServiceRequest getServiceRequest() {
        return serviceRequest;
    }

    @Override
    protected ServiceResponse getServiceResponse() {
        return serviceResponse;
    }

    @Override
    public void abort() {
        // TODO Auto-generated method stub
        
    }
}
