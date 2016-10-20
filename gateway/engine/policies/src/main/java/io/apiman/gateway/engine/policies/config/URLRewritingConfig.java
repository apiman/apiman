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
package io.apiman.gateway.engine.policies.config;

/**
 * Configuration object for the URL re-writing policy.
 */
public class URLRewritingConfig {

    private String fromRegex;
    private String toReplacement;
    private boolean processRequestUrl;
    private boolean processRequestHeaders;
    private boolean processResponseHeaders;
    private boolean processResponseBody;

    /**
     * Constructor.
     */
    public URLRewritingConfig() {
    }

    /**
     * @return the fromRegex
     */
    public String getFromRegex() {
        return fromRegex;
    }

    /**
     * @param fromRegex the fromRegex to set
     */
    public void setFromRegex(String fromRegex) {
        this.fromRegex = fromRegex;
    }

    /**
     * @return the toReplacement
     */
    public String getToReplacement() {
        return toReplacement;
    }

    /**
     * @param toReplacement the toReplacement to set
     */
    public void setToReplacement(String toReplacement) {
        this.toReplacement = toReplacement;
    }

    /**
     * @return the processRequestUrl
     */
    public boolean isProcessRequestUrl() {
        return processRequestUrl;
    }

    /**
     * @param processRequestUrl the processRequestUrl to set
     */
    public void setProcessRequestUrl(boolean processRequestUrl) {
        this.processRequestUrl = processRequestUrl;
    }

    /**
     * @return the processRequestHeaders
     */
    public boolean isProcessRequestHeaders() {
        return processRequestHeaders;
    }

    /**
     * @param processRequestHeaders the processRequestHeaders to set
     */
    public void setProcessRequestHeaders(boolean processRequestHeaders) {
        this.processRequestHeaders = processRequestHeaders;
    }

    /**
     * @return the processResponseHeaders
     */
    public boolean isProcessResponseHeaders() {
        return processResponseHeaders;
    }

    /**
     * @param processResponseHeaders the processResponseHeaders to set
     */
    public void setProcessResponseHeaders(boolean processResponseHeaders) {
        this.processResponseHeaders = processResponseHeaders;
    }

    /**
     * @return the processResponseBody
     */
    public boolean isProcessResponseBody() {
        return processResponseBody;
    }

    /**
     * @param processResponseBody the processResponseBody to set
     */
    public void setProcessResponseBody(boolean processResponseBody) {
        this.processResponseBody = processResponseBody;
    }
}
