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
package io.apiman.manager.api.es;

import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.IIdmStorage;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

/**
 * A git implementation of the IDM storage interface.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class EsIdmStorage implements IIdmStorage {
    
    /**
     * Constructor.
     */
    public EsIdmStorage() {
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#createUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void createUser(UserBean user) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUser(java.lang.String)
     */
    @Override
    public UserBean getUser(String userId) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#updateUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void updateUser(UserBean user) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#findUsers(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#createRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void createRole(RoleBean role) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getRole(java.lang.String)
     */
    @Override
    public RoleBean getRole(String roleId) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#updateRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void updateRole(RoleBean role) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#deleteRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void deleteRole(RoleBean role) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#findRoles(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#createMembership(io.apiman.manager.api.beans.idm.RoleMembershipBean)
     */
    @Override
    public void createMembership(RoleMembershipBean membership) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#deleteMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMembership(String userId, String roleId, String organizationId) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#deleteMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMemberships(String userId, String organizationId) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUserMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUserMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId, String organizationId)
            throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getOrgMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getOrgMemberships(String organizationId) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getPermissions(java.lang.String)
     */
    @Override
    public Set<PermissionBean> getPermissions(String userId) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

}
