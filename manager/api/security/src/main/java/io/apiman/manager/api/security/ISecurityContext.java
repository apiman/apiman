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
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;

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
     * @return the currently authentiated user.
     */
    public String getCurrentUser();

    /**
     * @return the currently authenticated user's full name
     */
    public String getFullName();

    /**
     * @return the currently authenticated user's email address
     */
    public String getEmail();

    /**
     * Returns true if the current user is an administrator.
     * @return true if admin, else false
     */
    public boolean isAdmin();

    /**
     * Returns true if the current user has permission to perform a particular
     * action for the provided organization.
     * @param permission the permission type
     * @param organizationId the org id
     * @return true if has permission, else false
     */
    public boolean hasPermission(PermissionType permission, String organizationId);

    /**
     * @param organizationId
     */
    public boolean isMemberOf(String organizationId);

    /**
     * Returns the set of organizations for which the current user is allowed
     * to perform a given action.
     * @param permission the permission type
     * @return set of permitted organizations
     */
    public Set<String> getPermittedOrganizations(PermissionType permission);

    /**
     * Gets a request header from the current in-scope request.
     * @param headerName the header name
     * @return the request header
     */
    public String getRequestHeader(String headerName);

    /**
     * Checks if the current user has permission to perform a particular
     * action for the provided organization.
     * @param permission the permission type
     * @param organizationId the org id
     * @throws NotAuthorizedException if the user is not authorized
     */
    void checkPermissions(PermissionType permission, String organizationId) throws NotAuthorizedException;

    /**
     * Throws an exception if the user has no admin permissions
     * @throws NotAuthorizedException if the user is not authorized
     */
    void checkAdminPermissions() throws NotAuthorizedException;

    /**
     * Throws an exception if the user has no admin permissions
     * or the user called not is own user resource
     * @throws NotAuthorizedException if the user is not authorized
     */
    void checkIfUserIsCurrentUser(String userId) throws NotAuthorizedException;

}
