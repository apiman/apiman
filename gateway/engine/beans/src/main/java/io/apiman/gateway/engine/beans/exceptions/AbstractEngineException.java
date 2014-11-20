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
 * Base class for all engine exceptions.
 *
 * @author eric.wittmann@redhat.com
 */
public class AbstractEngineException extends RuntimeException {

    private static final long serialVersionUID = -1802150539023180027L;

    /**
     * Constructor.
     */
    public AbstractEngineException() {
    }
    
    /**
     * Constructor.
     * @param message
     */
    public AbstractEngineException(String message) {
        super(message);
    }
    
    /**
     * Constructor.
     * @param cause
     */
    public AbstractEngineException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     * @param message
     * @param cause
     */
    public AbstractEngineException(String message, Throwable cause) {
        super(message, cause);
    }

}
