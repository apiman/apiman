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

import static java.util.Optional.ofNullable;

import java.io.IOException;

import org.apache.commons.codec.digest.Md5Crypt;

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
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

/**
 * Policy that enables caching for back-end APIs responses.
 *
 * @author rubenrm1@gmail.com
 */
public class CachingPolicy extends AbstractMappedDataPolicy<CachingConfig> implements IDataPolicy {

    private static final String KEY_SEPARATOR = ":"; //$NON-NLS-1$
    private static final String SHOULD_CACHE_ATTR = CachingPolicy.class.getName() + ".should-cache"; //$NON-NLS-1$
    private static final String CACHE_ID_ATTR = CachingPolicy.class.getName() + ".cache-id"; //$NON-NLS-1$
    private static final String CACHED_RESPONSE = CachingPolicy.class.getName() + ".cached-response"; //$NON-NLS-1$

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
     * received from the back-end API
     *
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(final ApiRequest request, final IPolicyContext context, final CachingConfig config,
            final IPolicyChain<ApiRequest> chain) {
        if (config.getTtl() > 0) {
            // Check to see if there is a cache entry for this request.  If so, we need to
            // short-circuit the connector factory by providing a connector interceptor
            String cacheId = buildCacheID(request);
            context.setAttribute(CACHE_ID_ATTR, cacheId);
            ICacheStoreComponent cache = context.getComponent(ICacheStoreComponent.class);
            cache.getBinary(cacheId, ApiResponse.class,
                    new IAsyncResultHandler<ISignalReadStream<ApiResponse>>() {
                        @Override
                        public void handle(IAsyncResult<ISignalReadStream<ApiResponse>> result) {
                            if (result.isError()) {
                                chain.throwError(result.getError());
                            } else {
                                ISignalReadStream<ApiResponse> cacheEntry = result.getResult();
                                if (cacheEntry != null) {
                                    context.setConnectorInterceptor(new CacheConnectorInterceptor(cacheEntry));
                                    context.setAttribute(SHOULD_CACHE_ATTR, Boolean.FALSE);
                                    context.setAttribute(CACHED_RESPONSE, cacheEntry.getHead());
                                }
                                chain.doApply(request);
                            }
                        }
                    });
        } else {
            context.setAttribute(SHOULD_CACHE_ATTR, Boolean.FALSE);
            chain.doApply(request);
        }
    }

    /**
     * @see AbstractMappedPolicy#doApply(ApiResponse, IPolicyContext, Object, IPolicyChain)
     */
    @Override
    protected void doApply(ApiResponse response, IPolicyContext context, CachingConfig config,
            IPolicyChain<ApiResponse> chain) {
        chain.doApply(response);
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedDataPolicy#requestDataHandler(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    protected IReadWriteStream<ApiRequest> requestDataHandler(ApiRequest request,
            IPolicyContext context, CachingConfig policyConfiguration) {
        // No need to handle the request stream (e.g. POST body)
        return null;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedDataPolicy#responseDataHandler(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    protected IReadWriteStream<ApiResponse> responseDataHandler(final ApiResponse response,
            IPolicyContext context, CachingConfig policyConfiguration) {

        // Possibly cache the response for future posterity.
        // Check the response code against list in config (empty/null list means cache all).
        final boolean shouldCache = (context.getAttribute(SHOULD_CACHE_ATTR, Boolean.TRUE) &&
                ofNullable(policyConfiguration.getStatusCodes())
                    .map(statusCodes -> statusCodes.isEmpty() || statusCodes.contains(String.valueOf(response.getCode())))
                    .orElse(true));

        if (shouldCache) {
            try {
                String cacheId = context.getAttribute(CACHE_ID_ATTR, null);
                ICacheStoreComponent cache = context.getComponent(ICacheStoreComponent.class);
                final ISignalWriteStream writeStream = cache.putBinary(cacheId, response, policyConfiguration.getTtl());
                return new AbstractStream<ApiResponse>() {
                    @Override
                    public ApiResponse getHead() {
                        return response;
                    }
                    @Override
                    protected void handleHead(ApiResponse head) {
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

<<<<<<< HEAD
	/**
	 * Builds a cached request id composed by the API key followed by the HTTP verb
	 * and the destination. In the case where there's no API key the ID will contain
	 * ApiOrgId + ApiId + ApiVersion + md5 of Query Parameters
	 */
	private static String buildCacheID(ApiRequest request) {
		StringBuilder req = new StringBuilder();
		if (request.getContract() != null) {
			req.append(request.getApiKey());
		} else {
			req.append(request.getApiOrgId()).append(KEY_SEPARATOR).append(request.getApiId()).append(KEY_SEPARATOR)
					.append(request.getApiVersion());
		}
		req.append(KEY_SEPARATOR).append(request.getType()).append(KEY_SEPARATOR).append(request.getDestination());

		if (!request.getQueryParams().isEmpty()) {
			req.append(KEY_SEPARATOR)
					.append(Md5Crypt.apr1Crypt(request.getQueryParams().toQueryString(), CACHE_ID_ATTR));
		}

		return req.toString();
	}
=======
    /**
     * Builds a cached request id composed by the API key followed by the HTTP
     * verb and the destination. In the case where there's no API key the ID
     * will contain ApiOrgId + ApiId + ApiVersion + md5 of Query Parameters if 
     * request have query params
     */
    private static String buildCacheID(ApiRequest request) {
        StringBuilder req = new StringBuilder();
        if (request.getContract() != null) {
            req.append(request.getApiKey());
        } else {
            req.append(request.getApiOrgId()).append(KEY_SEPARATOR).append(request.getApiId())
                    .append(KEY_SEPARATOR).append(request.getApiVersion());
        }
        req.append(KEY_SEPARATOR).append(request.getType()).append(KEY_SEPARATOR)
                .append(request.getDestination());
		if (!request.getQueryParams().isEmpty()) {
			req.append(KEY_SEPARATOR)
					.append(Md5Crypt.apr1Crypt(request.getQueryParams().toQueryString(), CACHE_ID_ATTR));
		}	
        return req.toString();
    }
>>>>>>> 0ef0b4fff1c7125308ce19ea6592106b934c942e

}
