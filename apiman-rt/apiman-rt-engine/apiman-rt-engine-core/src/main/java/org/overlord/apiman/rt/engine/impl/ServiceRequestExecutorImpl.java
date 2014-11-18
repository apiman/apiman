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

import java.util.List;

import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IEngineResult;
import org.overlord.apiman.rt.engine.IServiceConnector;
import org.overlord.apiman.rt.engine.IServiceRequestExecutor;
import org.overlord.apiman.rt.engine.async.AsyncResultImpl;
import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.async.IAsyncResult;
import org.overlord.apiman.rt.engine.async.IAsyncResultHandler;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceContract;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.io.IBuffer;
import org.overlord.apiman.rt.engine.io.ISignalReadStream;
import org.overlord.apiman.rt.engine.io.ISignalWriteStream;
import org.overlord.apiman.rt.engine.io.IWriteStream;
import org.overlord.apiman.rt.engine.policy.Chain;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;
import org.overlord.apiman.rt.engine.policy.PolicyWithConfiguration;
import org.overlord.apiman.rt.engine.policy.RequestChain;
import org.overlord.apiman.rt.engine.policy.ResponseChain;

/**
 * Manages a single request-response sequence. It is executed in the following
 * order:
 *
 * - Invoke and evaluate request chain.
 * - Invoke back-end connector {@link IServiceConnector}.
 * - Invoke handler set on {@link #streamHandler(IAsyncHandler)} chunks stream
 *   through request chain, into connector).
 * - Invoke and evaluate response chain.
 * - Return results via {@link #resultHandler}.
 *
 * In the case of failure, the {@link #resultHandler} is called at the earliest
 * opportunity.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class ServiceRequestExecutorImpl implements IServiceRequestExecutor {
    private ServiceRequest request;
    private ServiceContract serviceContract;
    private IPolicyContext context;
    private List<PolicyWithConfiguration> policies;
    private IConnectorFactory connectorFactory;
    private boolean finished = false;

    private IAsyncResultHandler<IEngineResult> resultHandler;
    private IAsyncHandler<PolicyFailure> policyFailureHandler;
    private IAsyncHandler<Throwable> policyErrorHandler;

    private IAsyncHandler<IWriteStream> inboundStreamHandler;

    private Chain<ServiceRequest> requestChain;
    private Chain<ServiceResponse> responseChain;

    /**
     * Constructs a new {@link ServiceRequestExecutorImpl}.
     * @param serviceRequest
     * @param resultHandler
     * @param serviceContract
     * @param context
     * @param policies
     * @param connectorFactory
     */
    public ServiceRequestExecutorImpl(ServiceRequest serviceRequest, 
            IAsyncResultHandler<IEngineResult> resultHandler,
            ServiceContract serviceContract,
            IPolicyContext context,
            List<PolicyWithConfiguration> policies,
            IConnectorFactory connectorFactory) {

        this.request = serviceRequest;
        this.resultHandler = resultHandler;
        this.serviceContract = serviceContract;
        this.context = context;
        this.policies = policies;
        this.connectorFactory = connectorFactory;
        this.policyFailureHandler = createPolicyFailureHandler();
        this.policyErrorHandler = createPolicyErrorHandler();
    }

    /**
     * @see org.overlord.apiman.rt.engine.IPolicyRequestExecutor#execute()
     */
    @Override
    public void execute() {
        // Set up the policy chain request, call #doApply to execute.
        requestChain = createRequestChain(new IAsyncHandler<ServiceRequest>() {

            @Override
            public void handle(ServiceRequest request) {
                final Service service = serviceContract.getService();

                IServiceConnector connector = connectorFactory.createConnector(request, service);

                // Open up a connection to the back-end if we're given the OK from the request chain
                // Attach the response handler here.
                final ISignalWriteStream connectorRequestStream = connector.request(request, createConnectorResponseHandler());

                // Write the body chunks from the *policy request* into the connector request.
                requestChain.bodyHandler(new IAsyncHandler<IBuffer>() {

                    @Override
                    public void handle(IBuffer buffer) {
                        connectorRequestStream.write(buffer);
                    }
                });

                // Indicate end from policy chain request to connector request.
                requestChain.endHandler(new IAsyncHandler<Void>() {

                    @Override
                    public void handle(Void result) {
                        connectorRequestStream.end();
                    }
                });

                // Once we have returned from connector.request, we know it is safe to start
                // writing chunks without buffering. At this point, it is the responsibility
                // of the implementation as to how they should cope with the chunks.
                handleStream();
            }
        });

        requestChain.policyFailureHandler(policyFailureHandler);
        requestChain.doApply(request);
    }

    /**
     * Creates a response handler that is called by the service connector once a connection
     * to the back end service has been made and a response received.
     */
    private IAsyncResultHandler<ISignalReadStream<ServiceResponse>> createConnectorResponseHandler() {
        return new IAsyncResultHandler<ISignalReadStream<ServiceResponse>>() {

            @Override
            public void handle(IAsyncResult<ISignalReadStream<ServiceResponse>> result) {
                if (result.isSuccess()) {
                    // The result came back. NB: still need to put it through the response chain.
                    final ISignalReadStream<ServiceResponse> connectorResponseStream = result.getResult();
                    ServiceResponse serviceResponse = connectorResponseStream.getHead();
                    context.setAttribute("apiman.engine.serviceResponse", serviceResponse); //$NON-NLS-1$

                    // Execute the response chain to evaluate the response.
                    responseChain = createResponseChain(new IAsyncHandler<ServiceResponse>() {

                        @Override
                        public void handle(ServiceResponse result) {
                            
                            // Send the service response to the caller.
                            final EngineResultImpl engineResult = new EngineResultImpl(result);

                            resultHandler.handle(AsyncResultImpl.<IEngineResult> create(engineResult));

                            // We've come all the way through the response chain successfully
                            responseChain.bodyHandler(new IAsyncHandler<IBuffer>() {

                                @Override
                                public void handle(IBuffer result) {
                                    engineResult.write(result);
                                }
                            });

                            responseChain.endHandler(new IAsyncHandler<Void>() {

                                @Override
                                public void handle(Void result) {
                                    engineResult.end();
                                    finished = true;
                                }
                            });

                            // Signal to the connector that it's safe to start transmitting data.
                            connectorResponseStream.transmit();
                        }
                    });

                    // Write data from the back-end response into the response chain.
                    connectorResponseStream.bodyHandler(new IAsyncHandler<IBuffer>() {

                        @Override
                        public void handle(IBuffer buffer) {
                            responseChain.write(buffer);
                        }
                    });

                    // Indicate back-end response is finished to the response chain.
                    connectorResponseStream.endHandler(new IAsyncHandler<Void>() {

                        @Override
                        public void handle(Void result) {
                            responseChain.end();
                        }
                    });

                    responseChain.doApply(serviceResponse);
                }
            }

        };
    }

    /**
     * Called when the service connector is ready to receive data from the inbound
     * client request.
     */
    protected void handleStream() {
        inboundStreamHandler.handle(new IWriteStream() {     
            boolean streamFinished = false;

            @Override
            public void write(IBuffer buffer) {
                if (streamFinished) {
                    throw new IllegalStateException("Attempted write after #end() was called."); //$NON-NLS-1$
                }

                requestChain.write(buffer);
            }

            @Override
            public void end() {
                requestChain.end();
                streamFinished = true;
            }


            @Override
            public boolean isFinished() {
                return streamFinished;
            }
        });
    }

    /**
     * @see org.overlord.apiman.rt.engine.IPolicyRequestExecutor#isFinished()
     */
    @Override
    public boolean isFinished() {
        return finished;
    }

    /**
     * @see org.overlord.apiman.rt.engine.IPolicyRequestExecutor#streamHandler(org.overlord.apiman.rt.engine.async.IAsyncHandler)
     */
    @Override
    public void streamHandler(IAsyncHandler<IWriteStream> handler) {
        this.inboundStreamHandler = handler;
    }

    /**
     * Creates the chain used to apply policies in order to the service request.
     * @param requestHandler
     */
    private Chain<ServiceRequest> createRequestChain(IAsyncHandler<ServiceRequest> requestHandler) {
        RequestChain requestChain = new RequestChain(policies, context);
        requestChain.headHandler(requestHandler);
        requestChain.policyFailureHandler(policyFailureHandler);
        requestChain.policyErrorHandler(policyErrorHandler);
        return requestChain;
    }

    /**
     * Creates the chain used to apply policies in reverse order to the service response.
     * @param responseHandler
     */
    private Chain<ServiceResponse> createResponseChain(IAsyncHandler<ServiceResponse> responseHandler) {
        ResponseChain responseChain = new ResponseChain(policies, context);
        responseChain.headHandler(responseHandler);
        responseChain.policyFailureHandler(policyFailureHandler);
        responseChain.policyErrorHandler(policyErrorHandler);
        return responseChain;
    }

    /**
     * Creates the handler to use when a policy failure occurs during processing
     * of a chain.
     */
    private IAsyncHandler<PolicyFailure> createPolicyFailureHandler() {
        return new IAsyncHandler<PolicyFailure>() {
            @Override
            public void handle(PolicyFailure result) {
                // One of the policies has triggered a failure. At this point we should stop processing and
                // send the failure to the client for appropriate handling.
                EngineResultImpl engineResult = new EngineResultImpl(result);
                resultHandler.handle(AsyncResultImpl.<IEngineResult> create(engineResult));
            }
        };
    }

    /**
     * Creates the handler to use when an error is detected during the processing
     * of a chain.
     */
    private IAsyncHandler<Throwable> createPolicyErrorHandler() {
        return new IAsyncHandler<Throwable>() {

            @Override
            public void handle(Throwable error) {
                resultHandler.handle(AsyncResultImpl.<IEngineResult> create(error));
            }
        };
    }
}
