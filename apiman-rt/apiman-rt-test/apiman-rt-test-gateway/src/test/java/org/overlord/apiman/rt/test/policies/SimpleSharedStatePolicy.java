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
package org.overlord.apiman.rt.test.policies;

import org.overlord.apiman.rt.engine.async.IAsyncResult;
import org.overlord.apiman.rt.engine.async.IAsyncResultHandler;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.components.ISharedStateComponent;
import org.overlord.apiman.rt.engine.io.IReadWriteStream;
import org.overlord.apiman.rt.engine.policy.IPolicy;
import org.overlord.apiman.rt.engine.policy.IPolicyChain;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;

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
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public Object parseConfiguration(String jsonConfiguration) {
        return new Object();
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#setConfiguration(java.lang.Object)
     */
    @Override
    public void setConfiguration(Object config) {
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#apply(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.policy.IPolicyContext, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(final ServiceRequest request, final IPolicyContext context, final IPolicyChain<ServiceRequest> chain) {
        final ISharedStateComponent sharedState = context.getComponent(ISharedStateComponent.class);
        final String namespace = "urn:" + getClass().getName();
        final String propName = "test-property";
        sharedState.getProperty(namespace, propName, "", new IAsyncResultHandler<String>() {
            @Override
            public void handle(IAsyncResult<String> result) {
                String propVal = result.getResult();
                String newVal = propVal + "+";
                sharedState.setProperty(namespace, propName, newVal, new IAsyncResultHandler<String>() {
                    @Override
                    public void handle(IAsyncResult<String> result) {
                        chain.doApply(request);
                    }
                });
            }
        });
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#apply(org.overlord.apiman.rt.engine.beans.ServiceResponse, org.overlord.apiman.rt.engine.policy.IPolicyContext, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(final ServiceResponse response, final IPolicyContext context, final IPolicyChain<ServiceResponse> chain) {
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

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#getConfiguration()
     */
    @Override
    public Object getConfiguration() {
        return null;
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#getRequestHandler()
     */
    @Override
    public IReadWriteStream<ServiceRequest> getRequestHandler() {
        return null;
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#getResponseHandler()
     */
    @Override
    public IReadWriteStream<ServiceResponse> getResponseHandler() {
        return null;
    }

}
