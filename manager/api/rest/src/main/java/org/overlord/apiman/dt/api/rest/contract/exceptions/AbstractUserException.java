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

package org.overlord.apiman.dt.api.rest.contract.exceptions;

/**
 * Base class for all user exceptions.  A user exception happens when the user
 * does something that is problematic, such as try to create an Organization
 * that already exists.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractUserException extends AbstractRestException {

    private static final long serialVersionUID = 8254519224298006332L;

    /**
     * Constructor.
     */
    public AbstractUserException() {
    }

    /**
     * Constructor.
     * @param message
     */
    public AbstractUserException(String message) {
        super(message);
    }
    
    /**
     * Constructor.
     * @param cause
     */
    public AbstractUserException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     * @param message
     * @param cause
     */
    public AbstractUserException(String message, Throwable cause) {
        super(message, cause);
    }

}
