/*
 * Copyright 2020 Scheer PAS Schweiz AG
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
 * Thrown when trying to get, update, or delete a developer that does not exist.
 */
public class DeveloperNotFoundException extends AbstractNotFoundException {
    private static final long serialVersionUID = -1864829072317282670L;

    /**
     * Constructor
     */
    public DeveloperNotFoundException() {
    }

    /**
     * Constructor
     *
     * @param message the exception message
     */
    public DeveloperNotFoundException(String message) {
        super(message);
    }

    /**
     * @return the errorCode
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.DEVELOPER_NOT_FOUND;
    }

    /**
     * @return the moreInfo
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.DEVELOPER_NOT_FOUND_INFO;
    }
}
