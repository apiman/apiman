/*
 * Copyright 2015 JBoss Inc
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

import io.apiman.gateway.engine.DependsOnComponents;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policies.config.URLRewritingConfig;
import io.apiman.gateway.engine.policies.rewrite.URLRewritingStream;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.util.Map.Entry;

/**
 * A policy that implements URL rewriting in the body and headers of the
 * response from a back-end API.
 *
 * @author eric.wittmann@redhat.com
 */
@DependsOnComponents(IBufferFactoryComponent.class)
public class URLRewritingPolicy extends AbstractMappedDataPolicy<URLRewritingConfig> {

    /**
     * Constructor.
     */
    public URLRewritingPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<URLRewritingConfig> getConfigurationClass() {
        return URLRewritingConfig.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, URLRewritingConfig config,
            IPolicyChain<ApiRequest> chain) {
        if (config.isProcessRequestUrl()) {
            if (request.getDestination() != null) {
                request.setDestination(request.getDestination().replaceAll(config.getFromRegex(), config.getToReplacement()));
            }
        }
        if (config.isProcessRequestHeaders()) {
            replaceHeaders(config, request.getHeaders());
        }
        super.doApply(request, context, config, chain);
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiResponse response, IPolicyContext context, URLRewritingConfig config,
            IPolicyChain<ApiResponse> chain) {
        final HeaderMap headers = response.getHeaders();
        if (config.isProcessResponseHeaders()) {
            replaceHeaders(config, headers);
        }
        if (config.isProcessResponseBody() && headers.containsKey("Content-Length")) { //$NON-NLS-1$
            headers.remove("Content-Length"); //$NON-NLS-1$
        }
        super.doApply(response, context, config, chain);
    }

    /**
     * Perform replacement.
     *
     * @param config
     * @param headers
     */
    private void replaceHeaders(URLRewritingConfig config, HeaderMap headers) {
        for (Entry<String, String> entry : headers) {
            String key = entry.getKey();
            String value = entry.getValue();
            value = doHeaderReplaceAll(value, config.getFromRegex(), config.getToReplacement());
            if (value != null) {
                headers.put(key, value);
            }
        }
    }

    /**
     * Finds all matching instances of the regular expression and replaces them with
     * the replacement value.
     * @param headerValue
     * @param fromRegex
     * @param toReplacement
     */
    private String doHeaderReplaceAll(String headerValue, String fromRegex, String toReplacement) {
        return headerValue.replaceAll(fromRegex, toReplacement);
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedDataPolicy#requestDataHandler(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    protected IReadWriteStream<ApiRequest> requestDataHandler(ApiRequest request,
            IPolicyContext context, URLRewritingConfig policyConfiguration) {
        // No need to process the inbound stream.
        return null;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedDataPolicy#responseDataHandler(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    protected IReadWriteStream<ApiResponse> responseDataHandler(ApiResponse response,
            IPolicyContext context, URLRewritingConfig policyConfiguration) {
        if (policyConfiguration.isProcessResponseBody()) {
            return new URLRewritingStream(context.getComponent(IBufferFactoryComponent.class), response,
                    policyConfiguration.getFromRegex(), policyConfiguration.getToReplacement());
        } else {
            return null;
        }
    }

}
