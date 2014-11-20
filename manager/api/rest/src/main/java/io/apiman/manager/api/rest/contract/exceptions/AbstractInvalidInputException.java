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
 * Base class for any exception that indicates "invalid input".
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractInvalidInputException extends AbstractUserException {

    private static final long serialVersionUID = -8851909147205592784L;

    /**
     * Constructor.
     */
    public AbstractInvalidInputException() {
    }

    /**
     * Constructor.
     * @param message
     */
    public AbstractInvalidInputException(String message) {
        super(message);
    }
    
    /**
     * Constructor.
     * @param cause
     */
    public AbstractInvalidInputException(Throwable cause) {
        super(cause);
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.exceptions.AbstractRestException#getHttpCode()
     */
    @Override
    public final int getHttpCode() {
        return ErrorCodes.HTTP_STATUS_CODE_INVALID_INPUT;
    }

}
