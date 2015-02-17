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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.policy.PolicyWithConfiguration;
import io.apiman.gateway.engine.policy.RequestChain;
import io.apiman.gateway.engine.policy.ResponseChain;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

/**
 * Test {@link RequestChain} and {@link ResponseChain} functionality.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class PolicyChainTest {

    private RequestChain requestChain;
    private ResponseChain responseChain;

    private ServiceRequest mockRequest;
    private ServiceResponse mockResponse;
    
    private IApimanBuffer mockBuffer;
    private IAsyncHandler<IApimanBuffer> mockBodyHandler;
    private IAsyncHandler<Void> mockEndHandler;
    private PassthroughPolicy policyOne;
    private PassthroughPolicy policyTwo;
    private PolicyWithConfiguration pwcOne;
    private PolicyWithConfiguration pwcTwo;
    private IPolicyContext mockContext;
    private List<PolicyWithConfiguration> policies;

    @Before
    public void setup() {   
        policies = new ArrayList<>();
        policyOne = spy(new PassthroughPolicy("1"));
        policyTwo = spy(new PassthroughPolicy("2"));
        pwcOne = new PolicyWithConfiguration(policyOne, new Object());
        pwcTwo = new PolicyWithConfiguration(policyTwo, new Object());
        
        //mockChain = mock(IPolicyChain.class);
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
        
        mockBodyHandler = (IAsyncHandler<IApimanBuffer>) mock(IAsyncHandler.class);
        mockEndHandler = (IAsyncHandler<Void>) mock(IAsyncHandler.class);
    }
    
    @Test
    public void shouldExecuteRequestChainTwice() {
        policies.add(pwcOne);
        policies.add(pwcTwo);
        
        requestChain = new RequestChain(policies, mockContext);

        requestChain.bodyHandler(mockBodyHandler);
        requestChain.endHandler(mockEndHandler);
        
        requestChain.doApply(mockRequest);
        requestChain.write(mockBuffer);
        requestChain.write(mockBuffer);
        requestChain.end();

        verify(mockBodyHandler, times(2)).handle(mockBuffer);
        verify(mockEndHandler, times(1)).handle((Void) null);
        
        InOrder order = inOrder(policyOne, policyTwo);
        order.verify(policyOne).apply(mockRequest, mockContext, pwcOne.getConfiguration(), requestChain);
        order.verify(policyTwo).apply(mockRequest, mockContext, pwcTwo.getConfiguration(), requestChain);
    }
    
    @Test
    public void shouldExecuteResponseChainTwice() {
        policies.add(pwcOne);
        policies.add(pwcTwo);
        
        responseChain = new ResponseChain(policies, mockContext);
        
        responseChain.bodyHandler(mockBodyHandler);
        responseChain.endHandler(mockEndHandler);
        
        responseChain.doApply(mockResponse);
        responseChain.write(mockBuffer);
        responseChain.write(mockBuffer);
        responseChain.end();

        verify(mockBodyHandler, times(2)).handle(mockBuffer);
        verify(mockEndHandler, times(1)).handle((Void) null);   
        
        InOrder order = inOrder(policyTwo, policyOne);
        order.verify(policyTwo).apply(mockResponse, mockContext, pwcTwo.getConfiguration(), responseChain);
        order.verify(policyOne).apply(mockResponse, mockContext, pwcOne.getConfiguration(), responseChain);
    }
    
    @Test
    public void shouldExecuteWithoutHandlers() {
        policies.add(pwcOne);
        requestChain = new RequestChain(policies, mockContext);
        requestChain.doApply(mockRequest);
        requestChain.end();
    }
    
    @Test
    public void shouldPreserveBufferOrder() {
        policies.add(pwcOne);
        requestChain = new RequestChain(policies, mockContext);
        requestChain.bodyHandler(mockBodyHandler);
        requestChain.endHandler(mockEndHandler);
        
        requestChain.doApply(mockRequest);
        
        IApimanBuffer buffer1 = (IApimanBuffer) mock(IApimanBuffer.class);
        IApimanBuffer buffer2 = (IApimanBuffer) mock(IApimanBuffer.class);
        IApimanBuffer buffer3 = (IApimanBuffer) mock(IApimanBuffer.class);
        
        requestChain.write(buffer1);
        requestChain.write(buffer2);
        requestChain.write(buffer3);
        
        requestChain.end();
        
        InOrder order = inOrder(mockBodyHandler, mockEndHandler);
        order.verify(mockBodyHandler).handle(buffer1);
        order.verify(mockBodyHandler).handle(buffer2);
        order.verify(mockBodyHandler).handle(buffer3);
        order.verify(mockEndHandler).handle((Void) null);
    }
    
    @Test
    public void shouldCallFailureHandlerOnDoFail() {
        policies.add(pwcOne);
        policies.add(pwcTwo);
        
        requestChain = new RequestChain(policies, mockContext);
        
        IAsyncHandler<PolicyFailure> mPolicyFailureHandler = mock(IAsyncHandler.class);
        
        PolicyFailure mPolicyFailure = mock(PolicyFailure.class);
        
        requestChain.policyFailureHandler(mPolicyFailureHandler);
        requestChain.bodyHandler(mockBodyHandler);
        requestChain.endHandler(mockEndHandler);
        
        requestChain.doApply(mockRequest);
        requestChain.doFailure(mPolicyFailure);

        verify(mPolicyFailureHandler).handle(mPolicyFailure);
    }
    
    @Test
    public void shouldCallErrorHandlerOnThrowError() {
        policies.add(pwcOne);
        policies.add(pwcTwo);
        
        requestChain = new RequestChain(policies, mockContext);
        
        IAsyncHandler<Throwable> mThrowableFailureHandler = mock(IAsyncHandler.class);
        
        Throwable mThrowable = mock(Throwable.class);
        
        requestChain.policyErrorHandler(mThrowableFailureHandler);
        requestChain.bodyHandler(mockBodyHandler);
        requestChain.endHandler(mockEndHandler);
        
        requestChain.doApply(mockRequest);
        requestChain.throwError(mThrowable);
        
        verify(mThrowableFailureHandler).handle(mThrowable);
    }
}