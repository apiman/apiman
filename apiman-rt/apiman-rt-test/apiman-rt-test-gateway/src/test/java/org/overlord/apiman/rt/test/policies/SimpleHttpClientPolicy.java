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
import org.overlord.apiman.rt.engine.components.IHttpClientComponent;
import org.overlord.apiman.rt.engine.components.http.HttpMethod;
import org.overlord.apiman.rt.engine.components.http.IHttpClientRequest;
import org.overlord.apiman.rt.engine.components.http.IHttpClientResponse;
import org.overlord.apiman.rt.engine.io.IReadWriteStream;
import org.overlord.apiman.rt.engine.policy.IPolicy;
import org.overlord.apiman.rt.engine.policy.IPolicyChain;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;

/**
 * A simple policy used to test the http client component.
 *
 * @author eric.wittmann@redhat.com
 */
public class SimpleHttpClientPolicy implements IPolicy {
    
    /**
     * Constructor.
     */
    public SimpleHttpClientPolicy() {
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
        final IHttpClientComponent httpClientComponent = context.getComponent(IHttpClientComponent.class);
        String endpoint = System.getProperty("apiman-rt-test-gateway.endpoints.echo"); //$NON-NLS-1$
        IHttpClientRequest clientRequest = httpClientComponent.request(endpoint, HttpMethod.GET, new IAsyncResultHandler<IHttpClientResponse>() {
            @Override
            public void handle(IAsyncResult<IHttpClientResponse> result) {
                if (result.isError()) {
                    request.getHeaders().put("X-HttpClient-Result", "Error"); //$NON-NLS-1$ //$NON-NLS-2$
                    request.getHeaders().put("X-HttpClient-Error", result.getError().getMessage()); //$NON-NLS-1$
                } else {
                    IHttpClientResponse clientResponse = result.getResult();
                    request.getHeaders().put("X-HttpClient-Result", "Success"); //$NON-NLS-1$ //$NON-NLS-2$
                    request.getHeaders().put("X-HttpClient-Response-Code", String.valueOf(clientResponse.getResponseCode())); //$NON-NLS-1$
                    request.getHeaders().put("X-HttpClient-Response-Message", String.valueOf(clientResponse.getResponseMessage())); //$NON-NLS-1$
                    clientResponse.close();
                }
                chain.doApply(request);
            }
        });
        clientRequest.addHeader("X-Test", getClass().getSimpleName()); //$NON-NLS-1$
        clientRequest.end();
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#apply(org.overlord.apiman.rt.engine.beans.ServiceResponse, org.overlord.apiman.rt.engine.policy.IPolicyContext, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ServiceResponse response, IPolicyContext context, IPolicyChain<ServiceResponse> chain) {
        chain.doApply(response);
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
