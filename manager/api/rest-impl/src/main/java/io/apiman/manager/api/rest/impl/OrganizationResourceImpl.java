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

import static java.util.stream.Collectors.toList;

import io.apiman.common.util.crypt.DataEncryptionContext;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.common.util.crypt.DataEncryptionContext.EntityType;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiPlanBean;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.apis.ApiVersionStatusBean;
import io.apiman.manager.api.beans.apis.NewApiBean;
import io.apiman.manager.api.beans.apis.NewApiDefinitionBean;
import io.apiman.manager.api.beans.apis.NewApiVersionBean;
import io.apiman.manager.api.beans.apis.UpdateApiBean;
import io.apiman.manager.api.beans.apis.UpdateApiVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.audit.data.EntityUpdatedData;
import io.apiman.manager.api.beans.audit.data.MembershipData;
import io.apiman.manager.api.beans.clients.ApiKeyBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.clients.NewClientBean;
import io.apiman.manager.api.beans.clients.NewClientVersionBean;
import io.apiman.manager.api.beans.clients.UpdateClientBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.NewContractBean;
import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.download.DownloadType;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.GrantRolesBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.members.MemberBean;
import io.apiman.manager.api.beans.members.MemberRoleBean;
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
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.plans.UpdatePlanBean;
import io.apiman.manager.api.beans.policies.NewPolicyBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyChainBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.policies.UpdatePolicyBean;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiEntryBean;
import io.apiman.manager.api.beans.summary.ApiPlanSummaryBean;
import io.apiman.manager.api.beans.summary.ApiRegistryBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionEndpointSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.ClientVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.core.IApiKeyGenerator;
import io.apiman.manager.api.core.IApiValidator;
import io.apiman.manager.api.core.IClientValidator;
import io.apiman.manager.api.core.IDownloadManager;
import io.apiman.manager.api.core.IMetricsAccessor;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.core.util.PolicyTemplateUtil;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.contract.IOrganizationResource;
import io.apiman.manager.api.rest.contract.IRoleResource;
import io.apiman.manager.api.rest.contract.IUserResource;
import io.apiman.manager.api.rest.contract.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.contract.exceptions.ApiAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ApiDefinitionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ApiNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ApiVersionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ApiVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ClientAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ClientNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ClientVersionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ClientVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ContractAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ContractNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.EntityStillActiveException;
import io.apiman.manager.api.rest.contract.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidApiStatusException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidClientStatusException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidMetricCriteriaException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidParameterException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidPlanStatusException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidVersionException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PlanAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PlanNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PlanVersionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.RoleNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.contract.exceptions.UserNotFoundException;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.i18n.Messages;
import io.apiman.manager.api.rest.impl.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.util.FieldValidator;
import io.apiman.manager.api.security.ISecurityContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.StreamSupport;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Implementation of the Organization API.
 *
 * @author eric.wittmann@redhat.com
 */
@RequestScoped
public class OrganizationResourceImpl implements IOrganizationResource {

    private static final long ONE_MINUTE_MILLIS = 1 * 60 * 1000L;
    private static final long ONE_HOUR_MILLIS = 1 * 60 * 60 * 1000L;
    private static final long ONE_DAY_MILLIS = 1 * 24 * 60 * 60 * 1000L;
    private static final long ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L;
    private static final long ONE_MONTH_MILLIS = 30 * 24 * 60 * 60 * 1000L;

    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject IMetricsAccessor metrics;

    @Inject IClientValidator clientValidator;
    @Inject IApiValidator apiValidator;
    @Inject IApiKeyGenerator apiKeyGenerator;
    @Inject IDataEncrypter encrypter;

    @Inject IDownloadManager downloadManager;

    @Inject IUserResource users;
    @Inject IRoleResource roles;

    @Inject ISecurityContext securityContext;
    @Inject IGatewayLinkFactory gatewayLinkFactory;

    @Context HttpServletRequest request;

    @Inject @ApimanLogger(OrganizationResourceImpl.class) IApimanLogger log;

