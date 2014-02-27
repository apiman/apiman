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

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.beans.idm.UserBean;
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.OrganizationSummaryBean;
import org.overlord.apiman.dt.api.persist.AlreadyExistsException;
import org.overlord.apiman.dt.api.persist.DoesNotExistException;
import org.overlord.apiman.dt.api.persist.IIdmStorage;
import org.overlord.apiman.dt.api.persist.IStorage;
import org.overlord.apiman.dt.api.persist.IStorageQuery;
import org.overlord.apiman.dt.api.persist.StorageException;
import org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;
import org.overlord.apiman.dt.api.security.ISecurityContext;

/**
 * Implementation of the Current User API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class CurrentUserResourceImpl implements ICurrentUserResource {
    
    @Inject
    IIdmStorage idmStorage;
    @Inject
    IStorage storage;
    @Inject
    IStorageQuery query;
    @Inject
    ISecurityContext securityContext;

    /**
     * Constructor.
     */
    public CurrentUserResourceImpl() {
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource#getInfo()
     */
    @Override
    public UserBean getInfo() {
        String userId = securityContext.getCurrentUser();
        try {
            return idmStorage.getUser(userId);
        } catch (DoesNotExistException e) {
            UserBean user = new UserBean();
            user.setUsername(userId);
            user.setJoinedOn(new Date());
            try {
                idmStorage.createUser(user);
            } catch (AlreadyExistsException e1) {
                throw new SystemErrorException(e);
            } catch (StorageException e1) {
                throw new SystemErrorException(e);
            }
            return user;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource#getOrganizations()
     */
    @Override
    public List<OrganizationSummaryBean> getOrganizations() {
        Set<String> permittedOrganizations = securityContext.getPermittedOrganizations(PermissionType.orgView);
        try {
            return query.getOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource#getApplications()
     */
    @Override
    public List<ApplicationSummaryBean> getApplications() {
        Set<String> permittedOrganizations = securityContext.getPermittedOrganizations(PermissionType.orgView);
        try {
            return query.getApplicationsInOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
}
