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

import org.overlord.apiman.dt.api.rest.i18n.Messages;


/**
 * Thrown when trying to create a Role that already exists.
 *
 * @author eric.wittmann@redhat.com
 */
public class RoleAlreadyExistsException extends AbstractAlreadyExistsException {

    private static final long serialVersionUID = -688580326437962778L;

    /**
     * Creates an exception from an role id.
     * @param roleId
     */
    public static final RoleAlreadyExistsException create(String roleId) {
        return new RoleAlreadyExistsException(Messages.i18n.format("RoleAlreadyExists", roleId)); //$NON-NLS-1$
    }
    
    /**
     * Constructor.
     */
    public RoleAlreadyExistsException() {
    }
    
    /**
     * Constructor.
     * @param message
     */
    public RoleAlreadyExistsException(String message) {
        super(message);
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.exceptions.AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.ROLE_ALREADY_EXISTS;
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.exceptions.AbstractRestException#getMoreInfo()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.ROLE_ALREADY_EXISTS_INFO;
    }

}
