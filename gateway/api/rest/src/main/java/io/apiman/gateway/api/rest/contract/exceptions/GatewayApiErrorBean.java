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
package io.apiman.gateway.api.rest.contract.exceptions;

import java.io.Serializable;

/**
 * The error bean sent by the Gateway API when an error occurs.
 *
 * @author eric.wittmann@redhat.com
 */
public class GatewayApiErrorBean implements Serializable {

    private static final long serialVersionUID = -7932481091144872463L;
    
    private String errorType;
    private String message;
    private String stacktrace;

    /**
     * Constructor.
     */
    public GatewayApiErrorBean() {
    }

    /**
     * @return the errorType
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * @param errorType the errorType to set
     */
    public void setErrorType(String errorType) {
        this.errorType = errorType;
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
     * @return the stacktrace
     */
    public String getStacktrace() {
        return stacktrace;
    }

    /**
     * @param stacktrace the stacktrace to set
     */
    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }
}
