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

import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.idm.NewRoleBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.UpdateRoleBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.IRoleResource;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.RoleAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.RoleNotFoundException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.util.RestHelper;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Implementation of the Role API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
@Transactional
public class RoleResourceImpl implements IRoleResource {

    private IStorage storage;
    private IStorageQuery query;
    private ISecurityContext securityContext;
    
    /**
     * Constructor.
     */
    @Inject
    public RoleResourceImpl(IStorage storage, IStorageQuery query, ISecurityContext securityContext) {
        this.storage = storage;
        this.query = query;
        this.securityContext = securityContext;
    }

    public RoleResourceImpl() {
    }

    /**
     * @see IRoleResource#create(io.apiman.manager.api.beans.idm.NewRoleBean)
     */
    @Override
    public RoleBean create(NewRoleBean bean) throws RoleAlreadyExistsException, NotAuthorizedException {
        securityContext.checkAdminPermissions();

        RoleBean role = new RoleBean();
        role.setAutoGrant(bean.getAutoGrant());
        role.setCreatedBy(securityContext.getCurrentUser());
        role.setCreatedOn(new Date());
        role.setDescription(bean.getDescription());
        role.setId(BeanUtils.idFromName(bean.getName()));
        role.setName(bean.getName());
        role.setPermissions(bean.getPermissions());
        try {
            if (storage.getRole(role.getId()) != null) {
                throw ExceptionFactory.roleAlreadyExistsException(role.getId());
            }
            storage.createRole(role);
            return role;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IRoleResource#get(java.lang.String)
     */
    @Override
    public RoleBean get(String roleId) throws RoleNotFoundException {
        // No permission check needed
        try {
            RoleBean role = getRoleFromStorage(roleId);
            // Hide sensitive data and set only needed data for the UI
            return RestHelper.hideSensitiveDataFromRoleBean(securityContext, role);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        } finally {
        }
    }
    
    /**
     * @see IRoleResource#update(java.lang.String, io.apiman.manager.api.beans.idm.UpdateRoleBean)
     */
    @Override
    public void update(String roleId, UpdateRoleBean bean) throws RoleNotFoundException, NotAuthorizedException {
        securityContext.checkAdminPermissions();

        try {
            RoleBean role = getRoleFromStorage(roleId);
            if (bean.getDescription() != null) {
                role.setDescription(bean.getDescription());
            }
            if (bean.getAutoGrant() != null) {
                role.setAutoGrant(bean.getAutoGrant());
            }
            if (bean.getName() != null) {
                role.setName(bean.getName());
            }
            if (bean.getPermissions() != null) {
                role.getPermissions().clear();
                role.getPermissions().addAll(bean.getPermissions());
            }
            storage.updateRole(role);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    private RoleBean getRoleFromStorage(String roleId) throws StorageException, RoleNotFoundException {
        RoleBean role = storage.getRole(roleId);
        if (role == null) {
            throw ExceptionFactory.roleNotFoundException(roleId);
        }
        return role;
    }

    /**
     * @see IRoleResource#delete(java.lang.String)
     */
    @Override
    public void delete(String roleId) throws RoleNotFoundException, NotAuthorizedException {
        securityContext.checkAdminPermissions();

        RoleBean bean = get(roleId);
        try {
            storage.deleteRole(bean);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IRoleResource#list()
     */
    @Override
    public List<RoleBean> list() {
        // No permission check needed
        try {
            SearchCriteriaBean criteria = new SearchCriteriaBean();
            criteria.setOrder("name", true); //$NON-NLS-1$

            // Hide sensitive data and set only needed data for the UI
            if (securityContext.isAdmin()) {
                return query.findRoles(criteria).getBeans();
            } else {
                List<RoleBean> roles = new ArrayList<>();
                for (RoleBean role : query.findRoles(criteria).getBeans()) {
                    roles.add(RestHelper.hideSensitiveDataFromRoleBean(securityContext, role));
                }
                return roles;
            }
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
}
