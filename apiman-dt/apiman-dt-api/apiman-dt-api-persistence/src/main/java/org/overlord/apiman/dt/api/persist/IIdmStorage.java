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
package org.overlord.apiman.dt.api.persist;

import java.util.Set;

import org.overlord.apiman.dt.api.beans.idm.PermissionBean;
import org.overlord.apiman.dt.api.beans.idm.RoleBean;
import org.overlord.apiman.dt.api.beans.idm.RoleMembershipBean;
import org.overlord.apiman.dt.api.beans.idm.UserBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;

/**
 * Interface to manage roles and users. This is separate from the
 * {@link IStorage} interface so that roles can be stored using a different
 * strategy. An obvious example is that the users and roles may be stored in an
 * LDAP directory while the core apiman data is stored in a database.
 * 
 * @author eric.wittmann@redhat.com
 */
public interface IIdmStorage {
    
    /**
     * Gets a user by id.
     * @param userId
     * @throws StorageException
     * @throws DoesNotExistException
     */
    public UserBean getUser(String userId) throws StorageException, DoesNotExistException;
    
    /**
     * Updates the personal information about a user.  Depending on implementation, this
     * method may not be supported.  For example, if the IDM system being used is read
     * only (perhaps some centrally managed LDAP system).
     * @param user
     * @throws StorageException
     * @throws DoesNotExistException
     */
    public void updateUser(UserBean user) throws StorageException, DoesNotExistException;

    /**
     * Returns a list of users that match the given search criteria.
     * @param criteria
     * @throws StorageException
     */
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException;

    /**
     * Creates a new role in the role storage system.  This is typically done
     * by a super admin of the system, to set up roles and what permissions
     * memberhip in those roles will grant.
     * @param role
     * @throws StorageException
     * @throws AlreadyExistsException
     */
    public void createRole(RoleBean role) throws StorageException, AlreadyExistsException;
    
    /**
     * Gets a role by id.
     * @param roleId
     * @throws StorageException
     * @throws DoesNotExistException
     */
    public RoleBean getRole(String roleId) throws StorageException, DoesNotExistException;

    /**
     * Updates a single role (typically with new permissions).
     * @param role
     * @throws StorageException
     * @throws AlreadyExistsException
     */
    public void updateRole(RoleBean role) throws StorageException, DoesNotExistException;

    /**
     * Deletes a role from the system.  This would also remove all memberships in
     * that role.  This should be done very infrequently!
     * @param role
     * @throws StorageException
     * @throws DoesNotExistException
     */
    public void deleteRole(RoleBean role) throws StorageException, DoesNotExistException;

    /**
     * Returns a list of users that match the given search criteria.
     * @param criteria
     * @throws StorageException
     */
    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) throws StorageException;

    /**
     * Grants membership into a role for a user.
     * @param membership
     * @throws StorageException
     * @throws AlreadyExistsException
     */
    public void createMembership(RoleMembershipBean membership) throws StorageException, AlreadyExistsException;
    
    /**
     * Deletes a single membership.
     * @param userId
     * @param roleId
     * @param organizationId
     * @throws StorageException
     * @throws DoesNotExistException
     */
    public void deleteMembership(String userId, String roleId, String organizationId) throws StorageException, DoesNotExistException;
    
    /**
     * Deletes all role memberships for a user in a given organization.
     * @param userId
     * @param organizationId
     * @throws StorageException
     */
    public void deleteMemberships(String userId, String organizationId) throws StorageException;
    
    /**
     * Gets all the user's memberships.
     * @param userId
     * @throws StorageException
     */
    public Set<RoleMembershipBean> getUserMemberships(String userId) throws StorageException;
    
    /**
     * Gets all the memberships configured for a particular organization.
     * @param organizationId
     * @throws StorageException
     */
    public Set<RoleMembershipBean> getOrgMemberships(String organizationId) throws StorageException;

    /**
     * Returns a set of permissions granted to the user due to their role
     * memberships.
     * @param userId
     * @throws StorageException
     */
    public Set<PermissionBean> getPermissions(String userId) throws StorageException;

}
