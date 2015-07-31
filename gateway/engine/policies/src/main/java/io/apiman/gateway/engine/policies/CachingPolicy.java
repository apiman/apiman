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

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.beans.exceptions.ComponentNotFoundException;
import io.apiman.gateway.engine.components.ICacheStoreComponent;
import io.apiman.gateway.engine.impl.CachedResponse;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.io.ISignalReadStream;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.engine.policies.caching.CacheConnectorInterceptor;
import io.apiman.gateway.engine.policies.config.CachingConfig;
import io.apiman.gateway.engine.policy.IConnectorInterceptor;
import io.apiman.gateway.engine.policy.IDataPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.io.IOException;

/**
 * Policy that enables caching for back-end services responses.
 *
 * @author rubenrm1@gmail.com
 */
public class CachingPolicy extends AbstractMappedDataPolicy<CachingConfig> implements IDataPolicy {

    private static final String KEY_SEPARATOR = ":"; //$NON-NLS-1$
    private static final String SHOULD_CACHE_ATTR = CachingPolicy.class.getName() + ".should-cache"; //$NON-NLS-1$
    private static final String CACHE_ID_ATTR = CachingPolicy.class.getName() + ".cache-id"; //$NON-NLS-1$

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
        if (config.getTtl() > 0) {
            // Check to see if there is a cache entry for this request.  If so, we need to
            // short-circuit the connector factory by providing a connector interceptor
            String cacheId = buildCacheID(request);
            context.setAttribute(CACHE_ID_ATTR, cacheId);
            ICacheStoreComponent cache = context.getComponent(ICacheStoreComponent.class);
            try {
                ISignalReadStream<ServiceResponse> cacheEntry = cache.getBinary(cacheId, ServiceResponse.class);
                if (cacheEntry != null) {
                    context.setConnectorInterceptor(new CacheConnectorInterceptor(cacheEntry));
                    context.setAttribute(SHOULD_CACHE_ATTR, Boolean.FALSE);
                }
                chain.doApply(request);
            } catch (IOException e) {
                chain.throwError(e);
            }
        } else {
            context.setAttribute(SHOULD_CACHE_ATTR, Boolean.FALSE);
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
     * @see io.apiman.gateway.engine.policies.AbstractMappedDataPolicy#requestDataHandler(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    protected IReadWriteStream<ServiceRequest> requestDataHandler(ServiceRequest request,
            IPolicyContext context, CachingConfig policyConfiguration) {
        return null;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedDataPolicy#responseDataHandler(io.apiman.gateway.engine.beans.ServiceResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    protected IReadWriteStream<ServiceResponse> responseDataHandler(final ServiceResponse response,
            IPolicyContext context, CachingConfig policyConfiguration) {
        // Possible cache the response for future posterity.
        Boolean shouldCache = context.getAttribute(SHOULD_CACHE_ATTR, Boolean.TRUE);
        if (shouldCache) {
            try {
                String cacheId = context.getAttribute(CACHE_ID_ATTR, null);
                ICacheStoreComponent cache = context.getComponent(ICacheStoreComponent.class);
                final ISignalWriteStream writeStream = cache.putBinary(cacheId, response, policyConfiguration.getTtl());
                return new AbstractStream<ServiceResponse>() {
                    @Override
                    public ServiceResponse getHead() {
                        return response;
                    }
                    @Override
                    protected void handleHead(ServiceResponse head) {
                    }
                    @Override
                    public void write(IApimanBuffer chunk) {
                        writeStream.write(chunk);
                        super.write(chunk);
                    }
                    @Override
                    public void end() {
                        writeStream.end();
                        super.end();
                    }
                };
            } catch (ComponentNotFoundException | IOException e) {
                // TODO log error
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Builds a cached request id composed by the API key followed by the HTTP
     * verb and the destination. In the case where there's no API key the ID
     * will contain ServiceOrgId + ServiceId + Service Version
     */
    private static String buildCacheID(ServiceRequest request) {
        StringBuilder req = new StringBuilder();
        if (request.getContract() != null) {
            req.append(request.getApiKey());
        } else {
            req.append(request.getServiceOrgId()).append(KEY_SEPARATOR).append(request.getServiceId())
                    .append(KEY_SEPARATOR).append(request.getServiceVersion());
        }
        req.append(KEY_SEPARATOR).append(request.getType()).append(KEY_SEPARATOR)
                .append(request.getDestination());
        return req.toString();
    }

}
