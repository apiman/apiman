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
package org.overlord.apiman.rt.gateway.servlet.components;

import org.overlord.apiman.rt.engine.async.IAsyncResultHandler;
import org.overlord.apiman.rt.engine.components.IHttpClientComponent;
import org.overlord.apiman.rt.engine.components.http.HttpMethod;
import org.overlord.apiman.rt.engine.components.http.IHttpClientRequest;
import org.overlord.apiman.rt.engine.components.http.IHttpClientResponse;

/**
 * A simple, synchronous implementation of {@link IHttpClientComponent}.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpClientComponentImpl implements IHttpClientComponent {
    
    /**
     * Constructor.
     */
    public HttpClientComponentImpl() {
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.components.IHttpClientComponent#request(java.lang.String, org.overlord.apiman.rt.engine.components.http.HttpMethod, org.overlord.apiman.rt.engine.async.IAsyncResultHandler)
     */
    @Override
    public IHttpClientRequest request(String endpoint, HttpMethod method, IAsyncResultHandler<IHttpClientResponse> handler) {
        return new HttpClientRequestImpl(endpoint, method, handler);
    }

}
