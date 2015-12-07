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
package io.apiman.gateway.engine.beans.exceptions;

/**
 * Exception thrown when there is a problem invoking a back end API.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiInvocationException extends AbstractEngineException {

    private static final long serialVersionUID = 4841489368870035902L;

    /**
     * Constructor.
     * @param message an error message
     */
    public ApiInvocationException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param message an error message
     * @param cause the exception cause the underlying cause
     */
    public ApiInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

}
