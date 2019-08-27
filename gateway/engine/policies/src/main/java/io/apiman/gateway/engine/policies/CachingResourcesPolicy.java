package io.apiman.gateway.engine.policies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.exceptions.ComponentNotFoundException;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.components.ICacheStoreComponent;
import io.apiman.gateway.engine.impl.CachedResponse;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.io.ISignalReadStream;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.engine.policies.caching.CacheConnectorInterceptor;
import io.apiman.gateway.engine.policies.config.CachingResourcesConfig;
import io.apiman.gateway.engine.policies.config.CachingResourcesSettingsEntry;
import io.apiman.gateway.engine.io.IPayloadIO;
import io.apiman.gateway.engine.policy.*;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Policy that enables caching for back-end APIs responses.
 *
 * @author benjaminkihm@scheer-group.com
 */
public class CachingResourcesPolicy extends AbstractMappedDataPolicy<CachingResourcesConfig> implements IDataPolicy {

    private static final String KEY_SEPARATOR = ":"; //$NON-NLS-1$
    private static final String SHOULD_CACHE_ATTR = CachingResourcesPolicy.class.getName() + ".should-cache"; //$NON-NLS-1$
    private static final String CACHE_ID_ATTR = CachingResourcesPolicy.class.getName() + ".cache-id"; //$NON-NLS-1$
    private static final String CACHED_RESPONSE = CachingResourcesPolicy.class.getName() + ".cached-response"; //$NON-NLS-1$

    private static final String CACHE_POSSIBLE_MATCHING_ENTRIES = CachingResourcesPolicy.class.getName() + ".possible-matching-entries";

    /**
     * Constructor.
     */
    public CachingResourcesPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<CachingResourcesConfig> getConfigurationClass() {
        return CachingResourcesConfig.class;
    }

    /**
     * If the request is cached an {@link IConnectorInterceptor} is set in order to prevent the back-end connection to be established.
     * Otherwise an empty {@link CachedResponse} will be added to the context, this will be used to cache the response once it has been
     * received from the back-end API
     *
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(final ApiRequest request, final IPolicyContext context, final CachingResourcesConfig config,
                           final IPolicyChain<ApiRequest> chain) {

        List<CachingResourcesSettingsEntry> possibleMatchingEntries = new ArrayList<CachingResourcesSettingsEntry>();
        if(config.getTtl() > 0) {
            for(CachingResourcesSettingsEntry entry : config.getCachingResourcesSettingsEntries()) {
                //check if caching policy allows wildcards for http method or path pattern or check if the corresponding policy entry matches the request http method or path pattern.
                if (matchesHttpMethod(entry.getHttpMethod(), request.getType()) && matchesPolicyEntryVsActualValue(entry.getPathPattern(), request.getDestination())) {
                    possibleMatchingEntries.add(entry);
                }
            }
            context.setAttribute(CACHE_POSSIBLE_MATCHING_ENTRIES, possibleMatchingEntries);
        }

        if (possibleMatchingEntries.size() > 0) {
            // Check to see if there is a cache entry for this request.
            // If so, we deliver the cached result by CacheConnectorInterceptor
            String cacheId = buildCacheID(request, context);
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
                                    markCacheEntryAsCached(cacheEntry, config);
                                    context.setConnectorInterceptor(new CacheConnectorInterceptor(cacheEntry));
                                    context.setAttribute(SHOULD_CACHE_ATTR, Boolean.FALSE);
                                    context.setAttribute(CACHED_RESPONSE, cacheEntry.getHead());
                                } else {
                                    context.setAttribute(SHOULD_CACHE_ATTR, Boolean.TRUE);
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
     * Set or overwrite Cache-Control header with ttl as max-age to mark the response as cached
     * @param cacheEntry
     * @param config
     */
    private void markCacheEntryAsCached(ISignalReadStream<ApiResponse> cacheEntry, final CachingResourcesConfig config) {
        if(cacheEntry.getHead() != null && cacheEntry.getHead() instanceof ApiResponse && cacheEntry.getHead().getHeaders() != null) {
            HeaderMap responseHeaders = cacheEntry.getHead().getHeaders();
            responseHeaders.put("Cache-Control", "max-age=" + String.valueOf(config.getTtl()));
        }
    }

