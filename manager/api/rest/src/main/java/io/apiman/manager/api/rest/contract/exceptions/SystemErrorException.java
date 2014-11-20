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
 * Thrown when something unexpected happens.
 *
 * @author eric.wittmann@redhat.com
 */
public class SystemErrorException extends AbstractSystemException {
    
    private static final long serialVersionUID = 5590264580639703192L;
    
    /**
     * Constructor.
     */
    public SystemErrorException() {
    }
    
    /**
     * Constructor.
     * @param message
     */
    public SystemErrorException(String message) {
        super(message);
    }
    
    /**
     * Constructor.
     * @param t
     */
    public SystemErrorException(Throwable t) {
        super(t);
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.exceptions.AbstractRestException#getHttpCode()
     */
    @Override
    public int getHttpCode() {
        return ErrorCodes.HTTP_STATUS_CODE_SYSTEM_ERROR;
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.exceptions.AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return -1;
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.exceptions.AbstractRestException#getMoreInfo()
     */
    @Override
    public String getMoreInfoUrl() {
        return null;
    }

}
