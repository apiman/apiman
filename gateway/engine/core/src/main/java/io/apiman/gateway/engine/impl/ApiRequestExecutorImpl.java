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

import io.apiman.common.util.ApimanStrLookup;
import io.apiman.gateway.engine.GatewayConfigProperties;
import io.apiman.gateway.engine.IApiConnection;
import io.apiman.gateway.engine.IApiConnectionResponse;
import io.apiman.gateway.engine.IApiConnector;
import io.apiman.gateway.engine.IApiRequestExecutor;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.exceptions.InvalidApiException;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.beans.exceptions.RequestAbortedException;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.i18n.Messages;
import io.apiman.gateway.engine.io.ByteBuffer;
import io.apiman.gateway.engine.io.BytesPayloadIO;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IPayloadIO;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.engine.io.JsonPayloadIO;
import io.apiman.gateway.engine.io.SoapPayloadIO;
import io.apiman.gateway.engine.io.XmlPayloadIO;
import io.apiman.gateway.engine.metrics.RequestMetric;
import io.apiman.gateway.engine.policy.Chain;
import io.apiman.gateway.engine.policy.IConnectorInterceptor;
import io.apiman.gateway.engine.policy.IDataPolicy;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import io.apiman.gateway.engine.policy.PolicyContextKeys;
import io.apiman.gateway.engine.policy.PolicyWithConfiguration;
import io.apiman.gateway.engine.policy.RequestChain;
import io.apiman.gateway.engine.policy.ResponseChain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * Manages a single request-response sequence. It is executed in the following
 * order:
 *
 * - Invoke and evaluate request chain.
 * - Invoke back-end connector {@link IApiConnector}.
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
public class ApiRequestExecutorImpl implements IApiRequestExecutor {

    private static final long DEFAULT_MAX_PAYLOAD_BUFFER_SIZE = 5 * 1024 * 1024; // in bytes

    private static StrLookup LOOKUP = new ApimanStrLookup();
    private static StrSubstitutor PROPERTY_SUBSTITUTOR = new StrSubstitutor(LOOKUP);
    static {
        PROPERTY_SUBSTITUTOR.setValueDelimiter(':');
    }

    private final IRegistry registry;
    private ApiRequest request;
    private Api api;
    private IPolicyContext context;
    private List<Policy> policies;
    private final IPolicyFactory policyFactory;
    private final IConnectorFactory connectorFactory;
    private final IBufferFactoryComponent bufferFactory;
    private boolean finished = false;

    private List<PolicyWithConfiguration> policyImpls;

    private IAsyncResultHandler<IEngineResult> resultHandler;
    private IAsyncHandler<PolicyFailure> policyFailureHandler;
    private IAsyncHandler<Throwable> policyErrorHandler;

    private IAsyncHandler<ISignalWriteStream> inboundStreamHandler;

    private Chain<ApiRequest> requestChain;
    private Chain<ApiResponse> responseChain;

    private IApiConnection apiConnection;
    private IApiConnectionResponse apiConnectionResponse;

    private IMetrics metrics;
    private RequestMetric requestMetric = new RequestMetric();

    private IPayloadIO payloadIO;
    // max payload buffer size (if not already set in the api itself)
    private long maxPayloadBufferSize = DEFAULT_MAX_PAYLOAD_BUFFER_SIZE;
    private boolean hasDataPolicy = false;

    /**
     * Constructs a new {@link ApiRequestExecutorImpl}.
     * @param apiRequest the api request
     * @param resultHandler the result handler
     * @param registry the registry
     * @param context the context
     * @param policyFactory the policy factory
     * @param connectorFactory the connector factory
     * @param metrics the metrics instance
     */
    public ApiRequestExecutorImpl(ApiRequest apiRequest,
            IAsyncResultHandler<IEngineResult> resultHandler, IRegistry registry, IPolicyContext context,
            IPolicyFactory policyFactory, IConnectorFactory connectorFactory, IMetrics metrics,
            IBufferFactoryComponent bufferFactory) {
        this.request = apiRequest;
        this.registry = registry;
        this.resultHandler = wrapResultHandler(resultHandler);
        this.context = context;
        this.policyFactory = policyFactory;
        this.connectorFactory = connectorFactory;
        this.policyFailureHandler = createPolicyFailureHandler();
        this.policyErrorHandler = createPolicyErrorHandler();
        this.metrics = metrics;
        this.bufferFactory = bufferFactory;

        String mbs = System.getProperty(GatewayConfigProperties.MAX_PAYLOAD_BUFFER_SIZE);
        if (mbs != null) {
            maxPayloadBufferSize = new Long(mbs);
        }
    }

