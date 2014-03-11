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
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.BeanUtils;
import org.overlord.apiman.dt.api.beans.apps.ApplicationBean;
import org.overlord.apiman.dt.api.beans.apps.ApplicationStatus;
import org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean;
import org.overlord.apiman.dt.api.beans.contracts.ContractBean;
import org.overlord.apiman.dt.api.beans.contracts.NewContractBean;
import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.plans.PlanVersionBean;
import org.overlord.apiman.dt.api.beans.services.ServicePlanBean;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ContractSummaryBean;
import org.overlord.apiman.dt.api.persist.AlreadyExistsException;
import org.overlord.apiman.dt.api.persist.DoesNotExistException;
import org.overlord.apiman.dt.api.persist.IIdmStorage;
import org.overlord.apiman.dt.api.persist.IStorage;
import org.overlord.apiman.dt.api.persist.IStorageQuery;
import org.overlord.apiman.dt.api.persist.StorageException;
import org.overlord.apiman.dt.api.rest.contract.IApplicationResource;
import org.overlord.apiman.dt.api.rest.contract.IRoleResource;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ContractAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ContractNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;
import org.overlord.apiman.dt.api.rest.impl.util.ExceptionFactory;
import org.overlord.apiman.dt.api.security.ISecurityContext;

/**
 * Implementation of the Application API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ApplicationResourceImpl implements IApplicationResource {

    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject IIdmStorage idmStorage;
    
    @Inject IUserResource users;
    @Inject IRoleResource roles;
    
    @Inject ISecurityContext securityContext;
    
    /**
     * Constructor.
     */
    public ApplicationResourceImpl() {
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IApplicationResource#create(java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationBean)
     */
    @Override
    public ApplicationBean create(String organizationId, ApplicationBean bean)
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
     * @see org.overlord.apiman.dt.api.rest.contract.IApplicationResource#get(java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationBean get(String organizationId, String applicationId)
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
     * @see org.overlord.apiman.dt.api.rest.contract.IApplicationResource#list(java.lang.String)
     */
    @Override
    public List<ApplicationSummaryBean> list(String organizationId) throws OrganizationNotFoundException,
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
            return query.getApplicationsInOrg(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IApplicationResource#update(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationBean)
     */
    @Override
    public void update(String organizationId, String applicationId, ApplicationBean bean)
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
     * @see org.overlord.apiman.dt.api.rest.contract.IApplicationResource#createVersion(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public ApplicationVersionBean createVersion(String organizationId, String applicationId, ApplicationVersionBean bean)
            throws ApplicationNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ApplicationBean application = storage.get(organizationId, applicationId, ApplicationBean.class);
            bean.setCreatedBy(securityContext.getCurrentUser());
            bean.setCreatedOn(new Date());
            bean.setStatus(ApplicationStatus.Created);
            bean.setApplication(application);
            storage.create(bean);
            return bean;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.applicationNotFoundException(applicationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IApplicationResource#getVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationVersionBean getVersion(String organizationId, String applicationId, String version)
            throws ApplicationVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ApplicationVersionBean applicationVersion = query.getApplicationVersion(organizationId, applicationId, version);
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
     * @see org.overlord.apiman.dt.api.rest.contract.IApplicationResource#updateVersion(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void updateVersion(String organizationId, String applicationId, String version, ApplicationVersionBean bean)
            throws ApplicationVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        // TODO throw error if version is not in the right state
        try {
            ApplicationVersionBean svb = getVersion(organizationId, applicationId, version);
            bean.setId(svb.getId());
            bean.setApplication(svb.getApplication());
            bean.setStatus(ApplicationStatus.Created);
            bean.setPublishedOn(null);
            bean.setRetiredOn(null);
            storage.update(bean);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.applicationNotFoundException(applicationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IApplicationResource#listVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ApplicationVersionBean> listVersions(String organizationId, String applicationId)
            throws ApplicationNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        // Try to get the application first - will throw a ApplicationNotFoundException if not found.
        get(organizationId, applicationId);
        
        try {
            return query.getApplicationVersions(organizationId, applicationId);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.applicationNotFoundException(applicationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IApplicationResource#createContract(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.contracts.NewContractBean)
     */
    @Override
    public ContractBean createContract(String organizationId, String applicationId, String version,
            NewContractBean bean) throws OrganizationNotFoundException, ApplicationNotFoundException,
            ServiceNotFoundException, PlanNotFoundException, ContractAlreadyExistsException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            ApplicationVersionBean avb = query.getApplicationVersion(organizationId, applicationId, version);
            if (avb == null)
                throw ExceptionFactory.applicationNotFoundException(bean.getServiceId());
            ServiceVersionBean svb = query.getServiceVersion(bean.getServiceOrgId(), bean.getServiceId(), bean.getServiceVersion());
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
            PlanVersionBean pvb = query.getPlanVersion(bean.getServiceOrgId(), bean.getPlanId(), planVersion);
            if (pvb == null)
                throw ExceptionFactory.planNotFoundException(bean.getPlanId());
            
            ContractBean contract = new ContractBean();
            contract.setApplication(avb);
            contract.setService(svb);
            contract.setPlan(pvb);
            contract.setCreatedBy(securityContext.getCurrentUser());
            contract.setCreatedOn(new Date());
            storage.create(contract);
            return contract;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IApplicationResource#getContract(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
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
     * @see org.overlord.apiman.dt.api.rest.contract.IApplicationResource#listContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ContractSummaryBean> listContracts(String organizationId, String applicationId, String version)
            throws ApplicationNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.appView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        // Try to get the application first - will throw a ApplicationNotFoundException if not found.
        get(organizationId, applicationId);
        
        try {
            return query.getApplicationContracts(organizationId, applicationId, version);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
}
