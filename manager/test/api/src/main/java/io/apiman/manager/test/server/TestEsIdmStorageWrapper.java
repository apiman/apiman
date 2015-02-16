/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.test.server;

import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.IIdmStorage;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.util.Set;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;


/**
 * @author eric.wittmann@redhat.com
 */
public class TestEsIdmStorageWrapper implements IIdmStorage {

    private Client esClient;
    private IIdmStorage delegate;

    /**
     * Constructor.
     * @param esClient 
     * @param delegate
     */
    public TestEsIdmStorageWrapper(Client esClient, IIdmStorage delegate) {
        this.esClient = esClient;
        this.delegate = delegate;
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#createUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void createUser(UserBean user) throws StorageException {
        this.delegate.createUser(user);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUser(java.lang.String)
     */
    @Override
    public UserBean getUser(String userId) throws StorageException {
        return this.delegate.getUser(userId);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#updateUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void updateUser(UserBean user) throws StorageException {
        this.delegate.updateUser(user);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#findUsers(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException {
        refresh();
        return this.delegate.findUsers(criteria);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#createRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void createRole(RoleBean role) throws StorageException {
        this.delegate.createRole(role);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getRole(java.lang.String)
     */
    @Override
    public RoleBean getRole(String roleId) throws StorageException {
        return this.delegate.getRole(roleId);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#updateRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void updateRole(RoleBean role) throws StorageException {
        this.delegate.updateRole(role);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#deleteRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void deleteRole(RoleBean role) throws StorageException {
        this.delegate.deleteRole(role);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#findRoles(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) throws StorageException {
        refresh();
        return this.delegate.findRoles(criteria);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#createMembership(io.apiman.manager.api.beans.idm.RoleMembershipBean)
     */
    @Override
    public void createMembership(RoleMembershipBean membership) throws StorageException {
        this.delegate.createMembership(membership);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#deleteMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMembership(String userId, String roleId, String organizationId) throws StorageException {
        this.delegate.deleteMembership(userId, roleId, organizationId);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#deleteMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMemberships(String userId, String organizationId) throws StorageException {
        refresh();
        this.delegate.deleteMemberships(userId, organizationId);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUserMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId) throws StorageException {
        refresh();
        return this.delegate.getUserMemberships(userId);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUserMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId, String organizationId)
            throws StorageException {
        refresh();
        return this.delegate.getUserMemberships(userId);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getOrgMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getOrgMemberships(String organizationId) throws StorageException {
        refresh();
        return this.delegate.getOrgMemberships(organizationId);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getPermissions(java.lang.String)
     */
    @Override
    public Set<PermissionBean> getPermissions(String userId) throws StorageException {
        refresh();
        return this.delegate.getPermissions(userId);
    }

    /**
     * Force a refresh in elasticsearch so that the result of any indexing operations
     * up to this point will be visible to searches.
     */
    private void refresh() {
        try {
            esClient.admin().indices().refresh(new RefreshRequest("apiman_manager")).get(); //$NON-NLS-1$
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
