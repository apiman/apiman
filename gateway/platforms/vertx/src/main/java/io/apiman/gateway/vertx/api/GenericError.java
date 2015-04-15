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
package io.apiman.gateway.vertx.api;

/**
 * A serializable (with jackson) error, which can be easily transmitted over the bus
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class GenericError extends RuntimeException {

    private static final long serialVersionUID = -3910654049532937590L;
    private int responseCode;
    private String message;
    private Throwable cause;

    /**
     * Empty constructor for Jackson
     */
    public GenericError() {}

    /**
     * Constructor
     * @param responseCode a response code
     * @param message an error message
     * @param cause the exception cause the exception that caused the problem
     */
    public GenericError(int responseCode, String message, Throwable cause) {
        super(message, cause);
        this.cause = cause;
        this.message = message;
        this.responseCode = responseCode;
    }

    /**
     * @return response code
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * @param message error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * @param responseCode set a response code
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * @param cause the exception cause cause of the exception
     */
    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    /**
     * The cause of the exception
     */
    @Override
    public Throwable getCause() {
        return cause;
    }
}
