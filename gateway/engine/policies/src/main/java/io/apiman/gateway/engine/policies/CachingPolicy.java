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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.IServiceConnector;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.engine.components.IDataStoreComponent;
import io.apiman.gateway.engine.impl.CachedResponse;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policies.config.CachingConfig;
import io.apiman.gateway.engine.policies.connectors.CachedResponseConnection;
import io.apiman.gateway.engine.policy.IConnectorInterceptor;
import io.apiman.gateway.engine.policy.IDataPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import org.apache.commons.lang.StringUtils;

/**
 * Policy that enables caching for back-end services responses.
 *
 * @author rubenrm1@gmail.com
 */
public class CachingPolicy extends AbstractMappedPolicy<CachingConfig> implements IDataPolicy {
    
    public static final String CACHED_RESPONSE = "apiman.policy.CachedResponse"; //$NON-NLS-1$
    private static final String KEY_SEPARATOR = ":"; //$NON-NLS-1$
    private static final String NAMESPACE = "urn:" + CachingPolicy.class.getName(); //$NON-NLS-1$
    
    /**
     * Constructor.
     */
    public CachingPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.AbstractPolicy#getConfigurationClass()
     */
    @Override
    protected Class<CachingConfig> getConfigurationClass() {
        return CachingConfig.class;
    }
    
    /**
     * If the request is cached an {@link IConnectorInterceptor} is set in order to prevent the back-end connection to be established.
     * Otherwise an empty {@link CachedResponse} will be added to the context, this will be used to cache the response once it has been
     * received from the back-end service
     * 
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(final ServiceRequest request, final IPolicyContext context, final CachingConfig config,
            final IPolicyChain<ServiceRequest> chain) {
        context.setAttribute("CachingPolicy::configuration", config); //$NON-NLS-1$
        IDataStoreComponent dataStore = context.getComponent(IDataStoreComponent.class);
        String requestID = buildRequestID(request);
        if(dataStore.hasProperty(NAMESPACE, requestID)) {
            context.setConnectorInterceptor(getCachedResponseInterceptor(context));
            dataStore.getProperty(NAMESPACE, requestID, null, new IAsyncResultHandler<CachedResponse>() {
                @Override
                public void handle(IAsyncResult<CachedResponse> result) {
                    context.setAttribute(CACHED_RESPONSE, result.getResult());
                    chain.doApply(request);
                }
            });
            
        } else {
            context.setAttribute(CACHED_RESPONSE, new CachedResponse(requestID));
            chain.doApply(request);
        }
    }
    
    /**
     * @see AbstractMappedPolicy#doApply(ServiceResponse, IPolicyContext, Object, IPolicyChain)
     */
    @Override
    protected void doApply(ServiceResponse response, IPolicyContext context, CachingConfig config,
            IPolicyChain<ServiceResponse> chain) {
        chain.doApply(response);
    }
    
    /**
     * Builds a cached request id composed by the API key followed by the HTTP
     * verb and the destination. In the case where there's no API key the ID
     * will contain ServiceOrgId + ServiceId + Service Version
     */
    private String buildRequestID(ServiceRequest request) {
        StringBuilder req = new StringBuilder();
        if (StringUtils.isNotBlank(request.getApiKey())) {
            req.append(request.getApiKey());
        } else {
            req.append(request.getServiceOrgId()).append(KEY_SEPARATOR)
                .append(request.getServiceId()).append(KEY_SEPARATOR)
                .append(request.getServiceVersion());
        }
        req.append(KEY_SEPARATOR)
            .append(request.getType()).append(KEY_SEPARATOR)
            .append(request.getDestination());
        return req.toString();
    }
    
    /**
     * The request will not be altered.
     * 
     * @see IDataPolicy#getRequestDataHandler(ServiceRequest, IPolicyContext)
     */
    @Override
    public IReadWriteStream<ServiceRequest> getRequestDataHandler(final ServiceRequest request,
            IPolicyContext context) {
        return null;
    }
    
    /**
     * The response is received from the back-end service and needs to be written into the {@link CachedResponse}.
     * The headers, response code and message should be written at this point and once the {@link CachedResponse}
     * is set, can be stored into the {@link IDataStoreComponent}
     * 
     * @see IDataPolicy#getResponseDataHandler(ServiceResponse, IPolicyContext)
     */
    @Override
    public IReadWriteStream<ServiceResponse> getResponseDataHandler(final ServiceResponse response,
            final IPolicyContext context) {
        final CachingConfig configuration = context.getAttribute("CachingPolicy::configuration", null); //$NON-NLS-1$
        return new AbstractStream<ServiceResponse>() {
            
            private CachedResponse cachedResponse = context.getAttribute(CACHED_RESPONSE, null);
            
            @Override
            public ServiceResponse getHead() {
                return response;
            }

            @Override
            protected void handleHead(ServiceResponse head) {
            }
            
            @Override
            public void write(IApimanBuffer chunk) {
                super.write(chunk);
                if(context.getConnectorInterceptor() == null && isSuccess()) {
                    cachedResponse.write(chunk);
                }
            }
            
            @Override
            public void end() {
                super.end();
                if(context.getConnectorInterceptor() == null) {
                    if(isSuccess() && !cachedResponse.isWriteFailed()) {
                        cachedResponse.endWrite();
                        cachedResponse.setCode(response.getCode());
                        cachedResponse.setMessage(response.getMessage());
                        cachedResponse.setHeaders(response.getHeaders());
                        IDataStoreComponent dataStore = context.getComponent(IDataStoreComponent.class);
                        dataStore.setProperty(NAMESPACE, cachedResponse.getId(), cachedResponse, configuration.getTtl(), new IAsyncResultHandler<CachedResponse>() {
                
                            @Override
                            public void handle(IAsyncResult<CachedResponse> result) {
                                // Nothing to do with the old value
                            }
                        });
                    }
                }
            }
            
            private boolean isSuccess() {
                return response.getCode() >= 200 && response.getCode() < 300;
            }
        };
    }
    
    private IConnectorInterceptor getCachedResponseInterceptor(final IPolicyContext context) {
        
        return new IConnectorInterceptor() {
            
            @Override
            public IServiceConnector createConnector() {
                return new IServiceConnector() {
                    
                    @Override
                    public IServiceConnection connect(ServiceRequest request,
                            IAsyncResultHandler<IServiceConnectionResponse> handler) throws ConnectorException {
                        final CachedResponseConnection cachedConnectionResponse = new CachedResponseConnection(context);
                        handler.handle(AsyncResultImpl.<IServiceConnectionResponse>create(cachedConnectionResponse));
                        return cachedConnectionResponse;
                    }
                    
                };
            }

        };
    }

}
