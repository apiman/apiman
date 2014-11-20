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
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.IIdmStorage;
import io.apiman.manager.api.core.exceptions.AlreadyExistsException;
import io.apiman.manager.api.core.exceptions.DoesNotExistException;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.contract.IRoleResource;
import io.apiman.manager.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.RoleAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.RoleNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.util.ExceptionFactory;
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
    IIdmStorage idmStorage;
    @Inject
    ISecurityContext securityContext;
    
    /**
     * Constructor.
     */
    public RoleResourceImpl() {
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IRoleResource#create(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public RoleBean create(RoleBean bean) throws RoleAlreadyExistsException, NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();

        bean.setId(BeanUtils.idFromName(bean.getName()));
        bean.setCreatedBy(securityContext.getCurrentUser());
        bean.setCreatedOn(new Date());
        try {
            idmStorage.createRole(bean);
            return bean;
        } catch (AlreadyExistsException e) {
            throw ExceptionFactory.roleAlreadyExistsException(bean.getId());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IRoleResource#get(java.lang.String)
     */
    @Override
    public RoleBean get(String roleId) throws RoleNotFoundException, NotAuthorizedException {
        try {
            return idmStorage.getRole(roleId);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.roleNotFoundException(roleId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IRoleResource#update(java.lang.String, io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void update(String roleId, RoleBean bean) throws RoleNotFoundException, NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        try {
            RoleBean role = idmStorage.getRole(roleId);
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
            idmStorage.updateRole(role);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.roleNotFoundException(roleId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IRoleResource#delete(java.lang.String)
     */
    @Override
    public void delete(String roleId) throws RoleNotFoundException, NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        RoleBean bean = get(roleId);
        try {
            idmStorage.deleteRole(bean);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.roleNotFoundException(roleId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IRoleResource#list()
     */
    @Override
    public List<RoleBean> list() throws NotAuthorizedException {
        try {
            SearchCriteriaBean criteria = new SearchCriteriaBean();
            criteria.setOrder("name", true); //$NON-NLS-1$
            return idmStorage.findRoles(criteria).getBeans();
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IRoleResource#search(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<RoleBean> search(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException, NotAuthorizedException {
        try {
            SearchCriteriaUtil.validateSearchCriteria(criteria);
            return idmStorage.findRoles(criteria);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @return the idmStorage
     */
    public IIdmStorage getIdmStorage() {
        return idmStorage;
    }

    /**
     * @param idmStorage the idmStorage to set
     */
    public void setIdmStorage(IIdmStorage idmStorage) {
        this.idmStorage = idmStorage;
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
}
