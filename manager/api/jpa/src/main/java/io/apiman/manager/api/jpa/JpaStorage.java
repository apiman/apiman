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
package io.apiman.manager.api.jpa;

import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntityType;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.gateways.GatewayType;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceGatewayBean;
import io.apiman.manager.api.beans.services.ServicePlanBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.summary.ApiEntryBean;
import io.apiman.manager.api.beans.summary.ApiRegistryBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.ApplicationVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.beans.summary.ServicePlanSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceVersionSummaryBean;
import io.apiman.manager.api.core.IApiKeyGenerator;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.AlreadyExistsException;
import io.apiman.manager.api.core.exceptions.DoesNotExistException;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.util.PolicyTemplateUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JPA implementation of the storage interface.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class JpaStorage extends AbstractJpaStorage implements IStorage, IStorageQuery, IApiKeyGenerator {

    private static Logger logger = LoggerFactory.getLogger(JpaStorage.class);

    /**
     * Constructor.
     */
    public JpaStorage() {
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#beginTx()
     */
    @Override
    public void beginTx() throws StorageException {
        super.beginTx();
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#commitTx()
     */
    @Override
    public void commitTx() throws StorageException {
        super.commitTx();
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#rollbackTx()
     */
    @Override
    public void rollbackTx() {
        super.rollbackTx();
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createApplication(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void createApplication(ApplicationBean application) throws StorageException,
            AlreadyExistsException {
        super.create(application);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createApplicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void createApplicationVersion(ApplicationVersionBean version) throws StorageException,
            AlreadyExistsException {
        super.create(version);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void createContract(ContractBean contract) throws StorageException, AlreadyExistsException {
        super.create(contract);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void createGateway(GatewayBean gateway) throws StorageException, AlreadyExistsException {
        super.create(gateway);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void createOrganization(OrganizationBean organization) throws StorageException,
            AlreadyExistsException {
        super.create(organization);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createPlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void createPlan(PlanBean plan) throws StorageException, AlreadyExistsException {
        super.create(plan);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createPlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void createPlanVersion(PlanVersionBean version) throws StorageException, AlreadyExistsException {
        super.create(version);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void createPolicy(PolicyBean policy) throws StorageException, AlreadyExistsException {
        super.create(policy);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createPolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void createPolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException,
            AlreadyExistsException {
        super.create(policyDef);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void createRole(RoleBean role) throws StorageException, AlreadyExistsException {
        super.create(role);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createService(io.apiman.manager.api.beans.services.ServiceBean)
     */
    @Override
    public void createService(ServiceBean service) throws StorageException, AlreadyExistsException {
        super.create(service);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createServiceVersion(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void createServiceVersion(ServiceVersionBean version) throws StorageException,
            AlreadyExistsException {
        super.create(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApplication(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void updateApplication(ApplicationBean application) throws StorageException, DoesNotExistException {
        super.update(application);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#updateApplicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void updateApplicationVersion(ApplicationVersionBean version) throws StorageException,
            DoesNotExistException {
        super.update(version);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#updateContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void updateContract(ContractBean contract) throws StorageException, DoesNotExistException {
        super.update(contract);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#updateGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void updateGateway(GatewayBean gateway) throws StorageException, DoesNotExistException {
        super.update(gateway);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#updateOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void updateOrganization(OrganizationBean organization) throws StorageException,
            DoesNotExistException {
        super.update(organization);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void updatePlan(PlanBean plan) throws StorageException, DoesNotExistException {
        super.update(plan);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void updatePlanVersion(PlanVersionBean version) throws StorageException, DoesNotExistException {
        super.update(version);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#updatePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void updatePolicy(PolicyBean policy) throws StorageException, DoesNotExistException {
        super.update(policy);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#updatePolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void updatePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException,
            DoesNotExistException {
        super.update(policyDef);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#updateRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void updateRole(RoleBean role) throws StorageException, DoesNotExistException {
        super.update(role);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#updateService(io.apiman.manager.api.beans.services.ServiceBean)
     */
    @Override
    public void updateService(ServiceBean service) throws StorageException, DoesNotExistException {
        super.update(service);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#updateServiceVersion(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void updateServiceVersion(ServiceVersionBean version) throws StorageException,
            DoesNotExistException {
        super.update(version);
    }
    

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void deleteOrganization(OrganizationBean organization) throws StorageException,
            DoesNotExistException {
        super.delete(organization);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApplication(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void deleteApplication(ApplicationBean application) throws StorageException, DoesNotExistException {
        super.delete(application);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApplicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void deleteApplicationVersion(ApplicationVersionBean version) throws StorageException,
            DoesNotExistException {
        super.delete(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void deleteContract(ContractBean contract) throws StorageException, DoesNotExistException {
        super.delete(contract);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteService(io.apiman.manager.api.beans.services.ServiceBean)
     */
    @Override
    public void deleteService(ServiceBean service) throws StorageException, DoesNotExistException {
        super.delete(service);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteServiceVersion(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void deleteServiceVersion(ServiceVersionBean version) throws StorageException,
            DoesNotExistException {
        super.delete(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void deletePlan(PlanBean plan) throws StorageException, DoesNotExistException {
        super.delete(plan);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void deletePlanVersion(PlanVersionBean version) throws StorageException, DoesNotExistException {
        super.delete(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void deletePolicy(PolicyBean policy) throws StorageException, DoesNotExistException {
        super.delete(policy);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void deleteGateway(GatewayBean gateway) throws StorageException, DoesNotExistException {
        super.delete(gateway);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void deletePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException,
            DoesNotExistException {
        super.delete(policyDef);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void deleteRole(RoleBean role) throws StorageException, DoesNotExistException {
        super.delete(role);
    }
    

    /**
     * @see io.apiman.manager.api.core.IStorage#getOrganization(java.lang.String)
     */
    @Override
    public OrganizationBean getOrganization(String id) throws StorageException, DoesNotExistException {
        return super.get(id, OrganizationBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApplication(java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationBean getApplication(String organizationId, String id) throws StorageException,
            DoesNotExistException {
        return super.get(organizationId, id, ApplicationBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getContract(java.lang.Long)
     */
    @Override
    public ContractBean getContract(Long id) throws StorageException, DoesNotExistException {
        return super.get(id, ContractBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getService(java.lang.String, java.lang.String)
     */
    @Override
    public ServiceBean getService(String organizationId, String id) throws StorageException,
            DoesNotExistException {
        return super.get(organizationId, id, ServiceBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlan(java.lang.String, java.lang.String)
     */
    @Override
    public PlanBean getPlan(String organizationId, String id) throws StorageException, DoesNotExistException {
        return super.get(organizationId, id, PlanBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPolicy(java.lang.Long)
     */
    @Override
    public PolicyBean getPolicy(Long id) throws StorageException, DoesNotExistException {
        return super.get(id, PolicyBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getGateway(java.lang.String)
     */
    @Override
    public GatewayBean getGateway(String id) throws StorageException, DoesNotExistException {
        return super.get(id, GatewayBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPolicyDefinition(java.lang.String)
     */
    @Override
    public PolicyDefinitionBean getPolicyDefinition(String id) throws StorageException, DoesNotExistException {
        return super.get(id, PolicyDefinitionBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getRole(java.lang.String)
     */
    @Override
    public RoleBean getRole(String id) throws StorageException, DoesNotExistException {
        return super.get(id, RoleBean.class);
    }

    /**
     * @see io.apiman.manager.api.jpa.AbstractJpaStorage#find(io.apiman.manager.api.beans.search.SearchCriteriaBean, java.lang.Class)
     */
    @Override
    protected <T> SearchResultsBean<T> find(SearchCriteriaBean criteria, Class<T> type) throws StorageException {
        beginTx();
        try {
            SearchResultsBean<T> rval = super.find(criteria, type);
            return rval;
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findOrganizations(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<OrganizationSummaryBean> findOrganizations(SearchCriteriaBean criteria)
            throws StorageException {
        SearchResultsBean<OrganizationBean> orgs = find(criteria, OrganizationBean.class);
        SearchResultsBean<OrganizationSummaryBean> rval = new SearchResultsBean<OrganizationSummaryBean>();
        rval.setTotalSize(orgs.getTotalSize());
        List<OrganizationBean> beans = orgs.getBeans();
        for (OrganizationBean bean : beans) {
            OrganizationSummaryBean osb = new OrganizationSummaryBean();
            osb.setId(bean.getId());
            osb.setName(bean.getName());
            osb.setDescription(bean.getDescription());
            rval.getBeans().add(osb);
        }
        return rval;
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findApplications(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ApplicationSummaryBean> findApplications(SearchCriteriaBean criteria)
            throws StorageException {
        SearchResultsBean<ApplicationBean> result = find(criteria, ApplicationBean.class);
        
        SearchResultsBean<ApplicationSummaryBean> rval = new SearchResultsBean<ApplicationSummaryBean>();
        rval.setTotalSize(result.getTotalSize());
        List<ApplicationBean> beans = result.getBeans();
        rval.setBeans(new ArrayList<ApplicationSummaryBean>(beans.size()));
        for (ApplicationBean application : beans) {
            ApplicationSummaryBean summary = new ApplicationSummaryBean();
            OrganizationBean organization = application.getOrganization();
            summary.setId(application.getId());
            summary.setName(application.getName());
            summary.setDescription(application.getDescription());
            // TODO find the number of contracts - probably need native SQL for that
            summary.setNumContracts(0);
            summary.setOrganizationId(application.getOrganization().getId());
            summary.setOrganizationName(organization.getName());
            rval.getBeans().add(summary);
        }
        return rval;
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findServices(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ServiceSummaryBean> findServices(SearchCriteriaBean criteria)
            throws StorageException {
        SearchResultsBean<ServiceBean> result = find(criteria, ServiceBean.class);
        SearchResultsBean<ServiceSummaryBean> rval = new SearchResultsBean<ServiceSummaryBean>();
        rval.setTotalSize(result.getTotalSize());
        List<ServiceBean> beans = result.getBeans();
        rval.setBeans(new ArrayList<ServiceSummaryBean>(beans.size()));
        for (ServiceBean service : beans) {
            ServiceSummaryBean summary = new ServiceSummaryBean();
            OrganizationBean organization = service.getOrganization();
            summary.setId(service.getId());
            summary.setName(service.getName());
            summary.setDescription(service.getDescription());
            summary.setCreatedOn(service.getCreatedOn());
            summary.setOrganizationId(service.getOrganization().getId());
            summary.setOrganizationName(organization.getName());
            rval.getBeans().add(summary);
        }
        return rval;
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findPlans(java.lang.String, io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<PlanSummaryBean> findPlans(String organizationId, SearchCriteriaBean criteria)
            throws StorageException {
        
        criteria.addFilter("organization.id", organizationId, SearchCriteriaFilterBean.OPERATOR_EQ); //$NON-NLS-1$
        SearchResultsBean<PlanBean> result = find(criteria, PlanBean.class);
        SearchResultsBean<PlanSummaryBean> rval = new SearchResultsBean<PlanSummaryBean>();
        rval.setTotalSize(result.getTotalSize());
        List<PlanBean> plans = result.getBeans();
        rval.setBeans(new ArrayList<PlanSummaryBean>(plans.size()));
        for (PlanBean plan : plans) {
            PlanSummaryBean summary = new PlanSummaryBean();
            OrganizationBean organization = plan.getOrganization();
            summary.setId(plan.getId());
            summary.setName(plan.getName());
            summary.setDescription(plan.getDescription());
            summary.setOrganizationId(plan.getOrganization().getId());
            summary.setOrganizationName(organization.getName());
            rval.getBeans().add(summary);
        }
        return rval;        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createAuditEntry(io.apiman.manager.api.beans.audit.AuditEntryBean)
     */
    @Override
    public void createAuditEntry(AuditEntryBean entry) throws StorageException {
        super.create(entry);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#auditEntity(java.lang.String, java.lang.String, java.lang.String, java.lang.Class, io.apiman.manager.api.beans.search.PagingBean)
     */
    @Override
    public <T> SearchResultsBean<AuditEntryBean> auditEntity(String organizationId, String entityId, String entityVersion,
            Class<T> type, PagingBean paging) throws StorageException {
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        if (paging != null) {
            criteria.setPaging(paging);
        } else {
            criteria.setPage(1);
            criteria.setPageSize(20);
        }
        criteria.setOrder("id", false); //$NON-NLS-1$
        if (organizationId != null) {
            criteria.addFilter("organizationId", organizationId, SearchCriteriaFilterBean.OPERATOR_EQ); //$NON-NLS-1$
        }
        if (entityId != null) {
            criteria.addFilter("entityId", entityId, SearchCriteriaFilterBean.OPERATOR_EQ); //$NON-NLS-1$
        }
        if (entityVersion != null) {
            criteria.addFilter("entityVersion", entityVersion, SearchCriteriaFilterBean.OPERATOR_EQ); //$NON-NLS-1$
        }
        if (type != null) {
            AuditEntityType entityType = null;
            if (type == OrganizationBean.class) {
                entityType = AuditEntityType.Organization;
            } else if (type == ApplicationBean.class) { 
                entityType = AuditEntityType.Application;
            } else if (type == ServiceBean.class) {
                entityType = AuditEntityType.Service;
            } else if (type == PlanBean.class) {
                entityType = AuditEntityType.Plan;
            }
            if (entityType != null) {
                criteria.addFilter("entityType", entityType.name(), SearchCriteriaFilterBean.OPERATOR_EQ); //$NON-NLS-1$
            }
        }
        
        return find(criteria, AuditEntryBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#auditUser(java.lang.String, io.apiman.manager.api.beans.search.PagingBean)
     */
    @Override
    public <T> SearchResultsBean<AuditEntryBean> auditUser(String userId, PagingBean paging)
            throws StorageException {
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        if (paging != null) {
            criteria.setPaging(paging);
        } else {
            criteria.setPage(1);
            criteria.setPageSize(20);
        }
        criteria.setOrder("when", false); //$NON-NLS-1$
        if (userId != null) {
            criteria.addFilter("who", userId, SearchCriteriaFilterBean.OPERATOR_EQ); //$NON-NLS-1$
        }
        
        return find(criteria, AuditEntryBean.class);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listGateways()
     */
    @Override
    public List<GatewaySummaryBean> listGateways() throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            
            @SuppressWarnings("nls")
            String sql = 
                    "SELECT g.id, g.name, g.description, g.type" + 
                    "  FROM gateways g" + 
                    " ORDER BY g.name ASC";
            Query query = entityManager.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
            List<Object[]> rows = (List<Object[]>) query.getResultList();
            List<GatewaySummaryBean> gateways = new ArrayList<GatewaySummaryBean>(rows.size());
            for (Object [] row : rows) {
                GatewaySummaryBean gateway = new GatewaySummaryBean();
                gateway.setId(String.valueOf(row[0]));
                gateway.setName(String.valueOf(row[1]));
                gateway.setDescription(String.valueOf(row[2]));
                gateway.setType(GatewayType.valueOf(String.valueOf(row[3])));
                gateways.add(gateway);
            }
            return gateways;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPolicyDefinitions()
     */
    @Override
    public List<PolicyDefinitionSummaryBean> listPolicyDefinitions() throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            
            @SuppressWarnings("nls")
            String sql = 
                    "SELECT pd.id, pd.policyImpl, pd.name, pd.description, pd.icon" + 
                    "  FROM policydefs pd" + 
                    " ORDER BY pd.name ASC";
            Query query = entityManager.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
            List<Object[]> rows = (List<Object[]>) query.getResultList();
            List<PolicyDefinitionSummaryBean> gateways = new ArrayList<PolicyDefinitionSummaryBean>(rows.size());
            for (Object [] row : rows) {
                PolicyDefinitionSummaryBean gateway = new PolicyDefinitionSummaryBean();
                gateway.setId(String.valueOf(row[0]));
                gateway.setPolicyImpl(String.valueOf(row[1]));
                gateway.setName(String.valueOf(row[2]));
                gateway.setDescription(String.valueOf(row[3]));
                gateway.setIcon(String.valueOf(row[4]));
                gateways.add(gateway);
            }
            return gateways;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getOrgs(java.util.Set)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<OrganizationSummaryBean> getOrgs(Set<String> orgIds) throws StorageException {
        List<OrganizationSummaryBean> orgs = new ArrayList<OrganizationSummaryBean>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT o from OrganizationBean o WHERE o.id IN :orgs ORDER BY o.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$
            List<OrganizationBean> qr = (List<OrganizationBean>) query.getResultList();
            for (OrganizationBean bean : qr) {
                OrganizationSummaryBean summary = new OrganizationSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                orgs.add(summary);
            }
            return orgs;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationsInOrg(java.lang.String)
     */
    @Override
    public List<ApplicationSummaryBean> getApplicationsInOrg(String orgId) throws StorageException {
        Set<String> orgIds = new HashSet<String>();
        orgIds.add(orgId);
        return getApplicationsInOrgs(orgIds);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationsInOrgs(java.util.Set)
     */
    @Override
    public List<ApplicationSummaryBean> getApplicationsInOrgs(Set<String> orgIds) throws StorageException {
        List<ApplicationSummaryBean> rval = new ArrayList<ApplicationSummaryBean>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT a FROM ApplicationBean a JOIN a.organization o WHERE o.id IN :orgs ORDER BY a.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<ApplicationBean> qr = (List<ApplicationBean>) query.getResultList();
            for (ApplicationBean bean : qr) {
                ApplicationSummaryBean summary = new ApplicationSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                // TODO find the number of contracts - probably need a native SQL query to pull that together
                summary.setNumContracts(0);
                OrganizationBean org = bean.getOrganization();
                summary.setOrganizationId(org.getId());
                summary.setOrganizationName(org.getName());
                rval.add(summary);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServicesInOrg(java.lang.String)
     */
    @Override
    public List<ServiceSummaryBean> getServicesInOrg(String orgId) throws StorageException {
        Set<String> orgIds = new HashSet<String>();
        orgIds.add(orgId);
        return getServicesInOrgs(orgIds);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServicesInOrgs(java.util.Set)
     */
    @Override
    public List<ServiceSummaryBean> getServicesInOrgs(Set<String> orgIds) throws StorageException {
        List<ServiceSummaryBean> rval = new ArrayList<ServiceSummaryBean>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT s FROM ServiceBean s JOIN s.organization o WHERE o.id IN :orgs ORDER BY s.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<ServiceBean> qr = (List<ServiceBean>) query.getResultList();
            for (ServiceBean bean : qr) {
                ServiceSummaryBean summary = new ServiceSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                summary.setCreatedOn(bean.getCreatedOn());
                OrganizationBean org = bean.getOrganization();
                summary.setOrganizationId(org.getId());
                summary.setOrganizationName(org.getName());
                rval.add(summary);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#getServiceVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ServiceVersionBean getServiceVersion(String orgId, String serviceId, String version)
            throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT v from ServiceVersionBean v JOIN v.service s JOIN s.organization o WHERE o.id = :orgId AND s.id = :serviceId AND v.version = :version"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("serviceId", serviceId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$
            
            return (ServiceVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServiceVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ServiceVersionSummaryBean> getServiceVersions(String orgId, String serviceId)
            throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            @SuppressWarnings("nls")
            String jpql = 
                      "SELECT v "
                    + "  FROM ServiceVersionBean v"
                    + "  JOIN v.service s"
                    + "  JOIN s.organization o"
                    + " WHERE o.id = :orgId"
                    + "  AND s.id = :serviceId"
                    + " ORDER BY v.id DESC";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("serviceId", serviceId); //$NON-NLS-1$
            
            List<ServiceVersionBean> serviceVersions = (List<ServiceVersionBean>) query.getResultList();
            List<ServiceVersionSummaryBean> rval = new ArrayList<ServiceVersionSummaryBean>(serviceVersions.size());
            for (ServiceVersionBean serviceVersion : serviceVersions) {
                ServiceVersionSummaryBean svsb = new ServiceVersionSummaryBean();
                svsb.setOrganizationId(serviceVersion.getService().getOrganization().getId());
                svsb.setOrganizationName(serviceVersion.getService().getOrganization().getName());
                svsb.setId(serviceVersion.getService().getId());
                svsb.setName(serviceVersion.getService().getName());
                svsb.setDescription(serviceVersion.getService().getDescription());
                svsb.setVersion(serviceVersion.getVersion());
                svsb.setStatus(serviceVersion.getStatus());
                rval.add(svsb);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServiceVersionPlans(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ServicePlanSummaryBean> getServiceVersionPlans(String organizationId, String serviceId,
            String version) throws StorageException {
        List<ServicePlanSummaryBean> plans = new ArrayList<ServicePlanSummaryBean>();
        
        beginTx();
        try {
            ServiceVersionBean versionBean = getServiceVersion(organizationId, serviceId, version);
            Set<ServicePlanBean> servicePlans = versionBean.getPlans();
            if (servicePlans != null) {
                for (ServicePlanBean spb : servicePlans) {
                    PlanVersionBean planVersion = getPlanVersion(organizationId, spb.getPlanId(), spb.getVersion());
                    ServicePlanSummaryBean summary = new ServicePlanSummaryBean();
                    summary.setPlanId(planVersion.getPlan().getId());
                    summary.setPlanName(planVersion.getPlan().getName());
                    summary.setPlanDescription(planVersion.getPlan().getDescription());
                    summary.setVersion(spb.getVersion());
                    plans.add(summary);
                }
            }
            return plans;
        } catch (StorageException e) {
            rollbackTx();
            throw e;
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServiceContracts(java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ContractSummaryBean> getServiceContracts(String organizationId, String serviceId,
            String version, int page, int pageSize) throws StorageException {
        int start = (page - 1) * pageSize;
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            @SuppressWarnings("nls")
            String jpql = 
                    "SELECT c from ContractBean c " +
                    "  JOIN c.service svcv " +
                    "  JOIN svcv.service svc " +
                    "  JOIN c.application appv " +
                    "  JOIN appv.application app " +
                    "  JOIN svc.organization sorg" +
                    "  JOIN app.organization aorg" +
                    " WHERE svc.id = :serviceId " +
                    "   AND sorg.id = :orgId " +
                    "   AND svcv.version = :version " +
                    " ORDER BY aorg.id, app.name ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", organizationId); //$NON-NLS-1$
            query.setParameter("serviceId", serviceId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$
            query.setFirstResult(start);
            query.setMaxResults(pageSize);
            List<ContractBean> contracts = (List<ContractBean>) query.getResultList();
            List<ContractSummaryBean> rval = new ArrayList<ContractSummaryBean>(contracts.size());
            for (ContractBean contractBean : contracts) {
                ApplicationBean application = contractBean.getApplication().getApplication();
                ServiceBean service = contractBean.getService().getService();
                PlanBean plan = contractBean.getPlan().getPlan();
                
                OrganizationBean appOrg = entityManager.find(OrganizationBean.class, application.getOrganization().getId());
                OrganizationBean svcOrg = entityManager.find(OrganizationBean.class, service.getOrganization().getId());
                
                ContractSummaryBean csb = new ContractSummaryBean();
                csb.setAppId(application.getId());
                csb.setKey(contractBean.getKey());
                csb.setAppOrganizationId(application.getOrganization().getId());
                csb.setAppOrganizationName(appOrg.getName());
                csb.setAppName(application.getName());
                csb.setAppVersion(contractBean.getApplication().getVersion());
                csb.setContractId(contractBean.getId());
                csb.setCreatedOn(contractBean.getCreatedOn());
                csb.setPlanId(plan.getId());
                csb.setPlanName(plan.getName());
                csb.setPlanVersion(contractBean.getPlan().getVersion());
                csb.setServiceDescription(service.getDescription());
                csb.setServiceId(service.getId());
                csb.setServiceName(service.getName());
                csb.setServiceOrganizationId(svcOrg.getId());
                csb.setServiceOrganizationName(svcOrg.getName());
                csb.setServiceVersion(contractBean.getService().getVersion());
                
                rval.add(csb);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApplicationVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationVersionBean getApplicationVersion(String orgId, String applicationId, String version)
            throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT v from ApplicationVersionBean v JOIN v.application a JOIN a.organization o WHERE o.id = :orgId AND a.id = :applicationId AND v.version = :version"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("applicationId", applicationId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$
            
            return (ApplicationVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ApplicationVersionSummaryBean> getApplicationVersions(String orgId, String applicationId)
            throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            @SuppressWarnings("nls")
            String jpql =
                      "SELECT v"
                    + "  FROM ApplicationVersionBean v"
                    + "  JOIN v.application a"
                    + "  JOIN a.organization o"
                    + " WHERE o.id = :orgId"
                    + "   AND a.id = :applicationId"
                    + " ORDER BY v.id DESC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("applicationId", applicationId); //$NON-NLS-1$
            List<ApplicationVersionBean> appVersions = (List<ApplicationVersionBean>) query.getResultList();
            List<ApplicationVersionSummaryBean> rval = new ArrayList<ApplicationVersionSummaryBean>();
            for (ApplicationVersionBean appVersion : appVersions) {
                ApplicationVersionSummaryBean avsb = new ApplicationVersionSummaryBean();
                avsb.setOrganizationId(appVersion.getApplication().getOrganization().getId());
                avsb.setOrganizationName(appVersion.getApplication().getOrganization().getName());
                avsb.setId(appVersion.getApplication().getId());
                avsb.setName(appVersion.getApplication().getName());
                avsb.setDescription(appVersion.getApplication().getDescription());
                avsb.setVersion(appVersion.getVersion());
                avsb.setStatus(appVersion.getStatus());
                
                rval.add(avsb);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ContractSummaryBean> getApplicationContracts(String organizationId, String applicationId,
            String version) throws StorageException {
        List<ContractSummaryBean> rval = new ArrayList<ContractSummaryBean>();

        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            @SuppressWarnings("nls")
            String jpql = 
                    "SELECT c from ContractBean c " +
                    "  JOIN c.application appv " +
                    "  JOIN appv.application app " +
                    "  JOIN app.organization aorg" +
                    " WHERE app.id = :applicationId " +
                    "   AND aorg.id = :orgId " +
                    "   AND appv.version = :version " +
                    " ORDER BY c.id ASC";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", organizationId); //$NON-NLS-1$
            query.setParameter("applicationId", applicationId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$
            List<ContractBean> contracts = (List<ContractBean>) query.getResultList();
            for (ContractBean contractBean : contracts) {
                ApplicationBean application = contractBean.getApplication().getApplication();
                ServiceBean service = contractBean.getService().getService();
                PlanBean plan = contractBean.getPlan().getPlan();
                
                OrganizationBean appOrg = entityManager.find(OrganizationBean.class, application.getOrganization().getId());
                OrganizationBean svcOrg = entityManager.find(OrganizationBean.class, service.getOrganization().getId());
                
                ContractSummaryBean csb = new ContractSummaryBean();
                csb.setAppId(application.getId());
                csb.setKey(contractBean.getKey());
                csb.setAppOrganizationId(application.getOrganization().getId());
                csb.setAppOrganizationName(appOrg.getName());
                csb.setAppName(application.getName());
                csb.setAppVersion(contractBean.getApplication().getVersion());
                csb.setContractId(contractBean.getId());
                csb.setCreatedOn(contractBean.getCreatedOn());
                csb.setPlanId(plan.getId());
                csb.setPlanName(plan.getName());
                csb.setPlanVersion(contractBean.getPlan().getVersion());
                csb.setServiceDescription(service.getDescription());
                csb.setServiceId(service.getId());
                csb.setServiceName(service.getName());
                csb.setServiceOrganizationId(svcOrg.getId());
                csb.setServiceOrganizationName(svcOrg.getName());
                csb.setServiceVersion(contractBean.getService().getVersion());
                
                rval.add(csb);
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApiRegistry(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiRegistryBean getApiRegistry(String organizationId, String applicationId, String version)
            throws StorageException {
        ApiRegistryBean rval = new ApiRegistryBean();

        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            @SuppressWarnings("nls")
            String jpql = 
                    "SELECT c from ContractBean c " +
                    "  JOIN c.application appv " +
                    "  JOIN appv.application app " +
                    "  JOIN app.organization aorg" +
                    " WHERE app.id = :applicationId " +
                    "   AND aorg.id = :orgId " +
                    "   AND appv.version = :version " +
                    " ORDER BY c.id ASC";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", organizationId); //$NON-NLS-1$
            query.setParameter("applicationId", applicationId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<ContractBean> contracts = (List<ContractBean>) query.getResultList();
            for (ContractBean contractBean : contracts) {
                ServiceVersionBean svb = contractBean.getService();
                ServiceBean service = svb.getService();
                PlanBean plan = contractBean.getPlan().getPlan();
                
                OrganizationBean svcOrg = service.getOrganization();
                
                ApiEntryBean entry = new ApiEntryBean();
                entry.setServiceId(service.getId());
                entry.setServiceName(service.getName());
                entry.setServiceOrgId(svcOrg.getId());
                entry.setServiceOrgName(svcOrg.getName());
                entry.setServiceVersion(svb.getVersion());
                entry.setPlanId(plan.getId());
                entry.setPlanName(plan.getName());
                entry.setPlanVersion(contractBean.getPlan().getVersion());
                entry.setApiKey(contractBean.getKey());
                
                Set<ServiceGatewayBean> gateways = svb.getGateways();
                if (gateways != null && gateways.size() > 0) {
                    ServiceGatewayBean sgb = gateways.iterator().next();
                    entry.setGatewayId(sgb.getGatewayId());
                }
                
                rval.getApis().add(entry);
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
        return rval;
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlansInOrg(java.lang.String)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrg(String orgId) throws StorageException {
        Set<String> orgIds = new HashSet<String>();
        orgIds.add(orgId);
        return getPlansInOrgs(orgIds);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlansInOrgs(java.util.Set)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrgs(Set<String> orgIds) throws StorageException {
        List<PlanSummaryBean> rval = new ArrayList<PlanSummaryBean>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT p FROM PlanBean p JOIN p.organization o WHERE o.id IN :orgs ORDER BY p.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<PlanBean> qr = (List<PlanBean>) query.getResultList();
            for (PlanBean bean : qr) {
                PlanSummaryBean summary = new PlanSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                OrganizationBean org = bean.getOrganization();
                summary.setOrganizationId(org.getId());
                summary.setOrganizationName(org.getName());
                rval.add(summary);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#getPlanVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PlanVersionBean getPlanVersion(String orgId, String planId, String version)
            throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT v from PlanVersionBean v JOIN v.plan p JOIN p.organization o WHERE o.id = :orgId AND p.id = :planId AND v.version = :version"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("planId", planId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$
            
            return (PlanVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlanVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<PlanVersionSummaryBean> getPlanVersions(String orgId, String planId)
            throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            @SuppressWarnings("nls")
            String jpql = "SELECT v from PlanVersionBean v" + 
                          "  JOIN v.plan p" +
                          "  JOIN p.organization o" +
                          " WHERE o.id = :orgId" + 
                          "   AND p.id = :planId" + 
                          " ORDER BY v.id DESC";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("planId", planId); //$NON-NLS-1$
            List<PlanVersionBean> planVersions = (List<PlanVersionBean>) query.getResultList();
            List<PlanVersionSummaryBean> rval = new ArrayList<PlanVersionSummaryBean>(planVersions.size());
            for (PlanVersionBean planVersion : planVersions) {
                PlanVersionSummaryBean pvsb = new PlanVersionSummaryBean();
                pvsb.setOrganizationId(planVersion.getPlan().getOrganization().getId());
                pvsb.setOrganizationName(planVersion.getPlan().getOrganization().getName());
                pvsb.setId(planVersion.getPlan().getId());
                pvsb.setName(planVersion.getPlan().getName());
                pvsb.setDescription(planVersion.getPlan().getDescription());
                pvsb.setVersion(planVersion.getVersion());
                pvsb.setStatus(planVersion.getStatus());
                rval.add(pvsb);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPolicies(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @SuppressWarnings({ "nls", "unchecked" })
    @Override
    public List<PolicySummaryBean> getPolicies(String organizationId, String entityId, String version,
            PolicyType type) throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = 
                      "SELECT p from PolicyBean p "
                    + " WHERE p.organizationId = :orgId "
                    + "   AND p.entityId = :entityId "
                    + "   AND p.entityVersion = :entityVersion "
                    + "   AND p.type = :type"
                    + " ORDER BY p.orderIndex ASC";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", organizationId);
            query.setParameter("entityId", entityId);
            query.setParameter("entityVersion", version);
            query.setParameter("type", type);
            
            List<PolicyBean> policyBeans = (List<PolicyBean>) query.getResultList();
            List<PolicySummaryBean> rval = new ArrayList<PolicySummaryBean>(policyBeans.size());
            for (PolicyBean policyBean : policyBeans) {
                PolicyTemplateUtil.generatePolicyDescription(policyBean);
                PolicySummaryBean psb = new PolicySummaryBean();
                psb.setId(policyBean.getId());
                psb.setName(policyBean.getName());
                psb.setDescription(policyBean.getDescription());
                psb.setPolicyDefinitionId(policyBean.getDefinition().getId());
                psb.setIcon(policyBean.getDefinition().getIcon());
                psb.setCreatedBy(policyBean.getCreatedBy());
                psb.setCreatedOn(policyBean.getCreatedOn());
                rval.add(psb);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getMaxPolicyOrderIndex(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @Override
    public int getMaxPolicyOrderIndex(String organizationId, String entityId, String entityVersion,
            PolicyType type) throws StorageException {
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        criteria.addFilter("organizationId", organizationId, SearchCriteriaFilterBean.OPERATOR_EQ); //$NON-NLS-1$
        criteria.addFilter("entityId", entityId, SearchCriteriaFilterBean.OPERATOR_EQ); //$NON-NLS-1$
        criteria.addFilter("entityVersion", entityVersion, SearchCriteriaFilterBean.OPERATOR_EQ); //$NON-NLS-1$
        criteria.addFilter("type", type.name(), SearchCriteriaFilterBean.OPERATOR_EQ); //$NON-NLS-1$
        criteria.setOrder("orderIndex", false); //$NON-NLS-1$
        criteria.setPage(1);
        criteria.setPageSize(1);
        SearchResultsBean<PolicyBean> resultsBean = find(criteria, PolicyBean.class);
        if (resultsBean.getBeans() == null || resultsBean.getBeans().isEmpty()) {
            return 0;
        } else {
            return resultsBean.getBeans().get(0).getOrderIndex();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IApiKeyGenerator#generate()
     */
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }

}
