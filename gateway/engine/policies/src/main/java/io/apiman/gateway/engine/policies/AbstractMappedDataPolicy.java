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

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policy.IDataPolicy;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * Base class for data policies that want to use jackson to map their config.
 *
 * @author eric.wittmann@redhat.com
 * @param <C> Policy configuration type
 */
public abstract class AbstractMappedDataPolicy<C> extends AbstractMappedPolicy<C> implements IDataPolicy {

    /**
     * Constructor.
     */
    public AbstractMappedDataPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.IDataPolicy#getRequestDataHandler(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final IReadWriteStream<ApiRequest> getRequestDataHandler(ApiRequest request,
            IPolicyContext context, Object policyConfiguration) {
        return requestDataHandler(request, context, (C) policyConfiguration);
    }

    /**
     * Subclasses must implement this.
     * @param request
     * @param context
     * @param policyConfiguration
     */
    protected abstract IReadWriteStream<ApiRequest> requestDataHandler(ApiRequest request,
            IPolicyContext context, C policyConfiguration);

    /**
     * @see io.apiman.gateway.engine.policy.IDataPolicy#getResponseDataHandler(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final IReadWriteStream<ApiResponse> getResponseDataHandler(ApiResponse response,
            IPolicyContext context, Object policyConfiguration) {
        return responseDataHandler(response, context, (C) policyConfiguration);
    }

    /**
     * Subclasses must implement this.
     * @param response
     * @param context
     * @param policyConfiguration
     */
    protected abstract IReadWriteStream<ApiResponse> responseDataHandler(ApiResponse response,
            IPolicyContext context, C policyConfiguration);

}
