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

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.apis.ApiVersionStatusBean;
import io.apiman.manager.api.beans.apis.NewApiBean;
import io.apiman.manager.api.beans.apis.NewApiDefinitionBean;
import io.apiman.manager.api.beans.apis.NewApiVersionBean;
import io.apiman.manager.api.beans.apis.UpdateApiBean;
import io.apiman.manager.api.beans.apis.UpdateApiVersionBean;
import io.apiman.manager.api.beans.apis.dto.ApiBeanDto;
import io.apiman.manager.api.beans.apis.dto.ApiPlanBeanDto;
import io.apiman.manager.api.beans.apis.dto.ApiVersionBeanDto;
import io.apiman.manager.api.beans.apis.dto.KeyValueTagDto;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.clients.ApiKeyBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.clients.NewClientBean;
import io.apiman.manager.api.beans.clients.NewClientVersionBean;
import io.apiman.manager.api.beans.clients.UpdateClientBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.NewContractBean;
import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.download.DownloadType;
import io.apiman.manager.api.beans.idm.GrantRolesBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.members.MemberBean;
import io.apiman.manager.api.beans.metrics.ClientUsagePerApiBean;
import io.apiman.manager.api.beans.metrics.HistogramIntervalType;
import io.apiman.manager.api.beans.metrics.ResponseStatsHistogramBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerClientBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerPlanBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsSummaryBean;
import io.apiman.manager.api.beans.metrics.UsageHistogramBean;
import io.apiman.manager.api.beans.metrics.UsagePerClientBean;
import io.apiman.manager.api.beans.metrics.UsagePerPlanBean;
import io.apiman.manager.api.beans.orgs.NewOrganizationBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.orgs.UpdateOrganizationBean;
import io.apiman.manager.api.beans.plans.NewPlanBean;
import io.apiman.manager.api.beans.plans.NewPlanVersionBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.plans.UpdatePlanBean;
import io.apiman.manager.api.beans.policies.NewPolicyBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyChainBean;
import io.apiman.manager.api.beans.policies.UpdatePolicyBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiPlanSummaryBean;
import io.apiman.manager.api.beans.summary.ApiRegistryBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionEndpointSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.ClientVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.core.IBlobStore;
import io.apiman.manager.api.core.IDownloadManager;
import io.apiman.manager.api.core.config.ApiManagerConfig;
import io.apiman.manager.api.rest.IOrganizationResource;
import io.apiman.manager.api.rest.exceptions.ApiAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ApiNotFoundException;
import io.apiman.manager.api.rest.exceptions.ApiVersionAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ApiVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.ClientAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ClientNotFoundException;
import io.apiman.manager.api.rest.exceptions.ClientVersionAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ClientVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.ContractAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ContractNotFoundException;
import io.apiman.manager.api.rest.exceptions.EntityStillActiveException;
import io.apiman.manager.api.rest.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidApiStatusException;
import io.apiman.manager.api.rest.exceptions.InvalidClientStatusException;
import io.apiman.manager.api.rest.exceptions.InvalidMetricCriteriaException;
import io.apiman.manager.api.rest.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.exceptions.InvalidPlanStatusException;
import io.apiman.manager.api.rest.exceptions.InvalidVersionException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.PlanNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanVersionAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.exceptions.RoleNotFoundException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.UserNotFoundException;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.rest.impl.util.RestHelper;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.ISecurityContext.EntityType;
import io.apiman.manager.api.service.ApiService;
import io.apiman.manager.api.service.ApiService.ApiDefinitionStream;
import io.apiman.manager.api.service.ClientAppService;
import io.apiman.manager.api.service.ContractService;
import io.apiman.manager.api.service.OrganizationService;
import io.apiman.manager.api.service.PlanService;
import io.apiman.manager.api.service.StatsService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;

import static io.apiman.manager.api.security.ISecurityContext.EntityType.API;
import static io.apiman.manager.api.security.ISecurityContext.EntityType.PLAN;

/**
 * REST layer for Organization
 *
 * TODO(msavy): split this into multiple interfaces & impls (ApiResource, ClientAppResource, etc).
 *
 * @author eric.wittmann@redhat.com
 * @author marc@blackparrotlabs.io
 */
