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
 * Thrown when trying to get, update, or delete a Client that does not exist.
 *
 * @author eric.wittmann@redhat.com
 */
@ApiResponse(responseCode = "404", description = "Client not found", useReturnTypeSchema = true)
public class ClientNotFoundException extends AbstractNotFoundException {

    private static final long serialVersionUID = 2885339519882791087L;

    /**
     * Constructor.
     */
    public ClientNotFoundException() {
    }

    /**
     * Constructor.
     * @param message the message
     */
    public ClientNotFoundException(String message) {
        super(message);
    }

    /**
     * @see AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.CLIENT_NOT_FOUND;
    }

    /**
     * @see AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.CLIENT_NOT_FOUND_INFO;
    }

}
