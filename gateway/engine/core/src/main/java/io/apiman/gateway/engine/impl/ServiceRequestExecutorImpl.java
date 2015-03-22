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

import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.IServiceConnector;
import io.apiman.gateway.engine.IServiceRequestExecutor;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.beans.exceptions.InvalidServiceException;
import io.apiman.gateway.engine.beans.exceptions.RequestAbortedException;
import io.apiman.gateway.engine.i18n.Messages;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.engine.metrics.RequestMetric;
import io.apiman.gateway.engine.policy.Chain;
import io.apiman.gateway.engine.policy.IConnectorInterceptor;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import io.apiman.gateway.engine.policy.ISettablePolicyContext;
import io.apiman.gateway.engine.policy.PolicyWithConfiguration;
import io.apiman.gateway.engine.policy.RequestChain;
import io.apiman.gateway.engine.policy.ResponseChain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

    private IRegistry registry;
    private ServiceRequest request;
    private Service service;
    private ISettablePolicyContext context;
    private List<Policy> policies;
    private IPolicyFactory policyFactory;
    private IConnectorFactory connectorFactory;
    private boolean finished = false;

    private List<PolicyWithConfiguration> policyImpls;

    private IAsyncResultHandler<IEngineResult> resultHandler;
    private IAsyncHandler<PolicyFailure> policyFailureHandler;
    private IAsyncHandler<Throwable> policyErrorHandler;

    private IAsyncHandler<ISignalWriteStream> inboundStreamHandler;

    private Chain<ServiceRequest> requestChain;
    private Chain<ServiceResponse> responseChain;

    private IServiceConnection serviceConnection;
    private IServiceConnectionResponse serviceConnectionResponse;
    
    private IMetrics metrics;
    private RequestMetric requestMetric = new RequestMetric();

    /**
     * Constructs a new {@link ServiceRequestExecutorImpl}.
     * @param serviceRequest
     * @param resultHandler
     * @param registry
     * @param context
     * @param policyFactory
     * @param connectorFactory
     * @param metrics 
     */
    public ServiceRequestExecutorImpl(ServiceRequest serviceRequest,
            IAsyncResultHandler<IEngineResult> resultHandler, IRegistry registry, ISettablePolicyContext context,
            IPolicyFactory policyFactory, IConnectorFactory connectorFactory, IMetrics metrics) {
        this.request = serviceRequest;
        this.registry = registry;
        this.resultHandler = wrapResultHandler(resultHandler);
        this.context = context;
        this.policyFactory = policyFactory;
        this.connectorFactory = connectorFactory;
        this.policyFailureHandler = createPolicyFailureHandler();
        this.policyErrorHandler = createPolicyErrorHandler();
        this.metrics = metrics;
    }

    /**
     * Wraps the result handler so that metrics can be properly recorded.
     * @param handler
     */
    private IAsyncResultHandler<IEngineResult> wrapResultHandler(final IAsyncResultHandler<IEngineResult> handler) {
        return new IAsyncResultHandler<IEngineResult>() {
            @Override
            public void handle(IAsyncResult<IEngineResult> result) {
                if (result.isError()) {
                    recordErrorMetrics(result.getError());
                } else {
                    IEngineResult engineResult = result.getResult();
                    if (engineResult.isFailure()) {
                        recordFailureMetrics(engineResult.getPolicyFailure());
                    } else {
                        recordSuccessMetrics(engineResult.getServiceResponse());
                    }
                }
                requestMetric.setRequestEnd(new Date());
                metrics.record(requestMetric);
                handler.handle(result);
            }
        };
    }

    /**
     * @param response
     */
    protected void recordSuccessMetrics(ServiceResponse response) {
        requestMetric.setResponseCode(response.getCode());
        requestMetric.setResponseMessage(response.getMessage());
    }

    /**
     * @param failure
     */
    protected void recordFailureMetrics(PolicyFailure failure) {
        requestMetric.setResponseCode(failure.getResponseCode());
        requestMetric.setFailureCode(failure.getFailureCode());
        requestMetric.setFailureReason(failure.getMessage());
    }

    /**
     * @param error
     */
    protected void recordErrorMetrics(Throwable error) {
        requestMetric.setResponseCode(500);
        requestMetric.setError(true);
        requestMetric.setErrorMessage(error.getMessage());
    }

    /**
     * @see io.apiman.gateway.engine.IServiceRequestExecutor#execute()
     */
    @Override
    public void execute() {
        // Fill out some of the basic metrics structure.
        requestMetric.setRequestStart(new Date());
        requestMetric.setResource(request.getDestination());
        requestMetric.setMethod(request.getType());
        requestMetric.setServiceOrgId(request.getServiceOrgId());
        requestMetric.setServiceId(request.getServiceId());
        requestMetric.setServiceVersion(request.getServiceVersion());
        
        // Create the handler that will be called once the policies are asynchronously
        // loaded (can happen this way due to the plugin framework).
        final IAsyncHandler<List<PolicyWithConfiguration>> policiesLoadedHandler = new IAsyncHandler<List<PolicyWithConfiguration>>() {
            @Override
            public void handle(List<PolicyWithConfiguration> result) {
                policyImpls = result;
                // Set up the policy chain request, call #doApply to execute.
                requestChain = createRequestChain(new IAsyncHandler<ServiceRequest>() {
                    @Override
                    public void handle(ServiceRequest request) {
                        IConnectorInterceptor connectorInterceptor = context.getConnectorInterceptor();
                        IServiceConnector connector = null;
                        if (connectorInterceptor == null) {
                            connector = connectorFactory.createConnector(request, service);
                        } else {
                            connector = connectorInterceptor.createConnector();
                        }

                        // TODO check for a null connector

                        // Open up a connection to the back-end if we're given the OK from the request chain
                        requestMetric.setServiceStart(new Date());
                        // Attach the response handler here.
                        serviceConnection = connector.connect(request, createServiceConnectionResponseHandler());

                        // Write the body chunks from the *policy request* into the connector request.
                        requestChain.bodyHandler(new IAsyncHandler<IApimanBuffer>() {
                            @Override
                            public void handle(IApimanBuffer buffer) {
                                serviceConnection.write(buffer);
                            }
                        });

                        // Indicate end from policy chain request to connector request.
                        requestChain.endHandler(new IAsyncHandler<Void>() {
                            @Override
                            public void handle(Void result) {
                                serviceConnection.end();
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
        };
        
        // If no API Key provided - the service must be public.  If an API Key *is* provided
        // then we lookup the Contract and use that.
        if (request.getApiKey() == null) {
            registry.getService(request.getServiceOrgId(), request.getServiceId(), request.getServiceVersion(), 
                new IAsyncResultHandler<Service>() {
                    @Override
                    public void handle(IAsyncResult<Service> result) {
                        if (result.isSuccess()) {
                            service = result.getResult();
                            if (service == null) {
                                Exception error = new InvalidServiceException(Messages.i18n.format("EngineImpl.ServiceNotFound")); //$NON-NLS-1$
                                resultHandler.handle(AsyncResultImpl.create(error, IEngineResult.class));
                            } else if (!service.isPublicService()) {
                                Exception error = new InvalidServiceException(Messages.i18n.format("EngineImpl.ServiceNotPublic")); //$NON-NLS-1$
                                resultHandler.handle(AsyncResultImpl.create(error, IEngineResult.class));
                            } else {
                                policies = service.getServicePolicies();
                                policyImpls = new ArrayList<>(policies.size());
                                loadPolicies(policiesLoadedHandler);
                            }
                        } else if (result.isError()) {
                            resultHandler.handle(AsyncResultImpl.create(result.getError(), IEngineResult.class));
                        }
                    }
                });
        } else {
            registry.getContract(request, new IAsyncResultHandler<ServiceContract>() {
                @Override
                public void handle(IAsyncResult<ServiceContract> result) {
                    if (result.isSuccess()) {
                        ServiceContract serviceContract = result.getResult();
                        requestMetric.setApplicationOrgId(serviceContract.getApplication().getOrganizationId());
                        requestMetric.setApplicationId(serviceContract.getApplication().getApplicationId());
                        requestMetric.setApplicationVersion(serviceContract.getApplication().getVersion());
                        requestMetric.setContractId(request.getApiKey());

                        
                        service = serviceContract.getService();
                        request.setContract(serviceContract);
                        policies = serviceContract.getPolicies();
                        policyImpls = new ArrayList<>(policies.size());
                        if (request.getServiceOrgId() != null) {
                            try {
                                validateRequest(request);
                            } catch (InvalidContractException e) {
                                resultHandler.handle(AsyncResultImpl.create(e, IEngineResult.class));
                                return;
                            }
                        }
                        loadPolicies(policiesLoadedHandler);
                    } else {
                        resultHandler.handle(AsyncResultImpl.create(result.getError(), IEngineResult.class));
                    }
                }
            });
        }
    }

    /**
     * Validates that the contract being used for the request is valid against the
     * service information included in the request.  Basically the request includes
     * information indicating which specific service is being invoked.  This method
     * ensures that the service information in the contract matches the requested
     * service.
     * @param request
     */
    protected void validateRequest(ServiceRequest request) throws InvalidContractException {
        ServiceContract contract = request.getContract();
        
        boolean matches = true;
        if (!contract.getService().getOrganizationId().equals(request.getServiceOrgId())) {
            matches = false;
        }
        if (!contract.getService().getServiceId().equals(request.getServiceId())) {
            matches = false;
        }
        if (!contract.getService().getVersion().equals(request.getServiceVersion())) {
            matches = false;
        }
        if (!matches) {
            throw new InvalidContractException(Messages.i18n.format("EngineImpl.InvalidContractForService", //$NON-NLS-1$
                    request.getServiceOrgId(), request.getServiceId(), request.getServiceVersion()));
        }
    }

    /**
     * Get/resolve the list of policies into a list of policies with config.  This operation is
     * done asynchronously so that plugins can be downloaded if needed.  Any errors in resolving
     * the policies will be reported back via the policyErrorHandler.
     * @param handler
     */
    private void loadPolicies(final IAsyncHandler<List<PolicyWithConfiguration>> handler) {
        final Set<Integer> totalCounter = new HashSet<>();
        final Set<Integer> errorCounter = new TreeSet<>();
        final List<PolicyWithConfiguration> rval = new ArrayList<>(policies.size());
        final List<Throwable> errors = new ArrayList<>(policies.size());
        final int numPolicies = policies.size();
        int index = 0;

        // If there aren't any policies, then no need to asynchronously load them!
        if (policies.isEmpty()) {
            handler.handle(policyImpls);
            return;
        }
        
        for (final Policy policy : policies) {
            rval.add(null);
            errors.add(null);
            final int localIdx = index++;
            policyFactory.loadPolicy(policy.getPolicyImpl(), new IAsyncResultHandler<IPolicy>() {
                @Override
                public void handle(IAsyncResult<IPolicy> result) {
                    if (result.isSuccess()) {
                        IPolicy policyImpl = result.getResult();
                        try {
                            Object policyConfig = policyFactory.loadConfig(policyImpl, policy.getPolicyJsonConfig());
                            PolicyWithConfiguration pwc = new PolicyWithConfiguration(policyImpl, policyConfig);
                            rval.set(localIdx, pwc);
                        } catch (Throwable t) {
                            errors.set(localIdx, t);
                            errorCounter.add(localIdx);
                        }
                    } else {
                        Throwable error = result.getError();
                        errors.set(localIdx, error);
                        errorCounter.add(localIdx);
                    }
                    totalCounter.add(localIdx);
                    // Have we done them all?
                    if (totalCounter.size() == numPolicies) {
                        // Did we get any errors?  If yes, report the first one. If no, then send back
                        // the fully resolved list of policies.
                        if (errorCounter.size() > 0) {
                            int errorIdx = errorCounter.iterator().next();
                            Throwable error = errors.get(errorIdx);
                            // TODO add some logging here to indicate which policy error'd out
                            //Policy errorPolicy = policies.get(errorIdx);
                            policyErrorHandler.handle(error);
                        } else {
                            handler.handle(rval);
                        }
                    }
                }
            });
        }
    }

    /**
     * Creates a response handler that is called by the service connector once a connection
     * to the back end service has been made and a response received.
     */
    private IAsyncResultHandler<IServiceConnectionResponse> createServiceConnectionResponseHandler() {
        return new IAsyncResultHandler<IServiceConnectionResponse>() {
            @Override
            public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                if (result.isSuccess()) {
                    requestMetric.setServiceEnd(new Date());
                    // The result came back. NB: still need to put it through the response chain.
                    serviceConnectionResponse = result.getResult();
                    ServiceResponse serviceResponse = serviceConnectionResponse.getHead();
                    context.setAttribute("apiman.engine.serviceResponse", serviceResponse); //$NON-NLS-1$

                    // Execute the response chain to evaluate the response.
                    responseChain = createResponseChain(new IAsyncHandler<ServiceResponse>() {

                        @Override
                        public void handle(ServiceResponse result) {
                            // Send the service response to the caller.
                            final EngineResultImpl engineResult = new EngineResultImpl(result);
                            engineResult.setConnectorResponseStream(serviceConnectionResponse);

                            resultHandler.handle(AsyncResultImpl.<IEngineResult> create(engineResult));

                            // We've come all the way through the response chain successfully
                            responseChain.bodyHandler(new IAsyncHandler<IApimanBuffer>() {

                                @Override
                                public void handle(IApimanBuffer result) {
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
                            serviceConnectionResponse.transmit();
                        }
                    });

                    // Write data from the back-end response into the response chain.
                    serviceConnectionResponse.bodyHandler(new IAsyncHandler<IApimanBuffer>() {

                        @Override
                        public void handle(IApimanBuffer buffer) {
                            responseChain.write(buffer);
                        }
                    });

                    // Indicate back-end response is finished to the response chain.
                    serviceConnectionResponse.endHandler(new IAsyncHandler<Void>() {

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
        inboundStreamHandler.handle(new ISignalWriteStream() {     
            boolean streamFinished = false;

            @Override
            public void write(IApimanBuffer buffer) {
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
            
            /**
             * @see io.apiman.gateway.engine.io.IAbortable#abort()
             */
            @Override
            public void abort() {
                // If this is called, it means that something went wrong on the inbound
                // side of things - so we need to make sure we abort and cleanup the
                // service connector resources.  We'll also call handle() on the result
                // handler so that the caller knows something went wrong.
                streamFinished = true;
                serviceConnection.abort();
                resultHandler.handle(AsyncResultImpl.<IEngineResult>create(new RequestAbortedException()));
            }


            @Override
            public boolean isFinished() {
                return streamFinished;
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.IServiceRequestExecutor#isFinished()
     */
    @Override
    public boolean isFinished() {
        return finished;
    }

    /**
     * @see io.apiman.gateway.engine.IServiceRequestExecutor#streamHandler(io.apiman.gateway.engine.async.IAsyncHandler)
     */
    @Override
    public void streamHandler(IAsyncHandler<ISignalWriteStream> handler) {
        this.inboundStreamHandler = handler;
    }

    /**
     * Creates the chain used to apply policies in order to the service request.
     * @param requestHandler
     */
    private Chain<ServiceRequest> createRequestChain(IAsyncHandler<ServiceRequest> requestHandler) {
        RequestChain requestChain = new RequestChain(policyImpls, context);
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
        ResponseChain responseChain = new ResponseChain(policyImpls, context);
        responseChain.headHandler(responseHandler);
        responseChain.policyFailureHandler(new IAsyncHandler<PolicyFailure>() {
            @Override
            public void handle(PolicyFailure result) {
                serviceConnectionResponse.abort();
                policyFailureHandler.handle(result);
            }
        });
        responseChain.policyErrorHandler(new IAsyncHandler<Throwable>() {
            @Override
            public void handle(Throwable result) {
                serviceConnectionResponse.abort();
                policyErrorHandler.handle(result);
            }
        });
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
