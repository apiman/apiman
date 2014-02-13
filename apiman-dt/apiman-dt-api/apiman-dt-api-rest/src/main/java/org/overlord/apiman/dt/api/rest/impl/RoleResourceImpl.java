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

package org.overlord.apiman.dt.api.rest.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.BeanUtils;
import org.overlord.apiman.dt.api.beans.idm.RoleBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.persist.AlreadyExistsException;
import org.overlord.apiman.dt.api.persist.DoesNotExistException;
import org.overlord.apiman.dt.api.persist.IIdmStorage;
import org.overlord.apiman.dt.api.persist.StorageException;
import org.overlord.apiman.dt.api.rest.contract.IRoleResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.RoleAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.RoleNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;
import org.overlord.apiman.dt.api.rest.impl.util.SearchCriteriaUtil;

/**
 * Implementation of the Role API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class RoleResourceImpl implements IRoleResource {
    
    @Inject
    IIdmStorage idmStorage;
    
    /**
     * Constructor.
     */
    public RoleResourceImpl() {
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IRoleResource#create(org.overlord.apiman.dt.api.beans.idm.RoleBean)
     */
    @Override
    public RoleBean create(RoleBean bean) throws RoleAlreadyExistsException {
        bean.setId(BeanUtils.idFromName(bean.getName()));
        try {
            idmStorage.createRole(bean);
            return bean;
        } catch (AlreadyExistsException e) {
            throw RoleAlreadyExistsException.create(bean.getId());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IRoleResource#get(java.lang.String)
     */
    @Override
    public RoleBean get(String roleId) throws RoleNotFoundException {
        try {
            return idmStorage.getRole(roleId);
        } catch (DoesNotExistException e) {
            throw RoleNotFoundException.create(roleId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IRoleResource#update(java.lang.String, org.overlord.apiman.dt.api.beans.idm.RoleBean)
     */
    @Override
    public void update(String roleId, RoleBean bean) throws RoleNotFoundException {
        bean.setId(roleId);
        try {
            idmStorage.updateRole(bean);
        } catch (DoesNotExistException e) {
            throw RoleNotFoundException.create(roleId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IRoleResource#delete(java.lang.String)
     */
    @Override
    public void delete(String roleId) throws RoleNotFoundException {
        RoleBean bean = get(roleId);
        try {
            idmStorage.deleteRole(bean);
        } catch (DoesNotExistException e) {
            throw RoleNotFoundException.create(roleId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IRoleResource#search(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<RoleBean> search(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException {
        try {
            SearchCriteriaUtil.validateSearchCriteria(criteria);
            return idmStorage.findRoles(criteria);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
}
