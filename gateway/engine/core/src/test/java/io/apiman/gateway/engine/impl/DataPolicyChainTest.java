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
package io.apiman.gateway.engine.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.policy.PolicyWithConfiguration;
import io.apiman.gateway.engine.policy.RequestChain;
import io.apiman.gateway.engine.policy.ResponseChain;
import io.apiman.gateway.engine.util.PassthroughDataPolicy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test {@link RequestChain} and {@link ResponseChain} functionality.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings({"nls"})
public class DataPolicyChainTest {

    private RequestChain requestChain;
    private ResponseChain responseChain;

    private ServiceRequest mockRequest;
    private ServiceResponse mockResponse;
    
    private IApimanBuffer mockBuffer;
    private IAsyncHandler<IApimanBuffer> mockBodyHandler;
    private IAsyncHandler<Void> mockEndHandler;
    private PassthroughDataPolicy policyOne;
    private PolicyWithConfiguration pwcOne;
    private IPolicyContext mockContext;
    private List<PolicyWithConfiguration> policies;
    
    
    @Mock private IReadWriteStream<ServiceRequest> mockRequestHandler;
    @Mock private IReadWriteStream<ServiceResponse> mockResponseHandler;

    @Before
    public void setup() {   
        MockitoAnnotations.initMocks(this);
        
        policies = new ArrayList<>();
        policyOne = spy(new PassthroughDataPolicy("1"));
        
        pwcOne = new PolicyWithConfiguration(policyOne, new Object());
        
        mockContext = mock(IPolicyContext.class);
        
        mockRequest = mock(ServiceRequest.class);
        given(mockRequest.getApiKey()).willReturn("bacon");
        given(mockRequest.getDestination()).willReturn("mars");
        given(mockRequest.getType()).willReturn("request");
        
        mockResponse = mock(ServiceResponse.class);
        given(mockRequest.getApiKey()).willReturn("bacon");
        given(mockRequest.getDestination()).willReturn("mars");
        given(mockRequest.getType()).willReturn("response");
        
        mockBuffer = mock(IApimanBuffer.class);
        given(mockBuffer.toString()).willReturn("bananas"); 
        
        mockRequestHandler = mock(IReadWriteStream.class);
        mockResponseHandler = mock(IReadWriteStream.class);
        
        mockBodyHandler = (IAsyncHandler<IApimanBuffer>) mock(IAsyncHandler.class);
        mockEndHandler = (IAsyncHandler<Void>) mock(IAsyncHandler.class);
    }
    
    @Test
    public void shouldEnsureNonNullRequestReceivedByHandlers() {
        policies.add(pwcOne);
        
        requestChain = new RequestChain(policies, mockContext);
        
        requestChain.bodyHandler(mockBodyHandler);
        requestChain.endHandler(mockEndHandler);
        
        requestChain.doApply(mockRequest);
        requestChain.write(mockBuffer);
        requestChain.end();
        
        verify(mockBodyHandler, times(1)).handle(mockBuffer);
        verify(mockEndHandler, times(1)).handle((Void) null);
        
        // At this point we must ensure that the request and responses are NOT null.
        verify(policyOne).getRequestDataHandler(mockRequest, mockContext);
    }
    
    @Test
    public void shouldEnsureNonNullResponseReceivedByHandlers() {
        policies.add(pwcOne);
        
        responseChain = new ResponseChain(policies, mockContext);
        
        responseChain.bodyHandler(mockBodyHandler);
        responseChain.endHandler(mockEndHandler);
        
        responseChain.doApply(mockResponse);
        responseChain.write(mockBuffer);
        responseChain.end();
        
        verify(mockBodyHandler, times(1)).handle(mockBuffer);
        verify(mockEndHandler, times(1)).handle((Void) null);
        
        // At this point we must ensure that the request and responses are NOT null.
        verify(policyOne).getResponseDataHandler(mockResponse, mockContext);
    }
    
  
}