    /**
     * Wraps the result handler so that metrics can be properly recorded.
     */
    private IAsyncResultHandler<IEngineResult> wrapResultHandler(final IAsyncResultHandler<IEngineResult> handler) {
        return (IAsyncResult<IEngineResult> result) -> {
            boolean doRecord = true;
            if (result.isError()) {
                recordErrorMetrics(result.getError());
            } else {
                IEngineResult engineResult = result.getResult();
                if (engineResult.isFailure()) {
                    recordFailureMetrics(engineResult.getPolicyFailure());
                } else {
                    recordSuccessMetrics(engineResult.getApiResponse());
                    doRecord = false; // don't record the metric now because we need to record # of bytes downloaded, which hasn't happened yet
                }
            }
            requestMetric.setRequestEnd(new Date());
            if (doRecord) {
                metrics.record(requestMetric);
            }
            handler.handle(result);
        };
    }

    /**
     * Record success metrics
     */
    protected void recordSuccessMetrics(ApiResponse response) {
        requestMetric.setResponseCode(response.getCode());
        requestMetric.setResponseMessage(response.getMessage());
    }

    /**
     * Record failure metrics
     */
    protected void recordFailureMetrics(PolicyFailure failure) {
        requestMetric.setResponseCode(failure.getResponseCode());
        requestMetric.setFailure(true);
        requestMetric.setFailureCode(failure.getFailureCode());
        requestMetric.setFailureReason(failure.getMessage());
    }

    /**
     * Record an error (i.e. Exception)
     */
    protected void recordErrorMetrics(Throwable error) {
        requestMetric.setResponseCode(500);
        requestMetric.setError(true);
        requestMetric.setErrorMessage(error.getMessage());
    }

