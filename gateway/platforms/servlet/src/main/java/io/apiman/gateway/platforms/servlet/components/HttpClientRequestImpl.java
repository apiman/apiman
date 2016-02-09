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
package io.apiman.gateway.platforms.servlet.components;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.gateway.engine.components.http.IHttpClientResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * A synchronous implementation of the http client request ({@link IHttpClientRequest}).
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpClientRequestImpl implements IHttpClientRequest {
    
    private String endpoint;
    private HttpMethod method;
    private Map<String, String> headers = new HashMap<>();
    private IAsyncResultHandler<IHttpClientResponse> handler;
    
    private HttpURLConnection connection;
    private OutputStream outputStream;
    
    private int readTimeoutMs = 15000;
    private int connectTimeoutMs = 10000;

    /**
     * Constructor.
     * @param endpoint the endpoint
     * @param method the method
     * @param handler the result handler
     */
    public HttpClientRequestImpl(String endpoint, HttpMethod method, IAsyncResultHandler<IHttpClientResponse> handler) {
        this.endpoint = endpoint;
        this.method = method;
        this.handler = handler;
    }
    
    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientRequest#setConnectTimeout(int)
     */
    @Override
    public void setConnectTimeout(int timeout) {
        this.connectTimeoutMs = timeout;
    }
    
    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientRequest#setReadTimeout(int)
     */
    @Override
    public void setReadTimeout(int timeout) {
        this.readTimeoutMs = timeout;
    }

    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientRequest#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
    }

    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientRequest#removeHeader(java.lang.String)
     */
    @Override
    public void removeHeader(String headerName) {
        headers.remove(headerName);
    }

    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientRequest#write(byte[])
     */
    @Override
    public void write(byte[] data) {
        if (connection == null) {
            connect();
        }
        try { 
            if (outputStream == null) {
                outputStream = connection.getOutputStream();
            }
            connection.getOutputStream().write(data);
        } catch (IOException e) {
            connection.disconnect();
            handler.handle(AsyncResultImpl.<IHttpClientResponse>create(e));
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientRequest#write(java.lang.String, java.lang.String)
     */
    @Override
    public void write(String data, String charsetName) {
        if (connection == null) {
            connect();
        }
        
        try { connection.getOutputStream().write(data.getBytes(charsetName)); } catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientRequest#end()
     */
    @Override
    public void end() {
        if (connection == null) {
            connect();
        }
        if (outputStream != null) {
            IOUtils.closeQuietly(outputStream);
            outputStream = null;
        }
        IHttpClientResponse clientResponse = new HttpClientResponseImpl(connection);
        handler.handle(AsyncResultImpl.create(clientResponse));
    }
    
    /**
     * Connect to the remote server.
     */
    private void connect() {
        try {
            URL url = new URL(this.endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(this.readTimeoutMs);
            connection.setConnectTimeout(this.connectTimeoutMs);
            connection.setRequestMethod(this.method.name());
            if (method == HttpMethod.POST || method == HttpMethod.PUT) {
                connection.setDoOutput(true);
            } else {
                connection.setDoOutput(false);
            }
            connection.setDoInput(true);
            connection.setUseCaches(false);
            for (String headerName : headers.keySet()) {
                String headerValue = headers.get(headerName);
                connection.setRequestProperty(headerName, headerValue);
            }
            connection.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