    /**
     * Verify policy entry against request value and ensure that only cachable http methods are cached.
     * @param policyEntry
     * @param requestValue
     * @return
     */
    private boolean matchesHttpMethod(String policyEntry, String requestValue) {
        if(policyEntry.equals(CachingResourcesSettingsEntry.MATCH_ALL)) {
            //check cachable http methods (see  https://developer.mozilla.org/en-US/docs/Glossary/cacheable)
            return requestValue.equalsIgnoreCase("GET") || requestValue.equalsIgnoreCase("POST") || requestValue.equalsIgnoreCase("HEAD");
        }
        return requestValue.equalsIgnoreCase(policyEntry);
    }

    /**
     * Verify policy entry against request value with wildcard check
     * @param policyEntry
     * @param requestvalue
     * @return
     */
    private boolean matchesPolicyEntryVsActualValue(String policyEntry, String requestvalue) {
        return policyEntry.equals(CachingResourcesSettingsEntry.MATCH_ALL) || requestvalue.matches(policyEntry);
    }

    /**
     * @see AbstractMappedPolicy#doApply(ApiResponse, IPolicyContext, Object, IPolicyChain)
     */
    @Override
    protected void doApply(ApiResponse response, IPolicyContext context, CachingResourcesConfig config,
                           IPolicyChain<ApiResponse> chain) {
        chain.doApply(response);
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedDataPolicy#requestDataHandler(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    protected IReadWriteStream<ApiRequest> requestDataHandler(ApiRequest request,
                                                              IPolicyContext context, CachingResourcesConfig policyConfiguration) {
        // No need to handle the request stream (e.g. POST body)
        return null;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedDataPolicy#responseDataHandler(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    protected IReadWriteStream<ApiResponse> responseDataHandler(final ApiResponse response,
                                                                IPolicyContext context, CachingResourcesConfig policyConfiguration) {
        if (response == null) {
            // if the response is empty because of a policy failure before we end here and return null
            return null;
        }

        List<CachingResourcesSettingsEntry> possibleMatchingCachingEntries = context.getAttribute(CACHE_POSSIBLE_MATCHING_ENTRIES, new ArrayList<CachingResourcesSettingsEntry>());
        boolean isAMatch = false;
        for (CachingResourcesSettingsEntry entry : possibleMatchingCachingEntries) {
            isAMatch = isAMatch || matchesPolicyEntryVsActualValue(entry.getStatusCode(), String.valueOf(response.getCode()));
        }
        // Possibly cache the response for future posterity.
        final boolean shouldCache = context.getAttribute(SHOULD_CACHE_ATTR, Boolean.FALSE) && isAMatch;

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
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Builds a cached request id composed by the API key followed by the HTTP
     * verb and the destination. In the case where there's no API key the ID
     * will contain ApiOrgId + ApiId + ApiVersion
     */
    private static String buildCacheID(ApiRequest request, IPolicyContext context) {
        StringBuilder cacheId = new StringBuilder();
        if (request.getContract() != null) {
            cacheId.append(request.getApiKey());
        } else {
            cacheId.append(request.getApiOrgId()).append(KEY_SEPARATOR).append(request.getApiId())
                    .append(KEY_SEPARATOR).append(request.getApiVersion());
        }
        cacheId.append(KEY_SEPARATOR).append(request.getType()).append(KEY_SEPARATOR)
                .append(request.getDestination());

        // According to RFC7234 (https://tools.ietf.org/html/rfc7234#section-2),
        // 'The primary cache key consists of the request method and target URI.'
        if (!request.getQueryParams().isEmpty()) {
            cacheId.append("?").append(request.getQueryParams().toQueryString());
        }

        //use hashed payload to ensure right caching of request with different bodies.
        IPayloadIO requestPayloadIO = context.getAttribute(PolicyContextKeys.REQUEST_PAYLOAD_IO, null);
        Object requestPayload = context.getAttribute(PolicyContextKeys.REQUEST_PAYLOAD, null);
        if (requestPayloadIO != null && requestPayload != null) {
            try {
                byte[] payloadBytes = requestPayloadIO.marshall(requestPayload);
                cacheId.append('_').append(DigestUtils.sha256Hex(payloadBytes));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return cacheId.toString();
    }
}
