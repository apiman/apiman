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

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.overlord.apiman.rt.engine.async.IAsyncHandler;
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
    
    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    /**
     * Constructor.
     */
    public HttpClientComponentImpl() {
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.components.IHttpClientComponent#request(java.lang.String, org.overlord.apiman.rt.engine.components.http.HttpMethod, org.overlord.apiman.rt.engine.async.IAsyncHandler)
     */
    @Override
    public IHttpClientRequest request(String endpoint, HttpMethod method, IAsyncHandler<IHttpClientResponse> handler) {
        HttpRequestBase hrb = null;
        switch (method) {
        case DELETE:
            hrb = new HttpDelete(endpoint);
            break;
        case GET:
            hrb = new HttpGet(endpoint);
            break;
        case HEAD:
            hrb = new HttpHead(endpoint);
            break;
        case OPTIONS:
            hrb = new HttpOptions(endpoint);
            break;
        case POST:
            hrb = new HttpPost(endpoint);
            break;
        case PUT:
            hrb = new HttpPut(endpoint);
            break;
        case TRACE:
            hrb = new HttpTrace(endpoint);
            break;
        }
        
        IHttpClientRequest request = new HttpClientRequestImpl(httpClient, hrb, handler);
        return request;
    }

}
