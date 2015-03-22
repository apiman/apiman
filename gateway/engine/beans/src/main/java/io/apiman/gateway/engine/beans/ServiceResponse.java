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
import java.util.HashMap;
import java.util.Map;

/**
 * The response sent back to a caller when a managed service is
 * invoked.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServiceResponse implements IServiceObjectBean, Serializable {

    private static final long serialVersionUID = -7245095046846226241L;
    
    private int code;
    private String message;
    private Map<String, String> headers = new HeaderHashMap();
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * Constructor.
     */
    public ServiceResponse() {
    }

    /**
     * @see io.apiman.gateway.engine.beans.IServiceObjectBean#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @see io.apiman.gateway.engine.beans.IServiceObjectBean#setHeaders(java.util.Map)
     */
    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    /**  
     * @param name Name of attribute
     * @param value Value of attribute
     */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }
    
    /**
     * @param name Name of attribute
     * @return Attribute if present; else null.
     */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }
}
