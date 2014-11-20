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

import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.api.core.IIdmStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.AlreadyExistsException;
import io.apiman.manager.api.core.exceptions.DoesNotExistException;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.contract.ICurrentUserResource;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
    private IStorageQuery query;
    @Inject
    private ISecurityContext securityContext;

    /**
     * Constructor.
     */
    public CurrentUserResourceImpl() {
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.ICurrentUserResource#getInfo()
     */
    @Override
    public UserBean getInfo() {
        String userId = securityContext.getCurrentUser();
        try {
            UserBean user = idmStorage.getUser(userId);
            user.setAdmin(securityContext.isAdmin());
            return user;
        } catch (DoesNotExistException e) {
            UserBean user = new UserBean();
            user.setUsername(userId);
            user.setFullName(userId);
            user.setEmail(userId + "@example.org"); //$NON-NLS-1$
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
     * @see io.apiman.manager.api.rest.contract.ICurrentUserResource#getOrganizations()
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
     * @see io.apiman.manager.api.rest.contract.ICurrentUserResource#getApplications()
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
    
    /**
     * @see io.apiman.manager.api.rest.contract.ICurrentUserResource#getServices()
     */
    @Override
    public List<ServiceSummaryBean> getServices() {
        Set<String> permittedOrganizations = securityContext.getPermittedOrganizations(PermissionType.orgView);
        try {
            return query.getServicesInOrgs(permittedOrganizations);
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
