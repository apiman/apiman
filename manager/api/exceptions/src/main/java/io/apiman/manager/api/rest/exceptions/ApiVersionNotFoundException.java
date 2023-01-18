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


import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Thrown when trying to get, update, or delete an API version that does not
 * exist.
 *
 * @author eric.wittmann@redhat.com
 */
@Schema(description = "Error when trying to get, update, or delete an API version that does not exist")
public class ApiVersionNotFoundException extends AbstractNotFoundException {

    private static final long serialVersionUID = -7120965195175475680L;

    /**
     * Constructor.
     */
    public ApiVersionNotFoundException() {
    }

    /**
     * Constructor.
     * @param message the exception message
     */
    public ApiVersionNotFoundException(String message) {
        super(message);
    }

    /**
     * @see AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.API_VERSION_NOT_FOUND;
    }

    /**
     * @see AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.API_VERSION_NOT_FOUND_INFO;
    }

}
