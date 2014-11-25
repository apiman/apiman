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
 * Thrown when trying to create a Gateway that already exists.
 *
 * @author eric.wittmann@redhat.com
 */
public class GatewayAlreadyExistsException extends AbstractAlreadyExistsException {

    private static final long serialVersionUID = -6073501704605940260L;

    /**
     * Constructor.
     */
    public GatewayAlreadyExistsException() {
    }
    
    /**
     * Constructor.
     * @param message
     */
    public GatewayAlreadyExistsException(String message) {
        super(message);
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.exceptions.AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.GATEWAY_ALREADY_EXISTS;
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.exceptions.AbstractRestException#getMoreInfo()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.GATEWAY_ALREADY_EXISTS_INFO;
    }

}
