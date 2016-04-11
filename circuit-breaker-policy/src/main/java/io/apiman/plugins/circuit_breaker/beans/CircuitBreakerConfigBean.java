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

package io.apiman.plugins.circuit_breaker.beans;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration object for the JSONP policy.
 *
 * @author Alexandre Kieling {@literal <alex.kieling@gmail.com>}
 */
public class CircuitBreakerConfigBean {

    @JsonProperty
    private Set<String> errorCodes = new HashSet<>();
    @JsonProperty
    private int window;
    @JsonProperty
    private int limit;
    @JsonProperty
    private int reset;
    @JsonProperty
    private int failureCode;

    /**
     * Constructor.
     */
    public CircuitBreakerConfigBean() {
    }

    /**
     * @return the errorCodes
     */
    public Set<String> getErrorCodes() {
        return errorCodes;
    }

    /**
     * @param errorCodes the errorCodes to set
     */
    public void setErrorCodes(Set<String> errorCodes) {
        this.errorCodes = errorCodes;
    }

    /**
     * @return the window
     */
    public int getWindow() {
        return window;
    }

    /**
     * @param window the window within which to keep faults (in seconds)
     */
    public void setWindow(int window) {
        this.window = window;
    }

    /**
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit the limit (# of faults)
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * @return the reset
     */
    public int getReset() {
        return reset;
    }

    /**
     * @param reset the reset time (in seconds)
     */
    public void setReset(int reset) {
        this.reset = reset;
    }

    /**
     * @return the failureCode
     */
    public int getFailureCode() {
        return failureCode;
    }

    /**
     * @param failureCode the failureCode to set
     */
    public void setFailureCode(int failureCode) {
        this.failureCode = failureCode;
    }
}
