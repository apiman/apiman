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
 * Thrown when the user attempts to do or see something that they
 * are not authorized (do not have permission) to.
 *
 * @author eric.wittmann@redhat.com
 */
@ApiResponse(responseCode = "403", description = "Action not authorized (user does not have permission to do this)")
public class NotAuthorizedException extends AbstractUserException {

    private static final long serialVersionUID = 5447085523881661547L;

    /**
     * Constructor.
     * @param message the exception message
     */
    public NotAuthorizedException(String message) {
        super(message);
    }

    /**
     * @see AbstractRestException#getHttpCode()
     */
    @Override
    public int getHttpCode() {
        return ErrorCodes.HTTP_STATUS_CODE_FORBIDDEN;
    }

    /**
     * @see AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return -1;
    }

    /**
     * @see AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return null;
    }

}
