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
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.idm.UserMapper;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.config.ApiManagerConfig;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.i18n.Messages;
import io.apiman.manager.api.security.impl.IndexedDiscoverabilities.DILookupResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
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
    private static final ThreadLocal<IndexedDiscoverabilities> discoverabilities = ThreadLocal.withInitial(IndexedDiscoverabilities::new);
    private static final UserMapper userMapper = UserMapper.INSTANCE;
    private final IStorageQuery query;
    private final IStorage storage;
    private final DiscoverabilityOptionsParser discoverabilityConfig;

    public AbstractSecurityContext(IStorageQuery query, IStorage storage, ApiManagerConfig config) {
        this.query = query;
        this.storage = storage;
        this.discoverabilityConfig = new DiscoverabilityOptionsParser(config.getIdmDiscoverabilityMappings());
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
        return isAdmin() || getPermissions().hasQualifiedPermission(permission, organizationId);
    }

    @Override
    public boolean hasAllPermissions(Set<PermissionType> permissions, String organizationId) {
        return permissions.stream().allMatch(permTest -> hasPermission(permTest, organizationId));
    }

    @Override
    public boolean hasAnyPermission(Set<PermissionType> permissions, String organizationId) {
        return permissions.stream().anyMatch(permTest -> hasPermission(permTest, organizationId));
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

    @Override
    public Set<DiscoverabilityLevel> getPermittedDiscoverabilities() {
        return getDiscoverabilities();
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
        if (userId == null || userId.isBlank()) {
            return new IndexedPermissions(Collections.emptySet());
        }
        try {
            return new IndexedPermissions(query.getPermissions(userId));
        } catch (StorageException e) {
            LOGGER.error(Messages.getString("AbstractSecurityContext.ErrorLoadingPermissions") + userId, e); //$NON-NLS-1$
            return new IndexedPermissions(new HashSet<>());
        }
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

    @Override
    public void checkAllPermissions(Set<PermissionType> permissions, String organizationId) throws NotAuthorizedException {
        if (!hasAllPermissions(permissions, organizationId)) {
            throw ExceptionFactory.notAuthorizedException();
        }
    }

    @Override
    public void checkAnyPermission(Set<PermissionType> permissions, String organizationId) throws NotAuthorizedException {
        if (!hasAnyPermission(permissions, organizationId)) {
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

    @Override
    public void checkPermissionsOrDiscoverability(EntityType entityType,
                                                  String orgId,
                                                  String entityId,
                                                  Set<PermissionType> permissionType) {
        if (!hasPermissionsOrDiscoverable(entityType, orgId, entityId, permissionType)) {
            throw ExceptionFactory.notAuthorizedException();
        }
    }

    @Override
    public void checkPermissionsOrDiscoverability(EntityType entityType,
                                                  String orgId,
                                                  String entityId,
                                                  String entityVersion,
                                                  Set<PermissionType> permissionType) {
        if (!hasPermissionsOrDiscoverable(entityType, orgId, entityId, entityVersion, permissionType)) {
            throw ExceptionFactory.notAuthorizedException();
        }
    }

    @Override
    public boolean hasPermissionsOrDiscoverable(EntityType entityType,
                                                String orgId,
                                                String entityId,
                                                Set<PermissionType> permissionType) {
        return hasPermissionsOrDiscoverable(entityType, orgId, entityId, null, permissionType);
    }

    @Override
    public boolean hasPermissionsOrDiscoverable(EntityType entityType,
                                                String orgId,
                                                String entityId,
                                                String entityVersion,
                                                Set<PermissionType> permissionType) {
        return hasAnyPermission(permissionType, orgId) || isDiscoverable(entityType, orgId, entityId, entityVersion);
    }

    @Override
    public boolean isDiscoverable(EntityType entityType, String organizationId, String entityId) {
        return isDiscoverable(entityType, organizationId, entityId, null);
    }

    @Override
    public boolean isDiscoverable(EntityType entityType, String orgId, String entityId, String entityVersion) {
        return isDiscoverable(entityType, orgId, entityId, entityVersion, getDiscoverabilities());
    }

    @Override
    public boolean isDiscoverable(EntityType entityType, String orgId, String entityId, String entityVersion, Set<DiscoverabilityLevel> discoverabilityLevelSet) {
        DILookupResult indexedResult = isVis(entityType, orgId, entityId, entityVersion, discoverabilityLevelSet);
        switch(indexedResult) {
            case DISCOVERABLE:
                return true;
            case NOT_DISCOVERABLE:
                return false;
            case NOT_IN_INDEX:
                // Handle cases where: (1) not in index, (2) bad information. Need to be careful not to get into cycle.
                IndexedDiscoverabilities indexedDiscoverabilities = discoverabilities.get();
                indexedDiscoverabilities.index(storage.getOrgApiPlansWithDiscoverability(orgId, Set.of(DiscoverabilityLevel.values())));
                DILookupResult retry = isVis(entityType, orgId, entityId, entityVersion, discoverabilityLevelSet);
                return retry == DILookupResult.DISCOVERABLE;
            default:
                throw new IllegalArgumentException("Unhandled index state: " + indexedResult);
        }
    }

    private DILookupResult isVis(EntityType entityType, String orgId, String entityId, String entityVersion, Set<DiscoverabilityLevel> discoverabilityLevelSet) {
        IndexedDiscoverabilities indexedDiscoverabilities = discoverabilities.get();
        if (entityVersion == null || entityVersion.isBlank()) {
            return indexedDiscoverabilities.isAnyDiscoverable(entityType, orgId, entityId, discoverabilityLevelSet);
        } else {
            return indexedDiscoverabilities.isDiscoverable(entityType, orgId, entityId, entityVersion, discoverabilityLevelSet);
        }
    }

    private Set<DiscoverabilityLevel> getDiscoverabilities() {
        HttpServletRequest request = servletRequest.get();

        if (request.getRemoteUser() != null) {
            Set<DiscoverabilityLevel> discoverabilities = new HashSet<>(4);
            discoverabilityConfig.getSourceToDiscoverability().forEach((source, discoverabilityConfig) -> {
                switch (source) {
                    case IDM_ROLE:
                        if (request.isUserInRole(discoverabilityConfig.getName())) {
                            discoverabilities.addAll(discoverabilityConfig.getDiscoverabilities());
                        }
                        break;
                    case IDM_ATTRIBUTE:
                    case APIMAN_ROLE:
                    case APIMAN_PERMISSION:
                        throw new UnsupportedOperationException("Support for " + source + " not available on this platform.");
                    default:
                        throw new IllegalStateException("Unexpected value: " + source);
                }
            });
            // TODO(msavy): make default discoverability configurable, perhaps?
            if (discoverabilities.isEmpty()) {
                discoverabilities.add(DiscoverabilityLevel.PORTAL);
                discoverabilities.add(DiscoverabilityLevel.ANONYMOUS);
            }
            return discoverabilities;
        } else {
            return Set.of(DiscoverabilityLevel.PORTAL, DiscoverabilityLevel.ANONYMOUS);
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
