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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.BeanUtils;
import org.overlord.apiman.dt.api.beans.apps.ApplicationBean;
import org.overlord.apiman.dt.api.beans.apps.ApplicationStatus;
import org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean;
import org.overlord.apiman.dt.api.beans.contracts.ContractBean;
import org.overlord.apiman.dt.api.beans.contracts.NewContractBean;
import org.overlord.apiman.dt.api.beans.idm.GrantRolesBean;
import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.beans.idm.RoleBean;
import org.overlord.apiman.dt.api.beans.idm.RoleMembershipBean;
import org.overlord.apiman.dt.api.beans.idm.UserBean;
import org.overlord.apiman.dt.api.beans.members.MemberBean;
import org.overlord.apiman.dt.api.beans.members.MemberRoleBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.plans.PlanBean;
import org.overlord.apiman.dt.api.beans.plans.PlanStatus;
import org.overlord.apiman.dt.api.beans.plans.PlanVersionBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyType;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaFilterBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.api.beans.services.ServicePlanBean;
import org.overlord.apiman.dt.api.beans.services.ServiceStatus;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ContractSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.PlanSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.PolicyChainSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServicePlanSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.core.IApiKeyGenerator;
import org.overlord.apiman.dt.api.core.IApplicationValidator;
import org.overlord.apiman.dt.api.core.IIdmStorage;
import org.overlord.apiman.dt.api.core.IServiceValidator;
import org.overlord.apiman.dt.api.core.IStorage;
import org.overlord.apiman.dt.api.core.IStorageQuery;
import org.overlord.apiman.dt.api.core.exceptions.AlreadyExistsException;
import org.overlord.apiman.dt.api.core.exceptions.DoesNotExistException;
import org.overlord.apiman.dt.api.core.exceptions.StorageException;
import org.overlord.apiman.dt.api.core.util.PolicyTemplateUtil;
import org.overlord.apiman.dt.api.rest.contract.IOrganizationResource;
import org.overlord.apiman.dt.api.rest.contract.IRoleResource;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ContractAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ContractNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.RoleNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.UserNotFoundException;
import org.overlord.apiman.dt.api.rest.impl.util.ExceptionFactory;
import org.overlord.apiman.dt.api.rest.impl.util.SearchCriteriaUtil;
import org.overlord.apiman.dt.api.security.ISecurityContext;