    /**
     * @see io.apiman.gateway.engine.IApiRequestExecutor#execute()
     */
    @Override
    public void execute() {
        // Strip apikey
        stripApiKey();

        // Fill out some of the basic metrics structure.
        requestMetric.setRequestStart(new Date());
        requestMetric.setUrl(request.getUrl());
        requestMetric.setResource(request.getDestination());
        requestMetric.setMethod(request.getType());
        requestMetric.setApiOrgId(request.getApiOrgId());
        requestMetric.setApiId(request.getApiId());
        requestMetric.setApiVersion(request.getApiVersion());

        context.setAttribute(PolicyContextKeys.REQUEST_METRIC, requestMetric);

        // Create the handler that will be called once the policies are asynchronously
        // loaded (can happen this way due to the plugin framework).
        final IAsyncHandler<List<PolicyWithConfiguration>> policiesLoadedHandler = (List<PolicyWithConfiguration> result) -> {
            policyImpls = result;
            // Set up the policy chain request, call #doApply to execute.
            requestChain = createRequestChain((ApiRequest req) -> {
                IConnectorInterceptor connectorInterceptor = context.getConnectorInterceptor();
                IApiConnector connector;
                if (connectorInterceptor == null) {
                    connector = connectorFactory.createConnector(req, api,
                            RequiredAuthType.parseType(api), hasDataPolicy);
                } else {
                    connector = connectorInterceptor.createConnector();
                }

                // TODO check for a null connector

                // Open up a connection to the back-end if we're given the OK from the request chain
                requestMetric.setApiStart(new Date());
                // Attach the response handler here.
                apiConnection = connector.connect(req, createApiConnectionResponseHandler());

                // Write the body chunks from the *policy request* into the connector request.
                requestChain.bodyHandler(buffer -> {
                    requestMetric.setBytesUploaded(requestMetric.getBytesUploaded() + buffer.length());
                    apiConnection.write(buffer);
                });

                // Indicate end from policy chain request to connector request.
                requestChain.endHandler(onEnd -> apiConnection.end());

                // Once we have returned from connector.request, we know it is safe to start
                // writing chunks without buffering. At this point, it is the responsibility
                // of the implementation as to how they should cope with the chunks.
                handleStream();
            });
            requestChain.policyFailureHandler(policyFailureHandler);
            requestChain.doApply(request);
        };

        // The handler used when we need to parse the inbound request payload into
        // an object and make it available via the policy context.
        final IAsyncResultHandler<Object> payloadParserHandler = new IAsyncResultHandler<Object>() {
            @Override
            public void handle(IAsyncResult<Object> result) {
                if (result.isSuccess()) {
                    final Object payload = result.getResult();
                    // Store the parsed object in the policy context.
                    context.setAttribute(PolicyContextKeys.REQUEST_PAYLOAD, payload);
                    context.setAttribute(PolicyContextKeys.REQUEST_PAYLOAD_IO, payloadIO);

                    // Now replace the inbound stream handler with one that uses the payload IO
                    // object to re-marshall the (possibly modified) payload object to bytes
                    // and sends that (because the *real* inbound stream has already been consumed)
                    streamHandler(new IAsyncHandler<ISignalWriteStream>() {
                        @Override
                        public void handle(ISignalWriteStream connectorStream) {
                            try {
                                if (payload == null) {
                                    connectorStream.end();
                                } else {
                                    payloadIO = context.getAttribute(PolicyContextKeys.REQUEST_PAYLOAD_IO, payloadIO);
                                    byte[] data = payloadIO.marshall(payload);
                                    IApimanBuffer buffer = bufferFactory.createBuffer(data);
                                    connectorStream.write(buffer);
                                    connectorStream.end();
                                }
                            } catch (Exception e) {
                                connectorStream.abort();
                                throw new RuntimeException(e);
                            }
                        }
                    });

                    // Load and executes the policies
                    loadPolicies(policiesLoadedHandler);
                } else {
                    resultHandler.handle(AsyncResultImpl.create(result.getError(), IEngineResult.class));
                }
            }
        };


        // If no API Key provided - the api must be public.  If an API Key *is* provided
        // then we lookup the Contract and use that.
        if (request.getApiKey() == null) {
            registry.getApi(request.getApiOrgId(), request.getApiId(), request.getApiVersion(),
                    (IAsyncResult<Api> apiResult) -> {
                        if (apiResult.isSuccess()) {
                            api = apiResult.getResult();

                            if (api == null) {
                                Exception error = new InvalidApiException(Messages.i18n.format("EngineImpl.ApiNotFound")); //$NON-NLS-1$
                                resultHandler.handle(AsyncResultImpl.create(error, IEngineResult.class));
                            } else if (!api.isPublicAPI()) {
                                Exception error = new InvalidApiException(Messages.i18n.format("EngineImpl.ApiNotPublic")); //$NON-NLS-1$
                                resultHandler.handle(AsyncResultImpl.create(error, IEngineResult.class));
                            } else {
                                resolvePropertyReplacements(api);

                                request.setApi(api);
                                policies = api.getApiPolicies();
                                policyImpls = new ArrayList<>(policies.size());

                                // If the API is configured to be "stateful", we need to parse the
                                // inbound request body into an object appropriate to the type and
                                // format of the API.  This could be a SOAP message, an XML document,
                                // or a JSON document
                                if (api.isParsePayload()) {
                                    parsePayload(payloadParserHandler);
                                } else {
                                    loadPolicies(policiesLoadedHandler);
                                }
                            }
                        } else if (apiResult.isError()) {
                            resultHandler.handle(AsyncResultImpl.create(apiResult.getError(), IEngineResult.class));
                        }
                    });
        } else {
            String apiOrgId = request.getApiOrgId();
            String apiId = request.getApiId();
            String apiVersion = request.getApiVersion();
            String apiKey = request.getApiKey();
            registry.getContract(apiOrgId, apiId, apiVersion, apiKey, (IAsyncResult<ApiContract> contractResult) -> {
                if (contractResult.isSuccess()) {
                    ApiContract apiContract = contractResult.getResult();

                    resolvePropertyReplacements(apiContract);

                    requestMetric.setClientOrgId(apiContract.getClient().getOrganizationId());
                    requestMetric.setClientId(apiContract.getClient().getClientId());
                    requestMetric.setClientVersion(apiContract.getClient().getVersion());
                    requestMetric.setPlanId(apiContract.getPlan());
                    requestMetric.setContractId(request.getApiKey());

                    api = apiContract.getApi();
                    request.setContract(apiContract);
                    request.setApi(api);
                    policies = apiContract.getPolicies();
                    policyImpls = new ArrayList<>(policies.size());
                    if (request.getApiOrgId() != null) {
                        try {
                            validateRequest(request);
                        } catch (InvalidContractException e) {
                            resultHandler.handle(AsyncResultImpl.create(e, IEngineResult.class));
                            return;
                        }
                    }

                    // If the API is configured to be "stateful", we need to parse the
                    // inbound request body into an object appropriate to the type and
                    // format of the API.  This could be a SOAP message, an XML document,
                    // or a JSON document
                    if (api.isParsePayload()) {
                        parsePayload(payloadParserHandler);
                    } else {
                        // Load and executes the policies
                        loadPolicies(policiesLoadedHandler);
                    }
                } else {
                    resultHandler.handle(AsyncResultImpl.create(contractResult.getError(), IEngineResult.class));
                }
            });
        }
    }

