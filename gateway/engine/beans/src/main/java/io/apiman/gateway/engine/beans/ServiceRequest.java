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
package io.apiman.gateway.engine.beans;

import java.io.Serializable;
import java.util.Map;

/**
 * An inbound request for a managed service.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServiceRequest implements Serializable {
    
    private static final long serialVersionUID = 8024669261165845962L;

    private String apiKey;
    private transient ServiceContract contract;
    private String type;
    private String destination;
    private Map<String, String> headers = new HeaderHashMap();
    private String remoteAddr;
    private Object rawRequest;

    /**
     * Constructor.
     */
    public ServiceRequest() {
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return the rawRequest
     */
    public Object getRawRequest() {
        return rawRequest;
    }

    /**
     * @param rawRequest the rawRequest to set
     */
    public void setRawRequest(Object rawRequest) {
        this.rawRequest = rawRequest;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
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
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @return the remoteAddr
     */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * @param remoteAddr the remoteAddr to set
     */
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    /**
     * @return the contract
     */
    public ServiceContract getContract() {
        return contract;
    }

    /**
     * @param contract the contract to set
     */
    public void setContract(ServiceContract contract) {
        this.contract = contract;
    }
}
