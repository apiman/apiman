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

package org.overlord.apiman.dt.api.rest.contract.exceptions;



/**
 * Thrown when trying to get, update, or delete an application that does not exist.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApplicationNotFoundException extends AbstractNotFoundException {

    private static final long serialVersionUID = 2885339519882791087L;

    /**
     * Constructor.
     */
    public ApplicationNotFoundException() {
    }
    
    /**
     * Constructor.
     * @param message
     */
    public ApplicationNotFoundException(String message) {
        super(message);
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.exceptions.AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.APP_NOT_FOUND;
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.exceptions.AbstractRestException#getMoreInfo()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.APP_NOT_FOUND_INFO;
    }

}
