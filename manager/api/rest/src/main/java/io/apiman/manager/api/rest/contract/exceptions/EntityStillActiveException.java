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
 * Thrown when attempting to delete an org, but it still has active sub-elements
 * (such as still published APIs or Client Apps).
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class EntityStillActiveException extends AbstractUserException {

    private static final long serialVersionUID = -6377298317341796900L;

    /**
     * Constructor.
     */
    public EntityStillActiveException() {
    }

    /**
     * Constructor.
     * @param message the exception message
     */
    public EntityStillActiveException(String message) {
        super(message);
    }

    /**
     * @see io.apiman.manager.api.rest.contract.exceptions.AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.ENTITY_STILL_ACTIVE_ERROR;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.exceptions.AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.ENTITY_STILL_ACTIVE_ERROR_INFO;
    }

    @Override
    public int getHttpCode() {
        return ErrorCodes.HTTP_STATUS_CODE_INVALID_STATE;
    }
}
