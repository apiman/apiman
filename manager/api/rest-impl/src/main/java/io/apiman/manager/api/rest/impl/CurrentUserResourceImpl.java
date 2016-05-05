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

import io.apiman.manager.api.beans.idm.CurrentUserBean;
import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.UpdateUserBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.core.INewUserBootstrapper;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.rest.contract.ICurrentUserResource;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.Date;
import java.util.HashSet;
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
    private IStorage storage;
    @Inject
    private IStorageQuery query;
    @Inject
    private ISecurityContext securityContext;
    @Inject @ApimanLogger(CurrentUserResourceImpl.class)
    private IApimanLogger log;
    @Inject
    private INewUserBootstrapper userBootstrapper;


    /**
     * Constructor.
     */
    public CurrentUserResourceImpl() {
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ICurrentUserResource#getInfo()
     */
    @Override
    public CurrentUserBean getInfo() {
        String userId = securityContext.getCurrentUser();

        try {
            CurrentUserBean rval = new CurrentUserBean();
            UserBean user;
            storage.beginTx();
            try {
                user = storage.getUser(userId);
            } finally {
                storage.rollbackTx();
            }
            if (user == null) {
                user = new UserBean();
                user.setUsername(userId);
                if (securityContext.getFullName() != null) {
                    user.setFullName(securityContext.getFullName());
                } else {
                    user.setFullName(userId);
                }
                if (securityContext.getEmail() != null) {
                    user.setEmail(securityContext.getEmail());
                } else {
                    user.setEmail(""); //$NON-NLS-1$
                }
                user.setJoinedOn(new Date());
                storage.beginTx();
                try {
                    storage.createUser(user);
                    userBootstrapper.bootstrapUser(user, storage);
                    storage.commitTx();
                } catch (StorageException e1) {
                    storage.rollbackTx();
                    throw new SystemErrorException(e1);
                }
                rval.initFromUser(user);
                rval.setAdmin(securityContext.isAdmin());
                rval.setPermissions(new HashSet<>());
            } else {
                rval.initFromUser(user);
                Set<PermissionBean> permissions = query.getPermissions(userId);
                rval.setPermissions(permissions);
                rval.setAdmin(securityContext.isAdmin());
            }

            log.debug(String.format("Getting info for user %s", user.getUsername())); //$NON-NLS-1$
            return rval;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ICurrentUserResource#updateInfo(io.apiman.manager.api.beans.idm.UpdateUserBean)
     */
    @Override
    public void updateInfo(UpdateUserBean info) {
        try {
            storage.beginTx();
            UserBean user = storage.getUser(securityContext.getCurrentUser());
            if (user == null) {
                throw new StorageException("User not found: " + securityContext.getCurrentUser()); //$NON-NLS-1$
            }
            if (info.getEmail() != null) {
                user.setEmail(info.getEmail());
            }
            if (info.getFullName() != null) {
                user.setFullName(info.getFullName());
            }
            storage.updateUser(user);
            storage.commitTx();
            log.debug(String.format("Successfully updated user %s: %s", user.getUsername(), user)); //$NON-NLS-1$
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ICurrentUserResource#getClientOrganizations()
     */
    @Override
    public List<OrganizationSummaryBean> getClientOrganizations() {
        Set<String> permittedOrganizations = securityContext.getPermittedOrganizations(PermissionType.clientEdit);
        try {
            return query.getOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ICurrentUserResource#getPlanOrganizations()
     */
    @Override
    public List<OrganizationSummaryBean> getPlanOrganizations() {
        Set<String> permittedOrganizations = securityContext.getPermittedOrganizations(PermissionType.planEdit);
        try {
            return query.getOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ICurrentUserResource#getApiOrganizations()
     */
    @Override
    public List<OrganizationSummaryBean> getApiOrganizations() {
        Set<String> permittedOrganizations = securityContext.getPermittedOrganizations(PermissionType.apiEdit);
        try {
            return query.getOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ICurrentUserResource#getClients()
     */
    @Override
    public List<ClientSummaryBean> getClients() {
        Set<String> permittedOrganizations = securityContext.getPermittedOrganizations(PermissionType.clientView);
        try {
            return query.getClientsInOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ICurrentUserResource#getApis()
     */
    @Override
    public List<ApiSummaryBean> getApis() {
        Set<String> permittedOrganizations = securityContext.getPermittedOrganizations(PermissionType.apiView);
        try {
            return query.getApisInOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
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
}
