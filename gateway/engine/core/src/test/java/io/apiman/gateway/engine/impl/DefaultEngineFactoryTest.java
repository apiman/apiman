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

import io.apiman.common.logging.IDelegateFactory;
import io.apiman.gateway.engine.IApiConnection;
import io.apiman.gateway.engine.IApiConnectionResponse;
import io.apiman.gateway.engine.IApiConnector;
import io.apiman.gateway.engine.IApiRequestExecutor;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
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
@SuppressWarnings({ "nls", "javadoc" })
public class DefaultEngineFactoryTest {

    private List<Policy> policyList;
    private IApimanBuffer mockBufferInbound;
    private IApimanBuffer mockBufferOutbound;
    private IAsyncHandler<IApimanBuffer> mockBodyHandler;
    private IAsyncHandler<Void> mockEndHandler;
    private IAsyncHandler<Void> transmitHandler;

    private MockApiConnection mockApiConnection;
    private MockApiConnectionResponse mockApiConnectionResponse;

    @Before
    public void setup() {
        Policy policyBean = mock(Policy.class);
        given(policyBean.getPolicyImpl()).willReturn(PassthroughPolicy.QUALIFIED_NAME);
        given(policyBean.getPolicyJsonConfig()).willReturn("{}");

        mockBufferInbound = mock(IApimanBuffer.class);
        given(mockBufferInbound.toString()).willReturn("stottie");

        mockBufferOutbound = mock(IApimanBuffer.class);
        given(mockBufferOutbound.toString()).willReturn("bacon");

        policyList = new ArrayList<>();
        policyList.add(policyBean);

        mockBodyHandler = mock(IAsyncHandler.class);
        mockEndHandler = mock(IAsyncHandler.class);
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
            protected IComponentRegistry createComponentRegistry(IPluginRegistry pluginRegistry) {
                return new DefaultComponentRegistry() {
                    @Override
                    protected void registerBufferFactoryComponent() {
                        addComponent(IBufferFactoryComponent.class, new ByteBufferFactoryComponent());
                    }
                };
            }

            @Override
            protected IConnectorFactory createConnectorFactory(IPluginRegistry pluginRegistry) {
                return new IConnectorFactory() {
                    @Override
                    public IApiConnector createConnector(ApiRequest request, Api api, RequiredAuthType requiredAuthType, boolean hasDataPolicy) {
                        Assert.assertEquals("test", api.getEndpointType());
                        Assert.assertEquals("test:endpoint", api.getEndpoint());
                        IApiConnector connector = new IApiConnector() {

                            /**
                             * @see io.apiman.gateway.engine.IApiConnector#connect(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
                             */
                            @Override
                            public IApiConnection connect(ApiRequest request,
                                    IAsyncResultHandler<IApiConnectionResponse> handler)
                                    throws ConnectorException {
                                final ApiResponse response = new ApiResponse();
                                response.setCode(200);
                                response.setMessage("OK"); //$NON-NLS-1$

                                mockApiConnectionResponse = new MockApiConnectionResponse() {

                                    @Override
                                    public void write(IApimanBuffer chunk) {
                                        handleBody(chunk);
                                    }

                                    @Override
                                    protected void handleHead(ApiResponse head) {
                                        return;
                                    }

                                    @Override
                                    public ApiResponse getHead() {
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
                                    public void abort(Throwable t) {
                                    }
                                };

                                IAsyncResult<IApiConnectionResponse> mockResponseResultHandler = mock(IAsyncResult.class);
                                given(mockResponseResultHandler.isSuccess()).willReturn(true);
                                given(mockResponseResultHandler.isError()).willReturn(false);
                                given(mockResponseResultHandler.getResult()).willReturn(mockApiConnectionResponse);

                                mockApiConnection = mock(MockApiConnection.class);
                                given(mockApiConnection.getHead()).willReturn(request);

                                // Handle head
                                handler.handle(mockResponseResultHandler);

                                return mockApiConnection;
                            }

                        };
                        return connector;
                    }
                };
            }

            @Override
            protected IDelegateFactory createLoggerFactory(IPluginRegistry pluginRegistry) {
                // TODO Auto-generated method stub
                return null;
            }
        };

        IEngine engine = factory.createEngine();
        Assert.assertNotNull(engine);

        // create a api
        Api api = new Api();
        api.setEndpointType("test");
        api.setEndpoint("test:endpoint");
        api.setOrganizationId("TestOrg");
        api.setApiId("TestApi");
        api.setVersion("1.0");
        // create a client
        Client app = new Client();
        app.setClientId("TestApp");
        app.setOrganizationId("TestOrg");
        app.setVersion("1.0");
        app.setApiKey("client-12345");
        Contract contract = new Contract();
        contract.setPlan("Gold");
        contract.setApiId("TestApi");
        contract.setApiOrgId("TestOrg");
        contract.setApiVersion("1.0");
        contract.setPolicies(policyList);

        app.addContract(contract);

        // simple api/app config
        engine.getRegistry().publishApi(api, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
            }
        });
        engine.getRegistry().registerClient(app, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
            }
        });

        ApiRequest request = new ApiRequest();
        request.setApiKey("client-12345");
        request.setApiId("TestApi");
        request.setApiOrgId("TestOrg");
        request.setApiVersion("1.0");
        request.setDestination("/");
        request.setUrl("http://localhost:9999/");
        request.setType("TEST");

        IApiRequestExecutor prExecutor = engine.executor(request, new IAsyncResultHandler<IEngineResult>() {

            @Override //At this point, we are either saying *fail* or *response connection is ready*
            public void handle(IAsyncResult<IEngineResult> result) {
                IEngineResult er = result.getResult();

                // No exception occurred
                Assert.assertTrue(result.isSuccess());

                // The chain evaluation succeeded
                Assert.assertNotNull(er);
                Assert.assertTrue(!er.isFailure());
                Assert.assertNotNull(er.getApiResponse());
                Assert.assertEquals("OK", er.getApiResponse().getMessage()); //$NON-NLS-1$

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
                mockApiConnectionResponse.write(mockBufferOutbound);
                mockApiConnectionResponse.end();
            }
        };

        prExecutor.execute();

        // Request handler should receive the mock inbound buffer once only
        verify(mockApiConnection, times(1)).write(mockBufferInbound);

        // Ultimately user should receive the contrived response and end in order.
        InOrder order = inOrder(mockBodyHandler, mockEndHandler);
        order.verify(mockBodyHandler).handle(mockBufferOutbound);
        order.verify(mockEndHandler).handle((Void) null);
    }
}
