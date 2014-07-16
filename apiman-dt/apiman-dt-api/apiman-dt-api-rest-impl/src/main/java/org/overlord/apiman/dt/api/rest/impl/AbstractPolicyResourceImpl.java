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

import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.policies.PolicyBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyType;
import org.overlord.apiman.dt.api.core.IStorage;
import org.overlord.apiman.dt.api.core.IStorageQuery;
import org.overlord.apiman.dt.api.core.exceptions.DoesNotExistException;
import org.overlord.apiman.dt.api.core.exceptions.StorageException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;
import org.overlord.apiman.dt.api.rest.impl.util.ExceptionFactory;
import org.overlord.apiman.dt.api.security.ISecurityContext;

/**
 * Base class for all resource impls that support managing policies.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractPolicyResourceImpl {
    
    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject ISecurityContext securityContext;

    /**
     * Creates a policy for the given entity (supports creating policies for applications,
     * services, and plans).
     * 
     * @param organizationId
     * @param entityId
     * @param entityVersion
     * @param bean
     * @return the stored policy bean (with updated information)
     * @throws NotAuthorizedException
     */
    protected PolicyBean doCreatePolicy(String organizationId, String entityId, String entityVersion,
            PolicyBean bean, PolicyType type) throws PolicyDefinitionNotFoundException {
        if (bean.getDefinition() == null) {
            ExceptionFactory.policyDefNotFoundException("null"); //$NON-NLS-1$
        }
        try {
            PolicyDefinitionBean def = storage.get(bean.getDefinition().getId(), PolicyDefinitionBean.class);
            bean.setDefinition(def);
        } catch (DoesNotExistException e) {
            ExceptionFactory.policyDefNotFoundException(bean.getDefinition().getId());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
        
        try {
            bean.setId(null);
            bean.setCreatedBy(securityContext.getCurrentUser());
            bean.setCreatedOn(new Date());
            bean.setModifiedBy(securityContext.getCurrentUser());
            bean.setModifiedOn(new Date());
            bean.setOrganizationId(organizationId);
            bean.setEntityId(entityId);
            bean.setEntityVersion(entityVersion);
            bean.setType(type);
            storage.create(bean);
            
            return bean;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * Gets a policy by its id.  Also verifies that the policy really does belong to
     * the entity indicated.
     * @param type
     * @param organizationId
     * @param entityId
     * @param entityVersion
     * @param policyId
     * @return a policy bean
     * @throws PolicyNotFoundException
     */
    protected PolicyBean doGetPolicy(PolicyType type, String organizationId, String entityId,
            String entityVersion, long policyId) throws PolicyNotFoundException {
        try {
            PolicyBean policy = storage.get(policyId, PolicyBean.class);
            if (policy.getType() != type) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            if (!policy.getOrganizationId().equals(organizationId)) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            if (!policy.getEntityId().equals(entityId)) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            if (!policy.getEntityVersion().equals(entityVersion)) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            return policy;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.policyNotFoundException(policyId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

}