@RequestScoped
@Transactional
public class OrganizationResourceImpl implements IOrganizationResource, DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(OrganizationResourceImpl.class);

    private ApiManagerConfig config;
    private OrganizationService organizationService;
    private ApiService apiService;
    private PlanService planService;
    private ClientAppService clientService;
    private ContractService contractService;
    private StatsService statsService;
    private IDownloadManager downloadManager;
    private ISecurityContext securityContext;
    private IBlobStore blobStore;
    private HttpServletRequest request;


    @Inject
    public OrganizationResourceImpl(
         ApiManagerConfig config,
         OrganizationService organizationService,
         ApiService apiService, PlanService planService,
         ClientAppService clientService,
         ContractService contractService,
         StatsService statsService,
         IDownloadManager downloadManager,
         ISecurityContext securityContext,
         IBlobStore blobStore,
         @Context HttpServletRequest request
    ) {
        this.config = config;
        this.organizationService = organizationService;
        this.apiService = apiService;
        this.planService = planService;
        this.clientService = clientService;
        this.contractService = contractService;
        this.statsService = statsService;
        this.downloadManager = downloadManager;
        this.securityContext = securityContext;
        this.blobStore = blobStore;
        this.request = request;
    }

    public OrganizationResourceImpl() {
    }

    @Override
    public OrganizationBean createOrg(NewOrganizationBean newOrgDto)
        throws OrganizationAlreadyExistsException, NotAuthorizedException, InvalidNameException {
        if (config.isAdminOnlyOrgCreationEnabled()) {
            securityContext.checkAdminPermissions();
        }
        LOGGER.debug("Attempting to create org: {0}", newOrgDto);
        return organizationService.createOrg(newOrgDto);
    }

    @Override
    public OrganizationBean getOrg(String organizationId) throws OrganizationNotFoundException {
        LOGGER.debug("Attempting to get org: {0}", organizationId);
        OrganizationBean organizationBean = organizationService.getOrg(organizationId);
        // Hide sensitive data and set only needed data for the UI
        if (securityContext.hasPermission(PermissionType.orgView, organizationId)){
            return organizationBean;
        } else {
            return RestHelper.hideSensitiveDataFromOrganizationBean(organizationBean);
        }
    }

    @Override
    public void updateOrg(String organizationId, UpdateOrganizationBean updateOrgDto)
        throws OrganizationNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgEdit, organizationId);

        LOGGER.debug("Attempting to update org {0}: {1}", organizationId, updateOrgDto);
        organizationService.updateOrg(organizationId, updateOrgDto);
    }

    @Override
    public void deleteOrg(String organizationId)
        throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException {
        securityContext.checkPermissions(PermissionType.orgAdmin, organizationId);

        LOGGER.debug("Attempting to delete org: {0}", organizationId);
        organizationService.deleteOrg(organizationId);
    }

    @Override
    public SearchResultsBean<AuditEntryBean> getOrgActivity(String organizationId, int page, int pageSize)
        throws OrganizationNotFoundException, NotAuthorizedException {
        // Only members are allowed to see this
        if (!securityContext.isMemberOf(organizationId)) {
            throw ExceptionFactory.notAuthorizedException();
        }
        LOGGER.debug("Attempting to get org activity: {0} (page {1} / pageSize {2}",
            organizationId, page, pageSize);
        return organizationService.activity(organizationId, page, pageSize);
    }

    @Override
    public ClientBean createClient(String organizationId, NewClientBean bean)
        throws OrganizationNotFoundException, ClientAlreadyExistsException, NotAuthorizedException, InvalidNameException {
        LOGGER.debug("Attempting to create client {0} in org {1}", bean, organizationId);
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);
        // TODO handle with interceptor?
        // As an upload will have happened separately, we need to attach to the blob so that it doesn't get wiped out later.
        blobStore.attachToBlob(bean.getImage());
        return clientService.createClient(organizationId, bean);
    }

    @Override
    public ClientBean getClient(String organizationId, String clientId)
        throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        LOGGER.debug("Attempting to get client {0} in org {1}", clientId, organizationId);
        return clientService.getClient(organizationId, clientId);
    }

    @Override
    public void updateClient(String organizationId, String clientId, UpdateClientBean bean) throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);
        LOGGER.debug("Attempting to update client {0} in org {1} with {2}", clientId, organizationId, bean);
        clientService.updateClient(organizationId, clientId, bean);
    }

    @Override
    public void deleteClient(String organizationId, String clientId)
        throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException {
        securityContext.checkPermissions(PermissionType.clientAdmin, organizationId);
        LOGGER.debug("Attempting to delete client {0} in org {1}", clientId, organizationId);
        clientService.deleteClient(organizationId, clientId);
    }

    @Override
    public SearchResultsBean<AuditEntryBean> getClientActivity(String organizationId, String clientId,
        int page, int pageSize) throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        LOGGER.debug("Attempting to get activity for client {0} in org {1}", clientId, organizationId);
        return clientService.getClientActivity(organizationId, clientId, page, pageSize);
    }

    @Override
    public List<ClientSummaryBean> listClients(String organizationId) throws OrganizationNotFoundException, NotAuthorizedException {
        securityContext.checkAllPermissions(Set.of(PermissionType.orgView, PermissionType.clientView), organizationId);
        LOGGER.debug("Attempting to list all clients in org {1}", organizationId);
        return clientService.listClients(organizationId);
    }

    @Override
    public ClientVersionBean createClientVersion(String organizationId, String clientId, NewClientVersionBean newClientVersion)
        throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException, ClientVersionAlreadyExistsException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);
        LOGGER.debug("Attempting to create clientVersion {0} in org {1} with {2}", clientId, organizationId, newClientVersion);
        return clientService.createClientVersion(organizationId, clientId, newClientVersion);
    }

    @Override
    public List<ClientVersionSummaryBean> listClientVersions(String organizationId, String clientId) throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkAllPermissions(Set.of(PermissionType.orgView, PermissionType.clientView), organizationId);
        LOGGER.debug("Attempting to list all clientVersions in client {0} in org {1}", clientId, organizationId);
        return clientService.listClientVersions(organizationId, clientId);
    }

    @Override
    public ApiKeyBean updateClientApiKey(String organizationId, String clientId, String version, ApiKeyBean bean)
        throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException, InvalidClientStatusException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);
        LOGGER.debug("Attempting to update client {0} with version {1} API key in org {2} with {3}", clientId, version, organizationId, bean);
        return clientService.updateClientApiKey(organizationId, clientId, version, bean);
    }

    @Override
    public ApiKeyBean getClientApiKey(String organizationId, String clientId, String version)
        throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        LOGGER.debug("Attempting to get client {0} with version {1} API key in org {2}", clientId, version, organizationId);
        return clientService.getClientApiKey(organizationId, clientId, version);
    }

    @Override
    public ClientVersionBean getClientVersion(String organizationId, String clientId, String version)
        throws ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        LOGGER.debug("Attempting to get client {0} with version {1} in org {1}", clientId, version, organizationId);
        return clientService.getClientVersion(organizationId, clientId, version);
    }

    @Override
    public SearchResultsBean<AuditEntryBean> getClientVersionActivity(String organizationId, String clientId, String version, int page, int pageSize)
        throws ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        return clientService.getClientVersionActivity(organizationId, clientId, version, page, pageSize);
    }

    @Override
    public ClientUsagePerApiBean getClientUsagePerApi(String organizationId, String clientId, String version, String fromDate, String toDate)
        throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        return statsService.getClientUsagePerApi(organizationId, clientId, version, fromDate, toDate);
    }

    @Override
    public ContractBean createContract(String organizationId,
        String clientId, String version, NewContractBean bean)
        throws OrganizationNotFoundException, ClientNotFoundException, ApiNotFoundException, PlanNotFoundException, ContractAlreadyExistsException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);
        return contractService.createContract(organizationId, clientId, version, bean);
    }

    @Override
    public ContractBean getContract(String organizationId, String clientId, String version, Long contractId)
            throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        return contractService.getContract(contractId);
    }

    @Override
    public Response probeContractPolicy(String organizationId, String clientId, String version, Long contractId, long policyId, String rawString)
            throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        List<IPolicyProbeResponse> probeResponses = contractService.probePolicy(contractId, policyId, rawString);
        return Response.ok(probeResponses).build();
    }

    @Override
    public List<ContractSummaryBean> getClientVersionContracts(String organizationId, String clientId, String version)
        throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        return clientService.getClientVersionContracts(organizationId, clientId, version);
    }

    @Override
    public Response getApiRegistryJSON(String organizationId, String clientId, String version, String download)
            throws ClientNotFoundException, NotAuthorizedException {
        if (BooleanUtils.toBoolean(download)) { //$NON-NLS-1$
            String path = String.format("%s/%s/%s", organizationId, clientId, version); //$NON-NLS-1$
            DownloadBean dbean = tryAction(() ->  downloadManager.createDownload(DownloadType.apiRegistryJson, path));
            return Response.ok(dbean, MediaType.APPLICATION_JSON).build();
        } else {
            return getApiRegistryJSONInternal(organizationId, clientId, version);
        }
    }

    @Override
    public Response getApiRegistryJSONInternal(String organizationId, String clientId, String version) throws ClientVersionNotFoundException {
        // We don't need a permission check here because the permission was already checked while creating the download id
        ApiRegistryBean apiRegistry = organizationService.getApiRegistry(organizationId, clientId, version);
        return Response.ok(apiRegistry, MediaType.APPLICATION_JSON)
            .header("Content-Disposition", "attachment; filename=api-registry.json") //$NON-NLS-1$ //$NON-NLS-2$
            .build();
    }

    public Response getApiRegistryXML(String organizationId, String clientId, String version,
        String download) throws ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

        if (BooleanUtils.toBoolean(download)) { //$NON-NLS-1$
            return tryAction(() -> {
                String path = String.format("%s/%s/%s", organizationId, clientId, version); //$NON-NLS-1$
                DownloadBean dbean = downloadManager.createDownload(DownloadType.apiRegistryXml, path);
                return Response.ok(dbean, MediaType.APPLICATION_JSON).build();
            });
        } else {
            return getApiRegistryXMLInternal(organizationId, clientId, version);
        }
    }

    public Response getApiRegistryXMLInternal(String organizationId, String clientId, String version) throws ClientVersionNotFoundException {
        // We don't need a permission check here because the permission was already checked while creating the download id
        ApiRegistryBean apiRegistry = organizationService.getApiRegistry(organizationId, clientId, version);
        return Response.ok(apiRegistry, MediaType.APPLICATION_XML)
            .header("Content-Disposition", "attachment; filename=api-registry.xml") //$NON-NLS-1$ //$NON-NLS-2$
            .build();
    }

    @Override
    public void deleteAllContracts(String organizationId, String clientId, String version)
        throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);
        contractService.deleteAllContracts(organizationId, clientId, version);
    }

    @Override
    public void deleteContract(String organizationId, String clientId, String version, Long contractId)
        throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException, InvalidClientStatusException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);
        contractService.deleteContract(organizationId, clientId, version, contractId);
    }

    @Override
    public PolicyBean createClientPolicy(String organizationId, String clientId, String version, NewPolicyBean bean)
        throws OrganizationNotFoundException, ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);
        return clientService.createClientPolicy(organizationId, clientId, version, bean);
    }

    @Override
    public PolicyBean getClientPolicy(String organizationId, String clientId, String version, long policyId)
        throws OrganizationNotFoundException, ClientVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        return clientService.getClientPolicy(organizationId, clientId, version, policyId);
    }

    @Override
    public void updateClientPolicy(String organizationId, String clientId, String version, long policyId, UpdatePolicyBean bean)
        throws OrganizationNotFoundException, ClientVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);
        clientService.updateClientPolicy(organizationId, clientId, version, policyId, bean);
    }

    @Override
    public void deleteClientPolicy(String organizationId, String clientId, String version, long policyId)
        throws OrganizationNotFoundException, ClientVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);
        clientService.deleteClientPolicy(organizationId, clientId, version, policyId);
    }

    @Override
    public List<PolicySummaryBean> listClientPolicies(String organizationId, String clientId, String version)
        throws OrganizationNotFoundException, ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        return clientService.listClientPolicies(organizationId, clientId, version);
    }

    @Override
    public void reorderClientPolicies(String organizationId, String clientId, String version, PolicyChainBean policyChain)
        throws OrganizationNotFoundException, ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);
        clientService.reorderClientPolicies(organizationId, clientId, version, policyChain);
    }

    @Override
    public ApiBeanDto createApi(String organizationId, NewApiBean bean)
        throws OrganizationNotFoundException, ApiAlreadyExistsException, NotAuthorizedException, InvalidNameException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);
        // As an upload will have happened separately, we need to attach to the blob so that it doesn't get wiped out later.
        blobStore.attachToBlob(bean.getImage());
        return apiService.createApi(organizationId, bean);
    }

    @Override
    public List<ApiSummaryBean> listApis(String organizationId) throws OrganizationNotFoundException {
        if (securityContext.hasAllPermissions(Set.of(PermissionType.orgView, PermissionType.apiView), organizationId)) {
            return apiService.listApis(organizationId);
        } else {
            return apiService.listApis(organizationId)
                    .stream()
                    .filter(api -> securityContext.isDiscoverable(API, organizationId, api.getId()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public ApiBeanDto getApi(String organizationId, String apiId)
        throws ApiNotFoundException, NotAuthorizedException {

        securityContext.checkPermissionsOrDiscoverability(
                API,
                organizationId,
                apiId,
                Set.of(PermissionType.apiView)
        );

        return apiService.getApi(organizationId, apiId);
    }

    @Override
    public void updateApi(String organizationId, String apiId, UpdateApiBean bean)
        throws ApiNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);
        apiService.updateApi(organizationId, apiId, bean);
    }

    @Override
    public void tagApi(String organizationId, String apiId, KeyValueTagDto bean) throws ApiNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);
        apiService.addTag(organizationId, apiId, bean);
    }

    @Override
    public void deleteApi(String organizationId, String apiId)
        throws ApiNotFoundException, NotAuthorizedException, InvalidApiStatusException {
        securityContext.checkPermissions(PermissionType.apiAdmin, organizationId);
        apiService.deleteApi(organizationId, apiId);
    }

    @Override
    public SearchResultsBean<AuditEntryBean> getApiActivity(String organizationId, String apiId, int page, int pageSize)
        throws ApiNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);
        return apiService.getApiActivity(organizationId, apiId, page, pageSize);
    }

    @Override
    public ApiVersionBeanDto createApiVersion(String organizationId, String apiId, NewApiVersionBean bean)
        throws ApiNotFoundException, NotAuthorizedException, InvalidVersionException, ApiVersionAlreadyExistsException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);
        return apiService.createApiVersion(organizationId, apiId, bean);
    }

    @Override
    public List<ApiVersionSummaryBean> listApiVersions(String organizationId, String apiId)
            throws ApiNotFoundException {
        if (securityContext.hasPermission(PermissionType.apiView, organizationId)) {
            return apiService.listApiVersions(organizationId, apiId);
        } else {
            return apiService.listApiVersions(organizationId, apiId).stream()
                    .filter(avb -> securityContext.isDiscoverable(EntityType.API, organizationId, apiId, avb.getVersion()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public ApiVersionBeanDto getApiVersion(String organizationId, String apiId, String version)
        throws ApiVersionNotFoundException {

        securityContext.checkPermissionsOrDiscoverability(
                API,
                organizationId,
                apiId,
                version,
                Set.of(PermissionType.apiView)
        );

        if (securityContext.hasPermission(PermissionType.apiView, organizationId)) {
            return apiService.getApiVersion(organizationId, apiId, version);
        } else {
            ApiVersionBeanDto av = apiService.getApiVersion(organizationId, apiId, version);
            Set<ApiPlanBeanDto> filteredPlans = av.getPlans().stream()
                    .filter(plan -> securityContext.isDiscoverable(PLAN, organizationId, plan.getPlanId(), plan.getVersion()))
                    .collect(Collectors.toSet());
            av.setPlans(filteredPlans);
            return av;
        }

    }

    @Override
    public ApiVersionStatusBean getApiVersionStatus(String organizationId, String apiId, String version)
        throws ApiVersionNotFoundException, NotAuthorizedException {

        securityContext.checkPermissionsOrDiscoverability(
                API,
                organizationId,
                apiId,
                version,
                Set.of(PermissionType.apiView)
        );

        return apiService.getApiVersionStatus(organizationId, apiId, version);
    }

    @Override
    public Response getApiDefinition(String organizationId, String apiId, String version)
        throws ApiVersionNotFoundException {

        securityContext.checkPermissionsOrDiscoverability(
                API,
                organizationId,
                apiId,
                version,
                Set.of(PermissionType.apiView)
        );

        ApiDefinitionStream apiDef = apiService.getApiDefinition(organizationId, apiId, version);
        return Response.ok()
            .entity(apiDef.getDefinition())
            .type(apiDef.getDefinitionType().getMediaType())
            .build();
    }

    @Override
    public ApiVersionEndpointSummaryBean getApiVersionEndpointInfo(String organizationId, String apiId, String version)
        throws ApiVersionNotFoundException, InvalidApiStatusException, GatewayNotFoundException {

        securityContext.checkPermissionsOrDiscoverability(
                API,
                organizationId,
                apiId,
                version,
                Set.of(PermissionType.apiView)
        );

        return apiService.getApiVersionEndpointInfo(organizationId, apiId, version);
    }

    @Override
    public ApiVersionBeanDto updateApiVersion(String organizationId, String apiId, String version, @Valid UpdateApiVersionBean bean)
        throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);
        return apiService.updateApiVersion(organizationId, apiId, version, bean);
    }

    @Override
    public void updateApiDefinition(String organizationId, String apiId, String version)
        throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        String contentType = request.getContentType();
        InputStream data;
        try {
            data = request.getInputStream();
        } catch (IOException e) {
            throw new SystemErrorException(e);
        }
        try {
            ApiDefinitionType newDefinitionType;
            if (contentType.toLowerCase().contains("application/json")) { //$NON-NLS-1$
                newDefinitionType = ApiDefinitionType.SwaggerJSON;
            } else if (contentType.toLowerCase().contains("application/x-yaml")) { //$NON-NLS-1$
                newDefinitionType = ApiDefinitionType.SwaggerYAML;
            } else if (contentType.toLowerCase().contains("application/wsdl+xml")) { //$NON-NLS-1$
                newDefinitionType = ApiDefinitionType.WSDL;
            } else {
                throw new SystemErrorException(Messages.i18n.format("InvalidApiDefinitionContentType", contentType)); //$NON-NLS-1$
            }
            apiService.setApiDefinition(organizationId, apiId, version, newDefinitionType, data);
            LOGGER.debug("Updated API definition for {0}", apiId); //$NON-NLS-1$
        } finally {
            IOUtils.closeQuietly(data);
        }
    }

    @Override
    public void updateApiDefinitionFromURL(String organizationId, String apiId, String version, NewApiDefinitionBean bean)
        throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);
        try {
            URL url = new URL(bean.getDefinitionUrl());
            InputStream is = url.openStream();
            apiService.setApiDefinition(organizationId, apiId, version, bean, is);
        } catch (IOException ioe) {
            throw new SystemErrorException(ioe);
        }
    }

    @Override
    public SearchResultsBean<AuditEntryBean> getApiVersionActivity(String organizationId, String apiId,
        String version, int page, int pageSize) throws ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                API,
                organizationId,
                apiId,
                version,
                Set.of(PermissionType.apiView)
        );
        return apiService.getApiVersionActivity(organizationId, apiId, version, page, pageSize);
    }

    @Override
    public List<ApiPlanSummaryBean> getApiVersionPlans(String organizationId, String apiId, String version)
         throws ApiVersionNotFoundException {
        securityContext.checkPermissionsOrDiscoverability(
                API,
                organizationId,
                apiId,
                version,
                Set.of(PermissionType.apiView)
        );
        return apiService.getApiVersionPlans(organizationId, apiId, version).stream()
                .filter(p -> securityContext.isDiscoverable(PLAN, organizationId, p.getPlanId(), p.getVersion()))
                .collect(Collectors.toList());
    }

    @Override
    public PolicyBean createApiPolicy(String organizationId, String apiId, String version,
        NewPolicyBean bean) throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);
        return apiService.createApiPolicy(organizationId, apiId, version, bean);
    }

    @Override
    public PolicyBean getApiPolicy(String organizationId, String apiId, String version, long policyId)
        throws OrganizationNotFoundException, ApiVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                API,
                organizationId,
                apiId,
                version,
                Set.of(PermissionType.apiView)
        );
        return apiService.getApiPolicy(organizationId, apiId, version, policyId);
    }

    @Override
    public void updateApiPolicy(String organizationId, String apiId, String version, long policyId,
         UpdatePolicyBean bean) throws OrganizationNotFoundException, ApiVersionNotFoundException,
                                            PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);
        apiService.updateApiPolicy(organizationId, apiId, version, policyId, bean);
    }

    @Override
    public void deleteApiPolicy(String organizationId, String apiId, String version, long policyId)
        throws OrganizationNotFoundException, ApiVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);
        apiService.deleteApiPolicy(organizationId, apiId, version, policyId);
    }

    @Override
    public void deleteApiDefinition(String organizationId, String apiId, String version)
        throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);
        apiService.deleteApiDefinition(organizationId, apiId, version);
    }

    @Override
    public List<PolicySummaryBean> listApiPolicies(String organizationId, String apiId, String version)
        throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                API,
                organizationId,
                apiId,
                version,
                Set.of(PermissionType.apiView)
        );
        return apiService.listApiPolicies(organizationId, apiId, version);
    }

    @Override
    public void reorderApiPolicies(String organizationId, String apiId, String version, PolicyChainBean policyChain)
        throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);
        apiService.reorderApiPolicies(organizationId, apiId, version, policyChain);
    }

    @Override
    public PolicyChainBean getApiPolicyChain(String organizationId, String apiId, String version,
        String planId) throws ApiVersionNotFoundException {
        securityContext.checkPermissionsOrDiscoverability(
                API,
                organizationId,
                apiId,
                version,
                Set.of(PermissionType.apiView)
        );
        return apiService.getApiPolicyChain(organizationId, apiId, version, planId);
    }

    @Override
    public List<ContractSummaryBean> getApiVersionContracts(String organizationId, String apiId,
        String version, int page, int pageSize) throws ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                API,
                organizationId,
                apiId,
                version,
                Set.of(PermissionType.apiView)
        );
        return apiService.getApiVersionContracts(organizationId, apiId, version, page, pageSize);
    }

    @Override
    public PlanBean createPlan(String organizationId, NewPlanBean bean)
        throws OrganizationNotFoundException, PlanAlreadyExistsException, NotAuthorizedException, InvalidNameException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);
        return planService.createPlan(organizationId, bean);
    }

    @Override
    public PlanBean getPlan(String organizationId, String planId)
        throws PlanNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.PLAN,
                organizationId,
                planId,
                Set.of(PermissionType.planView)
        );
        return planService.getPlan(organizationId, planId);
    }

    @Override
    public SearchResultsBean<AuditEntryBean> getPlanActivity(String organizationId, String planId, int page,
        int pageSize) throws PlanNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.PLAN,
                organizationId,
                planId,
                Set.of(PermissionType.planView)
        );
        return planService.getPlanActivity(organizationId, planId, page, pageSize);
    }

    @Override
    public List<PlanSummaryBean> listPlans(String organizationId)
        throws OrganizationNotFoundException, NotAuthorizedException {
        if (securityContext.hasAllPermissions(Set.of(PermissionType.orgView, PermissionType.planView), organizationId)) {
            return planService.listPlans(organizationId);
        } else {
            return planService.listPlans(organizationId)
                    .stream()
                    .filter(plan -> securityContext.isDiscoverable(EntityType.PLAN, organizationId, plan.getId()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void updatePlan(String organizationId, String planId, UpdatePlanBean bean)
        throws PlanNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);
        planService.updatePlan(organizationId, planId, bean);
    }

    @Override
    public PlanVersionBean createPlanVersion(String organizationId, String planId, NewPlanVersionBean bean)
        throws PlanNotFoundException, NotAuthorizedException, InvalidVersionException, PlanVersionAlreadyExistsException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);
        return planService.createPlanVersion(organizationId, planId, bean);
    }

    @Override
    public List<PlanVersionSummaryBean> listPlanVersions(String organizationId, String planId)
        throws PlanNotFoundException, NotAuthorizedException {
        if (securityContext.hasPermission(PermissionType.planView, organizationId)) {
            return planService.listPlanVersions(organizationId, planId);
        } else {
            return planService.listPlanVersions(organizationId, planId).stream()
                    .filter(pvb -> securityContext.isDiscoverable(EntityType.PLAN, organizationId, planId, pvb.getVersion()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version)
        throws PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.PLAN,
                organizationId,
                planId,
                Set.of(PermissionType.planView)
        );
        return planService.getPlanVersion(organizationId, planId, version);
    }

    @Override
    public SearchResultsBean<AuditEntryBean> getPlanVersionActivity(String organizationId,
        String planId, String version, int page, int pageSize)
        throws PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.PLAN,
                organizationId,
                planId,
                Set.of(PermissionType.planView)
        );
        return planService.getPlanVersionActivity(organizationId, planId, version, page, pageSize);
    }

    @Override
    public PolicyBean createPlanPolicy(String organizationId, String planId, String version,
        NewPolicyBean bean)
        throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);
        return planService.createPlanPolicy(organizationId, planId, version, bean);
    }

    @Override
    public List<PolicySummaryBean> listPlanPolicies(String organizationId, String planId, String version)
        throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.PLAN,
                organizationId,
                planId,
                Set.of(PermissionType.planView)
        );
        return planService.listPlanPolicies(organizationId, planId, version);
    }

    @Override
    public PolicyBean getPlanPolicy(String organizationId, String planId, String version, long policyId)
        throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.PLAN,
                organizationId,
                planId,
                Set.of(PermissionType.planView)
        );
        return planService.getPlanPolicy(organizationId, planId, version, policyId);
    }

    @Override
    public void updatePlanPolicy(String organizationId, String planId, String version, long policyId,
        UpdatePolicyBean bean)
        throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);
        planService.updatePlanPolicy(organizationId, planId, version, policyId, bean);
    }

    @Override
    public void deletePlanPolicy(String organizationId, String planId, String version, long policyId)
        throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);
        planService.deletePlanPolicy(organizationId, planId, version, policyId);
    }

    @Override
    public void deletePlan(String organizationId, String planId)
        throws PlanNotFoundException, NotAuthorizedException, InvalidPlanStatusException {
        securityContext.checkPermissions(PermissionType.planAdmin, organizationId);
        planService.deletePlan(organizationId, planId);
    }

    @Override
    public void reorderPlanPolicies(String organizationId, String planId, String version, PolicyChainBean policyChain)
        throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);
        planService.reorderPlanPolicies(organizationId, planId, version, policyChain);
    }

    @Override
    public void grant(String organizationId, GrantRolesBean bean)
        throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgAdmin, organizationId);
        organizationService.grant(organizationId, bean);
    }

    @Override
    public void revoke(String organizationId, String roleId, String userId)
        throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgAdmin, organizationId);
        organizationService.revoke(organizationId, roleId, userId);
    }

    @Override
    public void revokeAll(String organizationId, String userId)
        throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgAdmin, organizationId);
        organizationService.revokeAll(organizationId, userId);
    }

    @Override
    public List<MemberBean> listMembers(String organizationId)
        throws OrganizationNotFoundException, NotAuthorizedException {
        // Only members are allowed to see other members
        if (!securityContext.isMemberOf(organizationId)) {
            throw ExceptionFactory.notAuthorizedException();
        }
        return organizationService.listMembers(organizationId);
    }

    @Override
    public UsageHistogramBean getUsage(String organizationId, String apiId, String version, HistogramIntervalType interval, String fromDate, String toDate)
            throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);
        return statsService.getUsage(organizationId, apiId, version, interval, fromDate, toDate);
    }

    @Override
    public UsagePerClientBean getUsagePerClient(String organizationId, String apiId, String version, String fromDate, String toDate)
        throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);
        return statsService.getUsagePerClient(organizationId, apiId, version, fromDate, toDate);
    }

    @Override
    public UsagePerPlanBean getUsagePerPlan(String organizationId,
        String apiId, String version, String fromDate, String toDate)
        throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);
        return statsService.getUsagePerPlan(organizationId, apiId, version, fromDate, toDate);
    }

    @Override
    public ResponseStatsHistogramBean getResponseStats(
        String organizationId, String apiId, String version,
        HistogramIntervalType interval, String fromDate, String toDate)
        throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);
        return statsService.getResponseStats(organizationId, apiId, version, interval, fromDate, toDate);
    }

    @Override
    public ResponseStatsSummaryBean getResponseStatsSummary(
        String organizationId, String apiId, String version, String fromDate, String toDate)
        throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);
        return statsService.getResponseStatsSummary(organizationId, apiId, version, fromDate, toDate);
    }

    @Override
    public ResponseStatsPerClientBean getResponseStatsPerClient(
        String organizationId, String apiId, String version, String fromDate, String toDate)
        throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);
        return statsService.getResponseStatsPerClient(organizationId, apiId, version, fromDate, toDate);
    }

    @Override
    public ResponseStatsPerPlanBean getResponseStatsPerPlan(
        String organizationId, String apiId, String version, String fromDate, String toDate)
        throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);
        return statsService.getResponseStatsPerPlan(organizationId, apiId, version, fromDate, toDate);
    }
}