    /**
     * Parse the inbound request's body into a payload object.  The object that is
     * produced will depend on the type and content-type of the API.  Options
     * include, but may not be limited to:
     * <ul>
     *   <li>REST+json</li>
     *   <li>REST+xml</li>
     *   <li>SOAP+xml</li>
     * </ul>
     * @param payloadResultHandler
     */
    protected void parsePayload(IAsyncResultHandler<Object> payloadResultHandler) {
        // Strip out any content-length header from the request.  It will very likely
        // no longer be accurate.
        request.getHeaders().remove("Content-Length"); //$NON-NLS-1$

        // Configure the api's max payload buffer size, if it's not already set.
        if (api.getMaxPayloadBufferSize() <= 0) {
            api.setMaxPayloadBufferSize(maxPayloadBufferSize);
        }

        // Now "handle" the inbound request stream, which will cause bytes to be streamed
        // to the writeStream we provide (which will store the bytes in a buffer for parsing)
        final ByteBuffer buffer = new ByteBuffer(2048);
        inboundStreamHandler.handle(new ISignalWriteStream() {
            private boolean done = false;

            @Override
            public void abort() {
                done = true;
                payloadResultHandler.handle(AsyncResultImpl.create(new Exception("Inbound request stream aborted."))); //$NON-NLS-1$
            }

            @Override
            public boolean isFinished() {
                return done;
            }

            @Override
            public void write(IApimanBuffer chunk) {
                if (done) {
                    return;
                }
                if (buffer.length() > api.getMaxPayloadBufferSize()) {
                    payloadResultHandler.handle(AsyncResultImpl.create(new Exception("Max request payload size exceeded."))); //$NON-NLS-1$
                    done = true;
                    return;
                }
                buffer.append(chunk);
            }

            @Override
            public void end() {
                if (done) {
                    return;
                }
                // When end() is called, the stream of bytes is done and we can parse them into
                // an appropriate payload object.
                done = true;
                if (buffer.length() == 0) {
                    payloadResultHandler.handle(AsyncResultImpl.create(null));
                } else {
                    payloadIO = null;
                    if ("soap".equalsIgnoreCase(api.getEndpointType())) { //$NON-NLS-1$
                        payloadIO = new SoapPayloadIO();
                    } else if ("rest".equalsIgnoreCase(api.getEndpointType())) { //$NON-NLS-1$
                        if ("xml".equalsIgnoreCase(api.getEndpointContentType())) { //$NON-NLS-1$
                            payloadIO = new XmlPayloadIO();
                        } else if ("json".equalsIgnoreCase(api.getEndpointContentType())) { //$NON-NLS-1$
                            payloadIO = new JsonPayloadIO();
                        }
                    }
                    if (payloadIO == null) {
                        payloadIO = new BytesPayloadIO();
                    }
                    try {
                        Object payload = payloadIO.unmarshall(buffer.getBytes());
                        payloadResultHandler.handle(AsyncResultImpl.create(payload));
                    } catch (Exception e) {
                        payloadResultHandler.handle(AsyncResultImpl.create(new Exception("Failed to parse inbound request payload.", e))); //$NON-NLS-1$
                    }
                }
            }

            @Override
            public void drainHandler(IAsyncHandler<Void> drainHandler) {
                apiConnection.drainHandler(drainHandler);
            }

            @Override
            public boolean isFull() {
                return apiConnection.isFull();
            }
        });
    }

