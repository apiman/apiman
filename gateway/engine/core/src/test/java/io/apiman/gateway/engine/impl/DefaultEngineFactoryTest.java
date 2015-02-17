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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.IServiceConnector;
import io.apiman.gateway.engine.IServiceRequestExecutor;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.engine.util.PassthroughPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

/**
 * Unit test for the default engine factory.
 */
@SuppressWarnings("nls")
public class DefaultEngineFactoryTest {

    private List<Policy> policyList;
    private IApimanBuffer mockBufferInbound;
    private IApimanBuffer mockBufferOutbound;
    private IAsyncHandler<IApimanBuffer> mockBodyHandler;
    private IAsyncHandler<Void> mockEndHandler;
    private IAsyncHandler<Void> transmitHandler;
    
    private MockServiceConnection mockServiceConnection;
    private MockServiceConnectionResponse mockServiceConnectionResponse;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        Policy policyBean = mock(Policy.class);
        given(policyBean.getPolicyImpl()).willReturn(PassthroughPolicy.QUALIFIED_NAME);
        given(policyBean.getPolicyJsonConfig()).willReturn("{}");
        
        mockBufferInbound = mock(IApimanBuffer.class);
        given(mockBufferInbound.toString()).willReturn("stottie");
        
        mockBufferOutbound = mock(IApimanBuffer.class);
        given(mockBufferOutbound.toString()).willReturn("bacon");

        policyList = new ArrayList<Policy>();   
        policyList.add(policyBean);
        
        mockBodyHandler = (IAsyncHandler<IApimanBuffer>) mock(IAsyncHandler.class);
        mockEndHandler = (IAsyncHandler<Void>) mock(IAsyncHandler.class);
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.impl.AbstractEngineFactory#createEngine()}.
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
                            
                            /**
                             * @see io.apiman.gateway.engine.IServiceConnector#connect(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
                             */
                            @SuppressWarnings("unchecked")
                            @Override
                            public IServiceConnection connect(ServiceRequest request,
                                    IAsyncResultHandler<IServiceConnectionResponse> handler)
                                    throws ConnectorException {
                                final ServiceResponse response = new ServiceResponse();
                                response.setCode(200);
                                response.setMessage("OK"); //$NON-NLS-1$

                                mockServiceConnectionResponse = new MockServiceConnectionResponse() {

                                    @Override
                                    public void write(IApimanBuffer chunk) {
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
                                
                                IAsyncResult<IServiceConnectionResponse> mockResponseResultHandler = mock(IAsyncResult.class);
                                given(mockResponseResultHandler.isSuccess()).willReturn(true);
                                given(mockResponseResultHandler.isError()).willReturn(false);
                                given(mockResponseResultHandler.getResult()).willReturn(mockServiceConnectionResponse);

                                mockServiceConnection = mock(MockServiceConnection.class);
                                given(mockServiceConnection.getHead()).willReturn(request);

                                // Handle head
                                handler.handle(mockResponseResultHandler);

                                return mockServiceConnection;
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
                mockServiceConnectionResponse.write(mockBufferOutbound);
                mockServiceConnectionResponse.end();
            }
        };
        
        prExecutor.execute();
        
        // Request handler should receive the mock inbound buffer once only
        verify(mockServiceConnection, times(1)).write(mockBufferInbound);
       
        // Ultimately user should receive the contrived response and end in order.
        InOrder order = inOrder(mockBodyHandler, mockEndHandler); 
        order.verify(mockBodyHandler).handle(mockBufferOutbound);
        order.verify(mockEndHandler).handle((Void) null);   
    }
}
