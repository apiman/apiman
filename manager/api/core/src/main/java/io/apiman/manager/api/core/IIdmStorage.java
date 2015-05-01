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
package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.util.Set;

/**
 * Interface to manage roles and users. This is separate from the
 * {@link IStorage} interface so that roles can be stored using a different
 * strategy. An obvious example is that the users and roles may be stored in an
 * LDAP directory while the core apiman data is stored in a database.
 *
 * Depending on implementation, various methods in this interface may not
 * be supported.  For example, if the IDM system being used is read only
 * (perhaps because it is backed by some centrally managed LDAP system).
 *
 * @author eric.wittmann@redhat.com
 */
public interface IIdmStorage {

    /**
     * Creates a user in the IDM system.
     * @param user the user
     * @throws StorageException if an exception occurs during storage attempt
     */
    public void createUser(UserBean user) throws StorageException;

    /**
     * Gets a user by id.
     * @param userId user's id
     * @return the user
     * @throws StorageException if an exception occurs during storage attempt
     */
    public UserBean getUser(String userId) throws StorageException;

    /**
     * Updates the personal information about a user.
     * @param user the user
     * @throws StorageException if an exception occurs during storage attempt
     */
    public void updateUser(UserBean user) throws StorageException;

    /**
     * Returns a list of users that match the given search criteria.
     * @param criteria search criteria
     * @return found users
     * @throws StorageException if an exception occurs during storage attempt
     */
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException;

    /**
     * Creates a new role in the role storage system.  This is typically done
     * by a super admin of the system, to set up roles and what permissions
     * memberhip in those roles will grant.
     * @param role the role
     * @throws StorageException  if an exception occurs during storage attempt
     */
    public void createRole(RoleBean role) throws StorageException;

    /**
     * Gets a role by id.
     * @param roleId the role id
     * @return the role
     * @throws StorageException if an exception occurs during storage attempt
     */
    public RoleBean getRole(String roleId) throws StorageException;

    /**
     * Updates a single role (typically with new permissions).
     * @param role the role
     * @throws StorageException if an exception occurs during storage attempt
     */
    public void updateRole(RoleBean role) throws StorageException;

    /**
     * Deletes a role from the system.  This would also remove all memberships in
     * that role.  This should be done very infrequently!
     * @param role the role
     * @throws StorageException if an exception occurs during storage attempt
     */
    public void deleteRole(RoleBean role) throws StorageException;

    /**
     * Returns a list of users that match the given search criteria.
     * @param criteria search criteria
     * @return the found roles
     * @throws StorageException if an exception occurs during storage attempt
     */
    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) throws StorageException;

    /**
     * Grants membership into a role for a user.
     * @param membership the membership
     * @throws StorageException if an exception occurs during storage attempt
     */
    public void createMembership(RoleMembershipBean membership) throws StorageException;

    /**
     * Returns a single membership or null if one does not exist.
     * @param userId the user id
     * @param roleId the role id
     * @param organizationId the organization id
     * @throws StorageException if an exception occurs during storage attempt
     * @return the membership
     */
    public RoleMembershipBean getMembership(String userId, String roleId, String organizationId) throws StorageException;

    /**
     * Deletes a single membership.
     * @param userId the user's id
     * @param roleId the role's id
     * @param organizationId the organization's id
     * @throws StorageException if an exception occurs during storage attempt
     */
    public void deleteMembership(String userId, String roleId, String organizationId) throws StorageException;

    /**
     * Deletes all role memberships for a user in a given organization.
     * @param userId the user's id
     * @param organizationId the organization's id
     * @throws StorageException if an exception occurs during storage attempt
     */
    public void deleteMemberships(String userId, String organizationId) throws StorageException;

    /**
     * Gets all the user's memberships.
     * @param userId the user's id
     * @return set of memberships
     * @throws StorageException if an exception occurs during storage attempt
     */
    public Set<RoleMembershipBean> getUserMemberships(String userId) throws StorageException;

    /**
     * Gets all the user's memberships for the given organization.
     * @param userId the user's id
     * @param organizationId the organization's id
     * @return set of memberships
     * @throws StorageException if an exception occurs during storage attempt
     */
    public Set<RoleMembershipBean> getUserMemberships(String userId, String organizationId) throws StorageException;

    /**
     * Gets all the memberships configured for a particular organization.
     * @param organizationId the organization's id
     * @return set of memberships
     * @throws StorageException if an exception occurs during storage attempt
     */
    public Set<RoleMembershipBean> getOrgMemberships(String organizationId) throws StorageException;

    /**
     * Returns a set of permissions granted to the user due to their role
     * memberships.
     * @param userId the user's id
     * @return set of permissions
     * @throws StorageException if an exception occurs during storage attempt
     */
    public Set<PermissionBean> getPermissions(String userId) throws StorageException;

}
