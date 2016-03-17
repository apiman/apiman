/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.test.policies;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Send one of these to the "send" method found in the {@link ApimanPolicyTest} class
 * in order to simulate an HTTP request being sent to the API Gateway.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyTestRequest {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static PolicyTestRequest build(PolicyTestRequestType method, String resource) {
        return new PolicyTestRequest(method, resource);
    }

    private final PolicyTestRequestType method;
    private final String resource;
    private Map<String, String> headers = new HashMap<>();
    private String body;
    private Map<String, Object> contextAttributes = new HashMap<>();

    /**
     * Constructor.
     * @param method
     * @param resource
     */
    public PolicyTestRequest(PolicyTestRequestType method, String resource) {
        this.method = method;
        this.resource = resource;
    }
    
    public PolicyTestRequest contextAttribute(String name, Object value) {
        contextAttributes.put(name, value);
        return this;
    }

    public PolicyTestRequest header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public PolicyTestRequest basicAuth(String username, String password) {
        return header("Authorization", "Basic " + Base64.encodeBase64String( (username + ':' + password).getBytes() )); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public PolicyTestRequest body(String body) {
        this.body = body;
        return this;
    }

    public PolicyTestRequest body(Object entity) {
        try {
            String body = mapper.writerFor(entity.getClass()).writeValueAsString(entity);
            return body(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the method
     */
    public PolicyTestRequestType method() {
        return method;
    }

    /**
     * @return the resource
     */
    public String resource() {
        return resource;
    }

    /**
     * @return the body
     */
    public String body() {
        return body;
    }

    /**
     * @return the headers
     */
    public Map<String, String> headers() {
        return headers;
    }
    
    /**
     * @return the contextAttributes
     */
    public Map<String, Object> contextAttributes() {
        return contextAttributes;
    }

}
