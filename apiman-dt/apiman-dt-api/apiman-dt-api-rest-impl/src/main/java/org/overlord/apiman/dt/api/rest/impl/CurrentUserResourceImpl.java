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
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.core.IIdmStorage;
import org.overlord.apiman.dt.api.core.IStorage;
import org.overlord.apiman.dt.api.core.IStorageQuery;
import org.overlord.apiman.dt.api.core.exceptions.AlreadyExistsException;
import org.overlord.apiman.dt.api.core.exceptions.DoesNotExistException;
import org.overlord.apiman.dt.api.core.exceptions.StorageException;
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
    private IIdmStorage idmStorage;
    @Inject
    private IStorage storage;
    @Inject
    private IStorageQuery query;
    @Inject
    private ISecurityContext securityContext;

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
        String userId = getSecurityContext().getCurrentUser();
        try {
            UserBean user = getIdmStorage().getUser(userId);
            user.setAdmin(getSecurityContext().isAdmin());
            return user;
        } catch (DoesNotExistException e) {
            UserBean user = new UserBean();
            user.setUsername(userId);
            user.setFullName(userId);
            user.setEmail(userId + "@example.org"); //$NON-NLS-1$
            user.setJoinedOn(new Date());
            try {
                getIdmStorage().createUser(user);
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
        Set<String> permittedOrganizations = getSecurityContext().getPermittedOrganizations(PermissionType.orgView);
        try {
            return getQuery().getOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource#getApplications()
     */
    @Override
    public List<ApplicationSummaryBean> getApplications() {
        Set<String> permittedOrganizations = getSecurityContext().getPermittedOrganizations(PermissionType.orgView);
        try {
            return getQuery().getApplicationsInOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource#getServices()
     */
    @Override
    public List<ServiceSummaryBean> getServices() {
        Set<String> permittedOrganizations = getSecurityContext().getPermittedOrganizations(PermissionType.orgView);
        try {
            return getQuery().getServicesInOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @return the idmStorage
     */
    public IIdmStorage getIdmStorage() {
        return idmStorage;
    }

    /**
     * @param idmStorage the idmStorage to set
     */
    public void setIdmStorage(IIdmStorage idmStorage) {
        this.idmStorage = idmStorage;
    }

    /**
     * @return the storage
     */
    public IStorage getStorage() {
        return storage;
    }

    /**
     * @param storage the storage to set
     */
    public void setStorage(IStorage storage) {
        this.storage = storage;
    }

    /**
     * @return the query
     */
    public IStorageQuery getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(IStorageQuery query) {
        this.query = query;
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
