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

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.BeanUtils;
import org.overlord.apiman.dt.api.beans.idm.GrantRoleBean;
import org.overlord.apiman.dt.api.beans.idm.RevokeRoleBean;
import org.overlord.apiman.dt.api.beans.idm.RoleMembershipBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.persist.AlreadyExistsException;
import org.overlord.apiman.dt.api.persist.DoesNotExistException;
import org.overlord.apiman.dt.api.persist.IIdmStorage;
import org.overlord.apiman.dt.api.persist.IStorage;
import org.overlord.apiman.dt.api.persist.StorageException;
import org.overlord.apiman.dt.api.rest.contract.IOrganizationResource;
import org.overlord.apiman.dt.api.rest.contract.IRoleResource;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.RoleNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.UserNotFoundException;
import org.overlord.apiman.dt.api.rest.impl.util.SearchCriteriaUtil;

/**
 * Implementation of the Organization API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class OrganizationResourceImpl implements IOrganizationResource {

    @Inject IStorage storage;
    @Inject IIdmStorage idmStorage;
    
    @Inject IUserResource users;
    @Inject IRoleResource roles;
    
    /**
     * Constructor.
     */
    public OrganizationResourceImpl() {
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#create(org.overlord.apiman.dt.api.beans.orgs.OrganizationBean)
     */
    @Override
    public OrganizationBean create(OrganizationBean bean) throws OrganizationAlreadyExistsException {
        bean.setId(BeanUtils.idFromName(bean.getName()));
        bean.setCreatedOn(new Date());
        try {
            storage.create(bean);
            return bean;
        } catch (AlreadyExistsException e) {
            throw OrganizationAlreadyExistsException.create(bean.getName());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#get(java.lang.String)
     */
    @Override
    public OrganizationBean get(String organizationId) throws OrganizationNotFoundException {
        try {
            return storage.get(organizationId, OrganizationBean.class);
        } catch (DoesNotExistException e) {
            throw OrganizationNotFoundException.create(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#search(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<OrganizationBean> search(SearchCriteriaBean criteria) throws InvalidSearchCriteriaException {
        try {
            SearchCriteriaUtil.validateSearchCriteria(criteria);
            return storage.find(criteria, OrganizationBean.class);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#grant(java.lang.String, org.overlord.apiman.dt.api.beans.idm.GrantRoleBean)
     */
    @Override
    public void grant(String organizationId, GrantRoleBean bean) throws OrganizationNotFoundException,
            RoleNotFoundException, UserNotFoundException {
        // Verify that the references are valid.
        get(organizationId);
        users.get(bean.getUserId());
        roles.get(bean.getRoleId());
        
        RoleMembershipBean membership = new RoleMembershipBean();
        membership.setOrganizationId(organizationId);
        membership.setUserId(bean.getUserId());
        membership.setRoleId(bean.getRoleId());
        
        try {
            idmStorage.createMembership(membership);
        } catch (AlreadyExistsException e) {
            // Do nothing - re-granting is OK.
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#revoke(java.lang.String, org.overlord.apiman.dt.api.beans.idm.RevokeRoleBean)
     */
    @Override
    public void revoke(String organizationId, RevokeRoleBean bean) throws OrganizationNotFoundException,
            RoleNotFoundException, UserNotFoundException {
        // Verify that the references are valid.
        get(organizationId);
        users.get(bean.getUserId());
        roles.get(bean.getRoleId());
        
        try {
            idmStorage.deleteMembership(organizationId, bean.getUserId(), bean.getRoleId());
        } catch (DoesNotExistException e) {
            // Do nothing - revoking something that doesn't exist is OK.
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
}