    /**
     * Strips the API key from the request (both the http headers and the query params).
     */
    private void stripApiKey() {
        request.getHeaders().remove("X-API-Key"); //$NON-NLS-1$
        request.getQueryParams().remove("apikey"); //$NON-NLS-1$
    }

    /**
     * Response API property replacements
     */
    protected void resolvePropertyReplacements(Api api) {
        if (api == null) {
            return;
        }
        String endpoint = api.getEndpoint();
        endpoint = resolveProperties(endpoint);
        api.setEndpoint(endpoint);

        Map<String, String> properties = api.getEndpointProperties();
        for (Entry<String, String> entry : properties.entrySet()) {
            String value = entry.getValue();
            value = resolveProperties(value);
            entry.setValue(value);
        }

        resolvePropertyReplacements(api.getApiPolicies());
    }

    /**
     * Resolve contract property replacements
     */
    protected void resolvePropertyReplacements(ApiContract apiContract) {
        if (apiContract == null) {
            return;
        }
        Api api = apiContract.getApi();
        if (api != null) {
            resolvePropertyReplacements(api);
        }
        resolvePropertyReplacements(apiContract.getPolicies());
    }

    /**
     * Resolve property replacements for list of policies
     */
    private void resolvePropertyReplacements(List<Policy> apiPolicies) {
        if (apiPolicies != null) {
            for (Policy policy : apiPolicies) {
                String config = policy.getPolicyJsonConfig();
                config = resolveProperties(config);
                policy.setPolicyJsonConfig(config);
            }
        }
    }

    /**
     * Resolve a property
     */
    private String resolveProperties(String value) {
        if (value.contains("${")) { //$NON-NLS-1$
            return PROPERTY_SUBSTITUTOR.replace(value);
        } else {
            return value;
        }
    }

    /**
     * Validates that the contract being used for the request is valid against the
     * api information included in the request.  Basically the request includes
     * information indicating which specific api is being invoked.  This method
     * ensures that the api information in the contract matches the requested
     * api.
     * @param request the request to validate
     */
    protected void validateRequest(ApiRequest request) throws InvalidContractException {
        ApiContract contract = request.getContract();

        boolean matches = true;
        if (!contract.getApi().getOrganizationId().equals(request.getApiOrgId())) {
            matches = false;
        }
        if (!contract.getApi().getApiId().equals(request.getApiId())) {
            matches = false;
        }
        if (!contract.getApi().getVersion().equals(request.getApiVersion())) {
            matches = false;
        }
        if (!matches) {
            throw new InvalidContractException(Messages.i18n.format("EngineImpl.InvalidContractForApi", //$NON-NLS-1$
                    request.getApiOrgId(), request.getApiId(), request.getApiVersion()));
        }
    }

    /**
     * Get/resolve the list of policies into a list of policies with config.  This operation is
     * done asynchronously so that plugins can be downloaded if needed.  Any errors in resolving
     * the policies will be reported back via the policyErrorHandler.
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
            policyFactory.loadPolicy(policy.getPolicyImpl(), (IAsyncResult<IPolicy> result) -> {
                if (result.isSuccess()) {
                    IPolicy policyImpl = result.getResult();
                    // Test whether pipeline contains any data policies. Connectors can use this for Content-Length pass-through.
                    if (policyImpl instanceof IDataPolicy) {
                        hasDataPolicy = true;
                    }
                    try {
                        Object policyConfig = policyFactory.loadConfig(policyImpl, policy.getPolicyImpl(), policy.getPolicyJsonConfig());
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
                    if (!errorCounter.isEmpty()) {
                        int errorIdx = errorCounter.iterator().next();
                        Throwable error = errors.get(errorIdx);
                        // TODO add some logging here to indicate which policy error'd out
                        //Policy errorPolicy = policies.get(errorIdx);
                        policyErrorHandler.handle(error);
                    } else {
                        handler.handle(rval);
                    }
                }
            });
        }
    }

    /**
     * Creates a response handler that is called by the api connector once a connection
     * to the back end api has been made and a response received.
     */
    private IAsyncResultHandler<IApiConnectionResponse> createApiConnectionResponseHandler() {
        return (IAsyncResult<IApiConnectionResponse> result) -> {
            if (result.isSuccess()) {
                requestMetric.setApiEnd(new Date());
                // The result came back. NB: still need to put it through the response chain.
                apiConnectionResponse = result.getResult();
                ApiResponse apiResponse = apiConnectionResponse.getHead();
                context.setAttribute("apiman.engine.apiResponse", apiResponse); //$NON-NLS-1$

                // Execute the response chain to evaluate the response.
                responseChain = createResponseChain((ApiResponse response) -> {
                    // Send the api response to the caller.
                    final EngineResultImpl engineResult = new EngineResultImpl(response);
                    engineResult.setConnectorResponseStream(apiConnectionResponse);

                    resultHandler.handle(AsyncResultImpl.create(engineResult));

                    // We've come all the way through the response chain successfully
                    responseChain.bodyHandler(buffer -> {
                        requestMetric.setBytesDownloaded(requestMetric.getBytesDownloaded() + buffer.length());
                        engineResult.write(buffer);
                    });

                    responseChain.endHandler(isEnd -> {
                        engineResult.end();
                        finished = true;
                        metrics.record(requestMetric);
                    });

                    // Signal to the connector that it's safe to start transmitting data.
                    apiConnectionResponse.transmit();
                });

                // Write data from the back-end response into the response chain.
                apiConnectionResponse.bodyHandler(buffer -> responseChain.write(buffer));

                // Indicate back-end response is finished to the response chain.
                apiConnectionResponse.endHandler(isEnd -> responseChain.end());

                responseChain.doApply(apiResponse);
            } else {
                // TODO handle the use case where there is an error!
            }
        };
    }

