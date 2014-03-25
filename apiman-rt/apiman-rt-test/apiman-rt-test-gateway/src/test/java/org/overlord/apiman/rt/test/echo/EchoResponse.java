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
package org.overlord.apiman.rt.test.echo;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple echo response POJO.
 *
 * @author eric.wittmann@redhat.com
 */
public class EchoResponse {

    /**
     * Create an echo response from the inbound information in the http server
     * exchange.
     * @param exchange
     * @return a new echo response
     */
    public static EchoResponse from(HttpServerExchange exchange) {
        EchoResponse response = new EchoResponse();
        response.setMethod(exchange.getRequestMethod().toString());
        response.setResource(exchange.getRequestPath());
        response.setLength(exchange.getRequestContentLength());
        response.setUri(exchange.getRequestURI());
        for (HeaderValues headerValues : exchange.getRequestHeaders()) {
            String name = headerValues.getHeaderName().toString();
            String value = headerValues.getFirst();
            response.getHeaders().put(name, value);
        }
        return response;
    }

    private String method;
    private String resource;
    private long length;
    private String uri;
    private Map<String, String> headers = new HashMap<String, String>();
    
    /**
     * Constructor.
     */
    public EchoResponse() {
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return the resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * @return the headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @param headers the headers to set
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * @return the length
     */
    public long getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
