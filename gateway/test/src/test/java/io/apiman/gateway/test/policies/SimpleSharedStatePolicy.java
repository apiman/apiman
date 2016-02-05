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

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * A simple policy used to test conversations.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls" })
public class SimpleSharedStatePolicy implements IPolicy {
    
    /**
     * Constructor.
     */
    public SimpleSharedStatePolicy() {
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
        final ISharedStateComponent sharedState = context.getComponent(ISharedStateComponent.class);
        final String namespace = "urn:" + getClass().getName();
        final String propName = "test-property";
        sharedState.getProperty(namespace, propName, "", new IAsyncResultHandler<String>() {
            @Override
            public void handle(IAsyncResult<String> result) {
                String propVal = result.getResult();
                String newVal = propVal + "+";
                sharedState.setProperty(namespace, propName, newVal, new IAsyncResultHandler<Void>() {
                    @Override
                    public void handle(IAsyncResult<Void> result) {
                        chain.doApply(request);
                    }
                });
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(final ApiResponse response, IPolicyContext context, Object config,
            final IPolicyChain<ApiResponse> chain) {
        final ISharedStateComponent sharedState = context.getComponent(ISharedStateComponent.class);
        final String namespace = "urn:" + getClass().getName();
        final String propName = "test-property";
        sharedState.getProperty(namespace, propName, "NOT_FOUND", new IAsyncResultHandler<String>() {
            @Override
            public void handle(IAsyncResult<String> result) {
                String propVal = result.getResult();
                response.getHeaders().put("X-Shared-State-Value", propVal);
                chain.doApply(response);
            }
        });
    }

}
