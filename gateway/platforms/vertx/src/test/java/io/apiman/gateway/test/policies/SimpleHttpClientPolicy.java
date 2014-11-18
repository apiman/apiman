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
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.gateway.engine.components.http.IHttpClientResponse;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.vertx.integration.java.RestTestBase;

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
     * @see io.apiman.gateway.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public Object parseConfiguration(String jsonConfiguration) {
        return new Object();
    }
    
    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(final ServiceRequest request, final IPolicyContext context, final Object config,
            final IPolicyChain<ServiceRequest> chain) {
        final IHttpClientComponent httpClientComponent = context.getComponent(IHttpClientComponent.class);
        // TODO this won't work with just setting a property because vertx is classloader isolated.
        String endpoint = RestTestBase.getEchoEndpoint(); //$NON-NLS-1$
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
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ServiceResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ServiceResponse response, IPolicyContext context, Object config,
            IPolicyChain<ServiceResponse> chain) {
        chain.doApply(response);
    }

}
