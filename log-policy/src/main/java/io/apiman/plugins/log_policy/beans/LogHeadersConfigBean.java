/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.plugins.log_policy.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration bean for the log policy.
 *
 * @author eric.wittmann@redhat.com
 */
public class LogHeadersConfigBean {

    @JsonProperty
    private LogHeadersDirectionType direction;

    /**
     * Whether to log the request/response headers.
     * Defaults to {@code true} for backwards compatibility.
     */
    @JsonProperty
    private boolean logHeaders = true;

    /**
     * Whether to log the response status code.
     */
    @JsonProperty
    private boolean logStatusCode;

    /**
     * Constructor.
     */
    public LogHeadersConfigBean() {
    }

    /**
     * @return the direction
     */
    public LogHeadersDirectionType getDirection() {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(LogHeadersDirectionType direction) {
        this.direction = direction;
    }

    /**
     * @return whether to log the headers
     */
    public boolean isLogHeaders() {
        return logHeaders;
    }

    /**
     * @return whether to log the status code
     */
    public boolean isLogStatusCode() {
        return logStatusCode;
    }
}
