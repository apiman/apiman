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
package io.apiman.test.common.resttest;

import java.util.HashMap;
import java.util.Map;

/**
 * Models a single rest test.  This is the java bean that is populated and returned
 * when a *.resttest file is parsed.
 *
 * @author eric.wittmann@redhat.com
 */
public class RestTest {
    
    private String requestMethod;
    private String requestPath;
    private String requestPayload;
    private Map<String, String> requestHeaders = new HashMap<String, String>();
    private String username;
    private String password;
    
    private int expectedStatusCode;
    private Map<String, String> expectedResponseHeaders = new HashMap<String, String>();
    private String expectedResponsePayload;
    
    /**
     * Constructor.
     */
    public RestTest() {
    }

    /**
     * @return the requestMethod
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /**
     * @param requestMethod the requestMethod to set
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * @return the requestPath
     */
    public String getRequestPath() {
        return requestPath;
    }

    /**
     * @param requestPath the requestPath to set
     */
    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    /**
     * @return the requestPayload
     */
    public String getRequestPayload() {
        return requestPayload;
    }

    /**
     * @param requestPayload the requestPayload to set
     */
    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    /**
     * @return the requestHeaders
     */
    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * @param requestHeaders the requestHeaders to set
     */
    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the expectedStatusCode
     */
    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    /**
     * @param expectedStatusCode the expectedStatusCode to set
     */
    public void setExpectedStatusCode(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }

    /**
     * @return the expectedResponseHeaders
     */
    public Map<String, String> getExpectedResponseHeaders() {
        return expectedResponseHeaders;
    }

    /**
     * @param expectedResponseHeaders the expectedResponseHeaders to set
     */
    public void setExpectedResponseHeaders(Map<String, String> expectedResponseHeaders) {
        this.expectedResponseHeaders = expectedResponseHeaders;
    }

    /**
     * @return the expectedResponsePayload
     */
    public String getExpectedResponsePayload() {
        return expectedResponsePayload;
    }

    /**
     * @param expectedResponsePayload the expectedResponsePayload to set
     */
    public void setExpectedResponsePayload(String expectedResponsePayload) {
        this.expectedResponsePayload = expectedResponsePayload;
    }

}
