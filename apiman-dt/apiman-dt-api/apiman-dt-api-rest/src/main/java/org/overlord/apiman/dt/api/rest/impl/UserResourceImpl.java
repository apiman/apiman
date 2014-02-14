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

package org.overlord.apiman.dt.api.rest.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.idm.UserBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.persist.DoesNotExistException;
import org.overlord.apiman.dt.api.persist.IIdmStorage;
import org.overlord.apiman.dt.api.persist.StorageException;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.UserNotFoundException;
import org.overlord.apiman.dt.api.security.ISecurityContext;

/**
 * Implementation of the User API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class UserResourceImpl implements IUserResource {
    
    @Inject
    IIdmStorage idmStorage;
    @Inject
    ISecurityContext securityContext;
    
    /**
     * Constructor.
     */
    public UserResourceImpl() {
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IUserResource#get(java.lang.String)
     */
    @Override
    public UserBean get(String userId) throws UserNotFoundException {
        try {
            return idmStorage.getUser(userId);
        } catch (DoesNotExistException e) {
            throw UserNotFoundException.create(userId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IUserResource#update(java.lang.String, org.overlord.apiman.dt.api.beans.idm.UserBean)
     */
    @Override
    public void update(String userId, UserBean user) throws UserNotFoundException, NotAuthorizedException {
        if (!securityContext.isAdmin() && !securityContext.getCurrentUser().equals(userId))
            throw new NotAuthorizedException();
        user.setUsername(userId);
        try {
            idmStorage.updateUser(user);
        } catch (DoesNotExistException e) {
            throw UserNotFoundException.create(userId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IUserResource#search(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<UserBean> search(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException {
        try {
            return idmStorage.findUsers(criteria);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
}
