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

package io.apiman.manager.api.rest.contract.exceptions;

/**
 * Base class for all system exceptions.  A system exception is one that happens
 * because something went wrong on the server.  Examples might include an error
 * connecting to a backend storage system, running out of memory, etc.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractSystemException extends AbstractRestException {

    private static final long serialVersionUID = 2309997591141385467L;

    /**
     * Constructor.
     */
    public AbstractSystemException() {
    }

    /**
     * Constructor.
     * @param message
     */
    public AbstractSystemException(String message) {
        super(message);
    }
    
    /**
     * Constructor.
     * @param cause
     */
    public AbstractSystemException(Throwable cause) {
        super(cause);
    }

}