    /**
     * Called when the api connector is ready to receive data from the inbound
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
                // api connector resources.  We'll also call handle() on the result
                // handler so that the caller knows something went wrong.
                streamFinished = true;
                apiConnection.abort();
                resultHandler.handle(AsyncResultImpl.<IEngineResult>create(new RequestAbortedException()));
            }


            @Override
            public boolean isFinished() {
                return streamFinished;
            }

            @Override
            public void drainHandler(IAsyncHandler<Void> drainHandler) {
                apiConnection.drainHandler(drainHandler);
            }

            @Override
            public boolean isFull() {
                return apiConnection.isFull();
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.IApiRequestExecutor#isFinished()
     */
    @Override
    public boolean isFinished() {
        return finished;
    }

    /**
     * @see io.apiman.gateway.engine.IApiRequestExecutor#streamHandler(io.apiman.gateway.engine.async.IAsyncHandler)
     */
    @Override
    public void streamHandler(IAsyncHandler<ISignalWriteStream> handler) {
        this.inboundStreamHandler = handler;
    }

    /**
     * Creates the chain used to apply policies in order to the api request.
     */
    private Chain<ApiRequest> createRequestChain(IAsyncHandler<ApiRequest> requestHandler) {
        RequestChain chain = new RequestChain(policyImpls, context);
        chain.headHandler(requestHandler);
        chain.policyFailureHandler(policyFailureHandler);
        chain.policyErrorHandler(policyErrorHandler);
        return chain;
    }

    /**
     * Creates the chain used to apply policies in reverse order to the api response.
     */
    private Chain<ApiResponse> createResponseChain(IAsyncHandler<ApiResponse> responseHandler) {
        ResponseChain chain = new ResponseChain(policyImpls, context);
        chain.headHandler(responseHandler);
        chain.policyFailureHandler(result -> {
            apiConnectionResponse.abort();
            policyFailureHandler.handle(result);
        });
        chain.policyErrorHandler(result -> {
            apiConnectionResponse.abort();
            policyErrorHandler.handle(result);
        });
        return chain;
    }

    /**
     * Creates the handler to use when a policy failure occurs during processing
     * of a chain.
     */
    private IAsyncHandler<PolicyFailure> createPolicyFailureHandler() {
        return policyFailure -> {
            // One of the policies has triggered a failure. At this point we should stop processing and
            // send the failure to the client for appropriate handling.
            EngineResultImpl engineResult = new EngineResultImpl(policyFailure);
            resultHandler.handle(AsyncResultImpl.<IEngineResult> create(engineResult));
        };
    }

    /**
     * Creates the handler to use when an error is detected during the processing
     * of a chain.
     */
    private IAsyncHandler<Throwable> createPolicyErrorHandler() {
        return error -> resultHandler.handle(AsyncResultImpl.<IEngineResult> create(error));
    }
}
