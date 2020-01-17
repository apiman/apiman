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

package io.apiman.manager.api.rest.impl;

import io.apiman.manager.api.beans.idm.UserPermissionsBean;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.IPermissionsResource;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.UserNotFoundException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implementation of the Permissions API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class PermissionsResourceImpl implements IPermissionsResource {
    
    @Inject
    IStorageQuery query;
    @Inject
    ISecurityContext securityContext;
    
    /**
     * Constructor.
     */
    public PermissionsResourceImpl() {
    }
    
    /**
     * @see IPermissionsResource#getPermissionsForUser(java.lang.String)
     */
    @Override
    public UserPermissionsBean getPermissionsForUser(String userId) throws UserNotFoundException, NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();

        try {
            UserPermissionsBean bean = new UserPermissionsBean();
            bean.setUserId(userId);
            bean.setPermissions(query.getPermissions(userId));
            return bean;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see IPermissionsResource#getPermissionsForCurrentUser()
     */
    @Override
    public UserPermissionsBean getPermissionsForCurrentUser() throws UserNotFoundException {
        try {
            String currentUser = securityContext.getCurrentUser();
            UserPermissionsBean bean = new UserPermissionsBean();
            bean.setUserId(currentUser);
            bean.setPermissions(query.getPermissions(currentUser));
            return bean;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @return the securityContext
     */
    public ISecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     * @param securityContext the securityContext to set
     */
    public void setSecurityContext(ISecurityContext securityContext) {
        this.securityContext = securityContext;
    }
    
}
