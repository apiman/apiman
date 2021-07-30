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
import io.apiman.manager.api.beans.policies.UpdatePolicyDefinitionBean;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyFormType;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.IPolicyDefinitionResource;
import io.apiman.manager.api.rest.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.PolicyDefinitionAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.PolicyDefinitionNotFoundException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
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

    private final IStorage storage;
    private final IStorageQuery query;
    private final ISecurityContext securityContext;
    
    /**
     * Constructor.
     */
    @Inject
    public PolicyDefinitionResourceImpl(IStorage storage, IStorageQuery query,
        ISecurityContext securityContext) {
        this.storage = storage;
        this.query = query;
        this.securityContext = securityContext;
    }

    /**
     * @see IPolicyDefinitionResource#list()
     */
    @Override
    public List<PolicyDefinitionSummaryBean> list() {
        // No permission check is needed
        try {
            return query.listPolicyDefinitions();
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IPolicyDefinitionResource#create(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public PolicyDefinitionBean create(PolicyDefinitionBean bean) throws PolicyDefinitionAlreadyExistsException, NotAuthorizedException {
        securityContext.checkAdminPermissions();

        // Auto-generate an ID if one isn't provided.
        if (bean.getId() == null || bean.getId().trim().isEmpty()) {
            bean.setId(BeanUtils.idFromName(bean.getName()));
        } else {
            bean.setId(BeanUtils.idFromName(bean.getId()));
        }
        try {
            storage.beginTx();
            if (storage.getPolicyDefinition(bean.getId()) != null) {
                throw ExceptionFactory.policyDefAlreadyExistsException(bean.getName());
            }
            if (bean.getFormType() == null) {
                bean.setFormType(PolicyFormType.Default);
            }
            // Store/persist the new policyDef
            storage.createPolicyDefinition(bean);
            storage.commitTx();
            return bean;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IPolicyDefinitionResource#get(java.lang.String)
     */
    @Override
    public PolicyDefinitionBean get(String policyDefinitionId) throws PolicyDefinitionNotFoundException {
        // No permission check is needed
        try {
            storage.beginTx();
            PolicyDefinitionBean bean = storage.getPolicyDefinition(policyDefinitionId);
            if (bean == null) {
                throw ExceptionFactory.policyDefNotFoundException(policyDefinitionId);
            }
            storage.commitTx();
            return bean;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IPolicyDefinitionResource#update(java.lang.String, io.apiman.manager.api.beans.policies.UpdatePolicyDefinitionBean)
     */
    @Override
    public void update(String policyDefinitionId, UpdatePolicyDefinitionBean bean)
            throws PolicyDefinitionNotFoundException, NotAuthorizedException {
        securityContext.checkAdminPermissions();

        try {
            storage.beginTx();
            PolicyDefinitionBean pdb = storage.getPolicyDefinition(policyDefinitionId);
            if (pdb == null) {
                throw ExceptionFactory.policyDefNotFoundException(policyDefinitionId);
            }
            if (pdb.getPluginId() != null) {
                throw new SystemErrorException(Messages.i18n.format("CannotUpdatePluginPolicyDef")); //$NON-NLS-1$
            }
            if (bean.getName() != null)
                pdb.setName(bean.getName());
            if (bean.getDescription() != null)
                pdb.setDescription(bean.getDescription());
            if (bean.getIcon() != null)
                pdb.setIcon(bean.getIcon());
            storage.updatePolicyDefinition(pdb);
            storage.commitTx();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see IPolicyDefinitionResource#delete(java.lang.String)
     */
    @Override
    public void delete(String policyDefinitionId) throws PolicyDefinitionNotFoundException,
            NotAuthorizedException {
       securityContext.checkAdminPermissions();

        try {
            storage.beginTx();
            PolicyDefinitionBean pdb = storage.getPolicyDefinition(policyDefinitionId);
            if (pdb == null) {
                throw ExceptionFactory.policyDefNotFoundException(policyDefinitionId);
            }
            if (pdb.getPluginId() != null) {
                throw new SystemErrorException(Messages.i18n.format("CannotDeletePluginPolicyDef")); //$NON-NLS-1$
            }
            storage.deletePolicyDefinition(pdb);
            storage.commitTx();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }
    
}
