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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.BeanUtils;
import org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.core.IStorage;
import org.overlord.apiman.dt.api.core.exceptions.AlreadyExistsException;
import org.overlord.apiman.dt.api.core.exceptions.DoesNotExistException;
import org.overlord.apiman.dt.api.core.exceptions.StorageException;
import org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyDefinitionAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;
import org.overlord.apiman.dt.api.rest.impl.util.ExceptionFactory;
import org.overlord.apiman.dt.api.security.ISecurityContext;

/**
 * Implementation of the PolicyDefinition API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class PolicyDefinitionResourceImpl implements IPolicyDefinitionResource {

    @Inject IStorage storage;
    @Inject ISecurityContext securityContext;
    
    /**
     * Constructor.
     */
    public PolicyDefinitionResourceImpl() {
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource#list()
     */
    @Override
    public List<PolicyDefinitionBean> list() throws NotAuthorizedException {
        try {
            SearchCriteriaBean criteria = new SearchCriteriaBean();
            criteria.setOrder("name", true); //$NON-NLS-1$
            criteria.setPage(1);
            criteria.setPageSize(500);
            SearchResultsBean<PolicyDefinitionBean> resultsBean = storage.find(criteria, PolicyDefinitionBean.class);
            return resultsBean.getBeans();
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource#create(org.overlord.apiman.dt.api.beans.orgs.PolicyDefinitionBean)
     */
    @Override
    public PolicyDefinitionBean create(PolicyDefinitionBean bean) throws PolicyDefinitionAlreadyExistsException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        bean.setId(BeanUtils.idFromName(bean.getName()));
        try {
            // Store/persist the new policyDef
            storage.create(bean);
            return bean;
        } catch (AlreadyExistsException e) {
            throw ExceptionFactory.policyDefAlreadyExistsException(bean.getName());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource#get(java.lang.String)
     */
    @Override
    public PolicyDefinitionBean get(String policyDefinitionId) throws PolicyDefinitionNotFoundException, NotAuthorizedException {
        try {
            return storage.get(policyDefinitionId, PolicyDefinitionBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.policyDefNotFoundException(policyDefinitionId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource#update(java.lang.String, org.overlord.apiman.dt.api.beans.orgs.PolicyDefinitionBean)
     */
    @Override
    public void update(String policyDefinitionId, PolicyDefinitionBean bean)
            throws PolicyDefinitionNotFoundException, NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        try {
            bean.setId(policyDefinitionId);
            PolicyDefinitionBean pdb = storage.get(policyDefinitionId, PolicyDefinitionBean.class);
            if (bean.getName() != null)
                pdb.setName(bean.getName());
            if (bean.getDescription() != null)
                pdb.setDescription(bean.getDescription());
            if (bean.getIcon() != null)
                pdb.setIcon(bean.getIcon());
            storage.update(pdb);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.policyDefNotFoundException(policyDefinitionId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource#delete(java.lang.String)
     */
    @Override
    public void delete(String policyDefinitionId) throws PolicyDefinitionNotFoundException,
            NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        try {
            PolicyDefinitionBean pdb = storage.get(policyDefinitionId, PolicyDefinitionBean.class);
            storage.delete(pdb);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.policyDefNotFoundException(policyDefinitionId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
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
