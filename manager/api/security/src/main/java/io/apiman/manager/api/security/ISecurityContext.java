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
package io.apiman.manager.api.security;

import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The security context used by the REST API to determine whether the
 * current user has appropriate access to see specific data or perform
 * certain actions.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ISecurityContext {

    /**
     * @return the currently authenticated user.
     */
    String getCurrentUser();

    /**
     * @return the currently authenticated user's full name
     */
    String getFullName();

    /**
     * @return the currently authenticated user's email address
     */
    String getEmail();

    /**
     * Returns true if the current user is an administrator.
     * @return true if admin, else false
     */
    boolean isAdmin();

    /**
     * Returns true if the current user has permission to perform a particular
     * action for the provided organization.
     * @param permission the permission type
     * @param organizationId the org id
     * @return true if has permission, else false
     */
    boolean hasPermission(PermissionType permission, String organizationId);

    boolean hasAllPermissions(Set<PermissionType> permissions, String organizationId);

    boolean hasAnyPermission(Set<PermissionType> permissions, String organizationId);

    /**
     * @param organizationId the org ID
     */
    boolean isMemberOf(String organizationId);

    /**
     * Returns the set of organizations for which the current user is allowed
     * to perform a given action.
     * @param permission the permission type
     * @return set of permitted organizations
     */
    Set<String> getPermittedOrganizations(PermissionType permission);

    Set<DiscoverabilityLevel> getPermittedDiscoverabilities();

    /**
     * Gets a request header from the current in-scope request.
     * @param headerName the header name
     * @return the request header
     */
    String getRequestHeader(String headerName);

    /**
     * Checks if the current user has permission to perform a particular
     * action for the provided organization.
     * @param permission the permission type
     * @param organizationId the org id
     * @throws NotAuthorizedException if the user is not authorized
     */
    void checkPermissions(PermissionType permission, String organizationId) throws NotAuthorizedException;

    void checkAllPermissions(Set<PermissionType> permissions, String organizationId) throws NotAuthorizedException;

    void checkAnyPermission(Set<PermissionType> permissions, String organizationId) throws NotAuthorizedException;

    /**
     * Throws an exception if the user has no admin permissions
     * @throws NotAuthorizedException if the user is not authorized
     */
    void checkAdminPermissions() throws NotAuthorizedException;

    enum EntityType {
        API, PLAN
    }

    boolean hasPermissionsOrDiscoverable(EntityType entityType,
                                         String orgId,
                                         String entityId,
                                         Set<PermissionType> permissionType);

    boolean hasPermissionsOrDiscoverable(EntityType entityType,
                                         String orgId,
                                         String entityId,
                                         String entityVersion,
                                         Set<PermissionType> permissionType);

    boolean isDiscoverable(EntityType entityType, String organizationId, String entityId);

    boolean isDiscoverable(EntityType entityType, String organizationId, String entityId, String entityVersion);

    boolean isDiscoverable(EntityType entityType, String orgId, String entityId, String entityVersion, Set<DiscoverabilityLevel> discoverabilityLevelSet);

    void checkPermissionsOrDiscoverability(EntityType entityType,
                                           String orgId,
                                           String entityId,
                                           Set<PermissionType> permissionType) throws NotAuthorizedException;

    void checkPermissionsOrDiscoverability(EntityType entityType,
                                           String orgId,
                                           String entityId,
                                           String entityVersion,
                                           Set<PermissionType> permissionType) throws NotAuthorizedException;


    /**
     * Throws an exception if the user has no admin permissions
     * or the user called not is own user resource
     * @throws NotAuthorizedException if the user is not authorized
     */
    void checkIfUserIsCurrentUser(String userId) throws NotAuthorizedException;


    /**
     * Find all users in an org who have a specific role.
     * <p>
     * This may interrogate a remote IDM (e.g. Keycloak), in which case the orgName may or may not be used.
     * Users of this method should be aware that the user returned may not be stored in Apiman's user table
     * (i.e. is in the IDM only).
     */
    List<UserDto> getRemoteUsersWithRole(String roleName);


    /**
     * Find all users in an org who have a specific role.
     * <p>
     * This may interrogate a remote IDM (e.g. Keycloak), in which case the orgName may or may not be used.
     * Users of this method should be aware that the user returned may not be stored in Apiman's user table
     * (i.e. is in the IDM only).
     */
    List<UserDto> getUsersWithRole(String roleName, String orgName);

    /**
     * Find all users in an org who have a specific permission
     */
    List<UserDto> getUsersWithPermission(PermissionType permission, String orgName);

    /**
     * Get Locale from security context (e.g. token, browser headers).
     */
    Locale getLocale();
}
