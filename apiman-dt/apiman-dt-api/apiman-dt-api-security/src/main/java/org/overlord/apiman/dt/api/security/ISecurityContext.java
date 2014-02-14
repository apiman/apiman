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
package org.overlord.apiman.dt.api.security;

import java.util.Set;

import org.overlord.apiman.dt.api.beans.idm.PermissionType;

/**
 * The security context used by the REST API to determine whether the
 * current user has appropriate access to see specific data or perform
 * certain actions.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ISecurityContext {
    
    /**
     * Returns the currently authentiated user.
     */
    public String getCurrentUser();

    /**
     * Returns true if the current user is an administrator.
     */
    public boolean isAdmin();

    /**
     * Returns true if the current user has permission to perform a particular
     * action for the provided organization.
     * @param permission
     * @param organizationId
     */
    public boolean hasPermission(PermissionType permission, String organizationId);
    
    /**
     * Returns the set of organizations for which the current user is allowed
     * to perform a given action.
     * @param permission
     */
    public Set<String> getPermittedOrganizations(PermissionType permission);

}