/**
 * Implementation of the Organization API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class OrganizationResourceImpl implements IOrganizationResource {

    @Inject IStorage storage;
    @Inject IIdmStorage idmStorage;
    @Inject IStorageQuery query;
    
    @Inject IApplicationValidator applicationValidator;
    @Inject IServiceValidator serviceValidator;
    @Inject IApiKeyGenerator apiKeyGenerator;

    @Inject IUserResource users;
    @Inject IRoleResource roles;
    
    @Inject ISecurityContext securityContext;

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
        bean.setCreatedBy(securityContext.getCurrentUser());
        try {
            // Store/persist the new organization
            storage.create(bean);

            // Auto-grant memberships in roles to the creator of the organization
            SearchCriteriaBean criteria = new SearchCriteriaBean();
            criteria.setPage(1);
            criteria.setPageSize(100);
            criteria.addFilter("autoGrant", "true", SearchCriteriaFilterBean.OPERATOR_BOOL_EQ); //$NON-NLS-1$ //$NON-NLS-2$
            List<RoleBean> autoGrantedRoles = idmStorage.findRoles(criteria).getBeans();
            for (RoleBean roleBean : autoGrantedRoles) {
                String currentUser = securityContext.getCurrentUser();
                String orgId = bean.getId();
                RoleMembershipBean membership = RoleMembershipBean.create(currentUser, roleBean.getId(), orgId);
                membership.setCreatedOn(new Date());
                idmStorage.createMembership(membership);
            }
            return bean;
        } catch (AlreadyExistsException e) {
            throw ExceptionFactory.organizationAlreadyExistsException(bean.getName());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#get(java.lang.String)
     */
    @Override
    public OrganizationBean get(String organizationId) throws OrganizationNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.orgView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            return storage.get(organizationId, OrganizationBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.organizationNotFoundException(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#update(java.lang.String, org.overlord.apiman.dt.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void update(String organizationId, OrganizationBean bean)
            throws OrganizationNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.orgEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            bean.setId(organizationId);
            storage.update(bean);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.organizationNotFoundException(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }


    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#create(java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationBean)
     */
    @Override
    public ApplicationBean createApp(String organizationId, ApplicationBean bean)
            throws OrganizationNotFoundException, ApplicationAlreadyExistsException, NotAuthorizedException {
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
            // Store/persist the new application
            storage.create(bean);
            return bean;
        } catch (AlreadyExistsException e) {
            throw ExceptionFactory.applicationAlreadyExistsException(bean.getName());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#get(java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationBean getApp(String organizationId, String applicationId)
            throws ApplicationNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            return storage.get(organizationId, applicationId, ApplicationBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.applicationNotFoundException(applicationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#list(java.lang.String)
     */
    @Override
    public List<ApplicationSummaryBean> listApps(String organizationId) throws OrganizationNotFoundException,
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
            return getQuery().getApplicationsInOrg(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#update(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationBean)
     */
    @Override
    public void updateApp(String organizationId, String applicationId, ApplicationBean bean)
            throws ApplicationNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            bean.setOrganizationId(organizationId);
            bean.setId(applicationId);
            storage.update(bean);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.applicationNotFoundException(applicationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createVersion(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public ApplicationVersionBean createAppVersion(String organizationId, String applicationId, ApplicationVersionBean bean)
            throws ApplicationNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ApplicationBean application = storage.get(organizationId, applicationId, ApplicationBean.class);
            bean.setCreatedBy(securityContext.getCurrentUser());
            bean.setCreatedOn(new Date());
            bean.setModifiedBy(securityContext.getCurrentUser());
            bean.setModifiedOn(new Date());
            bean.setStatus(ApplicationStatus.Created);
            bean.setApplication(application);
            if (getApplicationValidator().isReady(bean)) {
                bean.setStatus(ApplicationStatus.Ready);
            } else {
                bean.setStatus(ApplicationStatus.Created);
            }
            storage.create(bean);
            return bean;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.applicationNotFoundException(applicationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationVersionBean getAppVersion(String organizationId, String applicationId, String version)
            throws ApplicationVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ApplicationVersionBean applicationVersion = getQuery().getApplicationVersion(organizationId, applicationId, version);
            if (applicationVersion == null)
                throw ExceptionFactory.applicationVersionNotFoundException(applicationId, version);
            return applicationVersion;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.applicationNotFoundException(applicationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updateVersion(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void updateAppVersion(String organizationId, String applicationId, String version, ApplicationVersionBean bean)
            throws ApplicationVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ApplicationVersionBean avb = getAppVersion(organizationId, applicationId, version);
            bean.setId(avb.getId());
            bean.setApplication(avb.getApplication());
            bean.setStatus(ApplicationStatus.Created);
            bean.setModifiedBy(securityContext.getCurrentUser());
            bean.setModifiedOn(new Date());
            bean.setPublishedOn(null);
            bean.setRetiredOn(null);
            if (getApplicationValidator().isReady(bean)) {
                bean.setStatus(ApplicationStatus.Ready);
            }
            storage.update(bean);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.applicationNotFoundException(applicationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ApplicationVersionBean> listAppVersions(String organizationId, String applicationId)
            throws ApplicationNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        // Try to get the application first - will throw a ApplicationNotFoundException if not found.
        getApp(organizationId, applicationId);
        
        try {
            return getQuery().getApplicationVersions(organizationId, applicationId);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.applicationNotFoundException(applicationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createContract(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.contracts.NewContractBean)
     */
    @Override
    public ContractBean createContract(String organizationId, String applicationId, String version,
            NewContractBean bean) throws OrganizationNotFoundException, ApplicationNotFoundException,
            ServiceNotFoundException, PlanNotFoundException, ContractAlreadyExistsException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            ApplicationVersionBean avb = getQuery().getApplicationVersion(organizationId, applicationId, version);
            if (avb == null)
                throw ExceptionFactory.applicationNotFoundException(applicationId);
            ServiceVersionBean svb = getQuery().getServiceVersion(bean.getServiceOrgId(), bean.getServiceId(), bean.getServiceVersion());
            if (svb == null)
                throw ExceptionFactory.serviceNotFoundException(bean.getServiceId());
            Set<ServicePlanBean> plans = svb.getPlans();
            String planVersion = null;
            for (ServicePlanBean servicePlanBean : plans) {
                if (servicePlanBean.getPlanId().equals(bean.getPlanId())) {
                    planVersion = servicePlanBean.getVersion();
                }
            }
            if (planVersion == null)
                throw ExceptionFactory.planNotFoundException(bean.getPlanId());
            PlanVersionBean pvb = getQuery().getPlanVersion(bean.getServiceOrgId(), bean.getPlanId(), planVersion);
            if (pvb == null)
                throw ExceptionFactory.planNotFoundException(bean.getPlanId());
            
            ContractBean contract = new ContractBean();
            contract.setApplication(avb);
            contract.setService(svb);
            contract.setPlan(pvb);
            contract.setCreatedBy(securityContext.getCurrentUser());
            contract.setCreatedOn(new Date());
            contract.setKey(getApiKeyGenerator().generate());
            storage.create(contract);
            
            // Update the version with new meta-data (e.g. modified-by)
            try {
                if (getApplicationValidator().isReady(avb)) {
                    avb.setStatus(ApplicationStatus.Ready);
                }
                avb.setModifiedBy(securityContext.getCurrentUser());
                avb.setModifiedOn(new Date());
                storage.update(avb);
            } catch (Exception e) {
                throw new SystemErrorException(e);
            }
            
            return contract;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getContract(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ContractBean getContract(String organizationId, String applicationId, String version,
            Long contractId) throws ApplicationNotFoundException, ContractNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ContractBean contract = storage.get(contractId, ContractBean.class);
            if (contract == null)
                throw ExceptionFactory.contractNotFoundException(contractId);
            return contract;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.contractNotFoundException(contractId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#deleteContract(java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public void deleteContract(String organizationId, String applicationId, String version, Long contractId)
            throws ApplicationNotFoundException, ContractNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ContractBean contract = storage.get(contractId, ContractBean.class);
            if (contract == null)
                throw ExceptionFactory.contractNotFoundException(contractId);
            storage.delete(contract);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.contractNotFoundException(contractId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }        
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ContractSummaryBean> listContracts(String organizationId, String applicationId, String version)
            throws ApplicationNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        // Try to get the application first - will throw a ApplicationNotFoundException if not found.
        getAppVersion(organizationId, applicationId, version);
        
        try {
            return getQuery().getApplicationContracts(organizationId, applicationId, version);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createPolicy(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public PolicyBean createAppPolicy(String organizationId, String applicationId, String version,
            PolicyBean bean) throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            ApplicationVersionBean avb = getQuery().getApplicationVersion(organizationId, applicationId, version);
            if (avb == null)
                throw ExceptionFactory.applicationVersionNotFoundException(applicationId, version);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
        
        return doCreatePolicy(organizationId, applicationId, version, bean, PolicyType.Application);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public PolicyBean getAppPolicy(String organizationId, String applicationId, String version, long policyId)
            throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            ApplicationVersionBean avb = getQuery().getApplicationVersion(organizationId, applicationId, version);
            if (avb == null)
                throw ExceptionFactory.applicationVersionNotFoundException(applicationId, version);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        return doGetPolicy(PolicyType.Application, organizationId, applicationId, version, policyId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updatePolicy(java.lang.String, java.lang.String, java.lang.String, long, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public void updateAppPolicy(String organizationId, String applicationId, String version,
            long policyId, PolicyBean bean) throws OrganizationNotFoundException,
            ApplicationVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        try {
            ApplicationVersionBean avb = getQuery().getApplicationVersion(organizationId, applicationId, version);
            if (avb == null)
                throw ExceptionFactory.applicationVersionNotFoundException(applicationId, version);
            PolicyBean policy = this.storage.get(policyId, PolicyBean.class);
            if (bean.getName() != null)
                policy.setName(bean.getName());
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
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#deletePolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public void deleteAppPolicy(String organizationId, String applicationId, String version, long policyId)
            throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        try {
            ApplicationVersionBean avb = getQuery().getApplicationVersion(organizationId, applicationId, version);
            if (avb == null)
                throw ExceptionFactory.applicationVersionNotFoundException(applicationId, version);
            PolicyBean policy = this.storage.get(policyId, PolicyBean.class);
            this.storage.delete(policy);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.policyNotFoundException(policyId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listPolicies(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<PolicyBean> listAppPolicies(String organizationId, String applicationId, String version)
            throws OrganizationNotFoundException, ApplicationVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Try to get the application first - will throw an exception if not found.
        getAppVersion(organizationId, applicationId, version);

        try {
            return getQuery().getPolicies(organizationId, applicationId, version, PolicyType.Application);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#create(java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceBean)
     */
    @Override
    public ServiceBean createService(String organizationId, ServiceBean bean)
            throws OrganizationNotFoundException, ServiceAlreadyExistsException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
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
            // Store/persist the new service
            storage.create(bean);
            return bean;
        } catch (AlreadyExistsException e) {
            throw ExceptionFactory.serviceAlreadyExistsException(bean.getName());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#get(java.lang.String, java.lang.String)
     */
    @Override
    public ServiceBean getService(String organizationId, String serviceId)
            throws ServiceNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            return storage.get(organizationId, serviceId, ServiceBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#list(java.lang.String)
     */
    @Override
    public List<ServiceSummaryBean> listServices(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.get(organizationId, OrganizationBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.organizationNotFoundException(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        try {
            return getQuery().getServicesInOrg(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#update(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceBean)
     */
    @Override
    public void updateService(String organizationId, String serviceId, ServiceBean bean)
            throws ServiceNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            bean.setOrganizationId(organizationId);
            bean.setId(serviceId);
            storage.update(bean);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createVersion(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceVersionBean)
     */
    @Override
    public ServiceVersionBean createServiceVersion(String organizationId, String serviceId, ServiceVersionBean bean)
            throws ServiceNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ServiceBean service = storage.get(organizationId, serviceId, ServiceBean.class);
            bean.setCreatedBy(securityContext.getCurrentUser());
            bean.setCreatedOn(new Date());
            bean.setModifiedBy(securityContext.getCurrentUser());
            bean.setModifiedOn(new Date());
            bean.setStatus(ServiceStatus.Created);
            bean.setService(service);
            if (getServiceValidator().isReady(bean)) {
                bean.setStatus(ServiceStatus.Ready);
            } else {
                bean.setStatus(ServiceStatus.Created);
            }
            storage.create(bean);
            return bean;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ServiceVersionBean getServiceVersion(String organizationId, String serviceId, String version)
            throws ServiceVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ServiceVersionBean serviceVersion = getQuery().getServiceVersion(organizationId, serviceId, version);
            if (serviceVersion == null)
                throw ExceptionFactory.serviceVersionNotFoundException(serviceId, version);
            return serviceVersion;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updateVersion(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void updateServiceVersion(String organizationId, String serviceId, String version, ServiceVersionBean bean)
            throws ServiceVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ServiceVersionBean svb = getServiceVersion(organizationId, serviceId, version);
            if (svb.getStatus() == ServiceStatus.Published || svb.getStatus() == ServiceStatus.Retired) {
                throw ExceptionFactory.invalidServiceStatusException();
            }
            bean.setId(svb.getId());
            bean.setService(svb.getService());
            bean.setStatus(ServiceStatus.Created);
            bean.setModifiedBy(securityContext.getCurrentUser());
            bean.setModifiedOn(new Date());
            bean.setPublishedOn(null);
            bean.setRetiredOn(null);
            if (getServiceValidator().isReady(bean)) {
                bean.setStatus(ServiceStatus.Ready);
            }
            storage.update(bean);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ServiceVersionBean> listServiceVersions(String organizationId, String serviceId)
            throws ServiceNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        // Try to get the service first - will throw a ServiceNotFoundException if not found.
        getService(organizationId, serviceId);
        
        try {
            return getQuery().getServiceVersions(organizationId, serviceId);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getVersionPlans(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ServicePlanSummaryBean> getServiceVersionPlans(String organizationId, String serviceId,
            String version) throws ServiceVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        // Ensure the version exists first.
        getServiceVersion(organizationId, serviceId, version);
        
        try {
            return getQuery().getServiceVersionPlans(organizationId, serviceId, version);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createPolicy(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public PolicyBean createServicePolicy(String organizationId, String serviceId, String version,
            PolicyBean bean) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            ServiceVersionBean avb = getQuery().getServiceVersion(organizationId, serviceId, version);
            if (avb == null)
                throw ExceptionFactory.serviceVersionNotFoundException(serviceId, version);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
        
        return doCreatePolicy(organizationId, serviceId, version, bean, PolicyType.Service);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public PolicyBean getServicePolicy(String organizationId, String serviceId, String version, long policyId)
            throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            ServiceVersionBean avb = getQuery().getServiceVersion(organizationId, serviceId, version);
            if (avb == null)
                throw ExceptionFactory.serviceVersionNotFoundException(serviceId, version);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        return doGetPolicy(PolicyType.Service, organizationId, serviceId, version, policyId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updatePolicy(java.lang.String, java.lang.String, java.lang.String, long, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public void updateServicePolicy(String organizationId, String serviceId, String version,
            long policyId, PolicyBean bean) throws OrganizationNotFoundException,
            ServiceVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        try {
            ServiceVersionBean avb = getQuery().getServiceVersion(organizationId, serviceId, version);
            if (avb == null)
                throw ExceptionFactory.serviceVersionNotFoundException(serviceId, version);
            PolicyBean policy = this.storage.get(policyId, PolicyBean.class);
            if (bean.getName() != null)
                policy.setName(bean.getName());
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
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#deletePolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public void deleteServicePolicy(String organizationId, String serviceId, String version, long policyId)
            throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        try {
            ServiceVersionBean avb = getQuery().getServiceVersion(organizationId, serviceId, version);
            if (avb == null)
                throw ExceptionFactory.serviceVersionNotFoundException(serviceId, version);
            PolicyBean policy = this.storage.get(policyId, PolicyBean.class);
            this.storage.delete(policy);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.policyNotFoundException(policyId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listPolicies(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<PolicyBean> listServicePolicies(String organizationId, String serviceId, String version)
            throws OrganizationNotFoundException, ServiceVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Try to get the service first - will throw an exception if not found.
        getServiceVersion(organizationId, serviceId, version);

        try {
            return getQuery().getPolicies(organizationId, serviceId, version, PolicyType.Service);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getPolicyChain(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PolicyChainSummaryBean getServicePolicyChain(String organizationId, String serviceId, String version,
            String planId) throws ServiceVersionNotFoundException, PlanNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Try to get the service first - will throw an exception if not found.
        ServiceVersionBean svb = getServiceVersion(organizationId, serviceId, version);

        try {
            String planVersion = null;
            Set<ServicePlanBean> plans = svb.getPlans();
            for (ServicePlanBean servicePlanBean : plans) {
                if (servicePlanBean.getPlanId().equals(planId)) {
                    planVersion = servicePlanBean.getVersion();
                    break;
                }
            }
            if (planVersion == null) {
                throw ExceptionFactory.planNotFoundException(planId);
            }
            List<PolicyBean> servicePolicies = getQuery().getPolicies(organizationId, serviceId, version, PolicyType.Service);
            List<PolicyBean> planPolicies = getQuery().getPolicies(organizationId, planId, planVersion, PolicyType.Plan);
            
            PolicyChainSummaryBean chain = new PolicyChainSummaryBean();
            chain.getPolicies().addAll(planPolicies);
            chain.getPolicies().addAll(servicePolicies);
            return chain;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }


    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#create(java.lang.String, org.overlord.apiman.dt.api.beans.apps.PlanBean)
     */
    @Override
    public PlanBean createPlan(String organizationId, PlanBean bean)
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
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#get(java.lang.String, java.lang.String)
     */
    @Override
    public PlanBean getPlan(String organizationId, String planId)
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
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#list(java.lang.String)
     */
    @Override
    public List<PlanSummaryBean> listPlans(String organizationId) throws OrganizationNotFoundException,
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
            return getQuery().getPlansInOrg(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#update(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.PlanBean)
     */
    @Override
    public void updatePlan(String organizationId, String planId, PlanBean bean)
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
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#search(java.lang.String, org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<PlanBean> searchPlans(String organizationId, SearchCriteriaBean criteria)
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
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createVersion(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.PlanVersionBean)
     */
    @Override
    public PlanVersionBean createPlanVersion(String organizationId, String planId, PlanVersionBean bean)
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
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version)
            throws PlanVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            PlanVersionBean planVersion = getQuery().getPlanVersion(organizationId, planId, version);
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
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updateVersion(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.PlanVersionBean)
     */
    @Override
    public void updatePlanVersion(String organizationId, String planId, String version, PlanVersionBean bean)
            throws PlanVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        // TODO throw error if version is not in the right state
        try {
            PlanVersionBean pvb = getPlanVersion(organizationId, planId, version);
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
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<PlanVersionBean> listPlanVersions(String organizationId, String planId)
            throws PlanNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        // Try to get the plan first - will throw a PlanNotFoundException if not found.
        getPlan(organizationId, planId);
        
        try {
            return getQuery().getPlanVersions(organizationId, planId);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.planNotFoundException(planId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createPolicy(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public PolicyBean createPlanPolicy(String organizationId, String planId, String version,
            PolicyBean bean) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            PlanVersionBean avb = getQuery().getPlanVersion(organizationId, planId, version);
            if (avb == null)
                throw ExceptionFactory.planVersionNotFoundException(planId, version);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
        
        return doCreatePolicy(organizationId, planId, version, bean, PolicyType.Plan);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public PolicyBean getPlanPolicy(String organizationId, String planId, String version, long policyId)
            throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            PlanVersionBean avb = getQuery().getPlanVersion(organizationId, planId, version);
            if (avb == null)
                throw ExceptionFactory.planVersionNotFoundException(planId, version);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        return doGetPolicy(PolicyType.Plan, organizationId, planId, version, policyId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updatePolicy(java.lang.String, java.lang.String, java.lang.String, long, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public void updatePlanPolicy(String organizationId, String planId, String version,
            long policyId, PolicyBean bean) throws OrganizationNotFoundException,
            PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        try {
            PlanVersionBean avb = getQuery().getPlanVersion(organizationId, planId, version);
            if (avb == null)
                throw ExceptionFactory.planVersionNotFoundException(planId, version);
            PolicyBean policy = this.storage.get(policyId, PolicyBean.class);
            if (bean.getName() != null)
                policy.setName(bean.getName());
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
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#deletePolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public void deletePlanPolicy(String organizationId, String planId, String version, long policyId)
            throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        try {
            PlanVersionBean avb = getQuery().getPlanVersion(organizationId, planId, version);
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
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listPolicies(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<PolicyBean> listPlanPolicies(String organizationId, String planId, String version)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Try to get the plan first - will throw an exception if not found.
        getPlanVersion(organizationId, planId, version);

        try {
            return getQuery().getPolicies(organizationId, planId, version, PolicyType.Plan);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }


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
            
            PolicyTemplateUtil.generatePolicyDescription(bean);
            return bean;
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }


    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#grant(java.lang.String, org.overlord.apiman.dt.api.beans.idm.GrantRolesBean)
     */
    @Override
    public void grant(String organizationId, GrantRolesBean bean) throws OrganizationNotFoundException,
            RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.orgEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        // Verify that the references are valid.
        get(organizationId);
        users.get(bean.getUserId());
        for (String roleId : bean.getRoleIds()) {
            roles.get(roleId);
        }

        try {
            for (String roleId : bean.getRoleIds()) {
                RoleMembershipBean membership = RoleMembershipBean.create(bean.getUserId(), roleId, organizationId);
                membership.setCreatedOn(new Date());
                idmStorage.createMembership(membership);
            }
        } catch (AlreadyExistsException e) {
            // Do nothing - re-granting is OK.
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#revoke(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void revoke(String organizationId, String roleId, String userId)
            throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.orgEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        get(organizationId);
        users.get(userId);
        roles.get(roleId);

        try {
            idmStorage.deleteMembership(userId, roleId, organizationId);
        } catch (DoesNotExistException e) {
            // Do nothing - revoking something that doesn't exist is OK.
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#revokeAll(java.lang.String, java.lang.String)
     */
    @Override
    public void revokeAll(String organizationId, String userId) throws OrganizationNotFoundException,
            RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.orgEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        get(organizationId);
        users.get(userId);

        try {
            idmStorage.deleteMemberships(userId, organizationId);
        } catch (DoesNotExistException e) {
            // Do nothing - revoking something that doesn't exist is OK.
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listMembers(java.lang.String)
     */
    @Override
    public List<MemberBean> listMembers(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.orgView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        get(organizationId);

        try {
            Set<RoleMembershipBean> memberships = idmStorage.getOrgMemberships(organizationId);
            TreeMap<String, MemberBean> members = new TreeMap<String, MemberBean>();
            for (RoleMembershipBean membershipBean : memberships) {
                String userId = membershipBean.getUserId();
                MemberBean member = members.get(userId);
                if (member == null) {
                    UserBean user = idmStorage.getUser(userId);
                    member = new MemberBean();
                    member.setEmail(user.getEmail());
                    member.setUserId(userId);
                    member.setUserName(user.getFullName());
                    member.setRoles(new ArrayList<MemberRoleBean>());
                    members.put(userId, member);
                }
                String roleId = membershipBean.getRoleId();
                RoleBean role = idmStorage.getRole(roleId);
                MemberRoleBean mrb = new MemberRoleBean();
                mrb.setRoleId(roleId);
                mrb.setRoleName(role.getName());
                member.getRoles().add(mrb);
                if (member.getJoinedOn() == null || membershipBean.getCreatedOn().compareTo(member.getJoinedOn()) < 0) {
                    member.setJoinedOn(membershipBean.getCreatedOn());
                }
            }
            return new ArrayList<MemberBean>(members.values());
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
            PolicyTemplateUtil.generatePolicyDescription(policy);
            return policy;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.policyNotFoundException(policyId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        } catch (Exception e) {
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
     * @return the users
     */
    public IUserResource getUsers() {
        return users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(IUserResource users) {
        this.users = users;
    }

    /**
     * @return the roles
     */
    public IRoleResource getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(IRoleResource roles) {
        this.roles = roles;
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

    /**
     * @return the applicationValidator
     */
    public IApplicationValidator getApplicationValidator() {
        return applicationValidator;
    }

    /**
     * @param applicationValidator the applicationValidator to set
     */
    public void setApplicationValidator(IApplicationValidator applicationValidator) {
        this.applicationValidator = applicationValidator;
    }

    /**
     * @return the serviceValidator
     */
    public IServiceValidator getServiceValidator() {
        return serviceValidator;
    }

    /**
     * @param serviceValidator the serviceValidator to set
     */
    public void setServiceValidator(IServiceValidator serviceValidator) {
        this.serviceValidator = serviceValidator;
    }

    /**
     * @return the apiKeyGenerator
     */
    public IApiKeyGenerator getApiKeyGenerator() {
        return apiKeyGenerator;
    }

    /**
     * @param apiKeyGenerator the apiKeyGenerator to set
     */
    public void setApiKeyGenerator(IApiKeyGenerator apiKeyGenerator) {
        this.apiKeyGenerator = apiKeyGenerator;
    }
    
}
