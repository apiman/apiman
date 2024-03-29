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

package io.apiman.manager.api.rest.exceptions;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Base class for "not found" exceptions.
 *
 * @author eric.wittmann@redhat.com
 */
@ApiResponse(responseCode = "404", description = "Resource was not found")
public abstract class AbstractNotFoundException extends AbstractUserException {

    private static final long serialVersionUID = -196398343525920762L;

    /**
     * Constructor.
     */
    public AbstractNotFoundException() {
    }

    /**
     * Constructor.
     * @param message the exception message
     */
    public AbstractNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param cause the exception cause
     */
    public AbstractNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     * @param message the exception message
     * @param cause the exception cause
     */
    public AbstractNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @see AbstractRestException#getHttpCode()
     */
    @Override
    public final int getHttpCode() {
        return ErrorCodes.HTTP_STATUS_CODE_NOT_FOUND;
    }

}
