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
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.IRoleResource;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.RoleAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.RoleNotFoundException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.util.SearchCriteriaUtil;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implementation of the Role API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class RoleResourceImpl implements IRoleResource {
    
    @Inject
    IStorage storage;
    @Inject
    IStorageQuery query;
    @Inject
    ISecurityContext securityContext;
    
    /**
     * Constructor.
     */
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
            getStorage().beginTx();
            if (getStorage().getRole(role.getId()) != null) {
                throw ExceptionFactory.roleAlreadyExistsException(role.getId());
            }
            getStorage().createRole(role);
            getStorage().commitTx();
            return role;
        } catch (StorageException e) {
            getStorage().rollbackTx();
            throw new SystemErrorException(e);
        }
    }
    // TODO Check
    /**
     * @see IRoleResource#get(java.lang.String)
     */
    @Override
    public RoleBean get(String roleId) throws RoleNotFoundException, NotAuthorizedException {
        try {
            getStorage().beginTx();
            RoleBean role = getRoleFromStorage(roleId);
            return role;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        } finally {
            getStorage().rollbackTx();
        }
    }
    
    /**
     * @see IRoleResource#update(java.lang.String, io.apiman.manager.api.beans.idm.UpdateRoleBean)
     */
    @Override
    public void update(String roleId, UpdateRoleBean bean) throws RoleNotFoundException, NotAuthorizedException {
        securityContext.checkAdminPermissions();

        try {
            getStorage().beginTx();
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
            getStorage().updateRole(role);
            getStorage().commitTx();
        } catch (StorageException e) {
            getStorage().rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    private RoleBean getRoleFromStorage(String roleId) throws StorageException, RoleNotFoundException {
        RoleBean role = getStorage().getRole(roleId);
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
            getStorage().beginTx();
            getStorage().deleteRole(bean);
            getStorage().commitTx();
        } catch (StorageException e) {
            getStorage().rollbackTx();
            throw new SystemErrorException(e);
        }
    }
    // TODO Check
    /**
     * @see IRoleResource#list()
     */
    @Override
    public List<RoleBean> list() throws NotAuthorizedException {
        try {
            SearchCriteriaBean criteria = new SearchCriteriaBean();
            criteria.setOrder("name", true); //$NON-NLS-1$
            return getQuery().findRoles(criteria).getBeans();
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    // TODO Check
    /**
     * @see IRoleResource#search(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<RoleBean> search(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException, NotAuthorizedException {
        try {
            SearchCriteriaUtil.validateSearchCriteria(criteria);
            return getQuery().findRoles(criteria);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @return the securityContext
     */
    public ISecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     * @param securityContext the securityContext to set
     */
    public void setSecurityContext(ISecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    /**
     * @return the storage
     */
    public IStorage getStorage() {
        return storage;
    }

    /**
     * @param storage the storage to set
     */
    public void setStorage(IStorage storage) {
        this.storage = storage;
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
}