    /**
     * Constructor.
     */
    public OrganizationResourceImpl() {
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#create(io.apiman.manager.api.beans.orgs.NewOrganizationBean)
     */
    @Override
    public OrganizationBean create(NewOrganizationBean bean) throws OrganizationAlreadyExistsException, InvalidNameException {
        FieldValidator.validateName(bean.getName());

        List<RoleBean> autoGrantedRoles;
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        criteria.setPage(1);
        criteria.setPageSize(100);
        criteria.addFilter("autoGrant", "true", SearchCriteriaFilterOperator.bool_eq); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            autoGrantedRoles = query.findRoles(criteria).getBeans();
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        if ("true".equals(System.getProperty("apiman.manager.require-auto-granted-org", "true"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (autoGrantedRoles.isEmpty()) {
                throw new SystemErrorException(Messages.i18n.format("OrganizationResourceImpl.NoAutoGrantRoleAvailable")); //$NON-NLS-1$
            }
        }

        OrganizationBean orgBean = new OrganizationBean();
        orgBean.setName(bean.getName());
        orgBean.setDescription(bean.getDescription());
        orgBean.setId(BeanUtils.idFromName(bean.getName()));
        orgBean.setCreatedOn(new Date());
        orgBean.setCreatedBy(securityContext.getCurrentUser());
        orgBean.setModifiedOn(new Date());
        orgBean.setModifiedBy(securityContext.getCurrentUser());
        try {
            // Store/persist the new organization
            storage.beginTx();
            if (storage.getOrganization(orgBean.getId()) != null) {
                throw ExceptionFactory.organizationAlreadyExistsException(bean.getName());
            }
            storage.createOrganization(orgBean);
            storage.createAuditEntry(AuditUtils.organizationCreated(orgBean, securityContext));

            // Auto-grant memberships in roles to the creator of the organization
            for (RoleBean roleBean : autoGrantedRoles) {
                String currentUser = securityContext.getCurrentUser();
                String orgId = orgBean.getId();
                RoleMembershipBean membership = RoleMembershipBean.create(currentUser, roleBean.getId(), orgId);
                membership.setCreatedOn(new Date());
                storage.createMembership(membership);
            }
            storage.commitTx();
            log.debug(String.format("Created organization %s: %s", orgBean.getName(), orgBean)); //$NON-NLS-1$
            return orgBean;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    @Override
    public void delete(@PathParam("organizationId") String organizationId) throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException {
        try {
            if (!securityContext.hasPermission(PermissionType.orgAdmin, organizationId))
                throw ExceptionFactory.notAuthorizedException();

            storage.beginTx();
            OrganizationBean organizationBean = storage.getOrganization(organizationId);
            if (organizationBean == null) {
                throw ExceptionFactory.organizationNotFoundException(organizationId);
            }

            // Any active app versions?
            Iterator<ClientVersionBean> clientAppsVers = storage.getAllClientVersions(organizationBean, ClientStatus.Registered, 5);

            if (clientAppsVers.hasNext()) {
                throw ExceptionFactory.entityStillActiveExceptionClientVersions(clientAppsVers);
            }

            // Any active API versions?
            Iterator<ApiVersionBean> apiVers = storage.getAllApiVersions(organizationBean, ApiStatus.Published, 5);
            if (apiVers.hasNext()) {
                throw ExceptionFactory.entityStillActiveExceptionApiVersions(apiVers);
            }

            // Any unbroken contracts?
            Iterator<ContractBean> contracts = storage.getAllContracts(organizationBean, 5);
            if (contracts.hasNext()) {
                throw ExceptionFactory.entityStillActiveExceptionContracts(contracts);
            }

            // Any active plans versions?
            Iterator<PlanVersionBean> planVers = storage.getAllPlanVersions(organizationBean, 5);
            if (planVers.hasNext()) {
                log.warn("There are locked plans(s): these will be deleted."); //$NON-NLS-1$
            }

            // Delete org
            storage.deleteOrganization(organizationBean);
            storage.commitTx();
            log.debug("Deleted Organization: " + organizationBean.getName()); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    @Override
    public void deleteClient(@PathParam("organizationId") String organizationId, @PathParam("clientId") String clientId) throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException {
        try {
            if (!securityContext.hasPermission(PermissionType.clientAdmin, organizationId))
                throw ExceptionFactory.notAuthorizedException();

            storage.beginTx();
            ClientBean client = storage.getClient(organizationId, clientId);
            if (client == null) {
                throw ExceptionFactory.clientNotFoundException(clientId);
            }
            Iterator<ClientVersionBean> clientVersions = storage.getAllClientVersions(organizationId, clientId);
            Iterable<ClientVersionBean> iterable = () -> clientVersions;

            List<ClientVersionBean> registeredElems = StreamSupport.stream(iterable.spliterator(), false)
                    .filter(clientVersion -> clientVersion.getStatus() == ClientStatus.Registered)
                    .limit(5)
                    .collect(toList());

            if (!registeredElems.isEmpty()) {
                throw ExceptionFactory.entityStillActiveExceptionClientVersions(registeredElems);
            }

            storage.deleteClient(client);
            storage.commitTx();
            log.debug("Deleted ClientApp: " + client.getName()); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    @Override
    public void deleteApi(@PathParam("organizationId") String organizationId, @PathParam("apiId") String apiId) throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException {
        try {
            if (!securityContext.hasPermission(PermissionType.apiAdmin, organizationId))
                throw ExceptionFactory.notAuthorizedException();

            storage.beginTx();
            ApiBean api = storage.getApi(organizationId, apiId);
            if (api == null) {
                throw ExceptionFactory.apiNotFoundException(apiId);
            }

            Iterator<ApiVersionBean> apiVersions = storage.getAllApiVersions(organizationId, apiId);
            Iterable<ApiVersionBean> iterable = () -> apiVersions;

            List<ApiVersionBean> registeredElems = StreamSupport.stream(iterable.spliterator(), false)
                    .filter(clientVersion -> clientVersion.getStatus() == ApiStatus.Published)
                    .limit(5)
                    .collect(toList());

            if (!registeredElems.isEmpty()) {
                throw ExceptionFactory.entityStillActiveExceptionApiVersions(registeredElems);
            }
            storage.deleteApi(api);
            storage.commitTx();
            log.debug("Deleted API: " + api.getName()); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#get(java.lang.String)
     */
    @Override
    public OrganizationBean get(String organizationId) throws OrganizationNotFoundException, NotAuthorizedException {
        try {
            storage.beginTx();
            OrganizationBean organizationBean = storage.getOrganization(organizationId);
            if (organizationBean == null) {
                throw ExceptionFactory.organizationNotFoundException(organizationId);
            }
            storage.commitTx();
            log.debug(String.format("Got organization %s: %s", organizationBean.getName(), organizationBean)); //$NON-NLS-1$
            return organizationBean;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#update(java.lang.String, io.apiman.manager.api.beans.orgs.UpdateOrganizationBean)
     */
    @Override
    public void update(String organizationId, UpdateOrganizationBean bean)
            throws OrganizationNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.orgEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            OrganizationBean orgForUpdate = storage.getOrganization(organizationId);
            if (orgForUpdate == null) {
                throw ExceptionFactory.organizationNotFoundException(organizationId);
            }

            EntityUpdatedData auditData = new EntityUpdatedData();
            if (AuditUtils.valueChanged(orgForUpdate.getDescription(), bean.getDescription())) {
                auditData.addChange("description", orgForUpdate.getDescription(), bean.getDescription()); //$NON-NLS-1$
                orgForUpdate.setDescription(bean.getDescription());
            }
            storage.updateOrganization(orgForUpdate);
            storage.createAuditEntry(AuditUtils.organizationUpdated(orgForUpdate, auditData, securityContext));
            storage.commitTx();
            log.debug(String.format("Updated organization %s: %s", orgForUpdate.getName(), orgForUpdate)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#activity(java.lang.String, int, int)
     */
    @Override
    public SearchResultsBean<AuditEntryBean> activity(String organizationId, int page, int pageSize)
            throws OrganizationNotFoundException, NotAuthorizedException {
        if (!securityContext.isMemberOf(organizationId))
            throw ExceptionFactory.notAuthorizedException();
        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }
        try {
            SearchResultsBean<AuditEntryBean> rval;
            PagingBean paging = new PagingBean();
            paging.setPage(page);
            paging.setPageSize(pageSize);
            rval = query.auditEntity(organizationId, null, null, null, paging);
            return rval;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#createClient(java.lang.String, io.apiman.manager.api.beans.clients.NewClientBean)
     */
    @Override
    public ClientBean createClient(String organizationId, NewClientBean bean)
            throws OrganizationNotFoundException, ClientAlreadyExistsException, NotAuthorizedException,
            InvalidNameException {
        if (!securityContext.hasPermission(PermissionType.clientEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        FieldValidator.validateName(bean.getName());

        ClientBean newClient = new ClientBean();
        newClient.setId(BeanUtils.idFromName(bean.getName()));
        newClient.setName(bean.getName());
        newClient.setDescription(bean.getDescription());
        newClient.setCreatedBy(securityContext.getCurrentUser());
        newClient.setCreatedOn(new Date());
        try {
            // Store/persist the new client
            storage.beginTx();
            OrganizationBean org = storage.getOrganization(organizationId);
            if (org == null) {
                throw ExceptionFactory.organizationNotFoundException(organizationId);
            }
            newClient.setOrganization(org);

            if (storage.getClient(org.getId(), newClient.getId()) != null) {
                throw ExceptionFactory.organizationAlreadyExistsException(bean.getName());
            }

            storage.createClient(newClient);
            storage.createAuditEntry(AuditUtils.clientCreated(newClient, securityContext));

            if (bean.getInitialVersion() != null) {
                NewClientVersionBean newClientVersion = new NewClientVersionBean();
                newClientVersion.setVersion(bean.getInitialVersion());
                createClientVersionInternal(newClientVersion, newClient);
            }

            storage.commitTx();

            log.debug(String.format("Created client %s: %s", newClient.getName(), newClient)); //$NON-NLS-1$
            return newClient;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getClient(java.lang.String, java.lang.String)
     */
    @Override
    public ClientBean getClient(String organizationId, String clientId)
            throws ClientNotFoundException, NotAuthorizedException {
        try {
            storage.beginTx();
            ClientBean clientBean = storage.getClient(organizationId, clientId);
            if (clientBean == null) {
                throw ExceptionFactory.clientNotFoundException(clientId);
            }
            storage.commitTx();
            log.debug(String.format("Got client %s: %s", clientBean.getName(), clientBean)); //$NON-NLS-1$
            return clientBean;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getClientActivity(java.lang.String, java.lang.String, int, int)
     */
    @Override
    public SearchResultsBean<AuditEntryBean> getClientActivity(String organizationId, String clientId,
            int page, int pageSize) throws ClientNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.clientView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }
        try {
            SearchResultsBean<AuditEntryBean> rval;
            PagingBean paging = new PagingBean();
            paging.setPage(page);
            paging.setPageSize(pageSize);
            rval = query.auditEntity(organizationId, clientId, null, ClientBean.class, paging);
            return rval;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#listClients(java.lang.String)
     */
    @Override
    public List<ClientSummaryBean> listClients(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        get(organizationId);

        try {
            return query.getClientsInOrg(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#updateClient(java.lang.String, java.lang.String, io.apiman.manager.api.beans.clients.UpdateClientBean)
     */
    @Override
    public void updateClient(String organizationId, String clientId, UpdateClientBean bean)
            throws ClientNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.clientEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            ClientBean clientForUpdate = storage.getClient(organizationId, clientId);
            if (clientForUpdate == null) {
                throw ExceptionFactory.clientNotFoundException(clientId);
            }
            EntityUpdatedData auditData = new EntityUpdatedData();
            if (AuditUtils.valueChanged(clientForUpdate.getDescription(), bean.getDescription())) {
                auditData.addChange("description", clientForUpdate.getDescription(), bean.getDescription()); //$NON-NLS-1$
                clientForUpdate.setDescription(bean.getDescription());
            }
            storage.updateClient(clientForUpdate);
            storage.createAuditEntry(AuditUtils.clientUpdated(clientForUpdate, auditData, securityContext));
            storage.commitTx();
            log.debug(String.format("Updated client %s: %s", clientForUpdate.getName(), clientForUpdate)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#createClientVersion(java.lang.String, java.lang.String, io.apiman.manager.api.beans.clients.NewClientVersionBean)
     */
    @Override
    public ClientVersionBean createClientVersion(String organizationId, String clientId,
            NewClientVersionBean bean) throws ClientNotFoundException, NotAuthorizedException,
            InvalidVersionException, ClientVersionAlreadyExistsException {
        if (!securityContext.hasPermission(PermissionType.clientEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        FieldValidator.validateVersion(bean.getVersion());

        ClientVersionBean newVersion;
        try {
            storage.beginTx();
            ClientBean client = storage.getClient(organizationId, clientId);
            if (client == null) {
                throw ExceptionFactory.clientNotFoundException(clientId);
            }

            if (storage.getClientVersion(organizationId, clientId, bean.getVersion()) != null) {
                throw ExceptionFactory.clientVersionAlreadyExistsException(clientId, bean.getVersion());
            }

            newVersion = createClientVersionInternal(bean, client);
            storage.commitTx();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }

        if (bean.isClone() && bean.getCloneVersion() != null) {
            try {
                List<ContractSummaryBean> contracts = getClientVersionContracts(organizationId, clientId, bean.getCloneVersion());
                for (ContractSummaryBean contract : contracts) {
                    NewContractBean ncb = new NewContractBean();
                    ncb.setPlanId(contract.getPlanId());
                    ncb.setApiId(contract.getApiId());
                    ncb.setApiOrgId(contract.getApiOrganizationId());
                    ncb.setApiVersion(contract.getApiVersion());
                    createContract(organizationId, clientId, newVersion.getVersion(), ncb);
                }
                List<PolicySummaryBean> policies = listClientPolicies(organizationId, clientId, bean.getCloneVersion());
                for (PolicySummaryBean policySummary : policies) {
                    PolicyBean policy = getClientPolicy(organizationId, clientId, bean.getCloneVersion(), policySummary.getId());
                    NewPolicyBean npb = new NewPolicyBean();
                    npb.setDefinitionId(policy.getDefinition().getId());
                    npb.setConfiguration(policy.getConfiguration());
                    createClientPolicy(organizationId, clientId, newVersion.getVersion(), npb);
                }
            } catch (Exception e) {
                // TODO it's ok if the clone fails - we did our best
            }
        }

        return newVersion;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getClientApiKey(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiKeyBean getClientApiKey(String organizationId, String clientId, String version)
            throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException {
        if (!securityContext.hasPermission(PermissionType.clientView, organizationId) ) {
            throw ExceptionFactory.notAuthorizedException();
        }
        ClientVersionBean client = getClientVersionInternal(organizationId, clientId, version, true);
        ApiKeyBean apiKeyBean = new ApiKeyBean();
        apiKeyBean.setApiKey(client.getApikey());
        return apiKeyBean;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#updateClientApiKey(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.clients.ApiKeyBean)
     */
    @Override
    public ApiKeyBean updateClientApiKey(String organizationId, String clientId, String version, ApiKeyBean bean)
            throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException,
            InvalidClientStatusException {
        if (!securityContext.hasPermission(PermissionType.clientEdit, organizationId) ) {
            throw ExceptionFactory.notAuthorizedException();
        }

        try {
            storage.beginTx();
            ClientVersionBean clientVersion = storage.getClientVersion(organizationId, clientId, version);
            if (clientVersion == null) {
                throw ExceptionFactory.clientVersionNotFoundException(clientId, version);
            }

            if (clientVersion.getStatus() == ClientStatus.Registered) {
                throw ExceptionFactory.invalidClientStatusException();
            }

            String newApiKey = bean.getApiKey();
            if (StringUtils.isEmpty(newApiKey)) {
                newApiKey = apiKeyGenerator.generate();
            }

            clientVersion.setApikey(newApiKey);
            clientVersion.setModifiedBy(securityContext.getCurrentUser());
            clientVersion.setModifiedOn(new Date());
            storage.updateClientVersion(clientVersion);

            storage.commitTx();
            log.debug(String.format("Updated an API Key for client %s version %s", clientVersion.getClient().getName(), clientVersion)); //$NON-NLS-1$
            ApiKeyBean rval = new ApiKeyBean();
            rval.setApiKey(newApiKey);
            return rval;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * Creates a new client version.
     * @param bean
     * @param client
     * @throws StorageException
     */
    protected ClientVersionBean createClientVersionInternal(NewClientVersionBean bean,
            ClientBean client) throws StorageException {
        if (!BeanUtils.isValidVersion(bean.getVersion())) {
            throw new StorageException("Invalid/illegal client version: " + bean.getVersion()); //$NON-NLS-1$
        }

        ClientVersionBean newVersion = new ClientVersionBean();
        newVersion.setClient(client);
        newVersion.setCreatedBy(securityContext.getCurrentUser());
        newVersion.setCreatedOn(new Date());
        newVersion.setModifiedBy(securityContext.getCurrentUser());
        newVersion.setModifiedOn(new Date());
        newVersion.setStatus(ClientStatus.Created);
        newVersion.setVersion(bean.getVersion());
        newVersion.setApikey(bean.getApiKey());
        if (newVersion.getApikey() == null) {
            newVersion.setApikey(apiKeyGenerator.generate());
        }

        storage.createClientVersion(newVersion);
        storage.createAuditEntry(AuditUtils.clientVersionCreated(newVersion, securityContext));

        log.debug(String.format("Created new client version %s: %s", newVersion.getClient().getName(), newVersion)); //$NON-NLS-1$
        return newVersion;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getClientVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ClientVersionBean getClientVersion(String organizationId, String clientId, String version)
            throws ClientVersionNotFoundException, NotAuthorizedException {
        boolean hasPermission = securityContext.hasPermission(PermissionType.clientView, organizationId);
        return getClientVersionInternal(organizationId, clientId, version, hasPermission);
    }

    /**
     * Does the same thing as getClientVersion() but accepts the 'hasPermission' param,
     * which lets callers dictate whether the user has clientView permission for the org.
     * @param organizationId
     * @param clientId
     * @param version
     * @param hasPermission
     */
    protected ClientVersionBean getClientVersionInternal(String organizationId, String clientId, String version,
            boolean hasPermission) {
        try {
            storage.beginTx();
            ClientVersionBean clientVersion = storage.getClientVersion(organizationId, clientId, version);
            if (clientVersion == null) {
                throw ExceptionFactory.clientVersionNotFoundException(clientId, version);
            }
            // Hide some data if the user doesn't have the clientView permission
            if (!hasPermission) {
                clientVersion.setApikey(null);
            }
            storage.commitTx();
            log.debug(String.format("Got new client version %s: %s", clientVersion.getClient().getName(), clientVersion)); //$NON-NLS-1$
            return clientVersion;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getClientVersionActivity(java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    @Override
    public SearchResultsBean<AuditEntryBean> getClientVersionActivity(String organizationId,
            String clientId, String version, int page, int pageSize)
            throws ClientVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.clientView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }
        try {
            SearchResultsBean<AuditEntryBean> rval;
            PagingBean paging = new PagingBean();
            paging.setPage(page);
            paging.setPageSize(pageSize);
            rval = query.auditEntity(organizationId, clientId, version, ClientBean.class, paging);
            return rval;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getClientUsagePerApi(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ClientUsagePerApiBean getClientUsagePerApi(String organizationId, String clientId,
            String version, String fromDate, String toDate) throws NotAuthorizedException,
            InvalidMetricCriteriaException {
        if (!securityContext.hasPermission(PermissionType.clientView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);

        return metrics.getClientUsagePerApi(organizationId, clientId, version, from, to);
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#listClientVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ClientVersionSummaryBean> listClientVersions(String organizationId, String clientId)
            throws ClientNotFoundException, NotAuthorizedException {
        // Try to get the client first - will throw a ClientNotFoundException if not found.
        getClient(organizationId, clientId);

        try {
            List<ClientVersionSummaryBean> clientVersions = query.getClientVersions(organizationId, clientId);
            boolean hasPermission = securityContext.hasPermission(PermissionType.clientView, organizationId);
            if (!hasPermission) {
                for (ClientVersionSummaryBean clientVersionSummaryBean : clientVersions) {
                    clientVersionSummaryBean.setApiKey(null);
                }
            }
            return clientVersions;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#createContract(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.contracts.NewContractBean)
     */
    @Override
    public ContractBean createContract(String organizationId, String clientId, String version,
            NewContractBean bean) throws OrganizationNotFoundException, ClientNotFoundException,
            ApiNotFoundException, PlanNotFoundException, ContractAlreadyExistsException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.clientEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        try {
            storage.beginTx();
            ContractBean contract = createContractInternal(organizationId, clientId, version, bean);

            storage.commitTx();
            log.debug(String.format("Created new contract %s: %s", contract.getId(), contract)); //$NON-NLS-1$
            return contract;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            // Up above we are optimistically creating the contract.  If it fails, check to see
            // if it failed because it was a duplicate.  If so, throw something sensible.  We
            // only do this on failure (we would get a FK contraint failure, for example) to
            // reduce overhead on the typical happy path.
            if (contractAlreadyExists(organizationId, clientId, version, bean)) {
                throw ExceptionFactory.contractAlreadyExistsException();
            } else {
                throw new SystemErrorException(e);
            }
        }
    }

    /**
     * Creates a contract.
     * @param organizationId
     * @param clientId
     * @param version
     * @param bean
     * @throws StorageException
     * @throws Exception
     */
    protected ContractBean createContractInternal(String organizationId, String clientId,
            String version, NewContractBean bean) throws StorageException, Exception {
        ContractBean contract;
        ClientVersionBean cvb;
        cvb = storage.getClientVersion(organizationId, clientId, version);
        if (cvb == null) {
            throw ExceptionFactory.clientVersionNotFoundException(clientId, version);
        }
        if (cvb.getStatus() == ClientStatus.Retired) {
            throw ExceptionFactory.invalidClientStatusException();
        }
        ApiVersionBean avb = storage.getApiVersion(bean.getApiOrgId(), bean.getApiId(), bean.getApiVersion());
        if (avb == null) {
            throw ExceptionFactory.apiNotFoundException(bean.getApiId());
        }
        if (avb.getStatus() != ApiStatus.Published) {
            throw ExceptionFactory.invalidApiStatusException();
        }
        Set<ApiPlanBean> plans = avb.getPlans();
        String planVersion = null;
        if (plans != null) {
            for (ApiPlanBean apiPlanBean : plans) {
                if (apiPlanBean.getPlanId().equals(bean.getPlanId())) {
                    planVersion = apiPlanBean.getVersion();
                }
            }
        }
        if (planVersion == null) {
            throw ExceptionFactory.planNotFoundException(bean.getPlanId());
        }
        PlanVersionBean pvb = storage.getPlanVersion(bean.getApiOrgId(), bean.getPlanId(), planVersion);
        if (pvb == null) {
            throw ExceptionFactory.planNotFoundException(bean.getPlanId());
        }
        if (pvb.getStatus() != PlanStatus.Locked) {
            throw ExceptionFactory.invalidPlanStatusException();
        }

        contract = new ContractBean();
        contract.setClient(cvb);
        contract.setApi(avb);
        contract.setPlan(pvb);
        contract.setCreatedBy(securityContext.getCurrentUser());
        contract.setCreatedOn(new Date());

        // Move the client to the "Ready" state if necessary.
        if (cvb.getStatus() == ClientStatus.Created && clientValidator.isReady(cvb, true)) {
            cvb.setStatus(ClientStatus.Ready);
        }

        storage.createContract(contract);
        storage.createAuditEntry(AuditUtils.contractCreatedFromClient(contract, securityContext));
        storage.createAuditEntry(AuditUtils.contractCreatedToApi(contract, securityContext));

        // Update the version with new meta-data (e.g. modified-by)
        cvb.setModifiedBy(securityContext.getCurrentUser());
        cvb.setModifiedOn(new Date());
        storage.updateClientVersion(cvb);

        return contract;
    }

    /**
     * Check to see if the contract already exists, by getting a list of all the
     * client's contracts and comparing with the one being created.
     * @param organizationId
     * @param clientId
     * @param version
     * @param bean
     */
    private boolean contractAlreadyExists(String organizationId, String clientId, String version,
            NewContractBean bean) {
        try {
            List<ContractSummaryBean> contracts = query.getClientContracts(organizationId, clientId, version);
            for (ContractSummaryBean contract : contracts) {
                if (contract.getApiOrganizationId().equals(bean.getApiOrgId()) &&
                    contract.getApiId().equals(bean.getApiId()) &&
                    contract.getApiVersion().equals(bean.getApiVersion()) &&
                    contract.getPlanId().equals(bean.getPlanId()))
                {
                    return true;
                }
            }
            return false;
        } catch (StorageException e) {
            return false;
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getContract(java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public ContractBean getContract(String organizationId, String clientId, String version,
            Long contractId) throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException {
        try {
            storage.beginTx();
            ContractBean contract = storage.getContract(contractId);
            if (contract == null)
                throw ExceptionFactory.contractNotFoundException(contractId);

            storage.commitTx();

            log.debug(String.format("Got contract %s: %s", contract.getId(), contract)); //$NON-NLS-1$
            return contract;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#deleteAllContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void deleteAllContracts(String organizationId, String clientId, String version)
            throws ClientNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.clientEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        List<ContractSummaryBean> contracts = getClientVersionContracts(organizationId, clientId, version);
        for (ContractSummaryBean contract : contracts) {
            deleteContract(organizationId, clientId, version, contract.getContractId());
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#deleteContract(java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public void deleteContract(String organizationId, String clientId, String version, Long contractId)
            throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException,
            InvalidClientStatusException {
        if (!securityContext.hasPermission(PermissionType.clientEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            ContractBean contract = storage.getContract(contractId);
            if (contract == null) {
                throw ExceptionFactory.contractNotFoundException(contractId);
            }
            if (!contract.getClient().getClient().getOrganization().getId().equals(organizationId)) {
                throw ExceptionFactory.contractNotFoundException(contractId);
            }
            if (!contract.getClient().getClient().getId().equals(clientId)) {
                throw ExceptionFactory.contractNotFoundException(contractId);
            }
            if (!contract.getClient().getVersion().equals(version)) {
                throw ExceptionFactory.contractNotFoundException(contractId);
            }
            if (contract.getClient().getStatus() == ClientStatus.Retired) {
                throw ExceptionFactory.invalidClientStatusException();
            }
            storage.deleteContract(contract);
            storage.createAuditEntry(AuditUtils.contractBrokenFromClient(contract, securityContext));
            storage.createAuditEntry(AuditUtils.contractBrokenToApi(contract, securityContext));

            // Update the version with new meta-data (e.g. modified-by)
            ClientVersionBean clientV = storage.getClientVersion(organizationId, clientId, version);
            clientV.setModifiedBy(securityContext.getCurrentUser());
            clientV.setModifiedOn(new Date());
            storage.updateClientVersion(clientV);

            storage.commitTx();
            log.debug(String.format("Deleted contract: %s", contract)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getClientVersionContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ContractSummaryBean> getClientVersionContracts(String organizationId, String clientId, String version)
            throws ClientNotFoundException, NotAuthorizedException {
        // Try to get the client first - will throw a ClientNotFoundException if not found.
        getClientVersion(organizationId, clientId, version);

        try {
            List<ContractSummaryBean> contracts = query.getClientContracts(organizationId, clientId, version);
            return contracts;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiRegistryJSON(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Response getApiRegistryJSON(String organizationId, String clientId, String version,
            String download) throws ClientNotFoundException, NotAuthorizedException {
        boolean hasPermission = securityContext.hasPermission(PermissionType.clientView, organizationId);
        if ("true".equals(download)) { //$NON-NLS-1$
            try {
                String path = String.format("%s/%s/%s/%s", organizationId, clientId, version, (hasPermission ? '+' : '-' )); //$NON-NLS-1$
                DownloadBean dbean = downloadManager.createDownload(DownloadType.apiRegistryJson, path);
                return Response.ok(dbean, MediaType.APPLICATION_JSON).build();
            } catch (StorageException e) {
                throw new SystemErrorException(e);
            }
        } else {
            return getApiRegistryJSON(organizationId, clientId, version, hasPermission);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiRegistryJSON(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public Response getApiRegistryJSON(String organizationId, String clientId, String version,
            boolean hasPermission) throws ClientNotFoundException, NotAuthorizedException {
        ApiRegistryBean apiRegistry = getApiRegistry(organizationId, clientId, version, hasPermission);
        return Response.ok(apiRegistry, MediaType.APPLICATION_JSON)
                .header("Content-Disposition", "attachment; filename=api-registry.json") //$NON-NLS-1$ //$NON-NLS-2$
                .build();
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiRegistryXML(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Response getApiRegistryXML(String organizationId, String clientId, String version,
            String download) throws ClientNotFoundException, NotAuthorizedException {
        boolean hasPermission = securityContext.hasPermission(PermissionType.clientView, organizationId);
        if ("true".equals(download)) { //$NON-NLS-1$
            try {
                String path = String.format("%s/%s/%s/%s", organizationId, clientId, version, (hasPermission ? '+' : '-' )); //$NON-NLS-1$
                DownloadBean dbean = downloadManager.createDownload(DownloadType.apiRegistryXml, path);
                return Response.ok(dbean, MediaType.APPLICATION_JSON).build();
            } catch (StorageException e) {
                throw new SystemErrorException(e);
            }
        } else {
            return getApiRegistryXML(organizationId, clientId, version, hasPermission);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiRegistryXML(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public Response getApiRegistryXML(String organizationId, String clientId, String version,
            boolean hasPermission) throws ClientNotFoundException, NotAuthorizedException {
        ApiRegistryBean apiRegistry = getApiRegistry(organizationId, clientId, version, hasPermission);
        return Response.ok(apiRegistry, MediaType.APPLICATION_XML)
                .header("Content-Disposition", "attachment; filename=api-registry.xml") //$NON-NLS-1$ //$NON-NLS-2$
                .build();
    }

    /**
     * Gets the API registry.
     * @param organizationId
     * @param clientId
     * @param version
     * @param hasPermission
     * @throws ClientNotFoundException
     * @throws NotAuthorizedException
     */
    protected ApiRegistryBean getApiRegistry(String organizationId, String clientId, String version,
            boolean hasPermission) throws ClientNotFoundException, NotAuthorizedException {
        // Try to get the client first - will throw a ClientNotFoundException if not found.
        ClientVersionBean clientVersion = getClientVersionInternal(organizationId, clientId, version, hasPermission);

        Map<String, IGatewayLink> gatewayLinks = new HashMap<>();
        Map<String, GatewayBean> gateways = new HashMap<>();
        boolean txStarted = false;
        try {
            ApiRegistryBean apiRegistry = query.getApiRegistry(organizationId, clientId, version);

            // Hide some stuff if the user doesn't have the clientView permission
            if (hasPermission) {
                apiRegistry.setApiKey(clientVersion.getApikey());
            }

            List<ApiEntryBean> apis = apiRegistry.getApis();

            storage.beginTx();
            txStarted = true;
            for (ApiEntryBean api : apis) {
                String gatewayId = api.getGatewayId();
                // Don't return the gateway id.
                api.setGatewayId(null);
                GatewayBean gateway = gateways.get(gatewayId);
                if (gateway == null) {
                    gateway = storage.getGateway(gatewayId);
                    gateways.put(gatewayId, gateway);
                }
                IGatewayLink link = gatewayLinks.get(gatewayId);
                if (link == null) {
                    link = gatewayLinkFactory.create(gateway);
                    gatewayLinks.put(gatewayId, link);
                }

                ApiEndpoint se = link.getApiEndpoint(api.getApiOrgId(), api.getApiId(), api.getApiVersion());
                String apiEndpoint = se.getEndpoint();
                api.setHttpEndpoint(apiEndpoint);
            }

            return apiRegistry;
        } catch (StorageException|GatewayAuthenticationException e) {
            throw new SystemErrorException(e);
        } finally {
            if (txStarted) {
                storage.rollbackTx();
            }
            for (IGatewayLink link : gatewayLinks.values()) {
                link.close();
            }
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#createClientPolicy(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.NewPolicyBean)
     */
    @Override
    public PolicyBean createClientPolicy(String organizationId, String clientId, String version,
            NewPolicyBean bean) throws OrganizationNotFoundException, ClientVersionNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.clientEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the Client exists
        ClientVersionBean cvb = getClientVersion(organizationId, clientId, version);

        PolicyBean policy = doCreatePolicy(organizationId, clientId, version, bean, PolicyType.Client);

        try {
            storage.beginTx();
            cvb.setModifiedBy(securityContext.getCurrentUser());
            cvb.setModifiedOn(new Date());
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            log.error(e);
        }

        return policy;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getClientPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public PolicyBean getClientPolicy(String organizationId, String clientId, String version, long policyId)
            throws OrganizationNotFoundException, ClientVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        boolean hasPermission = securityContext.hasPermission(PermissionType.clientView, organizationId);
        // Make sure the client version exists
        getClientVersion(organizationId, clientId, version);

        PolicyBean policy = doGetPolicy(PolicyType.Client, organizationId, clientId, version, policyId);

        if (!hasPermission) {
            policy.setConfiguration(null);
        }

        return policy;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#updateClientPolicy(java.lang.String, java.lang.String, java.lang.String, long, io.apiman.manager.api.beans.policies.UpdatePolicyBean)
     */
    @Override
    public void updateClientPolicy(String organizationId, String clientId, String version,
            long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
            ClientVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.clientEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the client version exists.
        ClientVersionBean cvb = getClientVersion(organizationId, clientId, version);

        try {
            storage.beginTx();
            PolicyBean policy = this.storage.getPolicy(PolicyType.Client, organizationId, clientId, version, policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            if (AuditUtils.valueChanged(policy.getConfiguration(), bean.getConfiguration())) {
                policy.setConfiguration(bean.getConfiguration());
                // TODO figure out what changed and include that in the audit entry
            }
            policy.setModifiedOn(new Date());
            policy.setModifiedBy(this.securityContext.getCurrentUser());
            storage.updatePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyUpdated(policy, PolicyType.Client, securityContext));

            cvb.setModifiedOn(new Date());
            cvb.setModifiedBy(securityContext.getCurrentUser());
            storage.updateClientVersion(cvb);

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
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#deleteClientPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public void deleteClientPolicy(String organizationId, String clientId, String version, long policyId)
            throws OrganizationNotFoundException, ClientVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.clientEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the client version exists;
        ClientVersionBean cvb = getClientVersion(organizationId, clientId, version);

        try {
            storage.beginTx();
            PolicyBean policy = this.storage.getPolicy(PolicyType.Client, organizationId, clientId, version, policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            storage.deletePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyRemoved(policy, PolicyType.Client, securityContext));

            cvb.setModifiedBy(securityContext.getCurrentUser());
            cvb.setModifiedOn(new Date());
            storage.updateClientVersion(cvb);

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
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#listClientPolicies(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<PolicySummaryBean> listClientPolicies(String organizationId, String clientId, String version)
            throws OrganizationNotFoundException, ClientVersionNotFoundException, NotAuthorizedException {
        // Try to get the client first - will throw an exception if not found.
        getClientVersion(organizationId, clientId, version);

        try {
            return query.getPolicies(organizationId, clientId, version, PolicyType.Client);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#reorderClientPolicies(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyChainBean)
     */
    @Override
    public void reorderClientPolicies(String organizationId, String clientId, String version,
            PolicyChainBean policyChain) throws OrganizationNotFoundException,
            ClientVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.clientEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the client version exists.
        ClientVersionBean cvb = getClientVersion(organizationId, clientId, version);

        try {
            storage.beginTx();
            List<Long> newOrder = new ArrayList<>(policyChain.getPolicies().size());
            for (PolicySummaryBean psb : policyChain.getPolicies()) {
                newOrder.add(psb.getId());
            }
            storage.reorderPolicies(PolicyType.Client, organizationId, clientId, version, newOrder);
            storage.createAuditEntry(AuditUtils.policiesReordered(cvb, PolicyType.Client, securityContext));

            cvb.setModifiedBy(securityContext.getCurrentUser());
            cvb.setModifiedOn(new Date());
            storage.updateClientVersion(cvb);

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
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#createApi(java.lang.String, io.apiman.manager.api.beans.apis.NewApiBean)
     */
    @Override
    public ApiBean createApi(String organizationId, NewApiBean bean)
            throws OrganizationNotFoundException, ApiAlreadyExistsException, NotAuthorizedException,
            InvalidNameException {
        if (!securityContext.hasPermission(PermissionType.apiEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        FieldValidator.validateName(bean.getName());

        ApiBean newApi = new ApiBean();
        newApi.setName(bean.getName());
        newApi.setDescription(bean.getDescription());
        newApi.setId(BeanUtils.idFromName(bean.getName()));
        newApi.setCreatedOn(new Date());
        newApi.setCreatedBy(securityContext.getCurrentUser());
        try {
            GatewaySummaryBean gateway = getSingularGateway();

            storage.beginTx();
            OrganizationBean orgBean = storage.getOrganization(organizationId);
            if (orgBean == null) {
                throw ExceptionFactory.organizationNotFoundException(organizationId);
            }
            if (storage.getApi(orgBean.getId(), newApi.getId()) != null) {
                throw ExceptionFactory.apiAlreadyExistsException(bean.getName());
            }
            newApi.setOrganization(orgBean);
            // Store/persist the new API
            storage.createApi(newApi);
            storage.createAuditEntry(AuditUtils.apiCreated(newApi, securityContext));

            if (bean.getInitialVersion() != null) {
                NewApiVersionBean newApiVersion = new NewApiVersionBean();
                newApiVersion.setEndpoint(bean.getEndpoint());
                newApiVersion.setEndpointType(bean.getEndpointType());
                newApiVersion.setEndpointContentType(bean.getEndpointContentType());
                newApiVersion.setPlans(bean.getPlans());
                newApiVersion.setPublicAPI(bean.getPublicAPI());
                newApiVersion.setVersion(bean.getInitialVersion());
                newApiVersion.setDefinitionUrl(bean.getDefinitionUrl());
                newApiVersion.setDefinitionType(bean.getDefinitionType());
                createApiVersionInternal(newApiVersion, newApi, gateway);
            }

            storage.commitTx();
            return newApi;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApi(java.lang.String, java.lang.String)
     */
    @Override
    public ApiBean getApi(String organizationId, String apiId)
            throws ApiNotFoundException, NotAuthorizedException {
        try {
            storage.beginTx();
            ApiBean bean = storage.getApi(organizationId, apiId);
            if (bean == null) {
                throw ExceptionFactory.apiNotFoundException(apiId);
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
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiActivity(java.lang.String, java.lang.String, int, int)
     */
    @Override
    public SearchResultsBean<AuditEntryBean> getApiActivity(String organizationId, String apiId,
            int page, int pageSize) throws ApiNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.apiView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }
        try {
            SearchResultsBean<AuditEntryBean> rval;
            PagingBean paging = new PagingBean();
            paging.setPage(page);
            paging.setPageSize(pageSize);
            rval = query.auditEntity(organizationId, apiId, null, ApiBean.class, paging);
            return rval;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#listApi(java.lang.String)
     */
    @Override
    public List<ApiSummaryBean> listApi(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        // make sure the org exists
        get(organizationId);

        try {
            return query.getApisInOrg(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#updateApi(java.lang.String, java.lang.String, io.apiman.manager.api.beans.apis.UpdateApiBean)
     */
    @Override
    public void updateApi(String organizationId, String apiId, UpdateApiBean bean)
            throws ApiNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.apiEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            ApiBean apiForUpdate = storage.getApi(organizationId, apiId);
            if (apiForUpdate == null) {
                throw ExceptionFactory.apiNotFoundException(apiId);
            }
            EntityUpdatedData auditData = new EntityUpdatedData();
            if (AuditUtils.valueChanged(apiForUpdate.getDescription(), bean.getDescription())) {
                auditData.addChange("description", apiForUpdate.getDescription(), bean.getDescription()); //$NON-NLS-1$
                apiForUpdate.setDescription(bean.getDescription());
            }
            storage.updateApi(apiForUpdate);
            storage.createAuditEntry(AuditUtils.apiUpdated(apiForUpdate, auditData, securityContext));
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
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#createApiVersion(java.lang.String, java.lang.String, io.apiman.manager.api.beans.apis.NewApiVersionBean)
     */
    @Override
    public ApiVersionBean createApiVersion(String organizationId, String apiId,
            NewApiVersionBean bean) throws ApiNotFoundException, NotAuthorizedException,
            InvalidVersionException, ApiVersionAlreadyExistsException {
        if (!securityContext.hasPermission(PermissionType.apiEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        FieldValidator.validateVersion(bean.getVersion());

        ApiVersionBean newVersion;
        try {
            GatewaySummaryBean gateway = getSingularGateway();

            storage.beginTx();
            ApiBean api = storage.getApi(organizationId, apiId);
            if (api == null) {
                throw ExceptionFactory.apiNotFoundException(apiId);
            }

            if (storage.getApiVersion(organizationId, apiId, bean.getVersion()) != null) {
                throw ExceptionFactory.apiVersionAlreadyExistsException(apiId, bean.getVersion());
            }

            newVersion = createApiVersionInternal(bean, api, gateway);
            storage.commitTx();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }

        if (bean.isClone() && bean.getCloneVersion() != null) {
            try {
                ApiVersionBean cloneSource = getApiVersion(organizationId, apiId, bean.getCloneVersion());

                // Clone primary attributes of the API version unless those attributes
                // were included in the NewApiVersionBean.  In other words, information
                // sent as part of the "create version" payload take precedence over the
                // cloned attributes.
                UpdateApiVersionBean updatedApi = new UpdateApiVersionBean();
                if (bean.getEndpoint() == null) {
                    updatedApi.setEndpoint(cloneSource.getEndpoint());
                }
                if (bean.getEndpointType() == null) {
                    updatedApi.setEndpointType(cloneSource.getEndpointType());
                }
                if (bean.getEndpointContentType() == null) {
                    updatedApi.setEndpointContentType(cloneSource.getEndpointContentType());
                }
                updatedApi.setEndpointProperties(cloneSource.getEndpointProperties());
                updatedApi.setGateways(cloneSource.getGateways());
                if (bean.getPlans() == null) {
                    updatedApi.setPlans(cloneSource.getPlans());
                }
                if (bean.getPublicAPI() == null) {
                    updatedApi.setPublicAPI(cloneSource.isPublicAPI());
                }
                newVersion = updateApiVersion(organizationId, apiId, bean.getVersion(), updatedApi);

                if (bean.getDefinitionUrl() == null) {
                    // Clone the API definition document
                    InputStream definition = null;
                    try {
                        Response response = getApiDefinition(organizationId, apiId, bean.getCloneVersion());
                        definition = (InputStream) response.getEntity();
                        storeApiDefinition(organizationId, apiId, newVersion.getVersion(),
                                cloneSource.getDefinitionType(), definition);
                    } catch (ApiDefinitionNotFoundException svnfe) {
                        // This is ok - it just means the API doesn't have one, so do nothing.
                    } catch (Exception sdnfe) {
                        log.error("Unable to create response", sdnfe); //$NON-NLS-1$
                    } finally {
                        IOUtils.closeQuietly(definition);
                    }
                }

                // Clone all API policies
                List<PolicySummaryBean> policies = listApiPolicies(organizationId, apiId, bean.getCloneVersion());
                for (PolicySummaryBean policySummary : policies) {
                    PolicyBean policy = getApiPolicy(organizationId, apiId, bean.getCloneVersion(), policySummary.getId());
                    NewPolicyBean npb = new NewPolicyBean();
                    npb.setDefinitionId(policy.getDefinition().getId());
                    npb.setConfiguration(policy.getConfiguration());
                    createApiPolicy(organizationId, apiId, newVersion.getVersion(), npb);
                }
            } catch (Exception e) {
                // TODO it's ok if the clone fails - we did our best
                if (e != null) {
                    Throwable t = e;
                    e = (Exception) t;
                }
            }
        }

        return newVersion;
    }

    /**
     * Creates an API version.
     * @param bean
     * @param api
     * @param gateway
     * @throws Exception
     * @throws StorageException
     */
    protected ApiVersionBean createApiVersionInternal(NewApiVersionBean bean,
            ApiBean api, GatewaySummaryBean gateway) throws Exception, StorageException {
        if (!BeanUtils.isValidVersion(bean.getVersion())) {
            throw new StorageException("Invalid/illegal API version: " + bean.getVersion()); //$NON-NLS-1$
        }

        ApiVersionBean newVersion = new ApiVersionBean();
        newVersion.setVersion(bean.getVersion());
        newVersion.setCreatedBy(securityContext.getCurrentUser());
        newVersion.setCreatedOn(new Date());
        newVersion.setModifiedBy(securityContext.getCurrentUser());
        newVersion.setModifiedOn(new Date());
        newVersion.setStatus(ApiStatus.Created);
        newVersion.setApi(api);
        newVersion.setEndpoint(bean.getEndpoint());
        newVersion.setEndpointType(bean.getEndpointType());
        newVersion.setEndpointContentType(bean.getEndpointContentType());
        if (bean.getPublicAPI() != null) {
            newVersion.setPublicAPI(bean.getPublicAPI());
        }
        if (bean.getPlans() != null) {
            newVersion.setPlans(bean.getPlans());
        }
        if (bean.getDefinitionType() != null) {
            newVersion.setDefinitionType(bean.getDefinitionType());
        }

        if (gateway != null && newVersion.getGateways() == null) {
            newVersion.setGateways(new HashSet<>());
            ApiGatewayBean sgb = new ApiGatewayBean();
            sgb.setGatewayId(gateway.getId());
            newVersion.getGateways().add(sgb);
        }

        if (apiValidator.isReady(newVersion)) {
            newVersion.setStatus(ApiStatus.Ready);
        } else {
            newVersion.setStatus(ApiStatus.Created);
        }

        // Ensure all of the plans are in the right status (locked)
        Set<ApiPlanBean> plans = newVersion.getPlans();
        if (plans != null) {
            for (ApiPlanBean splanBean : plans) {
                String orgId = newVersion.getApi().getOrganization().getId();
                PlanVersionBean pvb = storage.getPlanVersion(orgId, splanBean.getPlanId(), splanBean.getVersion());
                if (pvb == null) {
                    throw new StorageException(Messages.i18n.format("PlanVersionDoesNotExist", splanBean.getPlanId(), splanBean.getVersion())); //$NON-NLS-1$
                }
                if (pvb.getStatus() != PlanStatus.Locked) {
                    throw new StorageException(Messages.i18n.format("PlanNotLocked", splanBean.getPlanId(), splanBean.getVersion())); //$NON-NLS-1$
                }
            }
        }

        storage.createApiVersion(newVersion);
        storage.createAuditEntry(AuditUtils.apiVersionCreated(newVersion, securityContext));

        if (bean.getDefinitionUrl() != null) {
            InputStream definition = null;
            try {
                definition = new URL(bean.getDefinitionUrl()).openStream();
                storage.updateApiDefinition(newVersion, definition);
            } catch (Exception e) {
                log.error("Unable to store API definition from: " + bean.getDefinitionUrl(), e); //$NON-NLS-1$
            } finally {
                IOUtils.closeQuietly(definition);
            }
        }

        return newVersion;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiVersionBean getApiVersion(String organizationId, String apiId, String version)
            throws ApiVersionNotFoundException, NotAuthorizedException {
        boolean hasPermission = securityContext.hasPermission(PermissionType.apiView, organizationId);
        try {
            storage.beginTx();
            ApiVersionBean apiVersion = storage.getApiVersion(organizationId, apiId, version);
            if (apiVersion == null) {
                throw ExceptionFactory.apiVersionNotFoundException(apiId, version);
            }
            storage.commitTx();
            if (!hasPermission) {
                apiVersion.setGateways(null);
            }
            decryptEndpointProperties(apiVersion);
            return apiVersion;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiVersionStatus(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiVersionStatusBean getApiVersionStatus(String organizationId, String apiId,
            String version) throws ApiVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.apiView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        ApiVersionBean versionBean = getApiVersion(organizationId, apiId, version);
        List<PolicySummaryBean> policies = listApiPolicies(organizationId, apiId, version);
        return apiValidator.getStatus(versionBean, policies);
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiDefinition(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Response getApiDefinition(String organizationId, String apiId, String version)
            throws ApiVersionNotFoundException, NotAuthorizedException {
        try {
            storage.beginTx();
            ApiVersionBean apiVersion = storage.getApiVersion(organizationId, apiId, version);
            if (apiVersion == null) {
                throw ExceptionFactory.apiVersionNotFoundException(apiId, version);
            }
            if (apiVersion.getDefinitionType() == ApiDefinitionType.None || apiVersion.getDefinitionType() == null) {
                throw ExceptionFactory.apiDefinitionNotFoundException(apiId, version);
            }
            InputStream definition = storage.getApiDefinition(apiVersion);
            if (definition == null) {
                throw ExceptionFactory.apiDefinitionNotFoundException(apiId, version);
            }
            ResponseBuilder builder = Response.ok().entity(definition);
            if (apiVersion.getDefinitionType() == ApiDefinitionType.SwaggerJSON) {
                builder.type(MediaType.APPLICATION_JSON);
            } else if (apiVersion.getDefinitionType() == ApiDefinitionType.SwaggerYAML) {
                builder.type("application/x-yaml"); //$NON-NLS-1$
            } else if (apiVersion.getDefinitionType() == ApiDefinitionType.WSDL) {
                builder.type("application/wsdl+xml"); //$NON-NLS-1$
            } else {
                IOUtils.closeQuietly(definition);
                throw new Exception("API definition type not supported: " + apiVersion.getDefinitionType()); //$NON-NLS-1$
            }
            storage.commitTx();
            return builder.build();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiVersionEndpointInfo(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiVersionEndpointSummaryBean getApiVersionEndpointInfo(String organizationId,
            String apiId, String version) throws ApiVersionNotFoundException,
            InvalidApiStatusException, GatewayNotFoundException {
        try {
            storage.beginTx();
            ApiVersionBean apiVersion = storage.getApiVersion(organizationId, apiId, version);
            if (apiVersion == null) {
                throw ExceptionFactory.apiVersionNotFoundException(apiId, version);
            }
            if (apiVersion.getStatus() != ApiStatus.Published) {
                throw new InvalidApiStatusException(Messages.i18n.format("ApiNotPublished")); //$NON-NLS-1$
            }
            Set<ApiGatewayBean> gateways = apiVersion.getGateways();
            if (gateways.isEmpty()) {
                throw new SystemErrorException("No Gateways for published API!"); //$NON-NLS-1$
            }
            GatewayBean gateway = storage.getGateway(gateways.iterator().next().getGatewayId());
            if (gateway == null) {
                throw new GatewayNotFoundException();
            }
            IGatewayLink link = gatewayLinkFactory.create(gateway);
            ApiEndpoint endpoint = link.getApiEndpoint(organizationId, apiId, version);
            ApiVersionEndpointSummaryBean rval = new ApiVersionEndpointSummaryBean();
            rval.setManagedEndpoint(endpoint.getEndpoint());
            storage.commitTx();
            log.debug(String.format("Got endpoint summary: %s", gateway)); //$NON-NLS-1$
            return rval;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiVersionActivity(java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    @Override
    public SearchResultsBean<AuditEntryBean> getApiVersionActivity(String organizationId,
            String apiId, String version, int page, int pageSize) throws ApiVersionNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.apiView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }
        try {
            SearchResultsBean<AuditEntryBean> rval;
            PagingBean paging = new PagingBean();
            paging.setPage(page);
            paging.setPageSize(pageSize);
            rval = query.auditEntity(organizationId, apiId, version, ApiBean.class, paging);
            return rval;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#updateApiVersion(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.apis.UpdateApiVersionBean)
     */
    @Override
    public ApiVersionBean updateApiVersion(String organizationId, String apiId, String version,
            UpdateApiVersionBean bean) throws ApiVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.apiEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);
        if (avb.isPublicAPI()) {
            if (avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        } else {
            if (avb.getStatus() == ApiStatus.Published || avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        }

        avb.setModifiedBy(securityContext.getCurrentUser());
        avb.setModifiedOn(new Date());
        EntityUpdatedData data = new EntityUpdatedData();
        if (AuditUtils.valueChanged(avb.getPlans(), bean.getPlans())) {
            data.addChange("plans", AuditUtils.asString_ApiPlanBeans(avb.getPlans()), AuditUtils.asString_ApiPlanBeans(bean.getPlans())); //$NON-NLS-1$
            if (avb.getPlans() == null) {
                avb.setPlans(new HashSet<>());
            }
            avb.getPlans().clear();
            if (bean.getPlans() != null) {
                avb.getPlans().addAll(bean.getPlans());
            }
        }
        if (AuditUtils.valueChanged(avb.getGateways(), bean.getGateways())) {
            data.addChange("gateways", AuditUtils.asString_ApiGatewayBeans(avb.getGateways()), AuditUtils.asString_ApiGatewayBeans(bean.getGateways())); //$NON-NLS-1$
            if (avb.getGateways() == null) {
                avb.setGateways(new HashSet<>());
            }
            avb.getGateways().clear();
            avb.getGateways().addAll(bean.getGateways());
        }
        if (AuditUtils.valueChanged(avb.getEndpoint(), bean.getEndpoint())) {
            // validate the endpoint is a URL
            validateEndpoint(bean.getEndpoint());
            data.addChange("endpoint", avb.getEndpoint(), bean.getEndpoint()); //$NON-NLS-1$
            avb.setEndpoint(bean.getEndpoint());
        }
        if (AuditUtils.valueChanged(avb.getEndpointType(), bean.getEndpointType())) {
            data.addChange("endpointType", avb.getEndpointType(), bean.getEndpointType()); //$NON-NLS-1$
            avb.setEndpointType(bean.getEndpointType());
        }
        if (AuditUtils.valueChanged(avb.getEndpointContentType(), bean.getEndpointContentType())) {
            data.addChange("endpointContentType", avb.getEndpointContentType(), bean.getEndpointContentType()); //$NON-NLS-1$
            avb.setEndpointContentType(bean.getEndpointContentType());
        }
        if (AuditUtils.valueChanged(avb.getEndpointProperties(), bean.getEndpointProperties())) {
            if (avb.getEndpointProperties() == null) {
                avb.setEndpointProperties(new HashMap<>());
            } else {
                avb.getEndpointProperties().clear();
            }
            if (bean.getEndpointProperties() != null) {
                avb.getEndpointProperties().putAll(bean.getEndpointProperties());
            }
        }
        if (AuditUtils.valueChanged(avb.isPublicAPI(), bean.getPublicAPI())) {
            data.addChange("publicAPI", String.valueOf(avb.isPublicAPI()), String.valueOf(bean.getPublicAPI())); //$NON-NLS-1$
            avb.setPublicAPI(bean.getPublicAPI());
        }

        try {
            if (avb.getGateways() == null || avb.getGateways().isEmpty()) {
                GatewaySummaryBean gateway = getSingularGateway();
                if (gateway != null && avb.getGateways() == null) {
                    avb.setGateways(new HashSet<>());
                    ApiGatewayBean sgb = new ApiGatewayBean();
                    sgb.setGatewayId(gateway.getId());
                    avb.getGateways().add(sgb);
                }
            }

            if (avb.getStatus() != ApiStatus.Published) {
                if (apiValidator.isReady(avb)) {
                    avb.setStatus(ApiStatus.Ready);
                } else {
                    avb.setStatus(ApiStatus.Created);
                }
            } else {
                if (!apiValidator.isReady(avb)) {
                    throw ExceptionFactory.invalidApiStatusException();
                }
            }
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }

        try {
            encryptEndpointProperties(avb);
            storage.beginTx();

            // Ensure all of the plans are in the right status (locked)
            Set<ApiPlanBean> plans = avb.getPlans();
            if (plans != null) {
                for (ApiPlanBean splanBean : plans) {
                    String orgId = avb.getApi().getOrganization().getId();
                    PlanVersionBean pvb = storage.getPlanVersion(orgId, splanBean.getPlanId(), splanBean.getVersion());
                    if (pvb == null) {
                        throw new StorageException(Messages.i18n.format("PlanVersionDoesNotExist", splanBean.getPlanId(), splanBean.getVersion())); //$NON-NLS-1$
                    }
                    if (pvb.getStatus() != PlanStatus.Locked) {
                        throw new StorageException(Messages.i18n.format("PlanNotLocked", splanBean.getPlanId(), splanBean.getVersion())); //$NON-NLS-1$
                    }
                }
            }

            storage.updateApiVersion(avb);
            storage.createAuditEntry(AuditUtils.apiVersionUpdated(avb, data, securityContext));
            storage.commitTx();
            log.debug(String.format("Successfully updated API Version: %s", avb)); //$NON-NLS-1$
            decryptEndpointProperties(avb);
            return avb;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#updateApiDefinition(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void updateApiDefinition(String organizationId, String apiId, String version)
            throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException {
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
            storeApiDefinition(organizationId, apiId, version, newDefinitionType, data);
            log.debug(String.format("Updated API definition for %s", apiId)); //$NON-NLS-1$
        } finally {
            IOUtils.closeQuietly(data);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#updateApiDefinitionFromURL(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.apis.NewApiDefinitionBean)
     */
    @Override
    public void updateApiDefinitionFromURL(String organizationId, String apiId, String version,
            NewApiDefinitionBean bean) throws ApiVersionNotFoundException, NotAuthorizedException,
                    InvalidApiStatusException {
        InputStream data;
        try {
            String definitionURL = bean.getDefinitionUrl();
            URL url = new URL(definitionURL);
            data = url.openStream();
        } catch (IOException e) {
            throw new SystemErrorException(e);
        }
        try {
            storeApiDefinition(organizationId, apiId, version, bean.getDefinitionType(), data);
            log.debug(String.format("Updated API definition for %s from URL %s", apiId, bean.getDefinitionUrl())); //$NON-NLS-1$
        } finally {
            IOUtils.closeQuietly(data);
        }
    }

    protected void storeApiDefinition(String organizationId, String apiId, String version,
            ApiDefinitionType definitionType, InputStream data) {
        if (!securityContext.hasPermission(PermissionType.apiEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            ApiVersionBean apiVersion = storage.getApiVersion(organizationId, apiId, version);
            if (apiVersion == null) {
                throw ExceptionFactory.apiVersionNotFoundException(apiId, version);
            }
            if (apiVersion.getDefinitionType() != definitionType) {
                apiVersion.setDefinitionType(definitionType);
                storage.updateApiVersion(apiVersion);
            }
            storage.createAuditEntry(AuditUtils.apiDefinitionUpdated(apiVersion, securityContext));
            storage.updateApiDefinition(apiVersion, data);

            apiVersion.setModifiedOn(new Date());
            apiVersion.setModifiedBy(securityContext.getCurrentUser());
            storage.updateApiVersion(apiVersion);

            storage.commitTx();
            log.debug(String.format("Stored API definition %s: %s", apiId, apiVersion)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#listApiVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ApiVersionSummaryBean> listApiVersions(String organizationId, String apiId)
            throws ApiNotFoundException, NotAuthorizedException {
        // Try to get the API first - will throw a ApiNotFoundException if not found.
        getApi(organizationId, apiId);

        try {
            return query.getApiVersions(organizationId, apiId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiVersionPlans(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ApiPlanSummaryBean> getApiVersionPlans(String organizationId, String apiId,
            String version) throws ApiVersionNotFoundException, NotAuthorizedException {
        // Ensure the version exists first.
        getApiVersion(organizationId, apiId, version);

        try {
            return query.getApiVersionPlans(organizationId, apiId, version);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#createApiPolicy(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.NewPolicyBean)
     */
    @Override
    public PolicyBean createApiPolicy(String organizationId, String apiId, String version,
            NewPolicyBean bean) throws OrganizationNotFoundException, ApiVersionNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.apiEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the API exists
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);
        if (avb.isPublicAPI()) {
            if (avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        } else {
            if (avb.getStatus() == ApiStatus.Published || avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        }

        PolicyBean policy = doCreatePolicy(organizationId, apiId, version, bean, PolicyType.Api);
        log.debug(String.format("Created API policy %s", avb)); //$NON-NLS-1$

        try {
            storage.beginTx();
            avb.setModifiedOn(new Date());
            avb.setModifiedBy(securityContext.getCurrentUser());
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            log.error(e);
        }

        return policy;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public PolicyBean getApiPolicy(String organizationId, String apiId, String version, long policyId)
            throws OrganizationNotFoundException, ApiVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {

        // Make sure the API exists
        getApiVersion(organizationId, apiId, version);

        PolicyBean policy = doGetPolicy(PolicyType.Api, organizationId, apiId, version, policyId);

        if (!securityContext.hasPermission(PermissionType.apiView, organizationId)) {
            policy.setConfiguration(null);
        }

        return policy;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#updateApiPolicy(java.lang.String,
     *      java.lang.String, java.lang.String, long, io.apiman.manager.api.beans.policies.UpdatePolicyBean)
     */
    @Override
    public void updateApiPolicy(String organizationId, String apiId, String version,
            long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
            ApiVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.apiEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the API exists
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        try {
            storage.beginTx();
            PolicyBean policy = storage.getPolicy(PolicyType.Api, organizationId, apiId, version, policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            // TODO capture specific change values when auditing policy updates
            if (AuditUtils.valueChanged(policy.getConfiguration(), bean.getConfiguration())) {
                policy.setConfiguration(bean.getConfiguration());
            }
            policy.setModifiedOn(new Date());
            policy.setModifiedBy(securityContext.getCurrentUser());
            storage.updatePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyUpdated(policy, PolicyType.Api, securityContext));

            avb.setModifiedBy(securityContext.getCurrentUser());
            avb.setModifiedOn(new Date());
            storage.updateApiVersion(avb);

            storage.commitTx();
            log.debug(String.format("Updated API policy %s", policy)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#deleteApiPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public void deleteApiPolicy(String organizationId, String apiId, String version, long policyId)
            throws OrganizationNotFoundException, ApiVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.apiEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the API exists and is in the right status.
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);
        if (avb.isPublicAPI()) {
            if (avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        } else {
            if (avb.getStatus() == ApiStatus.Published || avb.getStatus() == ApiStatus.Retired) {
                throw ExceptionFactory.invalidApiStatusException();
            }
        }

        try {
            storage.beginTx();
            PolicyBean policy = this.storage.getPolicy(PolicyType.Api, organizationId, apiId, version, policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            storage.deletePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyRemoved(policy, PolicyType.Api, securityContext));

            avb.setModifiedBy(securityContext.getCurrentUser());
            avb.setModifiedOn(new Date());
            storage.updateApiVersion(avb);

            storage.commitTx();
            log.debug(String.format("Deleted API %s policy: %s", apiId, policy)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#deleteApiDefinition(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void deleteApiDefinition(String organizationId, String apiId, String version)
            throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.apiEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            ApiVersionBean apiVersion = storage.getApiVersion(organizationId, apiId, version);
            if (apiVersion == null) {
                throw ExceptionFactory.apiVersionNotFoundException(apiId, version);
            }
            apiVersion.setDefinitionType(ApiDefinitionType.None);
            apiVersion.setModifiedBy(securityContext.getCurrentUser());
            apiVersion.setModifiedOn(new Date());
            storage.createAuditEntry(AuditUtils.apiDefinitionDeleted(apiVersion, securityContext));
            storage.deleteApiDefinition(apiVersion);
            storage.updateApiVersion(apiVersion);
            storage.commitTx();
            log.debug(String.format("Deleted API %s definition %s", apiId, apiVersion)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#listApiPolicies(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<PolicySummaryBean> listApiPolicies(String organizationId, String apiId, String version)
            throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        // Try to get the API first - will throw an exception if not found.
        getApiVersion(organizationId, apiId, version);

        try {
            return query.getPolicies(organizationId, apiId, version, PolicyType.Api);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#reorderApiPolicies(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyChainBean)
     */
    @Override
    public void reorderApiPolicies(String organizationId, String apiId, String version,
            PolicyChainBean policyChain) throws OrganizationNotFoundException,
            ApiVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.apiEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the API exists
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        try {
            storage.beginTx();
            List<Long> newOrder = new ArrayList<>(policyChain.getPolicies().size());
            for (PolicySummaryBean psb : policyChain.getPolicies()) {
                newOrder.add(psb.getId());
            }
            storage.reorderPolicies(PolicyType.Api, organizationId, apiId, version, newOrder);
            storage.createAuditEntry(AuditUtils.policiesReordered(avb, PolicyType.Api, securityContext));

            avb.setModifiedBy(securityContext.getCurrentUser());
            avb.setModifiedOn(new Date());
            storage.updateApiVersion(avb);

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
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiPolicyChain(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PolicyChainBean getApiPolicyChain(String organizationId, String apiId, String version,
            String planId) throws ApiVersionNotFoundException, PlanNotFoundException, NotAuthorizedException {
        // Try to get the API first - will throw an exception if not found.
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        try {
            String planVersion = null;
            Set<ApiPlanBean> plans = avb.getPlans();
            if (plans != null) {
                for (ApiPlanBean apiPlanBean : plans) {
                    if (apiPlanBean.getPlanId().equals(planId)) {
                        planVersion = apiPlanBean.getVersion();
                        break;
                    }
                }
            }
            if (planVersion == null) {
                throw ExceptionFactory.planNotFoundException(planId);
            }
            List<PolicySummaryBean> apiPolicies = query.getPolicies(organizationId, apiId, version, PolicyType.Api);
            List<PolicySummaryBean> planPolicies = query.getPolicies(organizationId, planId, planVersion, PolicyType.Plan);

            PolicyChainBean chain = new PolicyChainBean();
            chain.getPolicies().addAll(planPolicies);
            chain.getPolicies().addAll(apiPolicies);
            return chain;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getApiVersionContracts(java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    @Override
    public List<ContractSummaryBean> getApiVersionContracts(String organizationId,
            String apiId, String version, int page, int pageSize) throws ApiVersionNotFoundException,
            NotAuthorizedException {
        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }

        // Try to get the API first - will throw an exception if not found.
        getApiVersion(organizationId, apiId, version);

        try {
            List<ContractSummaryBean> contracts = query.getContracts(organizationId, apiId, version, page, pageSize);
            log.debug(String.format("Got API %s version %s contracts: %s", apiId, version, contracts)); //$NON-NLS-1$
            return contracts;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getUsage(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, java.lang.String, java.lang.String)
     */
    @Override
    public UsageHistogramBean getUsage(String organizationId, String apiId, String version,
            HistogramIntervalType interval, String fromDate, String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException {
        if (!securityContext.hasPermission(PermissionType.apiView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);

        if (interval == null) {
            interval = HistogramIntervalType.day;
        }
        validateMetricRange(from, to);
        validateTimeSeriesMetric(from, to, interval);
        return metrics.getUsage(organizationId, apiId, version, interval, from, to);
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getUsagePerClient(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public UsagePerClientBean getUsagePerClient(String organizationId, String apiId, String version,
            String fromDate, String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException {
        if (!securityContext.hasPermission(PermissionType.apiView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);
        return metrics.getUsagePerClient(organizationId, apiId, version, from, to);
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getUsagePerPlan(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public UsagePerPlanBean getUsagePerPlan(String organizationId, String apiId, String version,
            String fromDate, String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException {
        if (!securityContext.hasPermission(PermissionType.apiView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);
        return metrics.getUsagePerPlan(organizationId, apiId, version, from, to);
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getResponseStats(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, java.lang.String, java.lang.String)
     */
    @Override
    public ResponseStatsHistogramBean getResponseStats(String organizationId, String apiId,
            String version, HistogramIntervalType interval, String fromDate, String toDate)
            throws NotAuthorizedException, InvalidMetricCriteriaException {
        if (!securityContext.hasPermission(PermissionType.apiView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        if (interval == null) {
            interval = HistogramIntervalType.day;
        }
        validateMetricRange(from, to);
        validateTimeSeriesMetric(from, to, interval);
        return metrics.getResponseStats(organizationId, apiId, version, interval, from, to);
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getResponseStatsSummary(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ResponseStatsSummaryBean getResponseStatsSummary(String organizationId, String apiId,
            String version, String fromDate, String toDate) throws NotAuthorizedException,
            InvalidMetricCriteriaException {
        if (!securityContext.hasPermission(PermissionType.apiView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);
        return metrics.getResponseStatsSummary(organizationId, apiId, version, from, to);
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getResponseStatsPerClient(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ResponseStatsPerClientBean getResponseStatsPerClient(String organizationId, String apiId,
            String version, String fromDate, String toDate) throws NotAuthorizedException,
            InvalidMetricCriteriaException {
        if (!securityContext.hasPermission(PermissionType.apiView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);
        return metrics.getResponseStatsPerClient(organizationId, apiId, version, from, to);
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getResponseStatsPerPlan(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ResponseStatsPerPlanBean getResponseStatsPerPlan(String organizationId, String apiId,
            String version, String fromDate, String toDate) throws NotAuthorizedException,
            InvalidMetricCriteriaException {
        if (!securityContext.hasPermission(PermissionType.apiView, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);
        return metrics.getResponseStatsPerPlan(organizationId, apiId, version, from, to);
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#createPlan(java.lang.String,
     *      io.apiman.manager.api.beans.plans.NewPlanBean)
     */
    @Override
    public PlanBean createPlan(String organizationId, NewPlanBean bean) throws OrganizationNotFoundException,
            PlanAlreadyExistsException, NotAuthorizedException, InvalidNameException {
        if (!securityContext.hasPermission(PermissionType.planEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        FieldValidator.validateName(bean.getName());

        PlanBean newPlan = new PlanBean();
        newPlan.setName(bean.getName());
        newPlan.setDescription(bean.getDescription());
        newPlan.setId(BeanUtils.idFromName(bean.getName()));
        newPlan.setCreatedOn(new Date());
        newPlan.setCreatedBy(securityContext.getCurrentUser());
        try {
            // Store/persist the new plan
            storage.beginTx();
            OrganizationBean orgBean = storage.getOrganization(organizationId);
            if (orgBean == null) {
                throw ExceptionFactory.organizationNotFoundException(organizationId);
            }
            if (storage.getPlan(orgBean.getId(), newPlan.getId()) != null) {
                throw ExceptionFactory.planAlreadyExistsException(newPlan.getName());
            }
            newPlan.setOrganization(orgBean);
            storage.createPlan(newPlan);
            storage.createAuditEntry(AuditUtils.planCreated(newPlan, securityContext));

            if (bean.getInitialVersion() != null) {
                NewPlanVersionBean newPlanVersion = new NewPlanVersionBean();
                newPlanVersion.setVersion(bean.getInitialVersion());
                createPlanVersionInternal(newPlanVersion, newPlan);
            }

            storage.commitTx();
            log.debug(String.format("Created plan: %s", newPlan)); //$NON-NLS-1$
            return newPlan;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getPlan(java.lang.String, java.lang.String)
     */
    @Override
    public PlanBean getPlan(String organizationId, String planId)
            throws PlanNotFoundException, NotAuthorizedException {
        try {
            storage.beginTx();
            PlanBean bean = storage.getPlan(organizationId, planId);
            if (bean == null) {
                throw ExceptionFactory.planNotFoundException(planId);
            }
            storage.commitTx();
            log.debug(String.format("Got plan: %s", bean)); //$NON-NLS-1$
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
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getPlanActivity(java.lang.String, java.lang.String, int, int)
     */
    @Override
    public SearchResultsBean<AuditEntryBean> getPlanActivity(String organizationId, String planId, int page, int pageSize)
            throws PlanNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.planView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }
        try {
            SearchResultsBean<AuditEntryBean> rval;
            PagingBean paging = new PagingBean();
            paging.setPage(page);
            paging.setPageSize(pageSize);
            rval = query.auditEntity(organizationId, planId, null, PlanBean.class, paging);
            return rval;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#listPlans(java.lang.String)
     */
    @Override
    public List<PlanSummaryBean> listPlans(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        get(organizationId);

        try {
            return query.getPlansInOrg(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#updatePlan(java.lang.String,
     * java.lang.String, io.apiman.manager.api.beans.plans.UpdatePlanBean)
     */
    @Override
    public void updatePlan(String organizationId, String planId, UpdatePlanBean bean)
            throws PlanNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.planEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        EntityUpdatedData auditData = new EntityUpdatedData();
        try {
            storage.beginTx();
            PlanBean planForUpdate = storage.getPlan(organizationId, planId);
            if (planForUpdate == null) {
                throw ExceptionFactory.planNotFoundException(planId);
            }
            if (AuditUtils.valueChanged(planForUpdate.getDescription(), bean.getDescription())) {
                auditData.addChange("description", planForUpdate.getDescription(), bean.getDescription()); //$NON-NLS-1$
                planForUpdate.setDescription(bean.getDescription());
            }
            storage.updatePlan(planForUpdate);
            storage.createAuditEntry(AuditUtils.planUpdated(planForUpdate, auditData, securityContext));
            storage.commitTx();
            log.debug(String.format("Updated plan: %s", planForUpdate)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#createPlanVersion(java.lang.String,
     *      java.lang.String, io.apiman.manager.api.beans.plans.NewPlanVersionBean)
     */
    @Override
    public PlanVersionBean createPlanVersion(String organizationId, String planId, NewPlanVersionBean bean)
            throws PlanNotFoundException, NotAuthorizedException, InvalidVersionException,
            PlanVersionAlreadyExistsException {
        if (!securityContext.hasPermission(PermissionType.planEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        FieldValidator.validateVersion(bean.getVersion());

        PlanVersionBean newVersion;
        try {
            storage.beginTx();
            PlanBean plan = storage.getPlan(organizationId, planId);
            if (plan == null) {
                throw ExceptionFactory.planNotFoundException(planId);
            }

            if (storage.getPlanVersion(organizationId, planId, bean.getVersion()) != null) {
                throw ExceptionFactory.planVersionAlreadyExistsException(planId, bean.getVersion());
            }

            newVersion = createPlanVersionInternal(bean, plan);
            storage.commitTx();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }

        if (bean.isClone() && bean.getCloneVersion() != null) {
            try {
                List<PolicySummaryBean> policies = listPlanPolicies(organizationId, planId, bean.getCloneVersion());
                for (PolicySummaryBean policySummary : policies) {
                    PolicyBean policy = getPlanPolicy(organizationId, planId, bean.getCloneVersion(), policySummary.getId());
                    NewPolicyBean npb = new NewPolicyBean();
                    npb.setDefinitionId(policy.getDefinition().getId());
                    npb.setConfiguration(policy.getConfiguration());
                    createPlanPolicy(organizationId, planId, newVersion.getVersion(), npb);
                }
            } catch (Exception e) {
                // TODO it's ok if the clone fails - we did our best
            }
        }

        log.debug(String.format("Created plan %s version: %s", planId, newVersion)); //$NON-NLS-1$
        return newVersion;
    }

    /**
     * Creates a plan version.
     * @param bean
     * @param plan
     * @throws StorageException
     */
    protected PlanVersionBean createPlanVersionInternal(NewPlanVersionBean bean, PlanBean plan)
            throws StorageException {
        if (!BeanUtils.isValidVersion(bean.getVersion())) {
            throw new StorageException("Invalid/illegal plan version: " + bean.getVersion()); //$NON-NLS-1$
        }

        PlanVersionBean newVersion = new PlanVersionBean();
        newVersion.setCreatedBy(securityContext.getCurrentUser());
        newVersion.setCreatedOn(new Date());
        newVersion.setModifiedBy(securityContext.getCurrentUser());
        newVersion.setModifiedOn(new Date());
        newVersion.setStatus(PlanStatus.Created);
        newVersion.setPlan(plan);
        newVersion.setVersion(bean.getVersion());
        storage.createPlanVersion(newVersion);
        storage.createAuditEntry(AuditUtils.planVersionCreated(newVersion, securityContext));
        return newVersion;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getPlanVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version)
            throws PlanVersionNotFoundException, NotAuthorizedException {
        try {
            storage.beginTx();
            PlanVersionBean planVersion = storage.getPlanVersion(organizationId, planId, version);
            if (planVersion == null) {
                throw ExceptionFactory.planVersionNotFoundException(planId, version);
            }
            storage.commitTx();
            log.debug(String.format("Got plan %s version: %s", planId, planVersion)); //$NON-NLS-1$
            return planVersion;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getPlanVersionActivity(java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    @Override
    public SearchResultsBean<AuditEntryBean> getPlanVersionActivity(String organizationId, String planId,
            String version, int page, int pageSize) throws PlanVersionNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.planView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }
        try {
            SearchResultsBean<AuditEntryBean> rval;
            PagingBean paging = new PagingBean();
            paging.setPage(page);
            paging.setPageSize(pageSize);
            rval = query.auditEntity(organizationId, planId, version, PlanBean.class, paging);
            return rval;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#listPlanVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<PlanVersionSummaryBean> listPlanVersions(String organizationId, String planId)
            throws PlanNotFoundException, NotAuthorizedException {
        // Try to get the plan first - will throw a PlanNotFoundException if not found.
        getPlan(organizationId, planId);

        try {
            return query.getPlanVersions(organizationId, planId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#createPlanPolicy(java.lang.String,
     *      java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.NewPolicyBean)
     */
    @Override
    public PolicyBean createPlanPolicy(String organizationId, String planId, String version,
            NewPolicyBean bean) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.planEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the plan version exists and is in the right state
        PlanVersionBean pvb = getPlanVersion(organizationId, planId, version);
        if (pvb.getStatus() == PlanStatus.Locked) {
            throw ExceptionFactory.invalidPlanStatusException();
        }

        log.debug(String.format("Creating plan %s policy %s", planId, pvb)); //$NON-NLS-1$
        PolicyBean policy = doCreatePolicy(organizationId, planId, version, bean, PolicyType.Plan);

        try {
            storage.beginTx();
            pvb.setModifiedOn(new Date());
            pvb.setModifiedBy(securityContext.getCurrentUser());
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            log.error(e);
        }

        return policy;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#getPlanPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public PolicyBean getPlanPolicy(String organizationId, String planId, String version, long policyId)
            throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        boolean hasPermission = securityContext.hasPermission(PermissionType.planView, organizationId);

        // Make sure the plan version exists
        getPlanVersion(organizationId, planId, version);

        PolicyBean policy = doGetPolicy(PolicyType.Plan, organizationId, planId, version, policyId);

        if (!hasPermission) {
            policy.setConfiguration(null);
        }

        log.debug(String.format("Got plan policy %s", policy)); //$NON-NLS-1$
        return policy;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#updatePlanPolicy(java.lang.String,
     *      java.lang.String, java.lang.String, long, io.apiman.manager.api.beans.policies.UpdatePolicyBean)
     */
    @Override
    public void updatePlanPolicy(String organizationId, String planId, String version,
            long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
            PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.planEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the plan version exists
        PlanVersionBean pvb = getPlanVersion(organizationId, planId, version);

        try {
            storage.beginTx();
            PolicyBean policy = storage.getPolicy(PolicyType.Plan, organizationId, planId, version, policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            if (AuditUtils.valueChanged(policy.getConfiguration(), bean.getConfiguration())) {
                policy.setConfiguration(bean.getConfiguration());
                // Note: we do not audit the policy configuration since it may have sensitive data
            }
            policy.setModifiedOn(new Date());
            policy.setModifiedBy(this.securityContext.getCurrentUser());
            storage.updatePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyUpdated(policy, PolicyType.Plan, securityContext));

            pvb.setModifiedBy(securityContext.getCurrentUser());
            pvb.setModifiedOn(new Date());
            storage.updatePlanVersion(pvb);

            storage.commitTx();
            log.debug(String.format("Updated plan policy %s", policy)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#deletePlanPolicy(java.lang.String, java.lang.String, java.lang.String, long)
     */
    @Override
    public void deletePlanPolicy(String organizationId, String planId, String version, long policyId)
            throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.planEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the plan version exists
        PlanVersionBean pvb = getPlanVersion(organizationId, planId, version);
        if (pvb.getStatus() == PlanStatus.Locked) {
            throw ExceptionFactory.invalidPlanStatusException();
        }

        try {
            storage.beginTx();
            PolicyBean policy = this.storage.getPolicy(PolicyType.Plan, organizationId, planId, version, policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            storage.deletePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyRemoved(policy, PolicyType.Plan, securityContext));

            pvb.setModifiedBy(securityContext.getCurrentUser());
            pvb.setModifiedOn(new Date());
            storage.updatePlanVersion(pvb);

            storage.commitTx();
            log.debug(String.format("Deleted plan policy %s", policy)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    @Override
    public void deletePlan(@PathParam("organizationId") String organizationId, @PathParam("planId") String planId)
            throws ApiNotFoundException, NotAuthorizedException, InvalidPlanStatusException {
        if (!securityContext.hasPermission(PermissionType.planAdmin, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        List<PlanVersionSummaryBean> lockedPlans = listPlanVersions(organizationId, planId).stream()
                .filter(summary -> summary.getStatus() == PlanStatus.Locked).collect(toList());

        if (!lockedPlans.isEmpty())
            throw ExceptionFactory.invalidPlanStatusException(lockedPlans);

        try {
            storage.beginTx();
            PlanBean plan = storage.getPlan(organizationId, planId);
            storage.deletePlan(plan);
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
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#listPlanPolicies(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<PolicySummaryBean> listPlanPolicies(String organizationId, String planId, String version)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        // Try to get the plan first - will throw an exception if not found.
        getPlanVersion(organizationId, planId, version);

        try {
            return query.getPolicies(organizationId, planId, version, PolicyType.Plan);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#reorderPlanPolicies(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyChainBean)
     */
    @Override
    public void reorderPlanPolicies(String organizationId, String planId, String version,
            PolicyChainBean policyChain) throws OrganizationNotFoundException,
            PlanVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.planEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();

        // Make sure the plan version exists
        PlanVersionBean pvb = getPlanVersion(organizationId, planId, version);

        try {
            storage.beginTx();
            List<Long> newOrder = new ArrayList<>(policyChain.getPolicies().size());
            for (PolicySummaryBean psb : policyChain.getPolicies()) {
                newOrder.add(psb.getId());
            }
            storage.reorderPolicies(PolicyType.Plan, organizationId, planId, version, newOrder);
            storage.createAuditEntry(AuditUtils.policiesReordered(pvb, PolicyType.Plan, securityContext));

            pvb.setModifiedBy(securityContext.getCurrentUser());
            pvb.setModifiedOn(new Date());
            storage.updatePlanVersion(pvb);

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
     * Creates a policy for the given entity (supports creating policies for clients,
     * APIs, and plans).
     *
     * @param organizationId
     * @param entityId
     * @param entityVersion
     * @param bean
     * @return the stored policy bean (with updated information)
     * @throws NotAuthorizedException
     */
    protected PolicyBean doCreatePolicy(String organizationId, String entityId, String entityVersion,
            NewPolicyBean bean, PolicyType type) throws PolicyDefinitionNotFoundException {
        if (bean.getDefinitionId() == null) {
            throw ExceptionFactory.policyDefNotFoundException("null"); //$NON-NLS-1$
        }
        PolicyDefinitionBean def;
        try {
            storage.beginTx();
            def = storage.getPolicyDefinition(bean.getDefinitionId());
            if (def == null) {
                throw ExceptionFactory.policyDefNotFoundException(bean.getDefinitionId());
            }
            storage.rollbackTx();
        } catch (AbstractRestException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }

        int newIdx;
        try {
            newIdx = query.getMaxPolicyOrderIndex(organizationId, entityId, entityVersion, type) + 1;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        try {
            PolicyBean policy = new PolicyBean();
            policy.setId(null);
            policy.setDefinition(def);
            policy.setName(def.getName());
            policy.setConfiguration(bean.getConfiguration());
            policy.setCreatedBy(securityContext.getCurrentUser());
            policy.setCreatedOn(new Date());
            policy.setModifiedBy(securityContext.getCurrentUser());
            policy.setModifiedOn(new Date());
            policy.setOrganizationId(organizationId);
            policy.setEntityId(entityId);
            policy.setEntityVersion(entityVersion);
            policy.setType(type);
            policy.setOrderIndex(newIdx);

            storage.beginTx();

            if (type == PolicyType.Client) {
                ClientVersionBean cvb = storage.getClientVersion(organizationId, entityId, entityVersion);
                cvb.setModifiedBy(securityContext.getCurrentUser());
                cvb.setModifiedOn(new Date());
                storage.updateClientVersion(cvb);
            } else if (type == PolicyType.Api) {
                ApiVersionBean avb = storage.getApiVersion(organizationId, entityId, entityVersion);
                avb.setModifiedBy(securityContext.getCurrentUser());
                avb.setModifiedOn(new Date());
                storage.updateApiVersion(avb);
            } else if (type == PolicyType.Plan) {
                PlanVersionBean pvb = storage.getPlanVersion(organizationId, entityId, entityVersion);
                pvb.setModifiedBy(securityContext.getCurrentUser());
                pvb.setModifiedOn(new Date());
                storage.updatePlanVersion(pvb);
            }

            storage.createPolicy(policy);
            storage.createAuditEntry(AuditUtils.policyAdded(policy, type, securityContext));
            storage.commitTx();

            PolicyTemplateUtil.generatePolicyDescription(policy);

            log.debug(String.format("Created client policy: %s", policy)); //$NON-NLS-1$
            return policy;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#grant(java.lang.String, io.apiman.manager.api.beans.idm.GrantRolesBean)
     */
    @Override
    public void grant(String organizationId, GrantRolesBean bean) throws OrganizationNotFoundException,
            RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.orgAdmin, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        // Verify that the references are valid.
        get(organizationId);
        users.get(bean.getUserId());
        for (String roleId : bean.getRoleIds()) {
            roles.get(roleId);
        }

        MembershipData auditData = new MembershipData();
        auditData.setUserId(bean.getUserId());
        try {
            storage.beginTx();
            for (String roleId : bean.getRoleIds()) {
                RoleMembershipBean membership = RoleMembershipBean.create(bean.getUserId(), roleId, organizationId);
                membership.setCreatedOn(new Date());
                // If the membership already exists, that's fine!
                if (storage.getMembership(bean.getUserId(), roleId, organizationId) == null) {
                    storage.createMembership(membership);
                }
                auditData.addRole(roleId);
            }
            storage.createAuditEntry(AuditUtils.membershipGranted(organizationId, auditData, securityContext));
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
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#revoke(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void revoke(String organizationId, String roleId, String userId)
            throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.orgAdmin, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        get(organizationId);
        users.get(userId);
        roles.get(roleId);

        MembershipData auditData = new MembershipData();
        auditData.setUserId(userId);

        try {
            storage.beginTx();
            storage.deleteMembership(userId, roleId, organizationId);
            auditData.addRole(roleId);
            storage.createAuditEntry(AuditUtils.membershipRevoked(organizationId, auditData, securityContext));
            storage.commitTx();
            log.debug(String.format("Revoked User %s Role %s Org %s", userId, roleId, organizationId)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#revokeAll(java.lang.String, java.lang.String)
     */
    @Override
    public void revokeAll(String organizationId, String userId) throws OrganizationNotFoundException,
            RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.orgAdmin, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        get(organizationId);
        users.get(userId);

        MembershipData auditData = new MembershipData();
        auditData.setUserId(userId);
        auditData.addRole("*"); //$NON-NLS-1$
        try {
            storage.beginTx();
            storage.deleteMemberships(userId, organizationId);
            storage.createAuditEntry(AuditUtils.membershipRevoked(organizationId, auditData, securityContext));
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
     * @see io.apiman.manager.api.rest.contract.IOrganizationResource#listMembers(java.lang.String)
     */
    @Override
    public List<MemberBean> listMembers(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        get(organizationId);

        try {
            Set<RoleMembershipBean> memberships = query.getOrgMemberships(organizationId);
            TreeMap<String, MemberBean> members = new TreeMap<>();
            storage.beginTx();
            for (RoleMembershipBean membershipBean : memberships) {
                String userId = membershipBean.getUserId();
                String roleId = membershipBean.getRoleId();
                RoleBean role = storage.getRole(roleId);

                // Role does not exist!
                if (role == null) {
                    continue;
                }

                MemberBean member = members.get(userId);
                if (member == null) {
                    UserBean user = storage.getUser(userId);
                    member = new MemberBean();
                    member.setEmail(user.getEmail());
                    member.setUserId(userId);
                    member.setUserName(user.getFullName());
                    member.setRoles(new ArrayList<>());
                    members.put(userId, member);
                }
                MemberRoleBean mrb = new MemberRoleBean();
                mrb.setRoleId(roleId);
                mrb.setRoleName(role.getName());
                member.getRoles().add(mrb);
                if (member.getJoinedOn() == null || membershipBean.getCreatedOn().compareTo(member.getJoinedOn()) < 0) {
                    member.setJoinedOn(membershipBean.getCreatedOn());
                }
            }
            return new ArrayList<>(members.values());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        } finally {
            storage.rollbackTx();
        }
    }

    /**
     * Gets a policy by its id.  Also verifies that the policy really does belong to
     * the entity indicated.
     * @param type the policy type
     * @param organizationId the org id
     * @param entityId the entity id
     * @param entityVersion the entity version
     * @param policyId the policy id
     * @return a policy bean
     * @throws PolicyNotFoundException
     */
    protected PolicyBean doGetPolicy(PolicyType type, String organizationId, String entityId,
            String entityVersion, long policyId) throws PolicyNotFoundException {
        try {
            storage.beginTx();
            PolicyBean policy = storage.getPolicy(type, organizationId, entityId, entityVersion, policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            storage.commitTx();
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
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @return a {@link GatewayBean} iff there is a single configured gateway in the system
     * @throws StorageException
     */
    private GatewaySummaryBean getSingularGateway() throws StorageException {
        List<GatewaySummaryBean> gateways = query.listGateways();
        if (gateways != null && gateways.size() == 1) {
            return gateways.get(0);
        } else {
            return null;
        }
    }

    /**
     * Decrypt the endpoint properties
     */
    private void decryptEndpointProperties(ApiVersionBean versionBean) {
        Map<String, String> endpointProperties = versionBean.getEndpointProperties();
        if (endpointProperties != null) {
            for (Entry<String, String> entry : endpointProperties.entrySet()) {
                DataEncryptionContext ctx = new DataEncryptionContext(
                        versionBean.getApi().getOrganization().getId(), 
                        versionBean.getApi().getId(),
                        versionBean.getVersion(), 
                        EntityType.Api);
                entry.setValue(encrypter.decrypt(entry.getValue(), ctx));
            }
        }
    }

    /**
     * Encrypt the endpoint properties
     */
    private void encryptEndpointProperties(ApiVersionBean versionBean) {
        Map<String, String> endpointProperties = versionBean.getEndpointProperties();
        if (endpointProperties != null) {
            for (Entry<String, String> entry : endpointProperties.entrySet()) {
                DataEncryptionContext ctx = new DataEncryptionContext(
                        versionBean.getApi().getOrganization().getId(), 
                        versionBean.getApi().getId(),
                        versionBean.getVersion(), 
                        EntityType.Api);
                entry.setValue(encrypter.encrypt(entry.getValue(), ctx));
            }
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
     * @return the metrics
     */
    public IMetricsAccessor getMetrics() {
        return this.metrics;
    }

    /**
     * @param metrics the metrics to set
     */
    public void setMetrics(IMetricsAccessor metrics) {
        this.metrics = metrics;
    }

    /**
     * @return the clientValidator
     */
    public IClientValidator getClientValidator() {
        return clientValidator;
    }

    /**
     * @param clientValidator the clientValidator to set
     */
    public void setClientValidator(IClientValidator clientValidator) {
        this.clientValidator = clientValidator;
    }

    /**
     * @return the apiValidator
     */
    public IApiValidator getApiValidator() {
        return apiValidator;
    }

    /**
     * @param apiValidator the apiValidator to set
     */
    public void setApiValidator(IApiValidator apiValidator) {
        this.apiValidator = apiValidator;
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

    /**
     * Parse the to date query param.
     */
    private DateTime parseFromDate(String fromDate) {
        // Default to the last 30 days
        DateTime defaultFrom = new DateTime().withZone(DateTimeZone.UTC).minusDays(30).withHourOfDay(0)
                .withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        return parseDate(fromDate, defaultFrom, true);
    }

    /**
     * Parse the from date query param.
     */
    private DateTime parseToDate(String toDate) {
        // Default to now
        return parseDate(toDate, new DateTime().withZone(DateTimeZone.UTC), false);
    }

    /**
     * Parses a query param representing a date into an actual date object.
     */
    private static DateTime parseDate(String dateStr, DateTime defaultDate, boolean floor) {
        if ("now".equals(dateStr)) { //$NON-NLS-1$
            return new DateTime();
        }
        if (dateStr.length() == 10) {
            DateTime parsed = ISODateTimeFormat.date().withZone(DateTimeZone.UTC).parseDateTime(dateStr);
            // If what we want is the floor, then just return it.  But if we want the
            // ceiling of the date, then we need to set the right params.
            if (!floor) {
                parsed = parsed.plusDays(1).minusMillis(1);
            }
            return parsed;
        }
        if (dateStr.length() == 20) {
            return ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime(dateStr);
        }
        if (dateStr.length() == 24) {
            return ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(dateStr);
        }
        return defaultDate;
    }

    /**
     * Ensures that the given date range is valid.
     */
    private void validateMetricRange(DateTime from, DateTime to) throws InvalidMetricCriteriaException {
        if (from.isAfter(to)) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("OrganizationResourceImpl.InvalidMetricDateRange")); //$NON-NLS-1$
        }
    }

    /**
     * Ensures that a time series can be created for the given date range and
     * interval, and that the
     */
    private void validateTimeSeriesMetric(DateTime from, DateTime to, HistogramIntervalType interval)
            throws InvalidMetricCriteriaException {
        long millis = to.getMillis() - from.getMillis();
        long divBy = ONE_DAY_MILLIS;
        switch (interval) {
        case day:
            divBy = ONE_DAY_MILLIS;
            break;
        case hour:
            divBy = ONE_HOUR_MILLIS;
            break;
        case minute:
            divBy = ONE_MINUTE_MILLIS;
            break;
        case month:
            divBy = ONE_MONTH_MILLIS;
            break;
        case week:
            divBy = ONE_WEEK_MILLIS;
            break;
        default:
            break;
        }
        long totalDataPoints = millis / divBy;
        if (totalDataPoints > 5000) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("OrganizationResourceImpl.MetricDataSetTooLarge")); //$NON-NLS-1$
        }
    }

    /**
     * Make sure we've got a valid URL.
     */
    private void validateEndpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new InvalidParameterException(Messages.i18n.format("OrganizationResourceImpl.InvalidEndpointURL")); //$NON-NLS-1$
        }
    }

}
