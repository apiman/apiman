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
package io.apiman.test.common.mock;

import io.apiman.gateway.engine.beans.util.HeaderMap;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple echo response POJO.
 *
 * @author eric.wittmann@redhat.com
 */
@XmlRootElement
public class EchoResponse {

    private String method;
    private String resource;
    private String uri;
    private HeaderMap headers = new HeaderMap();
    private Long bodyLength;
    private String bodySha1;
    private Long counter;

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
    public HeaderMap getHeaders() {
        return headers;
    }

    /**
     * @param headers the headers to set
     */
    public void setHeaders(HeaderMap headers) {
        this.headers = headers;
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

    /**
     * @return the bodyLength
     */
    public Long getBodyLength() {
        return bodyLength;
    }

    /**
     * @param bodyLength the bodyLength to set
     */
    public void setBodyLength(Long bodyLength) {
        this.bodyLength = bodyLength;
    }

    /**
     * @return the bodySha1
     */
    public String getBodySha1() {
        return bodySha1;
    }

    /**
     * @param bodySha1 the bodySha1 to set
     */
    public void setBodySha1(String bodySha1) {
        this.bodySha1 = bodySha1;
    }

    public Long getCounter() {
        return counter;
    }

    public void setCounter(Long counter) {
        this.counter = counter;
    }

}
