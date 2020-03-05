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
 * Thrown when an action is performed but an error occurs during processing.
 *
 * @author eric.wittmann@redhat.com
 */
public class ActionException extends AbstractUserException {

    private static final long serialVersionUID = -5626995900681339688L;

    /**
     * Constructor.
     */
    public ActionException() {
    }
    
    /**
     * Constructor.
     * @param message the exception message
     */
    public ActionException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param message the exception message
     * @param cause the exception cause
     */
    public ActionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @see io.apiman.manager.api.rest.exceptions.AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.ACTION_ERROR;
    }
    
    /**
     * @see io.apiman.manager.api.rest.exceptions.AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.ACTION_ERROR_INFO;
    }

    /**
     * @see io.apiman.manager.api.rest.exceptions.AbstractRestException#getHttpCode()
     */
    @Override
    public int getHttpCode() {
        return ErrorCodes.HTTP_STATUS_CODE_SYSTEM_ERROR;
    }

}
