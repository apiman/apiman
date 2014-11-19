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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IEngine;
import org.overlord.apiman.rt.engine.IEngineResult;
import org.overlord.apiman.rt.engine.IServiceConnector;
import org.overlord.apiman.rt.engine.IServiceRequestExecutor;
import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.async.IAsyncResult;
import org.overlord.apiman.rt.engine.async.IAsyncResultHandler;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Contract;
import org.overlord.apiman.rt.engine.beans.Policy;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.ConnectorException;
import org.overlord.apiman.rt.engine.io.AbstractSignalStream;
import org.overlord.apiman.rt.engine.io.IBuffer;
import org.overlord.apiman.rt.engine.io.ISignalReadStream;
import org.overlord.apiman.rt.engine.io.ISignalWriteStream;

/**
 * Unit test for the default engine factory.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class DefaultEngineFactoryTest {

    private List<Policy> policyList;
    private IBuffer mockBufferInbound;
    private IBuffer mockBufferOutbound;
    private IAsyncHandler<IBuffer> mockBodyHandler;
    private IAsyncHandler<Void> mockEndHandler;
    private IAsyncHandler<Void> transmitHandler;
    
    private AbstractSignalStream<ServiceRequest> mockRequestHandler;
    private AbstractSignalStream<ServiceResponse> mockResponseHandler;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        Policy policyBean = mock(Policy.class);
        given(policyBean.getPolicyImpl()).willReturn(PassthroughPolicy.QUALIFIED_NAME);
        given(policyBean.getPolicyJsonConfig()).willReturn("{}");
        
        mockBufferInbound = mock(IBuffer.class);
        given(mockBufferInbound.toString()).willReturn("stottie");
        
        mockBufferOutbound = mock(IBuffer.class);
        given(mockBufferOutbound.toString()).willReturn("bacon");

        policyList = new ArrayList<Policy>();   
        policyList.add(policyBean);
        
        mockBodyHandler = (IAsyncHandler<IBuffer>) mock(IAsyncHandler.class);
        mockEndHandler = (IAsyncHandler<Void>) mock(IAsyncHandler.class);
    }

    /**
     * Test method for {@link org.overlord.apiman.rt.engine.impl.AbstractEngineFactory#createEngine()}.
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @Test
    public void testCreateEngine() throws InterruptedException, ExecutionException {
        DefaultEngineFactory factory = new DefaultEngineFactory() {
            @Override
            protected IConnectorFactory createConnectorFactory() {
                return new IConnectorFactory() {
                    @Override
                    public IServiceConnector createConnector(ServiceRequest request, Service service) {
                        Assert.assertEquals("test", service.getEndpointType());
                        Assert.assertEquals("test:endpoint", service.getEndpoint());
                        IServiceConnector connector = new IServiceConnector() {

                            @SuppressWarnings("unchecked")
                            @Override
                            public ISignalWriteStream request(ServiceRequest request,
                                    IAsyncResultHandler<ISignalReadStream<ServiceResponse>> responseHandler)
                                            throws ConnectorException {
                                final ServiceResponse response = new ServiceResponse();
                                response.setCode(200);
                                response.setMessage("OK"); //$NON-NLS-1$

                                mockResponseHandler = new AbstractSignalStream<ServiceResponse>() {

                                    @Override
                                    public void write(IBuffer chunk) {
                                        handleBody(chunk);
                                    }

                                    @Override
                                    protected void handleHead(ServiceResponse head) {
                                        return;
                                    }

                                    @Override
                                    public ServiceResponse getHead() {
                                        return response;
                                    }

                                    @Override
                                    public void end() {
                                        handleEnd();
                                    }

                                    @Override
                                    public void transmit() {
                                        transmitHandler.handle((Void) null);
                                    }

                                    @Override
                                    public void abort() {
                                    }
                                };
                                
                                IAsyncResult<ISignalReadStream<ServiceResponse>> mockResponseResultHandler = mock(IAsyncResult.class);
                                given(mockResponseResultHandler.isSuccess()).willReturn(true);
                                given(mockResponseResultHandler.isError()).willReturn(false);
                                given(mockResponseResultHandler.getResult()).willReturn(mockResponseHandler);

                                mockRequestHandler = mock(AbstractSignalStream.class);
                                given(mockRequestHandler.getHead()).willReturn(request);

                                // Handle head
                                responseHandler.handle(mockResponseResultHandler);

                                return mockRequestHandler;
                            }
                            
                        };
                        return connector;
                    }
                };
            }
        };
        
        IEngine engine = factory.createEngine();
        Assert.assertNotNull(engine);
        
        // create a service
        Service service = new Service();
        service.setEndpointType("test");
        service.setEndpoint("test:endpoint");
        service.setOrganizationId("TestOrg");
        service.setServiceId("TestService");
        service.setVersion("1.0");
        // create an app
        Application app = new Application();
        app.setApplicationId("TestApp"); 
        app.setOrganizationId("TestOrg");
        app.setVersion("1.0");
        Contract contract = new Contract();
        contract.setApiKey("12345");
        contract.setServiceId("TestService");
        contract.setServiceOrgId("TestOrg"); 
        contract.setServiceVersion("1.0"); 
        contract.setPolicies(policyList);
        
        app.addContract(contract);
        
        // simple service/app config
        engine.publishService(service);
        engine.registerApplication(app);
        
        ServiceRequest request = new ServiceRequest();
        request.setApiKey("12345");
        request.setDestination("/");
        request.setType("TEST");
                        
        IServiceRequestExecutor prExecutor = engine.executor(request, new IAsyncResultHandler<IEngineResult>() {
            
            @Override //At this point, we are either saying *fail* or *response connection is ready*
            public void handle(IAsyncResult<IEngineResult> result) {
                IEngineResult er = result.getResult();
                
                // No exception occurred
                Assert.assertTrue(result.isSuccess());
                
                // The chain evaluation succeeded
                Assert.assertNotNull(er);
                Assert.assertTrue(!er.isFailure());
                Assert.assertNotNull(er.getServiceResponse());
                Assert.assertEquals("OK", er.getServiceResponse().getMessage()); //$NON-NLS-1$
                
                er.bodyHandler(mockBodyHandler);
                er.endHandler(mockEndHandler);  
            }
        });
        
        prExecutor.streamHandler(new IAsyncHandler<ISignalWriteStream>() {
            
            @Override
            public void handle(ISignalWriteStream streamWriter) {
                streamWriter.write(mockBufferInbound);
                streamWriter.end();
            }
        });
           
        transmitHandler = new IAsyncHandler<Void>() {
            
            @Override
            public void handle(Void result) {
                // NB: This is cheating slightly for testing purposes, we don't have real async here.
                // Only now start writing stuff, so user has had opportunity to set handlers
                mockResponseHandler.write(mockBufferOutbound);
                mockResponseHandler.end();
            }
        };
        
        prExecutor.execute();
        
        // Request handler should receive the mock inbound buffer once only
        verify(mockRequestHandler, times(1)).write(mockBufferInbound);
       
        // Ultimately user should receive the contrived response and end in order.
        InOrder order = inOrder(mockBodyHandler, mockEndHandler); 
        order.verify(mockBodyHandler).handle(mockBufferOutbound);
        order.verify(mockEndHandler).handle((Void) null);   
    }
}
