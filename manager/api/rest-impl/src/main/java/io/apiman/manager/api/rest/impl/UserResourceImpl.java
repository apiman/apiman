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

import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.idm.*;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.core.INewUserBootstrapper;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.IUserResource;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.UserNotFoundException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the User API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class UserResourceImpl implements IUserResource {
    
    @Inject
    private
    IStorage storage;
    @Inject
    ISecurityContext securityContext;
    @Inject
    IStorageQuery query;
    @Inject
    private INewUserBootstrapper userBootstrapper;

    /**
     * Constructor.
     */
    public UserResourceImpl() {
    }

    /**
     * @see IUserResource#get(java.lang.String)
     */
    @Override
    public UserBean get(String userId) throws UserNotFoundException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getUserInternal(userId);
    }

    /**
     * @see IUserResource#getInfo()
     */
    @Override
    public CurrentUserBean getInfo() {
        String userId = securityContext.getCurrentUser();

        try {
            CurrentUserBean currentUser = new CurrentUserBean();
            UserBean user = getUserInternal(userId);
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
                currentUser.setPermissions(new HashSet<>());
            } else {
                Set<PermissionBean> permissions = query.getPermissions(userId);
                currentUser.setPermissions(permissions);
            }
            currentUser.initFromUser(user);
            currentUser.setAdmin(securityContext.isAdmin());
            return currentUser;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    private UserBean getUserInternal(String userId) {
        try {
            storage.beginTx();
            UserBean user = storage.getUser(userId);
            return user;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        } finally {
            storage.rollbackTx();
        }
    }
    
    /**
     * @see IUserResource#update(java.lang.String, io.apiman.manager.api.beans.idm.UpdateUserBean)
     */
    @Override
    public void update(String userId, UpdateUserBean user) throws UserNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        try {
            UserBean updatedUser = getUserInternal(userId);
            if (updatedUser == null) {
                throw ExceptionFactory.userNotFoundException(userId);
            }
            if (user.getEmail() != null) {
                updatedUser.setEmail(user.getEmail());
            }
            if (user.getFullName() != null) {
                updatedUser.setFullName(user.getFullName());
            }

            storage.beginTx();
            storage.updateUser(updatedUser);
            storage.commitTx();
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IUserResource#getOrganizations(java.lang.String)
     */
    @Override
    public List<OrganizationSummaryBean> getOrganizations(String userId) throws NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        Set<String> permittedOrganizations = new HashSet<>();
        try {
            Set<RoleMembershipBean> memberships = query.getUserMemberships(userId);
            for (RoleMembershipBean membership : memberships) {
                permittedOrganizations.add(membership.getOrganizationId());
            }
            return query.getOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IUserResource#getClients(java.lang.String)
     */
    @Override
    public List<ClientSummaryBean> getClients(String userId) throws NotAuthorizedException, SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getClientsInternal(userId, PermissionType.clientView);
    }
    /**
     * @see IUserResource#getEditableClients(String)
     * */
    @Override
    public List<ClientSummaryBean> getEditableClients(String userId) throws NotAuthorizedException, SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getClientsInternal(userId, PermissionType.clientEdit);
    }

    private List<ClientSummaryBean> getClientsInternal(String userId, PermissionType permissionType) throws SystemErrorException {
        try {
            return query.getClientsInOrgs(getPermittedOrganizations(userId, permissionType));
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IUserResource#getApis(java.lang.String)
     */
    @Override
    public List<ApiSummaryBean> getApis(String userId) throws NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        Set<String> permittedOrganizations = getPermittedOrganizations(userId, PermissionType.apiView);
        try {
            return query.getApisInOrgs(permittedOrganizations);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IUserResource#getActivity(java.lang.String, int, int)
     */
    @Override
    public SearchResultsBean<AuditEntryBean> getActivity(String userId, int page, int pageSize) throws NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }
        try {
            SearchResultsBean<AuditEntryBean> rval;
            PagingBean paging = new PagingBean();
            paging.setPage(page);
            paging.setPageSize(pageSize);
            rval = query.auditUser(userId, paging);
            return rval;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IUserResource#getPermissionsForUser(java.lang.String)
     */
    @Override
    public UserPermissionsBean getPermissionsForUser(String userId) throws UserNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

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
     * @see IUserResource#getClientOrganizations(String)
     */
    @Override
    public List<OrganizationSummaryBean> getClientOrganizations(String userId) throws SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getOrganizationsInternal(userId, PermissionType.clientEdit);
    }

    /**
     * @see IUserResource#getApiOrganizations(String)
     */
    @Override
    public List<OrganizationSummaryBean> getApiOrganizations(String userId) throws SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getOrganizationsInternal(userId, PermissionType.apiEdit);
    }

    /**
     * @see IUserResource#getPlanOrganizations(String)
     */
    @Override
    public List<OrganizationSummaryBean> getPlanOrganizations(String userId) throws SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getOrganizationsInternal(userId, PermissionType.planEdit);
    }

    private Set<String> getPermittedOrganizations(String userId, PermissionType permissionType) throws SystemErrorException {
        Set<String> permittedOrganizations = new HashSet<>();
        try {
            Set<PermissionBean> permissions = query.getPermissions(userId);
            for (PermissionBean permission : permissions) {
                if (permission.getName() == permissionType) {
                    permittedOrganizations.add(permission.getOrganizationId());
                }
            }
            return permittedOrganizations;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    private List<OrganizationSummaryBean> getOrganizationsInternal(String userId, PermissionType permissionType) throws SystemErrorException {
        try {
            return query.getOrgs(getPermittedOrganizations(userId, permissionType));
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
