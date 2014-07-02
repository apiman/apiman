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
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.BeanUtils;
import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.plans.PlanBean;
import org.overlord.apiman.dt.api.beans.plans.PlanStatus;
import org.overlord.apiman.dt.api.beans.plans.PlanVersionBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyType;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.beans.summary.PlanSummaryBean;
import org.overlord.apiman.dt.api.core.IIdmStorage;
import org.overlord.apiman.dt.api.core.exceptions.AlreadyExistsException;
import org.overlord.apiman.dt.api.core.exceptions.DoesNotExistException;
import org.overlord.apiman.dt.api.core.exceptions.StorageException;
import org.overlord.apiman.dt.api.rest.contract.IPlanResource;
import org.overlord.apiman.dt.api.rest.contract.IRoleResource;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;
import org.overlord.apiman.dt.api.rest.impl.util.ExceptionFactory;
import org.overlord.apiman.dt.api.rest.impl.util.SearchCriteriaUtil;

/**
 * Implementation of the Plan API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class PlanResourceImpl extends AbstractPolicyResourceImpl implements IPlanResource {

    @Inject IIdmStorage idmStorage;
    
    @Inject IUserResource users;
    @Inject IRoleResource roles;
    
    /**
     * Constructor.
     */
    public PlanResourceImpl() {
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#create(java.lang.String, org.overlord.apiman.dt.api.beans.apps.PlanBean)
     */
    @Override
    public PlanBean create(String organizationId, PlanBean bean)
            throws OrganizationNotFoundException, PlanAlreadyExistsException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            storage.get(organizationId, OrganizationBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.organizationNotFoundException(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        String currentUser = securityContext.getCurrentUser();

        bean.setOrganizationId(organizationId);
        bean.setId(BeanUtils.idFromName(bean.getName()));
        bean.setCreatedOn(new Date());
        bean.setCreatedBy(currentUser);
        try {
            // Store/persist the new plan
            storage.create(bean);
            return bean;
        } catch (AlreadyExistsException e) {
            throw ExceptionFactory.planAlreadyExistsException(bean.getName());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#get(java.lang.String, java.lang.String)
     */
    @Override
    public PlanBean get(String organizationId, String planId)
            throws PlanNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            return storage.get(organizationId, planId, PlanBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.planNotFoundException(planId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#list(java.lang.String)
     */
    @Override
    public List<PlanSummaryBean> list(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.get(organizationId, OrganizationBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.organizationNotFoundException(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        try {
            return query.getPlansInOrg(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#update(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.PlanBean)
     */
    @Override
    public void update(String organizationId, String planId, PlanBean bean)
            throws PlanNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            bean.setOrganizationId(organizationId);
            bean.setId(planId);
            storage.update(bean);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.planNotFoundException(planId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#search(java.lang.String, org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<PlanBean> search(String organizationId, SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        try {
            storage.get(organizationId, OrganizationBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.organizationNotFoundException(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        SearchCriteriaUtil.validateSearchCriteria(criteria);

        // TODO only return plans that the user is permitted to see
        try {
            return storage.find(criteria, PlanBean.class);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#createVersion(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.PlanVersionBean)
     */
    @Override
    public PlanVersionBean createVersion(String organizationId, String planId, PlanVersionBean bean)
            throws PlanNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            PlanBean plan = storage.get(organizationId, planId, PlanBean.class);
            bean.setCreatedBy(securityContext.getCurrentUser());
            bean.setCreatedOn(new Date());
            bean.setModifiedBy(securityContext.getCurrentUser());
            bean.setModifiedOn(new Date());
            bean.setStatus(PlanStatus.Created);
            bean.setPlan(plan);
            storage.create(bean);
            return bean;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.planNotFoundException(planId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#getVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PlanVersionBean getVersion(String organizationId, String planId, String version)
            throws PlanVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            PlanVersionBean planVersion = query.getPlanVersion(organizationId, planId, version);
            if (planVersion == null)
                throw ExceptionFactory.planVersionNotFoundException(planId, version);
            return planVersion;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.planNotFoundException(planId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#updateVersion(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.PlanVersionBean)
     */
    @Override
    public void updateVersion(String organizationId, String planId, String version, PlanVersionBean bean)
            throws PlanVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        // TODO throw error if version is not in the right state
        try {
            PlanVersionBean pvb = getVersion(organizationId, planId, version);
            bean.setId(pvb.getId());
            bean.setPlan(pvb.getPlan());
            bean.setStatus(PlanStatus.Created);
            bean.setModifiedBy(securityContext.getCurrentUser());
            bean.setModifiedOn(new Date());
            bean.setLockedOn(null);
            storage.update(bean);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.planNotFoundException(planId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#listVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<PlanVersionBean> listVersions(String organizationId, String planId)
            throws PlanNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        // Try to get the plan first - will throw a PlanNotFoundException if not found.
        get(organizationId, planId);
        
        try {
            return query.getPlanVersions(organizationId, planId);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.planNotFoundException(planId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#createPolicy(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public PolicyBean createPolicy(String organizationId, String planId, String version,
            PolicyBean bean) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            PlanVersionBean avb = query.getPlanVersion(organizationId, planId, version);
            if (avb == null)
                throw ExceptionFactory.planVersionNotFoundException(planId, version);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
        
        return doCreatePolicy(organizationId, planId, version, bean, PolicyType.Plan);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#getPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public PolicyBean getPolicy(String organizationId, String planId, String version, long policyId)
            throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            PlanVersionBean avb = query.getPlanVersion(organizationId, planId, version);
            if (avb == null)
                throw ExceptionFactory.planVersionNotFoundException(planId, version);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        return doGetPolicy(PolicyType.Plan, organizationId, planId, version, policyId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#updatePolicy(java.lang.String, java.lang.String, java.lang.String, long, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public void updatePolicy(String organizationId, String planId, String version,
            long policyId, PolicyBean bean) throws OrganizationNotFoundException,
            PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        try {
            PlanVersionBean avb = query.getPlanVersion(organizationId, planId, version);
            if (avb == null)
                throw ExceptionFactory.planVersionNotFoundException(planId, version);
            PolicyBean policy = this.storage.get(policyId, PolicyBean.class);
            if (bean.getName() != null)
                policy.setName(bean.getName());
            if (bean.getDescription() != null)
                policy.setDescription(bean.getDescription());
            if (bean.getConfiguration() != null)
                policy.setConfiguration(bean.getConfiguration());
            policy.setModifiedOn(new Date());
            policy.setModifiedBy(this.securityContext.getCurrentUser());
            this.storage.update(policy);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.policyNotFoundException(policyId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#deletePolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public void deletePolicy(String organizationId, String planId, String version, long policyId)
            throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        try {
            PlanVersionBean avb = query.getPlanVersion(organizationId, planId, version);
            if (avb == null)
                throw ExceptionFactory.planVersionNotFoundException(planId, version);
            PolicyBean policy = this.storage.get(policyId, PolicyBean.class);
            this.storage.delete(policy);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.policyNotFoundException(policyId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPlanResource#listPolicies(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<PolicyBean> listPolicies(String organizationId, String planId, String version)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Try to get the plan first - will throw an exception if not found.
        getVersion(organizationId, planId, version);

        try {
            return query.getPolicies(organizationId, planId, version, PolicyType.Plan);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

}
