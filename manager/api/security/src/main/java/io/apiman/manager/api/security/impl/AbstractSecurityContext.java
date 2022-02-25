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
package io.apiman.manager.api.security.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.idm.UserMapper;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.i18n.Messages;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Base class for security context implementations.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractSecurityContext implements ISecurityContext {

    protected static final ThreadLocal<HttpServletRequest> servletRequest = new ThreadLocal<>();
    private static final ThreadLocal<IndexedPermissions> permissions = new ThreadLocal<>();
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(AbstractSecurityContext.class);
    private static final UserMapper userMapper = UserMapper.INSTANCE;
    @Inject
    private IStorageQuery query;
    @Inject
    private IStorage storage;

    /**
     * Constructor.
     */
    public AbstractSecurityContext() {
    }

    /**
     * Called to clear the current thread local permissions bean.
     */
    protected static void clearPermissions() {
        permissions.remove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAdmin() {
        // TODO warning - hard coded role value here
        return servletRequest.get().isUserInRole("apiadmin"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestHeader(String headerName) {
        return servletRequest.get().getHeader(headerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentUser() {
        return servletRequest.get().getRemoteUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmail() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPermission(PermissionType permission, String organizationId) {
        // Admins can do everything.
        if (isAdmin()) {
            return true;
        } else {
            return getPermissions().hasQualifiedPermission(permission, organizationId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMemberOf(String organizationId) {
        if (isAdmin()) {
            return true;
        }
        return getPermissions().isMemberOf(organizationId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getPermittedOrganizations(PermissionType permission) {
        return getPermissions().getOrgQualifiers(permission);
    }

    /**
     * @return the user permissions for the current user
     */
    private IndexedPermissions getPermissions() {
        IndexedPermissions rval = permissions.get();
        if (rval == null) {
            rval = loadPermissions();
            permissions.set(rval);
        }
        return rval;
    }

    /**
     * Loads the current user's permissions into a thread local variable.
     */
    private IndexedPermissions loadPermissions() {
        String userId = getCurrentUser();
        try {
            return new IndexedPermissions(getQuery().getPermissions(userId));
        } catch (StorageException e) {
            LOGGER.error(Messages.getString("AbstractSecurityContext.ErrorLoadingPermissions") + userId, e); //$NON-NLS-1$
            return new IndexedPermissions(new HashSet<>());
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
     * {@inheritDoc}
     */
    @Override
    public void checkPermissions(PermissionType permission, String organizationId) throws NotAuthorizedException {
        if (!hasPermission(permission, organizationId)){
            throw ExceptionFactory.notAuthorizedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAdminPermissions() throws NotAuthorizedException {
        if (!isAdmin()) {
             throw ExceptionFactory.notAuthorizedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkIfUserIsCurrentUser(String userId) throws NotAuthorizedException {
        if (!isAdmin() && !getCurrentUser().equals(userId)) {
            throw ExceptionFactory.notAuthorizedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserDto> getUsersWithPermission(PermissionType permission, String orgName) {
        try {
            return storage.getAllUsersWithPermission(permission, orgName)
                          .stream()
                          .map(userMapper::toDto)
                          .collect(Collectors.toList());
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Locale getLocale();

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserDto> getUsersWithRole(String roleName, String orgName) {
        try {
            return storage.getAllUsersWithRole(roleName, orgName)
                          .stream()
                          .map(userMapper::toDto)
                          .collect(Collectors.toList());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
}
