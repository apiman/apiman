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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.overlord.apiman.rt.engine.async.AsyncResultImpl;
import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.components.http.IHttpClientRequest;
import org.overlord.apiman.rt.engine.components.http.IHttpClientResponse;

/**
 * A synchronous implementation of the http client request ({@link IHttpClientRequest}).
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpClientRequestImpl implements IHttpClientRequest {
    
    private CloseableHttpClient httpClient;
    private HttpRequestBase httpRequest;
    private IAsyncHandler<IHttpClientResponse> handler;
    private ByteArrayOutputStream body = new ByteArrayOutputStream();

    /**
     * Constructor.
     * @param httpClient
     * @param httpRequest
     * @param handler
     */
    public HttpClientRequestImpl(CloseableHttpClient httpClient, HttpRequestBase httpRequest, IAsyncHandler<IHttpClientResponse> handler) {
        this.httpClient = httpClient;
        this.httpRequest = httpRequest;
        this.handler = handler;
    }

    /**
     * @see org.overlord.apiman.rt.engine.components.http.IHttpClientRequest#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String headerName, String headerValue) {
        httpRequest.addHeader(headerName, headerValue);
    }

    /**
     * @see org.overlord.apiman.rt.engine.components.http.IHttpClientRequest#removeHeader(java.lang.String)
     */
    @Override
    public void removeHeader(String headerName) {
        httpRequest.removeHeaders(headerName);
    }

    /**
     * @see org.overlord.apiman.rt.engine.components.http.IHttpClientRequest#write(byte[])
     */
    @Override
    public void write(byte[] data) {
        try { body.write(data); } catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * @see org.overlord.apiman.rt.engine.components.http.IHttpClientRequest#write(java.lang.String)
     */
    @Override
    public void write(String data) {
        try { body.write(data.getBytes("UTF-8")); } catch (IOException e) { throw new RuntimeException(e); } //$NON-NLS-1$
    }

    /**
     * @see org.overlord.apiman.rt.engine.components.http.IHttpClientRequest#end()
     */
    @Override
    public void end() {
        if (body.size() > 0) {
            ByteArrayEntity entity = new ByteArrayEntity(body.toByteArray());
            ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(entity);
        }
        try {
            HttpResponse response = httpClient.execute(httpRequest);
            InputStream content = response.getEntity().getContent();
            IHttpClientResponse clientResponse = new HttpClientResponseImpl(response, content);
            handler.handle(AsyncResultImpl.create(clientResponse));
        } catch (ClientProtocolException e) {
            handler.handle(AsyncResultImpl.<IHttpClientResponse>create(e));
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.<IHttpClientResponse>create(e));
        }
    }

}
