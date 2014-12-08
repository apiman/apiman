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
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.AlreadyExistsException;
import io.apiman.manager.api.core.exceptions.DoesNotExistException;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.contract.IPolicyDefinitionResource;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyDefinitionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implementation of the PolicyDefinition API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class PolicyDefinitionResourceImpl implements IPolicyDefinitionResource {

    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject ISecurityContext securityContext;
    
    /**
     * Constructor.
     */
    public PolicyDefinitionResourceImpl() {
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IPolicyDefinitionResource#list()
     */
    @Override
    public List<PolicyDefinitionBean> list() throws NotAuthorizedException {
        try {
            SearchResultsBean<PolicyDefinitionBean> resultsBean = query.listPolicyDefinitions();
            List<PolicyDefinitionBean> beans = resultsBean.getBeans();
            return beans;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IPolicyDefinitionResource#create(io.apiman.manager.api.beans.orgs.PolicyDefinitionBean)
     */
    @Override
    public PolicyDefinitionBean create(PolicyDefinitionBean bean) throws PolicyDefinitionAlreadyExistsException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        bean.setId(BeanUtils.idFromName(bean.getName()));
        try {
            storage.beginTx();
            // Store/persist the new policyDef
            storage.create(bean);
            storage.commitTx();
            return bean;
        } catch (AlreadyExistsException e) {
            storage.rollbackTx();
            throw ExceptionFactory.policyDefAlreadyExistsException(bean.getName());
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IPolicyDefinitionResource#get(java.lang.String)
     */
    @Override
    public PolicyDefinitionBean get(String policyDefinitionId) throws PolicyDefinitionNotFoundException, NotAuthorizedException {
        try {
            storage.beginTx();
            PolicyDefinitionBean bean = storage.get(policyDefinitionId, PolicyDefinitionBean.class);
            storage.commitTx();
            return bean;
        } catch (DoesNotExistException e) {
            storage.rollbackTx();
            throw ExceptionFactory.policyDefNotFoundException(policyDefinitionId);
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IPolicyDefinitionResource#update(java.lang.String, io.apiman.manager.api.beans.orgs.PolicyDefinitionBean)
     */
    @Override
    public void update(String policyDefinitionId, PolicyDefinitionBean bean)
            throws PolicyDefinitionNotFoundException, NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            bean.setId(policyDefinitionId);
            PolicyDefinitionBean pdb = storage.get(policyDefinitionId, PolicyDefinitionBean.class);
            if (bean.getName() != null)
                pdb.setName(bean.getName());
            if (bean.getDescription() != null)
                pdb.setDescription(bean.getDescription());
            if (bean.getIcon() != null)
                pdb.setIcon(bean.getIcon());
            storage.update(pdb);
            storage.commitTx();
        } catch (DoesNotExistException e) {
            storage.rollbackTx();
            throw ExceptionFactory.policyDefNotFoundException(policyDefinitionId);
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IPolicyDefinitionResource#delete(java.lang.String)
     */
    @Override
    public void delete(String policyDefinitionId) throws PolicyDefinitionNotFoundException,
            NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            PolicyDefinitionBean pdb = storage.get(policyDefinitionId, PolicyDefinitionBean.class);
            storage.delete(pdb);
            storage.commitTx();
        } catch (DoesNotExistException e) {
            storage.rollbackTx();
            throw ExceptionFactory.policyDefNotFoundException(policyDefinitionId);
        } catch (StorageException e) {
            storage.rollbackTx();
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
