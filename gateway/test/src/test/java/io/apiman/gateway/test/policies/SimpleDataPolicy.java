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
package io.apiman.gateway.test.policies;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policy.IDataPolicy;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.io.UnsupportedEncodingException;

/**
 * A simple policy used for testing data policies.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class SimpleDataPolicy implements IPolicy, IDataPolicy {

    /**
     * Constructor.
     */
    public SimpleDataPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public Object parseConfiguration(String jsonConfiguration) {
        return new Object();
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(final ApiRequest request, final IPolicyContext context, final Object config,
            final IPolicyChain<ApiRequest> chain) {
        chain.doApply(request);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ApiResponse response, IPolicyContext context, Object config,
            IPolicyChain<ApiResponse> chain) {
        chain.doApply(response);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IDataPolicy#getRequestDataHandler(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    public IReadWriteStream<ApiRequest> getRequestDataHandler(final ApiRequest request,
            final IPolicyContext context, final Object policyConfiguration) {
        return new AbstractStream<ApiRequest>() {
            @Override
            public ApiRequest getHead() {
                return request;
            }

            @Override
            protected void handleHead(ApiRequest head) {
            }

            @Override
            public void write(IApimanBuffer chunk) {
                try {
                    String chunkstr = chunk.toString("UTF-8");
                    if (chunkstr.contains("$NAME")) {
                        chunkstr = chunkstr.replaceAll("\\$NAME", "Barry Allen");
                        IBufferFactoryComponent bufferFactory = context.<IBufferFactoryComponent>getComponent(IBufferFactoryComponent.class);

                        super.write(bufferFactory.createBuffer(chunkstr));
                    } else {
                        super.write(chunk);
                    }
                } catch (UnsupportedEncodingException e) {
                    super.write(chunk);
                }
            }
        };
    }

    /**
     * @see io.apiman.gateway.engine.policy.IDataPolicy#getResponseDataHandler(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    public IReadWriteStream<ApiResponse> getResponseDataHandler(ApiResponse response,
            IPolicyContext context, Object policyConfiguration) {
        return null;
    }

}
