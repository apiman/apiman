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
import io.apiman.manager.api.beans.idm.CurrentUserBean;
import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UpdateUserBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.idm.UserPermissionsBean;
import io.apiman.manager.api.beans.notifications.NotificationCriteriaBean;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
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
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.rest.impl.util.SearchCriteriaUtil;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.service.NotificationService;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Implementation of the User API.
 *
 * TODO(msavy): Extract into service fully
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
@Transactional
public class UserResourceImpl implements IUserResource, DataAccessUtilMixin {

    private IStorage storage;
    private NotificationService notificationService;
    private ISecurityContext securityContext;
    private IStorageQuery query;
    private INewUserBootstrapper userBootstrapper;

    /**
     * Constructor.
     */
    @Inject
    public UserResourceImpl(IStorage storage,
        NotificationService notificationService,
        ISecurityContext securityContext,
        IStorageQuery query,
        INewUserBootstrapper userBootstrapper) {
        this.storage = storage;
        this.notificationService = notificationService;
        this.securityContext = securityContext;
        this.query = query;
        this.userBootstrapper = userBootstrapper;
    }

    public UserResourceImpl() {
    }

    @Override
    public UserBean get(String userId) throws UserNotFoundException {
        securityContext.checkIfUserIsCurrentUser(userId);
        return getUserInternal(userId);
    }

    @Override
    public CurrentUserBean getInfo() {
        String userId = securityContext.getCurrentUser();

        return tryAction(() -> {
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

                storage.createUser(user);
                userBootstrapper.bootstrapUser(user, storage);

                currentUser.setPermissions(new HashSet<>());
            } else {
                Set<PermissionBean> permissions = query.getPermissions(userId);
                currentUser.setPermissions(permissions);
            }
            currentUser.initFromUser(user);
            currentUser.setAdmin(securityContext.isAdmin());
            return currentUser;
        });
    }

    @Override
    public void update(String userId, UpdateUserBean user) throws UserNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        tryAction(() -> {
            UserBean updatedUser = tryAction(() -> storage.getUser(userId));
            if (updatedUser == null) {
                throw ExceptionFactory.userNotFoundException(userId);
            }
            if (user.getEmail() != null) {
                updatedUser.setEmail(user.getEmail());
            }
            if (user.getFullName() != null) {
                updatedUser.setFullName(user.getFullName());
            }

            storage.updateUser(updatedUser);
        });
    }

    @Override
    public List<OrganizationSummaryBean> getOrganizations(String userId) throws NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        Set<String> permittedOrganizations = new HashSet<>();

        return tryAction(() -> {
            Set<RoleMembershipBean> memberships = query.getUserMemberships(userId);
            for (RoleMembershipBean membership : memberships) {
                permittedOrganizations.add(membership.getOrganizationId());
            }
            return query.getOrgs(permittedOrganizations);
        });
    }

    @Override
    public List<ClientSummaryBean> getClients(String userId) throws NotAuthorizedException, SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getClientsInternal(userId, PermissionType.clientView);
    }

    @Override
    public List<ClientSummaryBean> getEditableClients(String userId) throws NotAuthorizedException, SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getClientsInternal(userId, PermissionType.clientEdit);
    }

    @Override
    public List<ApiSummaryBean> getApis(String userId) throws NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        Set<String> permittedOrganizations = getPermittedOrganizations(userId, PermissionType.apiView);
        return tryAction(() -> query.getApisInOrgs(permittedOrganizations));
    }

    @Override
    public SearchResultsBean<AuditEntryBean> getActivity(String userId, int page, int pageSize) throws NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        try {
            PagingBean paging = PagingBean.create(page, pageSize);
            return query.auditUser(userId, paging);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    @Override
    public UserPermissionsBean getPermissionsForUser(String userId) throws UserNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return tryAction(() -> {
            UserPermissionsBean bean = new UserPermissionsBean();
            bean.setUserId(userId);
            bean.setPermissions(query.getPermissions(userId));
            return bean;
        });
    }

    @Override
    public SearchResultsBean<NotificationDto<?>> searchLatestNotificationsForUser(String userId, NotificationCriteriaBean criteria)
         throws UserNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return notificationService.searchNotificationsByRecipient(userId, criteria);
    }

    @Override
    public List<OrganizationSummaryBean> getClientOrganizations(String userId) throws SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getOrganizationsInternal(userId, PermissionType.clientEdit);
    }

    @Override
    public List<OrganizationSummaryBean> getApiOrganizations(String userId) throws SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getOrganizationsInternal(userId, PermissionType.apiEdit);
    }

    @Override
    public List<OrganizationSummaryBean> getPlanOrganizations(String userId) throws SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getOrganizationsInternal(userId, PermissionType.planEdit);
    }

    private Set<String> getPermittedOrganizations(String userId, PermissionType permissionType) throws SystemErrorException {
        Set<String> permittedOrganizations = new HashSet<>();

        Set<PermissionBean> permissions = tryAction(() -> query.getPermissions(userId));
        for (PermissionBean permission : permissions) {
            if (permission.getName() == permissionType) {
                permittedOrganizations.add(permission.getOrganizationId());
            }
        }
        return permittedOrganizations;
    }

    private List<OrganizationSummaryBean> getOrganizationsInternal(String userId, PermissionType permissionType) throws SystemErrorException {
        return tryAction(() -> query.getOrgs(getPermittedOrganizations(userId, permissionType)));
    }

    private UserBean getUserInternal(String id) {
        return tryAction(() -> storage.getUser(id));
    }

    private List<ClientSummaryBean> getClientsInternal(String userId, PermissionType permissionType) throws SystemErrorException {
        return tryAction(() -> query.getClientsInOrgs(getPermittedOrganizations(userId, permissionType)));
    }
}
