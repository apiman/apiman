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
package org.overlord.apiman.dt.api.fuse6.jaxrs;

import java.util.List;

import org.overlord.apiman.dt.api.beans.apps.ApplicationBean;
import org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean;
import org.overlord.apiman.dt.api.beans.contracts.ContractBean;
import org.overlord.apiman.dt.api.beans.contracts.NewContractBean;
import org.overlord.apiman.dt.api.beans.idm.GrantRolesBean;
import org.overlord.apiman.dt.api.beans.members.MemberBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.plans.PlanBean;
import org.overlord.apiman.dt.api.beans.plans.PlanVersionBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ContractSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.PlanSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.PolicyChainSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServicePlanSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.rest.contract.IOrganizationResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ContractAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ContractNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidServiceStatusException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.RoleNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.UserNotFoundException;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * Organization resource proxy.
 *
 * @author eric.wittmann@redhat.com
 */
public class FuseOrganizationResource extends AbstractFuseResource<IOrganizationResource> implements IOrganizationResource {
    
    /**
     * Constructor.
     */
    public FuseOrganizationResource() {
    }

    /**
     * @see org.overlord.apiman.dt.api.fuse6.jaxrs.AbstractFuseResource#getProxy()
     */
    @Override
    protected IOrganizationResource getProxy() {
        return ServiceRegistryUtil.getSingleService(IOrganizationResource.class);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#create(org.overlord.apiman.dt.api.beans.orgs.OrganizationBean)
     */
    @Override
    public OrganizationBean create(OrganizationBean bean) throws OrganizationAlreadyExistsException,
            NotAuthorizedException {
        return getProxy().create(bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#get(java.lang.String)
     */
    @Override
    public OrganizationBean get(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        return getProxy().get(organizationId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#update(java.lang.String, org.overlord.apiman.dt.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void update(String organizationId, OrganizationBean bean) throws OrganizationNotFoundException,
            NotAuthorizedException {
        getProxy().update(organizationId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createApp(java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationBean)
     */
    @Override
    public ApplicationBean createApp(String organizationId, ApplicationBean bean)
            throws OrganizationNotFoundException, ApplicationAlreadyExistsException, NotAuthorizedException {
        return getProxy().createApp(organizationId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getApp(java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationBean getApp(String organizationId, String applicationId)
            throws ApplicationNotFoundException, NotAuthorizedException {
        return getProxy().getApp(organizationId, applicationId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listApps(java.lang.String)
     */
    @Override
    public List<ApplicationSummaryBean> listApps(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        return getProxy().listApps(organizationId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updateApp(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationBean)
     */
    @Override
    public void updateApp(String organizationId, String applicationId, ApplicationBean bean)
            throws ApplicationNotFoundException, NotAuthorizedException {
        getProxy().updateApp(organizationId, applicationId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createAppVersion(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public ApplicationVersionBean createAppVersion(String organizationId, String applicationId,
            ApplicationVersionBean bean) throws ApplicationNotFoundException, NotAuthorizedException {
        return getProxy().createAppVersion(organizationId, applicationId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listAppVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ApplicationVersionBean> listAppVersions(String organizationId, String applicationId)
            throws ApplicationNotFoundException, NotAuthorizedException {
        return getProxy().listAppVersions(organizationId, applicationId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getAppVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationVersionBean getAppVersion(String organizationId, String applicationId, String version)
            throws ApplicationVersionNotFoundException, NotAuthorizedException {
        return getProxy().getAppVersion(organizationId, applicationId, version);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updateAppVersion(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void updateAppVersion(String organizationId, String applicationId, String version,
            ApplicationVersionBean bean) throws ApplicationVersionNotFoundException, NotAuthorizedException {
        getProxy().updateAppVersion(organizationId, applicationId, version, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createContract(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.contracts.NewContractBean)
     */
    @Override
    public ContractBean createContract(String organizationId, String applicationId, String version,
            NewContractBean bean) throws OrganizationNotFoundException, ApplicationNotFoundException,
            ServiceNotFoundException, PlanNotFoundException, ContractAlreadyExistsException,
            NotAuthorizedException {
        return getProxy().createContract(organizationId, applicationId, version, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getContract(java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public ContractBean getContract(String organizationId, String applicationId, String version,
            Long contractId) throws ApplicationNotFoundException, ContractNotFoundException,
            NotAuthorizedException {
        return getProxy().getContract(organizationId, applicationId, version, contractId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ContractSummaryBean> listContracts(String organizationId, String applicationId, String version)
            throws ApplicationNotFoundException, NotAuthorizedException {
        return getProxy().listContracts(organizationId, applicationId, version);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#deleteContract(java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public void deleteContract(String organizationId, String applicationId, String version, Long contractId)
            throws ApplicationNotFoundException, ContractNotFoundException, NotAuthorizedException {
        getProxy().deleteContract(organizationId, applicationId, version, contractId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createAppPolicy(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public PolicyBean createAppPolicy(String organizationId, String applicationId, String version,
            PolicyBean bean) throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            NotAuthorizedException {
        return getProxy().createAppPolicy(organizationId, applicationId, version, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getAppPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public PolicyBean getAppPolicy(String organizationId, String applicationId, String version, long policyId)
            throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        return getProxy().getAppPolicy(organizationId, applicationId, version, policyId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updateAppPolicy(java.lang.String, java.lang.String, java.lang.String, long, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public void updateAppPolicy(String organizationId, String applicationId, String version, long policyId,
            PolicyBean bean) throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        getProxy().updateAppPolicy(organizationId, applicationId, version, policyId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#deleteAppPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public void deleteAppPolicy(String organizationId, String applicationId, String version, long policyId)
            throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        getProxy().deleteAppPolicy(organizationId, applicationId, version, policyId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listAppPolicies(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<PolicyBean> listAppPolicies(String organizationId, String applicationId, String version)
            throws OrganizationNotFoundException, ApplicationVersionNotFoundException, NotAuthorizedException {
        return getProxy().listAppPolicies(organizationId, applicationId, version);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createService(java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceBean)
     */
    @Override
    public ServiceBean createService(String organizationId, ServiceBean bean)
            throws OrganizationNotFoundException, ServiceAlreadyExistsException, NotAuthorizedException {
        return getProxy().createService(organizationId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getService(java.lang.String, java.lang.String)
     */
    @Override
    public ServiceBean getService(String organizationId, String serviceId) throws ServiceNotFoundException,
            NotAuthorizedException {
        return getProxy().getService(organizationId, serviceId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listServices(java.lang.String)
     */
    @Override
    public List<ServiceSummaryBean> listServices(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        return getProxy().listServices(organizationId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updateService(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceBean)
     */
    @Override
    public void updateService(String organizationId, String serviceId, ServiceBean bean)
            throws ServiceNotFoundException, NotAuthorizedException {
        getProxy().updateService(organizationId, serviceId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createServiceVersion(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceVersionBean)
     */
    @Override
    public ServiceVersionBean createServiceVersion(String organizationId, String serviceId,
            ServiceVersionBean bean) throws ServiceNotFoundException, NotAuthorizedException {
        return getProxy().createServiceVersion(organizationId, serviceId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listServiceVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ServiceVersionBean> listServiceVersions(String organizationId, String serviceId)
            throws ServiceNotFoundException, NotAuthorizedException {
        return getProxy().listServiceVersions(organizationId, serviceId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getServiceVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ServiceVersionBean getServiceVersion(String organizationId, String serviceId, String version)
            throws ServiceVersionNotFoundException, NotAuthorizedException {
        return getProxy().getServiceVersion(organizationId, serviceId, version);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updateServiceVersion(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void updateServiceVersion(String organizationId, String serviceId, String version,
            ServiceVersionBean bean) throws ServiceVersionNotFoundException, NotAuthorizedException,
            InvalidServiceStatusException {
        getProxy().updateServiceVersion(organizationId, serviceId, version, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getServiceVersionPlans(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ServicePlanSummaryBean> getServiceVersionPlans(String organizationId, String serviceId,
            String version) throws ServiceVersionNotFoundException, NotAuthorizedException {
        return getProxy().getServiceVersionPlans(organizationId, serviceId, version);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createServicePolicy(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public PolicyBean createServicePolicy(String organizationId, String serviceId, String version,
            PolicyBean bean) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            NotAuthorizedException {
        return getProxy().createServicePolicy(organizationId, serviceId, version, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getServicePolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public PolicyBean getServicePolicy(String organizationId, String serviceId, String version, long policyId)
            throws OrganizationNotFoundException, ServiceVersionNotFoundException, PolicyNotFoundException,
            NotAuthorizedException {
        return getProxy().getServicePolicy(organizationId, serviceId, version, policyId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updateServicePolicy(java.lang.String, java.lang.String, java.lang.String, long, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public void updateServicePolicy(String organizationId, String serviceId, String version, long policyId,
            PolicyBean bean) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        getProxy().updateServicePolicy(organizationId, serviceId, version, policyId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#deleteServicePolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public void deleteServicePolicy(String organizationId, String serviceId, String version, long policyId)
            throws OrganizationNotFoundException, ServiceVersionNotFoundException, PolicyNotFoundException,
            NotAuthorizedException {
        getProxy().deleteServicePolicy(organizationId, serviceId, version, policyId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listServicePolicies(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<PolicyBean> listServicePolicies(String organizationId, String serviceId, String version)
            throws OrganizationNotFoundException, ServiceVersionNotFoundException, NotAuthorizedException {
        return getProxy().listServicePolicies(organizationId, serviceId, version);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getServicePolicyChain(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PolicyChainSummaryBean getServicePolicyChain(String organizationId, String serviceId,
            String version, String planId) throws ServiceVersionNotFoundException, NotAuthorizedException {
        return getProxy().getServicePolicyChain(organizationId, serviceId, version, planId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createPlan(java.lang.String, org.overlord.apiman.dt.api.beans.plans.PlanBean)
     */
    @Override
    public PlanBean createPlan(String organizationId, PlanBean bean) throws OrganizationNotFoundException,
            PlanAlreadyExistsException, NotAuthorizedException {
        return getProxy().createPlan(organizationId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getPlan(java.lang.String, java.lang.String)
     */
    @Override
    public PlanBean getPlan(String organizationId, String planId) throws PlanNotFoundException,
            NotAuthorizedException {
        return getProxy().getPlan(organizationId, planId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listPlans(java.lang.String)
     */
    @Override
    public List<PlanSummaryBean> listPlans(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        return getProxy().listPlans(organizationId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updatePlan(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.plans.PlanBean)
     */
    @Override
    public void updatePlan(String organizationId, String planId, PlanBean bean) throws PlanNotFoundException,
            NotAuthorizedException {
        getProxy().updatePlan(organizationId, planId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createPlanVersion(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.plans.PlanVersionBean)
     */
    @Override
    public PlanVersionBean createPlanVersion(String organizationId, String planId, PlanVersionBean bean)
            throws PlanNotFoundException, NotAuthorizedException {
        return getProxy().createPlanVersion(organizationId, planId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listPlanVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<PlanVersionBean> listPlanVersions(String organizationId, String planId)
            throws PlanNotFoundException, NotAuthorizedException {
        return getProxy().listPlanVersions(organizationId, planId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getPlanVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version)
            throws PlanVersionNotFoundException, NotAuthorizedException {
        return getProxy().getPlanVersion(organizationId, planId, version);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updatePlanVersion(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void updatePlanVersion(String organizationId, String planId, String version, PlanVersionBean bean)
            throws PlanVersionNotFoundException, NotAuthorizedException {
        getProxy().updatePlanVersion(organizationId, planId, version, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#searchPlans(java.lang.String, org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<PlanBean> searchPlans(String organizationId, SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        return getProxy().searchPlans(organizationId, criteria);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#createPlanPolicy(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public PolicyBean createPlanPolicy(String organizationId, String planId, String version, PolicyBean bean)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        return getProxy().createPlanPolicy(organizationId, planId, version, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#getPlanPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public PolicyBean getPlanPolicy(String organizationId, String planId, String version, long policyId)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException,
            NotAuthorizedException {
        return getProxy().getPlanPolicy(organizationId, planId, version, policyId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#updatePlanPolicy(java.lang.String, java.lang.String, java.lang.String, long, org.overlord.apiman.dt.api.beans.policies.PolicyBean)
     */
    @Override
    public void updatePlanPolicy(String organizationId, String planId, String version, long policyId,
            PolicyBean bean) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        getProxy().updatePlanPolicy(organizationId, planId, version, policyId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#deletePlanPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public void deletePlanPolicy(String organizationId, String planId, String version, long policyId)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException,
            NotAuthorizedException {
        getProxy().deletePlanPolicy(organizationId, planId, version, policyId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listPlanPolicies(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<PolicyBean> listPlanPolicies(String organizationId, String planId, String version)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        return getProxy().listPlanPolicies(organizationId, planId, version);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#grant(java.lang.String, org.overlord.apiman.dt.api.beans.idm.GrantRolesBean)
     */
    @Override
    public void grant(String organizationId, GrantRolesBean bean) throws OrganizationNotFoundException,
            RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        getProxy().grant(organizationId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#revoke(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void revoke(String organizationId, String roleId, String userId)
            throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException,
            NotAuthorizedException {
        getProxy().revoke(organizationId, roleId, userId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#revokeAll(java.lang.String, java.lang.String)
     */
    @Override
    public void revokeAll(String organizationId, String userId) throws OrganizationNotFoundException,
            RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        getProxy().revokeAll(organizationId, userId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#listMembers(java.lang.String)
     */
    @Override
    public List<MemberBean> listMembers(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        return getProxy().listMembers(organizationId);
    }

}
