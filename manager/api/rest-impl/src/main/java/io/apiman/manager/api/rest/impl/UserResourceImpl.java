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

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.Preconditions;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.idm.CurrentUserBean;
import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.UpdateUserBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.idm.UserMapper;
import io.apiman.manager.api.beans.idm.UserPermissionsBean;
import io.apiman.manager.api.beans.notifications.NotificationCriteriaBean;
import io.apiman.manager.api.beans.notifications.NotificationStatus;
import io.apiman.manager.api.beans.notifications.dto.CreateNotificationFilterDto;
import io.apiman.manager.api.beans.notifications.dto.NotificationActionDto;
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
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.rest.impl.util.SearchCriteriaUtil;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.service.NotificationService;
import io.apiman.manager.api.service.UserService;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

/**
 * Implementation of the User API.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
@Transactional
@PermitAll
public class UserResourceImpl implements IUserResource, DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(UserResourceImpl.class);
    private IStorage storage;
    private NotificationService notificationService;
    private UserService userService;
    private ISecurityContext securityContext;
    private IStorageQuery query;
    private INewUserBootstrapper userBootstrapper;
    private UserMapper userMapper = UserMapper.INSTANCE;

    /**
     * Constructor.
     */
    @Inject
    public UserResourceImpl(IStorage storage,
         NotificationService notificationService,
         UserService userService,
         ISecurityContext securityContext,
         IStorageQuery query,
         INewUserBootstrapper userBootstrapper) {
        this.storage = storage;
        this.notificationService = notificationService;
        this.userService = userService;
        this.securityContext = securityContext;
        this.query = query;
        this.userBootstrapper = userBootstrapper;
    }

    public UserResourceImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDto get(String userId) throws UserNotFoundException {
        securityContext.checkIfUserIsCurrentUser(userId);
        return userMapper.toDto(userService.getUserById(userId));
    }

    // TODO(msavy): refactor and move to service
    /**
     * {@inheritDoc}
     */
    @Override
    public CurrentUserBean getInfo() {
        String userId = securityContext.getCurrentUser();

        return tryAction(() -> {
            CurrentUserBean currentUser = new CurrentUserBean();
            UserBean user = userService.getUserById(userId);
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
                if (securityContext.getLocale() != null) {
                    user.setLocale(securityContext.getLocale());
                }
                storage.createUser(user);
                userBootstrapper.bootstrapUser(user, storage);

                currentUser.setPermissions(new HashSet<>());
            } else {
                LOGGER.debug("Got existing user: {0}", user);
                Set<PermissionBean> permissions = query.getPermissions(userId);
                currentUser.setPermissions(permissions);
                updateMutableFields(user);
            }
            currentUser.initFromUser(user);
            currentUser.setAdmin(securityContext.isAdmin());
            return currentUser;
        });
    }

    private void updateMutableFields(UserBean user) {
        boolean anyChanged = false;

        if (notNullOrNotEq(user.getLocale(), securityContext.getLocale())) {
            anyChanged = true;
            user.setLocale(securityContext.getLocale());
        }
        if (notNullOrNotEq(user.getEmail(), securityContext.getEmail())) {
            anyChanged = true;
            user.setEmail(securityContext.getEmail());
        }
        if (notNullOrNotEq(user.getFullName(), securityContext.getFullName())) {
            anyChanged = true;
            user.setFullName(securityContext.getFullName());
        }

        if (anyChanged) {
            LOGGER.debug("Updated user after detecting change(s) to mutable attributes: {0}", user);
            tryAction(() -> storage.updateUser(user));
        }
    }

    private boolean notNullOrNotEq(Object existingValue, Object newValue) {
        return newValue != null && !(existingValue.equals(newValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String userId, UpdateUserBean user) throws UserNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        userService.update(userId, user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrganizationSummaryBean> getOrganizations(String userId) throws NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return userService.getPermittedOrgs(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ClientSummaryBean> getClients(String userId) throws NotAuthorizedException, SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getClientsInternal(userId, PermissionType.clientView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ClientSummaryBean> getEditableClients(String userId)
         throws NotAuthorizedException, SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getClientsInternal(userId, PermissionType.clientEdit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ApiSummaryBean> getApis(String userId) throws NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        Set<String> permittedOrganizations = getPermittedOrganizations(userId, PermissionType.apiView);
        return tryAction(() -> query.getApisInOrgs(permittedOrganizations));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResultsBean<AuditEntryBean> getActivity(String userId, int page, int pageSize)
         throws NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        try {
            PagingBean paging = PagingBean.create(page, pageSize);
            return query.auditUser(userId, paging);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPermissionsBean getPermissionsForUser(String userId)
         throws UserNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return tryAction(() -> {
            UserPermissionsBean bean = new UserPermissionsBean();
            bean.setUserId(userId);
            bean.setPermissions(query.getPermissions(userId));
            return bean;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResultsBean<NotificationDto<?>> getNotificationsForUser(String userId, NotificationCriteriaBean criteria)
         throws UserNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return notificationService.searchNotificationsByRecipient(userId, criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response getNotificationCountForUser(String userId, boolean includeDismissed)
         throws UserNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);
        // notificationService uses "unreadOnly" for boolean, but REST is "includeDismissed", so we need to invert.
        boolean unreadOnly = !includeDismissed;
        int notificationCount = notificationService.getNotificationsCount(userId, unreadOnly);
        return Response.noContent()
                       .header("X-Total-Count", notificationCount)
                       .header("Total-Count", notificationCount)
                       .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response markNotifications(String userId, NotificationActionDto notificationAction)
         throws UserNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(userId);
        if (notificationAction.isMarkAll()) {
            Preconditions.checkArgument(notificationAction.getStatus() != NotificationStatus.OPEN,
                 "When using markAll a non-OPEN status must be used: " + notificationAction.getStatus());
            notificationService.markAllNotificationsReadByUserId(userId, notificationAction.getStatus());
        } else {
            notificationService.markNotificationsWithStatus(userId, notificationAction.getNotificationIds(), notificationAction.getStatus());
        }
        return Response.noContent().build();
    }

    @Override
    public Response createNotificationFilter(String userId, CreateNotificationFilterDto createFilter) {
        securityContext.checkIfUserIsCurrentUser(userId);
        notificationService.createFilter(userId, createFilter);
        return Response.accepted().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrganizationSummaryBean> getClientOrganizations(String userId) throws SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getOrganizationsInternal(userId, PermissionType.clientEdit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrganizationSummaryBean> getApiOrganizations(String userId) throws SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getOrganizationsInternal(userId, PermissionType.apiEdit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrganizationSummaryBean> getPlanOrganizations(String userId) throws SystemErrorException {
        securityContext.checkIfUserIsCurrentUser(userId);

        return getOrganizationsInternal(userId, PermissionType.planEdit);
    }

    private Set<String> getPermittedOrganizations(String userId, PermissionType permissionType) {
        return userService.getPermissions(userId)
                   .stream()
                   .filter(permissionBean -> permissionBean.getName().equals(permissionType))
                   .map(PermissionBean::getOrganizationId)
                   .collect(Collectors.toSet());
    }

    // TODO move to service
    private List<OrganizationSummaryBean> getOrganizationsInternal(String userId, PermissionType permissionType)
         throws SystemErrorException {
        return tryAction(() -> query.getOrgs(getPermittedOrganizations(userId, permissionType)));
    }

    // TODO move to service
    private List<ClientSummaryBean> getClientsInternal(String userId, PermissionType permissionType) throws SystemErrorException {
        try {
            return query.getClientsInOrgs(getPermittedOrganizations(userId, permissionType));
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
}
