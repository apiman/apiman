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
package org.overlord.apiman.dt.api.core;

import java.util.List;
import java.util.Set;

import org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean;
import org.overlord.apiman.dt.api.beans.plans.PlanVersionBean;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ContractSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.OrganizationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.PlanSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServicePlanSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.core.exceptions.StorageException;


/**
 * Specific querying of the storage layer.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IStorageQuery {
    
    /**
     * Returns summary info for all organizations in the given set.
     * @param orgIds
     * @throws StorageException
     */
    public List<OrganizationSummaryBean> getOrgs(Set<String> orgIds) throws StorageException;

    /**
     * Returns summary info for all applications in all organizations in the given set.
     * @param orgIds
     * @throws StorageException
     */
    public List<ApplicationSummaryBean> getApplicationsInOrgs(Set<String> orgIds) throws StorageException;

    /**
     * Returns summary info for all applications in the given organization.
     * @param orgId
     * @throws StorageException
     */
    public List<ApplicationSummaryBean> getApplicationsInOrg(String orgId) throws StorageException;

    /**
     * Returns the application version bean with the given info (orgId, appId, version).
     * @param organizationId
     * @param applicationId
     * @param version
     */
    public ApplicationVersionBean getApplicationVersion(String organizationId, String applicationId,
            String version) throws StorageException;

    /**
     * Returns all application versions for a given app.
     * @param organizationId
     * @param applicationId
     */
    public List<ApplicationVersionBean> getApplicationVersions(String organizationId, String applicationId)
            throws StorageException;

    /**
     * Returns all Contracts for the application.
     * @param organizationId
     * @param applicationId
     * @param version
     */
    public List<ContractSummaryBean> getApplicationContracts(String organizationId, String applicationId, String version)
            throws StorageException;

    /**
     * Returns summary info for all services in all organizations in the given set.
     * @param orgIds
     * @throws StorageException
     */
    public List<ServiceSummaryBean> getServicesInOrgs(Set<String> orgIds) throws StorageException;

    /**
     * Returns summary info for all services in the given organization.
     * @param orgId
     * @throws StorageException
     */
    public List<ServiceSummaryBean> getServicesInOrg(String orgId) throws StorageException;
    
    /**
     * Returns the service version bean with the given info (orgid, serviceid, version).
     * @param orgId
     * @param serviceId
     * @param version
     * @throws StorageException
     */
    public ServiceVersionBean getServiceVersion(String orgId, String serviceId, String version) throws StorageException;
    
    /**
     * Returns all service versions for a given service.
     * @param orgId
     * @param serviceId
     * @throws StorageException
     */
    public List<ServiceVersionBean> getServiceVersions(String orgId, String serviceId) throws StorageException;

    /**
     * Returns the service plans configured for the given service version.
     * @param organizationId
     * @param serviceId
     * @param version
     */
    public List<ServicePlanSummaryBean> getServiceVersionPlans(String organizationId, String serviceId,
            String version) throws StorageException;

    /**
     * Returns summary info for all plans in all organizations in the given set.
     * @param orgIds
     * @throws StorageException
     */
    public List<PlanSummaryBean> getPlansInOrgs(Set<String> orgIds) throws StorageException;

    /**
     * Returns summary info for all plans in the given organization.
     * @param orgId
     * @throws StorageException
     */
    public List<PlanSummaryBean> getPlansInOrg(String orgId) throws StorageException;

    /**
     * Returns the plan version bean with the given info (orgId, appId, version).
     * @param organizationId
     * @param planId
     * @param version
     * @throws StorageException
     */
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version)
            throws StorageException;

    /**
     * Returns all plan versions for a given plan.
     * @param organizationId
     * @param planId
     * @throws StorageException
     */
    public List<PlanVersionBean> getPlanVersions(String organizationId, String planId)
            throws StorageException;
    
}
