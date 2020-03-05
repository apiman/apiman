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

/**
 * Thrown when the user attempts to create an entity with an invalid name.
 *
 * @author eric.wittmann@redhat.com
 */
public class InvalidNameException extends AbstractInvalidInputException {

    private static final long serialVersionUID = 1318922157199658825L;

    /**
     * Constructor.
     *
     * @param message the exception message
     */
    public InvalidNameException(String message) {
        super(message);
    }

    /**
     * @see AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.NAME_INVALID;
    }

    /**
     * @see AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.NAME_INVALID_INFO;
    }

}
