package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.crypt.DataEncryptionContext;
import io.apiman.common.util.crypt.DataEncryptionContext.EntityType;
import io.apiman.common.util.crypt.IDataEncrypter;
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
import io.apiman.manager.api.core.config.ApiManagerConfig;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.util.PolicyTemplateUtil;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.IOrganizationResource;
import io.apiman.manager.api.rest.IRoleResource;
import io.apiman.manager.api.rest.IUserResource;
import io.apiman.manager.api.rest.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.exceptions.ApiAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ApiDefinitionNotFoundException;
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
import io.apiman.manager.api.rest.exceptions.InvalidParameterException;
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
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.util.FieldValidator;
import io.apiman.manager.api.rest.impl.util.RestHelper;
import io.apiman.manager.api.rest.impl.util.SwaggerWsdlHelper;
import io.apiman.manager.api.security.ISecurityContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import static java.util.stream.Collectors.toList;

/**
 * Organization services
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class OrganizationService {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(OrganizationService.class);

    private static final long ONE_MINUTE_MILLIS = 1 * 60 * 1000L;
    private static final long ONE_HOUR_MILLIS = 1 * 60 * 60 * 1000L;
    private static final long ONE_DAY_MILLIS = 1 * 24 * 60 * 60 * 1000L;
    private static final long ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L;
    private static final long ONE_MONTH_MILLIS = 30 * 24 * 60 * 60 * 1000L;

    private final ApiManagerConfig config;

    private final IStorage storage;
    private final IStorageQuery query;
    private final IMetricsAccessor metrics;

    private final IClientValidator clientValidator;
    private final IApiValidator apiValidator;
    private final IApiKeyGenerator apiKeyGenerator;
    private final IDataEncrypter encrypter;

    private final IDownloadManager downloadManager;

    private final IUserResource users;
    private final IRoleResource roles;

    private final ISecurityContext securityContext;
    private final IGatewayLinkFactory gatewayLinkFactory;

    /**
     * Constructor.
     */
    @Inject
    public OrganizationService(
        ApiManagerConfig config,
        IStorage storage,
        IStorageQuery query,
        IMetricsAccessor metrics,
        IClientValidator clientValidator,
        IApiValidator apiValidator,
        IApiKeyGenerator apiKeyGenerator,
        IDataEncrypter encrypter,
        IDownloadManager downloadManager,
        IUserResource users,
        IRoleResource roles,
        ISecurityContext securityContext,
        IGatewayLinkFactory gatewayLinkFactory
    ) {
        this.config = config;
        this.storage = storage;
        this.query = query;
        this.metrics = metrics;
        this.clientValidator = clientValidator;
        this.apiValidator = apiValidator;
        this.apiKeyGenerator = apiKeyGenerator;
        this.encrypter = encrypter;
        this.downloadManager = downloadManager;
        this.users = users;
        this.roles = roles;
        this.securityContext = securityContext;
        this.gatewayLinkFactory = gatewayLinkFactory;
    }

    @Transactional
    public OrganizationBean create(
        NewOrganizationBean bean) throws OrganizationAlreadyExistsException, InvalidNameException {
        if (config.isAdminOnlyOrgCreationEnabled()) {
            securityContext.checkAdminPermissions();
        }

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

        return tryAction(() -> {
            OrganizationBean orgBean = new OrganizationBean();
            orgBean.setName(bean.getName());
            orgBean.setDescription(bean.getDescription());
            orgBean.setId(BeanUtils.idFromName(bean.getName()));
            orgBean.setCreatedOn(new Date());
            orgBean.setCreatedBy(securityContext.getCurrentUser());
            orgBean.setModifiedOn(new Date());
            orgBean.setModifiedBy(securityContext.getCurrentUser());

            // Store/persist the new organization
            if (storage.getOrganization(orgBean.getId()) != null) {
                throw ExceptionFactory.organizationAlreadyExistsException(bean.getName());
            }

            storage.createOrganization(orgBean);
            storage.createAuditEntry(AuditUtils.organizationCreated(orgBean, securityContext));

            // Auto-grant memberships in roles to the creator of the organization
            for (RoleBean roleBean : autoGrantedRoles) {
                String currentUser = securityContext.getCurrentUser();
                String orgId = orgBean.getId();
                RoleMembershipBean membership = RoleMembershipBean.create(currentUser, roleBean.getId(),
                    orgId);
                membership.setCreatedOn(new Date());
                storage.createMembership(membership);
            }

            LOGGER.debug(String.format("Created organization %s: %s", orgBean.getName(), orgBean)); //$NON-NLS-1$

            return orgBean;
        });
    }

    @Transactional
    public void delete(String organizationId)
        throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException {
        securityContext.checkPermissions(PermissionType.orgAdmin, organizationId);

        tryAction(() -> {
            OrganizationBean organizationBean = getOrganizationFromStorage(organizationId);

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
                LOGGER.warn("There are locked plans(s): these will be deleted."); //$NON-NLS-1$
            }

            // Delete org
            storage.deleteOrganization(organizationBean);

            LOGGER.debug("Deleted Organization: " + organizationBean.getName()); //$NON-NLS-1$
        });
    }

    @Transactional
    public void deleteClient(String organizationId, String clientId)
        throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException {
        securityContext.checkPermissions(PermissionType.clientAdmin, organizationId);

        tryAction(() -> {
            ClientBean client = getClientFromStorage(organizationId, clientId);
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
            LOGGER.debug("Deleted ClientApp: " + client.getName()); //$NON-NLS-1$
        });
    }

    private ClientBean getClientFromStorage(String organizationId, String clientId) throws StorageException {
        ClientBean client = storage.getClient(organizationId, clientId);
        if (client == null) {
            throw ExceptionFactory.clientNotFoundException(clientId);
        }
        return client;
    }

    @Transactional
    public void deleteApi(String organizationId, String apiId)
        throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException {
        securityContext.checkPermissions(PermissionType.apiAdmin, organizationId);
        tryAction(() -> {
            ApiBean api = getApiFromStorage(organizationId, apiId);

            Iterator<ApiVersionBean> apiVersions = storage.getAllApiVersions(organizationId, apiId);
            Iterable<ApiVersionBean> iterable = () -> apiVersions;

            List<ApiVersionBean> apiVersionBeans = StreamSupport.stream(iterable.spliterator(), false)
                .collect(toList());

            List<ApiVersionBean> registeredElems = apiVersionBeans.stream()
                .filter(clientVersion -> clientVersion.getStatus() == ApiStatus.Published)
                .limit(5)
                .collect(toList());

            if (!registeredElems.isEmpty()) {
                throw ExceptionFactory.entityStillActiveExceptionApiVersions(registeredElems);
            }

            for (ApiVersionBean apiVersion : apiVersionBeans) {
                // add apiBean to apiVersionBean, otherwise deleteApiDefinition fails for EsStorage
                apiVersion.setApi(api);
                if (apiVersionHasApiDefinition(apiVersion)) {
                    storage.deleteApiDefinition(apiVersion);
                }
            }

            storage.deleteApi(api);
            LOGGER.debug("Deleted API: " + api.getName()); //$NON-NLS-1$
        });
    }

    @Transactional
    public OrganizationBean get(String organizationId) throws OrganizationNotFoundException {
        // No permission check is needed, because this would break All Organizations UI
        OrganizationBean organizationBean = tryAction(() -> getOrganizationFromStorage(organizationId));

        LOGGER.debug(String.format("Got organization %s: %s", organizationBean.getName(), organizationBean)); //$NON-NLS-1$

        // Hide sensitive data and set only needed data for the UI
        if (securityContext.hasPermission(PermissionType.orgView, organizationId)){
            return organizationBean;
        } else {
            return RestHelper.hideSensitiveDataFromOrganizationBean(organizationBean);
        }
    }

    @Transactional
    public void update(String organizationId, UpdateOrganizationBean bean)
        throws OrganizationNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgEdit, organizationId);

        OrganizationBean orgForUpdate = tryAction(() -> getOrganizationFromStorage(organizationId));

        EntityUpdatedData auditData = new EntityUpdatedData();
        if (AuditUtils.valueChanged(orgForUpdate.getDescription(), bean.getDescription())) {
            auditData.addChange("description", orgForUpdate.getDescription(), bean.getDescription()); //$NON-NLS-1$
            orgForUpdate.setDescription(bean.getDescription());
        }

        tryAction(() -> storage.updateOrganization(orgForUpdate));
        tryAction(() -> storage.createAuditEntry(AuditUtils.organizationUpdated(orgForUpdate, auditData, securityContext));

        LOGGER.debug(String.format("Updated organization %s: %s", orgForUpdate.getName(), orgForUpdate)); //$NON-NLS-1$
    }

    @Transactional
    public SearchResultsBean<AuditEntryBean> activity(String organizationId, int page, int pageSize)
        throws OrganizationNotFoundException, NotAuthorizedException {
        // Only members are allowed to see this
        if (!securityContext.isMemberOf(organizationId)) {
            throw ExceptionFactory.notAuthorizedException();
        }

        final int finalPage = Math.max(page, 1);
        final int finalPageSize = pageSize <= 0 ? 20 : pageSize;

        PagingBean paging = new PagingBean();
        paging.setPage(finalPage);
        paging.setPageSize(finalPageSize);
        return tryAction(() -> query.auditEntity(organizationId, null, null, null, paging));
    }

    @Transactional
    public ClientBean createClient(String organizationId, NewClientBean bean)
        throws OrganizationNotFoundException, ClientAlreadyExistsException, NotAuthorizedException,
        InvalidNameException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        FieldValidator.validateName(bean.getName());

        ClientBean newClient = new ClientBean();
        newClient.setId(BeanUtils.idFromName(bean.getName()));
        newClient.setName(bean.getName());
        newClient.setDescription(bean.getDescription());
        newClient.setCreatedBy(securityContext.getCurrentUser());
        newClient.setCreatedOn(new Date());

        tryAction(() -> {
            // Store/persist the new client
            OrganizationBean org = getOrganizationFromStorage(organizationId);
            newClient.setOrganization(org);

            if (storage.getClient(org.getId(), newClient.getId()) != null) {
                throw ExceptionFactory.clientAlreadyExistsException(bean.getName());
            }

            storage.createClient(newClient);
            storage.createAuditEntry(AuditUtils.clientCreated(newClient, securityContext));

            if (bean.getInitialVersion() != null) {
                NewClientVersionBean newClientVersion = new NewClientVersionBean();
                newClientVersion.setVersion(bean.getInitialVersion());
                createClientVersionInternal(newClientVersion, newClient);
            }
        });

        LOGGER.debug(String.format("Created client %s: %s", newClient.getName(), newClient)); //$NON-NLS-1$
        return newClient;
    }

    @Transactional
    public ClientBean getClient(String organizationId, String clientId)
        throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        ClientBean clientBean = tryAction(() -> getClientFromStorage(organizationId, clientId));
        LOGGER.debug(String.format("Got client %s: %s", clientBean.getName(), clientBean)); //$NON-NLS-1$
        return clientBean;
    }

    @Transactional
    public SearchResultsBean<AuditEntryBean> getClientActivity(String organizationId, String clientId,
        int page, int pageSize) throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

        final int finalPage = Math.max(page, 1);
        final int finalPageSize = pageSize <= 0 ? 20 : pageSize;

        PagingBean paging = new PagingBean();
        paging.setPage(finalPage);
        paging.setPageSize(finalPageSize);
        return tryAction(() -> query.auditEntity(organizationId, clientId, null, ClientBean.class, paging));
    }

    @Transactional
    public List<ClientSummaryBean> listClients(String organizationId) throws OrganizationNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgView, organizationId);
        return tryAction(() -> query.getClientsInOrg(organizationId));
    }

    @Transactional
    public void updateClient(String organizationId, String clientId, UpdateClientBean bean)
        throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        tryAction(() -> {
            ClientBean clientForUpdate = getClientFromStorage(organizationId, clientId);
            EntityUpdatedData auditData = new EntityUpdatedData();
            if (AuditUtils.valueChanged(clientForUpdate.getDescription(), bean.getDescription())) {
                auditData.addChange("description", clientForUpdate.getDescription(), bean.getDescription()); //$NON-NLS-1$
                clientForUpdate.setDescription(bean.getDescription());
            }
            storage.updateClient(clientForUpdate);
            storage.createAuditEntry(AuditUtils.clientUpdated(clientForUpdate, auditData, securityContext));

            LOGGER.debug(String.format("Updated client %s: %s", clientForUpdate.getName(), clientForUpdate)); //$NON-NLS-1$
        });
    }

    // Manual TX, could refactor this into just one tx?
    public ClientVersionBean createClientVersion(String organizationId, String clientId,
        NewClientVersionBean bean) throws ClientNotFoundException, NotAuthorizedException,
        InvalidVersionException, ClientVersionAlreadyExistsException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        FieldValidator.validateVersion(bean.getVersion());

        ClientVersionBean newVersion;
        try {
            storage.beginTx();
            ClientBean client = getClientFromStorage(organizationId, clientId);
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

    @Transactional
    public ApiKeyBean getClientApiKey(String organizationId, String clientId, String version)
        throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

        ClientVersionBean client = tryAction(() -> getClientVersionInternal(organizationId, clientId, version));
        ApiKeyBean apiKeyBean = new ApiKeyBean();
        apiKeyBean.setApiKey(client.getApikey());
        return apiKeyBean;
    }

    @Transactional
    public ApiKeyBean updateClientApiKey(String organizationId, String clientId, String version, ApiKeyBean bean)
        throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException,
        InvalidClientStatusException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);


        ClientVersionBean clientVersion = tryAction(() -> getClientVersionInternal(organizationId, clientId, version));

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

        tryAction(() -> storage.updateClientVersion(clientVersion));

        LOGGER.debug(String.format("Updated an API Key for client %s version %s", clientVersion.getClient().getName(), clientVersion)); //$NON-NLS-1$
        ApiKeyBean rval = new ApiKeyBean();
        rval.setApiKey(newApiKey);
        return rval;
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

        LOGGER.debug(String.format("Created new client version %s: %s", newVersion.getClient().getName(), newVersion)); //$NON-NLS-1$
        return newVersion;
    }

    /**
     * @see IOrganizationResource#getClientVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    
    public ClientVersionBean getClientVersion(String organizationId, String clientId, String version)
        throws ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        return tryAction(() -> getClientVersionInternal(organizationId, clientId, version));
    }

    /**
     * Gets the client version internally
     * which lets callers dictate whether the user has clientView permission for the org.
     * @param organizationId the organizationId
     * @param clientId the clientId
     * @param version the version
     * @return the client version
     * @throws ClientVersionNotFoundException if client not found
     */
    protected ClientVersionBean getClientVersionInternal(String organizationId, String clientId, String version)
        throws ClientVersionNotFoundException, StorageException {
        ClientVersionBean clientVersion = getClientVersionFromStorage(organizationId, clientId, version);
        LOGGER.debug(String.format("Got new client version %s: %s", clientVersion.getClient().getName(), clientVersion)); //$NON-NLS-1$
        return clientVersion;
    }

    private ClientVersionBean getClientVersionFromStorage(String organizationId, String clientId, String version) throws StorageException, ClientVersionNotFoundException {
        ClientVersionBean clientVersion = storage.getClientVersion(organizationId, clientId, version);
        if (clientVersion == null) {
            throw ExceptionFactory.clientVersionNotFoundException(clientId, version);
        }
        return clientVersion;
    }

    @Transactional
    public SearchResultsBean<AuditEntryBean> getClientVersionActivity(String organizationId,
        String clientId, String version, int page, int pageSize)
        throws ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

        final int finalPage = Math.max(page, 1);
        final int finalPageSize = pageSize <= 0 ? 20 : pageSize;

        PagingBean paging = new PagingBean();
        paging.setPage(finalPage);
        paging.setPageSize(finalPageSize);
        return tryAction(() -> query.auditEntity(organizationId, clientId, version, ClientBean.class, paging));
    }

    @Transactional
    public ClientUsagePerApiBean getClientUsagePerApi(String organizationId, String clientId,
        String version, String fromDate, String toDate) throws NotAuthorizedException,
        InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

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

    @Transactional
    public List<ClientVersionSummaryBean> listClientVersions(String organizationId, String clientId)
        throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        // Try to get the client first - will throw a ClientNotFoundException if not found.
        getClient(organizationId, clientId);

        return tryAction(() -> query.getClientVersions(organizationId, clientId));
    }

    // TODO Manual TX
    public ContractBean createContract(String organizationId, String clientId, String version,
        NewContractBean bean) throws OrganizationNotFoundException, ClientNotFoundException,
        ApiNotFoundException, PlanNotFoundException, ContractAlreadyExistsException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        try {
            storage.beginTx();
            ContractBean contract = createContractInternal(organizationId, clientId, version, bean);

            storage.commitTx();
            LOGGER.debug(String.format("Created new contract %s: %s", contract.getId(), contract)); //$NON-NLS-1$
            return contract;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            // Up above, we are optimistically creating the contract.  If it fails, check to see
            // if it failed because it was a duplicate.  If so, throw something sensible.  We
            // only do this on failure (we would get a FK constraint failure, for example) to
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
     */
    protected ContractBean createContractInternal(String organizationId, String clientId,
        String version, NewContractBean bean) throws StorageException, Exception {
        ClientVersionBean cvb = getClientVersionFromStorage(organizationId, clientId, version);

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
        PlanVersionBean pvb = getPlanVersionFromStorage(bean.getApiOrgId(), bean.getPlanId(), planVersion);
        if (pvb.getStatus() != PlanStatus.Locked) {
            throw ExceptionFactory.invalidPlanStatusException();
        }

        ContractBean contract = new ContractBean();
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

    @Transactional
    public ContractBean getContract(String organizationId, String clientId, String version,
        Long contractId) throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

        return tryAction(() -> {
            ContractBean contract = storage.getContract(contractId);
            if (contract == null)
                throw ExceptionFactory.contractNotFoundException(contractId);

            LOGGER.debug(String.format("Got contract %s: %s", contract.getId(), contract)); //$NON-NLS-1$
            return contract;
        });
    }

    @Transactional
    public void deleteAllContracts(String organizationId, String clientId, String version)
        throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        List<ContractSummaryBean> contracts = getClientVersionContracts(organizationId, clientId, version);
        for (ContractSummaryBean contract : contracts) {
            deleteContract(organizationId, clientId, version, contract.getContractId());
        }
    }

    @Transactional
    public void deleteContract(String organizationId, String clientId, String version, Long contractId)
        throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException,
        InvalidClientStatusException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        tryAction(() -> {
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
            ClientVersionBean clientV = getClientVersionFromStorage(organizationId, clientId, version);
            clientV.setModifiedBy(securityContext.getCurrentUser());
            clientV.setModifiedOn(new Date());
            storage.updateClientVersion(clientV);

            LOGGER.debug(String.format("Deleted contract: %s", contract)); //$NON-NLS-1$
        });
    }

    @Transactional
    public List<ContractSummaryBean> getClientVersionContracts(String organizationId, String clientId, String version)
        throws ClientNotFoundException, NotAuthorizedException {

        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        // Try to get the client first - will throw a ClientNotFoundException if not found.
        getClientVersionInternal(organizationId, clientId, version);

        return tryAction(() -> query.getClientContracts(organizationId, clientId, version));
    }

    public Response getApiRegistryJSON(String organizationId, String clientId, String version,
        String download) throws ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

        if (BooleanUtils.toBoolean(download)) { //$NON-NLS-1$
            return tryAction(() -> {
                String path = String.format("%s/%s/%s", organizationId, clientId, version); //$NON-NLS-1$
                DownloadBean dbean = downloadManager.createDownload(DownloadType.apiRegistryJson, path);
                return Response.ok(dbean, MediaType.APPLICATION_JSON).build();
            });
        } else {
            return getApiRegistryJSONInternal(organizationId, clientId, version);
        }
    }

    public Response getApiRegistryJSONInternal(String organizationId, String clientId, String version) throws ClientVersionNotFoundException {
        // We don't need a permission check here because the permission was already checked while creating the download id
        ApiRegistryBean apiRegistry = getApiRegistry(organizationId, clientId, version);
        return Response.ok(apiRegistry, MediaType.APPLICATION_JSON)
            .header("Content-Disposition", "attachment; filename=api-registry.json") //$NON-NLS-1$ //$NON-NLS-2$
            .build();
    }

    @Transactional
    public Response getApiRegistryXML(String organizationId, String clientId, String version,
        String download) throws ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

        if ("true".equals(download)) { //$NON-NLS-1$
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
        ApiRegistryBean apiRegistry = getApiRegistry(organizationId, clientId, version);
        return Response.ok(apiRegistry, MediaType.APPLICATION_XML)
            .header("Content-Disposition", "attachment; filename=api-registry.xml") //$NON-NLS-1$ //$NON-NLS-2$
            .build();
    }

    /**
     * Gets the API registry.
     */
    @Transactional
    private ApiRegistryBean getApiRegistry(String organizationId, String clientId, String version) throws ClientVersionNotFoundException {
        // Try to get the client first - will throw a ClientVersionNotFoundException if not found.
        ClientVersionBean clientVersion = getClientVersionInternal(organizationId, clientId, version);

        Map<String, IGatewayLink> gatewayLinks = new HashMap<>();
        Map<String, GatewayBean> gateways = new HashMap<>();
        boolean txStarted = false;
        try {
            ApiRegistryBean apiRegistry = query.getApiRegistry(organizationId, clientId, version);
            apiRegistry.setApiKey(clientVersion.getApikey());

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
        } catch (StorageException| GatewayAuthenticationException e) {
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

    @Transactional
    public PolicyBean createClientPolicy(String organizationId, String clientId, String version,
        NewPolicyBean bean) throws OrganizationNotFoundException, ClientVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        return tryAction(() -> {
            // Make sure the Client exists
            ClientVersionBean cvb = getClientVersionInternal(organizationId, clientId, version);

            PolicyBean policy = doCreatePolicy(organizationId, clientId, version, bean, PolicyType.Client);

            cvb.setModifiedBy(securityContext.getCurrentUser());
            cvb.setModifiedOn(new Date());

            return policy;
        });
    }

    // Tx via doGetPolicy for now.
    public PolicyBean getClientPolicy(String organizationId, String clientId, String version, long policyId)
        throws OrganizationNotFoundException, ClientVersionNotFoundException,
        PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

        // Make sure the client version exists
        getClientVersionInternal(organizationId, clientId, version);

        PolicyBean policy = doGetPolicy(PolicyType.Client, organizationId, clientId, version, policyId);
        return policy;
    }

    @Transactional
    public void updateClientPolicy(String organizationId, String clientId, String version,
        long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
        ClientVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        // Make sure the client version exists.
        ClientVersionBean cvb = getClientVersionInternal(organizationId, clientId, version);

        tryAction(() -> {
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
        });
    }

    @Transactional
    public void deleteClientPolicy(String organizationId, String clientId, String version, long policyId)
        throws OrganizationNotFoundException, ClientVersionNotFoundException,
        PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        // Make sure the client version exists;
        ClientVersionBean cvb = getClientVersionInternal(organizationId, clientId, version);

        tryAction(() -> {
            PolicyBean policy = this.storage.getPolicy(PolicyType.Client, organizationId, clientId, version, policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            storage.deletePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyRemoved(policy, PolicyType.Client, securityContext));

            cvb.setModifiedBy(securityContext.getCurrentUser());
            cvb.setModifiedOn(new Date());
            storage.updateClientVersion(cvb);
        });
    }

    @Transactional
    public List<PolicySummaryBean> listClientPolicies(String organizationId, String clientId, String version)
        throws OrganizationNotFoundException, ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        // Try to get the client version first - will throw an exception if not found.
        getClientVersionInternal(organizationId, clientId, version);

        return tryAction(() -> query.getPolicies(organizationId, clientId, version, PolicyType.Client));
    }

    @Transactional
    public void reorderClientPolicies(String organizationId, String clientId, String version,
        PolicyChainBean policyChain) throws OrganizationNotFoundException,
        ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        // Make sure the client version exists.
        ClientVersionBean cvb = getClientVersionInternal(organizationId, clientId, version);

        tryAction(() -> {
            List<Long> newOrder = new ArrayList<>(policyChain.getPolicies().size());
            for (PolicySummaryBean psb : policyChain.getPolicies()) {
                newOrder.add(psb.getId());
            }
            storage.reorderPolicies(PolicyType.Client, organizationId, clientId, version, newOrder);
            storage.createAuditEntry(AuditUtils.policiesReordered(cvb, PolicyType.Client, securityContext));

            cvb.setModifiedBy(securityContext.getCurrentUser());
            cvb.setModifiedOn(new Date());
            storage.updateClientVersion(cvb);
        });
    }

    @Transactional
    public ApiBean createApi(String organizationId, NewApiBean bean)
        throws OrganizationNotFoundException, ApiAlreadyExistsException, NotAuthorizedException,
        InvalidNameException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        FieldValidator.validateName(bean.getName());

        ApiBean newApi = new ApiBean();
        newApi.setName(bean.getName());
        newApi.setDescription(bean.getDescription());
        newApi.setId(BeanUtils.idFromName(bean.getName()));
        newApi.setCreatedOn(new Date());
        newApi.setCreatedBy(securityContext.getCurrentUser());

        return tryAction(() -> {
            GatewaySummaryBean gateway = getSingularGateway();

            OrganizationBean orgBean = getOrganizationFromStorage(organizationId);
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
                newApiVersion.setParsePayload(bean.getParsePayload());
                newApiVersion.setDisableKeysStrip(bean.getDisableKeysStrip());
                newApiVersion.setVersion(bean.getInitialVersion());
                newApiVersion.setDefinitionUrl(bean.getDefinitionUrl());
                newApiVersion.setDefinitionType(bean.getDefinitionType());
                createApiVersionInternal(newApiVersion, newApi, gateway);
            }

            return newApi;
        });
    }

    @Transactional
    public ApiBean getApi(String organizationId, String apiId)
        throws ApiNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);
        return tryAction(() -> getApiFromStorage(organizationId, apiId));
    }

    /**
     * Gets the API from storage
     * @param organizationId the organizationId
     * @param apiId the apiId
     * @return the api
     * @throws StorageException if the API is not found
     */
    private ApiBean getApiFromStorage(String organizationId, String apiId) throws StorageException, ApiNotFoundException {
        ApiBean apiBean = storage.getApi(organizationId, apiId);
        if (apiBean == null) {
            throw ExceptionFactory.apiNotFoundException(apiId);
        }
        return apiBean;
    }

    @Transactional
    public SearchResultsBean<AuditEntryBean> getApiActivity(String organizationId, String apiId,
        int page, int pageSize) throws ApiNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }

        final PagingBean paging = new PagingBean();
        paging.setPage(page);
        paging.setPageSize(pageSize);

        return tryAction(() -> query.auditEntity(organizationId, apiId, null, ApiBean.class, paging));
    }

    @Transactional
    public List<ApiSummaryBean> listApis(String organizationId) throws OrganizationNotFoundException {
        // No permission check is needed, because this would break All Organizations UI

        // make sure the org exists
        get(organizationId);

        return tryAction(() -> {
            // Hide sensitive data and set only needed data for the UI
            if (securityContext.hasPermission(PermissionType.orgView, organizationId)) {
                return query.getApisInOrg(organizationId);
            } else {
                return RestHelper.hideSensitiveDataFromApiSummaryBeanList(query.getApisInOrg(organizationId));
            }
        });
    }

    @Transactional
    public void updateApi(String organizationId, String apiId, UpdateApiBean bean)
        throws ApiNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        tryAction(() -> {
            ApiBean apiForUpdate = getApiFromStorage(organizationId, apiId);
            EntityUpdatedData auditData = new EntityUpdatedData();
            if (AuditUtils.valueChanged(apiForUpdate.getDescription(), bean.getDescription())) {
                auditData.addChange("description", apiForUpdate.getDescription(), bean.getDescription()); //$NON-NLS-1$
                apiForUpdate.setDescription(bean.getDescription());
            }
            storage.updateApi(apiForUpdate);
            storage.createAuditEntry(AuditUtils.apiUpdated(apiForUpdate, auditData, securityContext));
        });
    }

    @Transactional
    public ApiVersionBean createApiVersion(String organizationId, String apiId,
        NewApiVersionBean bean) throws ApiNotFoundException, NotAuthorizedException,
        InvalidVersionException, ApiVersionAlreadyExistsException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        FieldValidator.validateVersion(bean.getVersion());

        ApiVersionBean newVersion = tryAction(() -> {
            GatewaySummaryBean gateway = getSingularGateway();

            ApiBean api = getApiFromStorage(organizationId, apiId);

            if (storage.getApiVersion(organizationId, apiId, bean.getVersion()) != null) {
                throw ExceptionFactory.apiVersionAlreadyExistsException(apiId, bean.getVersion());
            }

            return createApiVersionInternal(bean, api, gateway);
        });

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
                if (bean.getParsePayload() == null) {
                    updatedApi.setParsePayload(bean.getParsePayload());
                }
                newVersion = updateApiVersion(organizationId, apiId, bean.getVersion(), updatedApi);

                if (bean.getDefinitionUrl() == null) {
                    // Clone the API definition document
                    InputStream definition = null;
                    try {
                        Response response = getApiDefinition(organizationId, apiId, bean.getCloneVersion());
                        definition = (InputStream) response.getEntity();
                        storeApiDefinition(organizationId, apiId, newVersion.getVersion(),
                            cloneSource.getDefinitionType(), definition, cloneSource.getDefinitionUrl());
                    } catch (ApiDefinitionNotFoundException svnfe) {
                        // This is ok - it just means the API doesn't have one, so do nothing.
                    } catch (Exception sdnfe) {
                        LOGGER.error("Unable to create response", sdnfe); //$NON-NLS-1$
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
        newVersion.setDefinitionUrl(bean.getDefinitionUrl());
        if (bean.getPublicAPI() != null) {
            newVersion.setPublicAPI(bean.getPublicAPI());
        }
        if (bean.getParsePayload() != null) {
            newVersion.setParsePayload(bean.getParsePayload());
        }
        if (bean.getDisableKeysStrip() != null) {
            newVersion.setDisableKeysStrip(bean.getDisableKeysStrip());
        }
        if (bean.getPlans() != null) {
            newVersion.setPlans(bean.getPlans());
        }
        if (bean.getDefinitionType() != null) {
            newVersion.setDefinitionType(bean.getDefinitionType());
        } else {
            newVersion.setDefinitionType(ApiDefinitionType.None);
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

        // Ensure all the plans are in the right status (locked)
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

        if (bean.getDefinitionUrl() != null) {
            InputStream definition = null;
            try {
                definition = new URL(bean.getDefinitionUrl()).openStream();
                storage.updateApiDefinition(newVersion, definition);
            } catch (Exception e) {
                LOGGER.error("Unable to store API definition from: " + bean.getDefinitionUrl(), e); //$NON-NLS-1$
                // Set definition type silently to None
                newVersion.setDefinitionType(ApiDefinitionType.None);
                storage.updateApiVersion(newVersion);
            } finally {
                IOUtils.closeQuietly(definition);
            }
        }

        storage.createAuditEntry(AuditUtils.apiVersionCreated(newVersion, securityContext));
        return newVersion;
    }

    @Transactional
    public ApiVersionBean getApiVersion(String organizationId, String apiId, String version)
        throws ApiVersionNotFoundException {
        // No permission check is needed, because this would break All APIs UI
        return tryAction(() -> {
            ApiVersionBean apiVersion = getApiVersionFromStorage(organizationId, apiId, version);

            if (securityContext.hasPermission(PermissionType.apiView, organizationId)) {
                decryptEndpointProperties(apiVersion);
                return apiVersion;
            } else {
                return RestHelper.hideSensitiveDataFromApiVersionBean(apiVersion);
            }
        });
    }

    @Transactional
    public ApiVersionStatusBean getApiVersionStatus(String organizationId, String apiId,
        String version) throws ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        ApiVersionBean versionBean = getApiVersion(organizationId, apiId, version);
        List<PolicySummaryBean> policies = listApiPolicies(organizationId, apiId, version);
        return apiValidator.getStatus(versionBean, policies);
    }

    @Transactional
    public Response getApiDefinition(String organizationId, String apiId, String version)
        throws ApiVersionNotFoundException {
        // No permission check is needed, because this would break All APIs UI
        // Allow the user to view a definition

        return tryAction(() -> {
            ApiVersionBean apiVersion = getApiVersionFromStorage(organizationId, apiId, version);
            if (apiVersion.getDefinitionType() == ApiDefinitionType.None
                || apiVersion.getDefinitionType() == null) {
                throw ExceptionFactory.apiDefinitionNotFoundException(apiId, version);
            }
            InputStream definition = storage.getApiDefinition(apiVersion);
            if (definition == null) {
                throw ExceptionFactory.apiDefinitionNotFoundException(apiId, version);
            }

            definition = updateDefinitionWithManagedEndpoint(organizationId, apiId, version, apiVersion,
                definition);
            ResponseBuilder builder = Response.ok().entity(definition);

            if (apiVersion.getDefinitionType() == ApiDefinitionType.SwaggerJSON) {
                builder.type(MediaType.APPLICATION_JSON);
            } else if (apiVersion.getDefinitionType() == ApiDefinitionType.SwaggerYAML) {
                builder.type("application/x-yaml"); //$NON-NLS-1$
            } else if (apiVersion.getDefinitionType() == ApiDefinitionType.WSDL) {
                builder.type("text/xml"); //$NON-NLS-1$
            } else {
                IOUtils.closeQuietly(definition);
                throw new Exception(
                    "API definition type not supported: " + apiVersion.getDefinitionType()); //$NON-NLS-1$
            }
            return builder.build();
        });
    }

    /**
     * Replaces the location with the location of the managed endpoint if it is a wsdl definition.
     * Replaces the host and base path with the information of the managed endpoint if it is a swagger 2+ definition.
     * Updates the definition in storage if needed.
     * @param organizationId the organizationId
     * @param apiId the apiId
     * @param version the version
     * @param definition the definition as stream
     * @param apiVersion the apiVersion
     * @return a ByteArrayInputStream with the updated definition
     * @throws IOException
     * @throws StorageException
     */
    protected InputStream updateDefinitionWithManagedEndpoint(String organizationId, String apiId, String version, ApiVersionBean apiVersion, InputStream definition) throws IOException, StorageException {
        // If it is not a published API we will not try to update the API definition. We will return definition from storage
        if (apiVersion.getStatus() != ApiStatus.Published) {
            return definition;
        }

        URL managedEndpoint = null;
        try {
            managedEndpoint = new URL(getApiVersionEndpointInfoFromStorage(apiVersion, organizationId, apiId, version).getManagedEndpoint());
        } catch (Exception e) {
            // If the gateway is not available we return the definition from storage
            return definition;
        }

        String definitionString = null;
        String updatedDefinitionString = null;
        if (apiVersion.getDefinitionType() == ApiDefinitionType.SwaggerJSON) {
            definitionString = SwaggerWsdlHelper.readSwaggerStreamToString(definition);
            updatedDefinitionString = SwaggerWsdlHelper.updateSwaggerDefinitionWithEndpoint(managedEndpoint, definitionString, apiVersion, storage);
        } else if (apiVersion.getDefinitionType() == ApiDefinitionType.SwaggerYAML) {
            definitionString = SwaggerWsdlHelper.convertYamlToJson(SwaggerWsdlHelper.readSwaggerStreamToString(definition));
            updatedDefinitionString = SwaggerWsdlHelper.updateSwaggerDefinitionWithEndpoint(managedEndpoint, definitionString, apiVersion, storage);
        } else if (apiVersion.getDefinitionType() == ApiDefinitionType.WSDL) {
            updatedDefinitionString = SwaggerWsdlHelper.updateLocationEndpointInWsdl(definition, managedEndpoint, apiVersion, storage);
        } else {
            return definition;
        }

        return new ByteArrayInputStream(updatedDefinitionString.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional
    public ApiVersionEndpointSummaryBean getApiVersionEndpointInfo(String organizationId, String apiId, String version)
        throws ApiVersionNotFoundException, InvalidApiStatusException {
        // No permission check is needed, because this would break All APIs UI
        return tryAction(() -> {
            ApiVersionBean apiVersion = getApiVersionFromStorage(organizationId, apiId, version);
            if (apiVersion.getStatus() != ApiStatus.Published) {
                throw new InvalidApiStatusException(Messages.i18n.format("ApiNotPublished")); //$NON-NLS-1$
            }
            ApiVersionEndpointSummaryBean rval = getApiVersionEndpointInfoFromStorage(apiVersion, organizationId, apiId, version);
            return rval;
        });
    }

    private ApiVersionEndpointSummaryBean getApiVersionEndpointInfoFromStorage(ApiVersionBean apiVersion, String organizationId,
        String apiId, String version) throws GatewayNotFoundException, GatewayAuthenticationException, StorageException {
        Set<ApiGatewayBean> gateways = apiVersion.getGateways();
        if (gateways.isEmpty()) {
            throw new SystemErrorException("No Gateways for published API!"); //$NON-NLS-1$
        }
        GatewayBean gateway = storage.getGateway(gateways.iterator().next().getGatewayId());
        if (gateway == null) {
            throw new GatewayNotFoundException();
        } else {
            LOGGER.debug(String.format("Got endpoint summary: %s", gateway)); //$NON-NLS-1$
        }
        IGatewayLink link = gatewayLinkFactory.create(gateway);
        ApiEndpoint endpoint = link.getApiEndpoint(organizationId, apiId, version);
        ApiVersionEndpointSummaryBean rval = new ApiVersionEndpointSummaryBean();
        rval.setManagedEndpoint(endpoint.getEndpoint());
        return rval;
    }

    @Transactional
    public SearchResultsBean<AuditEntryBean> getApiVersionActivity(String organizationId,
        String apiId, String version, int page, int pageSize) throws ApiVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        if (page <= 1) {
            page = 1;
        }
        if (pageSize == 0) {
            pageSize = 20;
        }
        PagingBean paging = new PagingBean();
        paging.setPage(page);
        paging.setPageSize(pageSize);

        return tryAction(() -> query.auditEntity(organizationId, apiId, version, ApiBean.class, paging));
    }

    @Transactional
    public ApiVersionBean updateApiVersion(String organizationId, String apiId, String version,
        UpdateApiVersionBean bean) throws ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

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
        if (AuditUtils.valueChanged(avb.isParsePayload(), bean.getParsePayload())) {
            data.addChange("parsePayload", String.valueOf(avb.isParsePayload()), String.valueOf(bean.getParsePayload())); //$NON-NLS-1$
            avb.setParsePayload(bean.getParsePayload());
        }

        if (AuditUtils.valueChanged(avb.getDisableKeysStrip(), bean.getDisableKeysStrip())) {
            data.addChange("disableKeysStrip", String.valueOf(avb.getDisableKeysStrip()), String.valueOf(bean.getDisableKeysStrip())); //$NON-NLS-1$
            avb.setDisableKeysStrip(bean.getDisableKeysStrip());
        }

       return tryAction(() -> {
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

            encryptEndpointProperties(avb);

            // Ensure all the plans are in the right status (locked)
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
            LOGGER.debug(String.format("Successfully updated API Version: %s", avb)); //$NON-NLS-1$
            decryptEndpointProperties(avb);
            return avb;
        });
    }

    @Transactional
    public void updateApiDefinition(String organizationId, String apiId, String version, String contentType, InputStream data)
        throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

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
            storeApiDefinition(organizationId, apiId, version, newDefinitionType, data, null);
            LOGGER.debug(String.format("Updated API definition for %s", apiId)); //$NON-NLS-1$
        } finally {
            IOUtils.closeQuietly(data);
        }
    }

    @Transactional
    public void updateApiDefinitionFromURL(String organizationId, String apiId, String version,
        NewApiDefinitionBean bean) throws ApiVersionNotFoundException, NotAuthorizedException,
        InvalidApiStatusException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        InputStream data = null;
        String definitionUrl;
        try {
            definitionUrl = bean.getDefinitionUrl();
            URL url = new URL(definitionUrl);
            data = url.openStream();

            storeApiDefinition(organizationId, apiId, version, bean.getDefinitionType(), data, definitionUrl);
            LOGGER.debug(String.format("Updated API definition for %s from URL %s", apiId, bean.getDefinitionUrl())); //$NON-NLS-1$
        } catch (IOException | StorageException e) {
            throw new SystemErrorException(e);
        } finally {
            IOUtils.closeQuietly(data);
        }
    }

    private void storeApiDefinition(String organizationId, String apiId, String version,
        ApiDefinitionType definitionType, InputStream data, String definitionUrl) throws StorageException {

        ApiVersionBean apiVersion = getApiVersionFromStorage(organizationId, apiId, version);

        if (apiVersion.getDefinitionType() != definitionType) {
            apiVersion.setDefinitionType(definitionType);
            storage.updateApiVersion(apiVersion);
        }
        // update the definition url silently in storage if it's a new one
        if ((definitionUrl != null && (apiVersion.getDefinitionUrl() == null || !apiVersion.getDefinitionUrl().equals(definitionUrl)))) {
            apiVersion.setDefinitionUrl(definitionUrl);
            storage.updateApiVersion(apiVersion);
        }
        storage.createAuditEntry(AuditUtils.apiDefinitionUpdated(apiVersion, securityContext));
        storage.updateApiDefinition(apiVersion, data);

        apiVersion.setModifiedOn(new Date());
        apiVersion.setModifiedBy(securityContext.getCurrentUser());
        storage.updateApiVersion(apiVersion);

        LOGGER.debug(String.format("Stored API definition %s: %s", apiId, apiVersion)); //$NON-NLS-1$
    }

    @Transactional
    public List<ApiVersionSummaryBean> listApiVersions(String organizationId, String apiId)
        throws ApiNotFoundException {
        // No permission check is needed, because this would break All APIs UI
        // Try to get the API first - will throw a ApiNotFoundException if not found.
        getApi(organizationId, apiId);
        return tryAction(() -> query.getApiVersions(organizationId, apiId));
    }

    @Transactional
    public List<ApiPlanSummaryBean> getApiVersionPlans(String organizationId, String apiId,
        String version) throws ApiVersionNotFoundException, NotAuthorizedException {
        // No permission check is needed, because this would break All APIs UI
        // Ensure the version exists first.
        getApiVersion(organizationId, apiId, version);

        return tryAction(() -> query.getApiVersionPlans(organizationId, apiId, version));
    }

    @Transactional
    public PolicyBean createApiPolicy(String organizationId, String apiId, String version,
        NewPolicyBean bean) throws OrganizationNotFoundException, ApiVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

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

        return tryAction(() -> {
            PolicyBean policy = doCreatePolicy(organizationId, apiId, version, bean, PolicyType.Api);
            LOGGER.debug(String.format("Created API policy %s", avb)); //$NON-NLS-1$

            avb.setModifiedOn(new Date());
            avb.setModifiedBy(securityContext.getCurrentUser());

            return policy;
        });
    }

    @Transactional
    public PolicyBean getApiPolicy(String organizationId, String apiId, String version, long policyId)
        throws OrganizationNotFoundException, ApiVersionNotFoundException,
        PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        // Make sure the API exists
        getApiVersion(organizationId, apiId, version);

        return doGetPolicy(PolicyType.Api, organizationId, apiId, version, policyId);
    }

    @Transactional
    public void updateApiPolicy(String organizationId, String apiId, String version,
        long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
        ApiVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        // Make sure the API exists
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        tryAction(() -> {
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
            LOGGER.debug(String.format("Updated API policy %s", policy)); //$NON-NLS-1$
        });
    }

    @Transactional
    public void deleteApiPolicy(String organizationId, String apiId, String version, long policyId)
        throws OrganizationNotFoundException, ApiVersionNotFoundException,
        PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

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

        tryAction(() -> {
            PolicyBean policy = this.storage.getPolicy(PolicyType.Api, organizationId, apiId, version,
                policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            storage.deletePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyRemoved(policy, PolicyType.Api, securityContext));

            avb.setModifiedBy(securityContext.getCurrentUser());
            avb.setModifiedOn(new Date());
            storage.updateApiVersion(avb);

            LOGGER.debug(String.format("Deleted API %s policy: %s", apiId, policy)); //$NON-NLS-1$
        });
    }

    @Transactional
    public void deleteApiDefinition(String organizationId, String apiId, String version)
        throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        tryAction(() -> {
            ApiVersionBean apiVersion = getApiVersionFromStorage(organizationId, apiId, version);
            apiVersion.setDefinitionType(ApiDefinitionType.None);
            apiVersion.setModifiedBy(securityContext.getCurrentUser());
            apiVersion.setModifiedOn(new Date());
            storage.createAuditEntry(AuditUtils.apiDefinitionDeleted(apiVersion, securityContext));
            storage.deleteApiDefinition(apiVersion);
            storage.updateApiVersion(apiVersion);
            LOGGER.debug(String.format("Deleted API %s definition %s", apiId, apiVersion)); //$NON-NLS-1$
        });
    }

    @Transactional
    public List<PolicySummaryBean> listApiPolicies(String organizationId, String apiId, String version)
        throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        // Try to get the API first - will throw an exception if not found.
        getApiVersion(organizationId, apiId, version);
        return tryAction(() -> query.getPolicies(organizationId, apiId, version, PolicyType.Api));
    }

    @Transactional
    public void reorderApiPolicies(String organizationId, String apiId, String version,
        PolicyChainBean policyChain) throws OrganizationNotFoundException,
        ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiEdit, organizationId);

        // Make sure the API exists
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        tryAction(() -> {
            List<Long> newOrder = new ArrayList<>(policyChain.getPolicies().size());
            for (PolicySummaryBean psb : policyChain.getPolicies()) {
                newOrder.add(psb.getId());
            }
            storage.reorderPolicies(PolicyType.Api, organizationId, apiId, version, newOrder);
            storage.createAuditEntry(AuditUtils.policiesReordered(avb, PolicyType.Api, securityContext));

            avb.setModifiedBy(securityContext.getCurrentUser());
            avb.setModifiedOn(new Date());
            storage.updateApiVersion(avb);
        });
    }

    @Transactional
    public PolicyChainBean getApiPolicyChain(String organizationId, String apiId, String version,
        String planId) throws ApiVersionNotFoundException, PlanNotFoundException {
        // No permission check is needed, because this would break All APIs UI

        // Try to get the API first - will throw an exception if not found.
        ApiVersionBean avb = getApiVersion(organizationId, apiId, version);

        return tryAction(() -> {
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
            // Hide sensitive data and set only needed data for the UI
            List<PolicySummaryBean> apiPolicies = RestHelper.hideSensitiveDataFromPolicySummaryBeanList(
                securityContext, query.getPolicies(organizationId, apiId, version, PolicyType.Api));
            List<PolicySummaryBean> planPolicies = RestHelper.hideSensitiveDataFromPolicySummaryBeanList(
                securityContext, query.getPolicies(organizationId, planId, planVersion, PolicyType.Plan));

            PolicyChainBean chain = new PolicyChainBean();
            chain.getPolicies().addAll(planPolicies);
            chain.getPolicies().addAll(apiPolicies);
            return chain;
        });
    }

    @Transactional
    public List<ContractSummaryBean> getApiVersionContracts(String organizationId,
        String apiId, String version, int page, int pageSize) throws ApiVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

        // Try to get the API first - will throw an exception if not found.
        getApiVersion(organizationId, apiId, version);

        final int finalPage = Math.max(page, 1);
        final int finalPageSize = pageSize == 0 ? 20 : pageSize;

        return tryAction(() -> {
            List<ContractSummaryBean> contracts = query.getContracts(organizationId, apiId, version, finalPage, finalPageSize);
            LOGGER.debug(String.format("Got API %s version %s contracts: %s", apiId, version, contracts)); //$NON-NLS-1$
            return contracts;
        });
    }

    @Transactional
    public UsageHistogramBean getUsage(String organizationId, String apiId, String version,
        HistogramIntervalType interval, String fromDate, String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

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

    @Transactional
    public UsagePerClientBean getUsagePerClient(String organizationId, String apiId, String version,
        String fromDate, String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

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

    @Transactional
    public UsagePerPlanBean getUsagePerPlan(String organizationId, String apiId, String version,
        String fromDate, String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

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

    @Transactional
    public ResponseStatsHistogramBean getResponseStats(String organizationId, String apiId,
        String version, HistogramIntervalType interval, String fromDate, String toDate)
        throws NotAuthorizedException, InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

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

    @Transactional
    public ResponseStatsSummaryBean getResponseStatsSummary(String organizationId, String apiId,
        String version, String fromDate, String toDate) throws NotAuthorizedException,
        InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

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

    @Transactional
    public ResponseStatsPerClientBean getResponseStatsPerClient(String organizationId, String apiId,
        String version, String fromDate, String toDate) throws NotAuthorizedException,
        InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

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

    @Transactional
    public ResponseStatsPerPlanBean getResponseStatsPerPlan(String organizationId, String apiId,
        String version, String fromDate, String toDate) throws NotAuthorizedException,
        InvalidMetricCriteriaException {
        securityContext.checkPermissions(PermissionType.apiView, organizationId);

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

    @Transactional
    public PlanBean createPlan(String organizationId, NewPlanBean bean) throws OrganizationNotFoundException,
        PlanAlreadyExistsException, NotAuthorizedException, InvalidNameException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        FieldValidator.validateName(bean.getName());

        PlanBean newPlan = new PlanBean();
        newPlan.setName(bean.getName());
        newPlan.setDescription(bean.getDescription());
        newPlan.setId(BeanUtils.idFromName(bean.getName()));
        newPlan.setCreatedOn(new Date());
        newPlan.setCreatedBy(securityContext.getCurrentUser());

        return tryAction(() -> {
            // Store/persist the new plan
            OrganizationBean orgBean = getOrganizationFromStorage(organizationId);
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

            LOGGER.debug(String.format("Created plan: %s", newPlan)); //$NON-NLS-1$
            return newPlan;
        });
    }

    @Transactional
    public PlanBean getPlan(String organizationId, String planId)
        throws PlanNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);
        return tryAction(() -> {
            PlanBean bean = storage.getPlan(organizationId, planId);
            if (bean == null) {
                throw ExceptionFactory.planNotFoundException(planId);
            }
            LOGGER.debug(String.format("Got plan: %s", bean)); //$NON-NLS-1$
            return bean;
        });
    }

    @Transactional
    public SearchResultsBean<AuditEntryBean> getPlanActivity(String organizationId, String planId, int page, int pageSize)
        throws PlanNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        final int finalPage = Math.max(page, 1);
        final int finalPageSize = pageSize <= 0 ? 20 : pageSize;

        PagingBean paging = new PagingBean();
        paging.setPage(finalPage);
        paging.setPageSize(finalPageSize);

        return tryAction(() -> query.auditEntity(organizationId, planId, null, PlanBean.class, paging));
    }

    @Transactional
    public List<PlanSummaryBean> listPlans(String organizationId) throws OrganizationNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgView, organizationId);

        get(organizationId);

        return tryAction(() -> query.getPlansInOrg(organizationId));
    }

    @Transactional
    public void updatePlan(String organizationId, String planId, UpdatePlanBean bean)
        throws PlanNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        EntityUpdatedData auditData = new EntityUpdatedData();

        tryAction(() -> {
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
            LOGGER.debug(String.format("Updated plan: %s", planForUpdate)); //$NON-NLS-1$
        });
    }

    @Transactional
    public PlanVersionBean createPlanVersion(String organizationId, String planId, NewPlanVersionBean bean)
        throws PlanNotFoundException, NotAuthorizedException, InvalidVersionException,
        PlanVersionAlreadyExistsException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        FieldValidator.validateVersion(bean.getVersion());

        PlanVersionBean newVersion = tryAction(() -> {
            PlanBean plan = storage.getPlan(organizationId, planId);
            if (plan == null) {
                throw ExceptionFactory.planNotFoundException(planId);
            }

            if (storage.getPlanVersion(organizationId, planId, bean.getVersion()) != null) {
                throw ExceptionFactory.planVersionAlreadyExistsException(planId, bean.getVersion());
            }

            return createPlanVersionInternal(bean, plan);
        });

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

        LOGGER.debug(String.format("Created plan %s version: %s", planId, newVersion)); //$NON-NLS-1$
        return newVersion;
    }

    /**
     * Creates a plan version.
     */
    private PlanVersionBean createPlanVersionInternal(NewPlanVersionBean bean, PlanBean plan)
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

    @Transactional
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version)
        throws PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        return getPlanVersionInternal(organizationId, planId, version);
    }

    private PlanVersionBean getPlanVersionInternal(String organizationId, String planId, String version) throws PlanVersionNotFoundException {
        return tryAction(() -> {
            PlanVersionBean planVersion = getPlanVersionFromStorage(organizationId, planId, version);
            LOGGER.debug(String.format("Got plan %s version: %s", planId, planVersion)); //$NON-NLS-1$
            return planVersion;
        });
    }

    private PlanVersionBean getPlanVersionFromStorage(String organizationId, String planId, String version) throws PlanVersionNotFoundException, StorageException {
        PlanVersionBean planVersion = storage.getPlanVersion(organizationId, planId, version);
        if (planVersion == null) {
            throw ExceptionFactory.planVersionNotFoundException(planId, version);
        }
        return planVersion;
    }

    @Transactional
    public SearchResultsBean<AuditEntryBean> getPlanVersionActivity(String organizationId, String planId,
        String version, int page, int pageSize) throws PlanVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        final int finalPage = Math.max(page, 1);
        final int finalPageSize = pageSize == 0 ? 20 : pageSize;

        PagingBean paging = new PagingBean();
        paging.setPage(finalPage);
        paging.setPageSize(finalPageSize);
        return tryAction(() -> query.auditEntity(organizationId, planId, version, PlanBean.class, paging));
    }

    @Transactional
    public List<PlanVersionSummaryBean> listPlanVersions(String organizationId, String planId)
        throws PlanNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        // Try to get the plan first - will throw a PlanNotFoundException if not found.
        getPlan(organizationId, planId);

        return tryAction(() -> query.getPlanVersions(organizationId, planId));
    }

    @Transactional
    public PolicyBean createPlanPolicy(String organizationId, String planId, String version,
        NewPolicyBean bean) throws OrganizationNotFoundException, PlanVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        // Make sure the plan version exists and is in the right state
        PlanVersionBean pvb = getPlanVersionInternal(organizationId, planId, version);
        if (pvb.getStatus() == PlanStatus.Locked) {
            throw ExceptionFactory.invalidPlanStatusException();
        }

        return tryAction(() -> {
            pvb.setModifiedOn(new Date());
            pvb.setModifiedBy(securityContext.getCurrentUser());

            LOGGER.debug(String.format("Creating plan %s policy %s", planId, pvb)); //$NON-NLS-1$
            return doCreatePolicy(organizationId, planId, version, bean, PolicyType.Plan);
        });
    }

    @Transactional
    public PolicyBean getPlanPolicy(String organizationId, String planId, String version, long policyId)
        throws OrganizationNotFoundException, PlanVersionNotFoundException,
        PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        // Make sure the plan version exists
        getPlanVersionInternal(organizationId, planId, version);

        PolicyBean policy = doGetPolicy(PolicyType.Plan, organizationId, planId, version, policyId);

        LOGGER.debug(String.format("Got plan policy %s", policy)); //$NON-NLS-1$
        return policy;
    }

    @Transactional
    public void updatePlanPolicy(String organizationId, String planId, String version,
        long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
        PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        // Make sure the plan version exists
        PlanVersionBean pvb = getPlanVersionInternal(organizationId, planId, version);

        tryAction(() -> {
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

            LOGGER.debug(String.format("Updated plan policy %s", policy)); //$NON-NLS-1$
        });
    }

    @Transactional
    public void deletePlanPolicy(String organizationId, String planId, String version, long policyId)
        throws OrganizationNotFoundException, PlanVersionNotFoundException,
        PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        // Make sure the plan version exists
        PlanVersionBean pvb = getPlanVersionInternal(organizationId, planId, version);
        if (pvb.getStatus() == PlanStatus.Locked) {
            throw ExceptionFactory.invalidPlanStatusException();
        }

        tryAction(() -> {
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
            LOGGER.debug(String.format("Deleted plan policy %s", policy)); //$NON-NLS-1$
        });
    }

    @Transactional
    public void deletePlan(String organizationId, String planId)
        throws ApiNotFoundException, NotAuthorizedException, InvalidPlanStatusException {
        securityContext.checkPermissions(PermissionType.planAdmin, organizationId);

        List<PlanVersionSummaryBean> lockedPlans = listPlanVersions(organizationId, planId).stream()
            .filter(summary -> summary.getStatus() == PlanStatus.Locked).collect(toList());

        if (!lockedPlans.isEmpty())
            throw ExceptionFactory.invalidPlanStatusException(lockedPlans);

        tryAction(() -> {
            PlanBean plan = storage.getPlan(organizationId, planId);
            storage.deletePlan(plan);
            storage.commitTx();
        });
    }

    @Transactional
    public List<PolicySummaryBean> listPlanPolicies(String organizationId, String planId, String version)
        throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        // Try to get the plan first - will throw an exception if not found.
        getPlanVersionInternal(organizationId, planId, version);

        return tryAction(() -> query.getPolicies(organizationId, planId, version, PolicyType.Plan));
    }

    @Transactional
    public void reorderPlanPolicies(String organizationId, String planId, String version,
        PolicyChainBean policyChain) throws OrganizationNotFoundException,
        PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        // Make sure the plan version exists
        PlanVersionBean pvb = getPlanVersionInternal(organizationId, planId, version);

        tryAction(() -> {
            List<Long> newOrder = new ArrayList<>(policyChain.getPolicies().size());
            for (PolicySummaryBean psb : policyChain.getPolicies()) {
                newOrder.add(psb.getId());
            }
            storage.reorderPolicies(PolicyType.Plan, organizationId, planId, version, newOrder);
            storage.createAuditEntry(AuditUtils.policiesReordered(pvb, PolicyType.Plan, securityContext));

            pvb.setModifiedBy(securityContext.getCurrentUser());
            pvb.setModifiedOn(new Date());
            storage.updatePlanVersion(pvb);
        });
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
     */
    private PolicyBean doCreatePolicy(String organizationId, String entityId, String entityVersion,
        NewPolicyBean bean, PolicyType type) throws Exception {

        if (bean.getDefinitionId() == null) {
            throw ExceptionFactory.policyDefNotFoundException("null"); //$NON-NLS-1$
        }

        PolicyDefinitionBean def = storage.getPolicyDefinition(bean.getDefinitionId());
        if (def == null) {
            throw ExceptionFactory.policyDefNotFoundException(bean.getDefinitionId());
        }

        int newIdx = query.getMaxPolicyOrderIndex(organizationId, entityId, entityVersion, type) + 1;

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

        PolicyTemplateUtil.generatePolicyDescription(policy);

        LOGGER.debug(String.format("Created client policy: %s", policy)); //$NON-NLS-1$
        return policy;
    }

    @Transactional
    public void grant(String organizationId, GrantRolesBean bean) throws OrganizationNotFoundException,
        RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgAdmin, organizationId);

        // Verify that the references are valid.
        get(organizationId);
        users.get(bean.getUserId());
        for (String roleId : bean.getRoleIds()) {
            roles.get(roleId);
        }

        MembershipData auditData = new MembershipData();
        auditData.setUserId(bean.getUserId());

        tryAction(() -> {
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
        });
    }

    @Transactional
    public void revoke(String organizationId, String roleId, String userId)
        throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgAdmin, organizationId);

        get(organizationId);
        users.get(userId);
        roles.get(roleId);

        MembershipData auditData = new MembershipData();
        auditData.setUserId(userId);

        tryAction(() -> {
            storage.deleteMembership(userId, roleId, organizationId);
            auditData.addRole(roleId);
            storage.createAuditEntry(AuditUtils.membershipRevoked(organizationId, auditData, securityContext));
            LOGGER.debug(String.format("Revoked User %s Role %s Org %s", userId, roleId, organizationId)); //$NON-NLS-1$
        });
    }

    @Transactional
    public void revokeAll(String organizationId, String userId) throws OrganizationNotFoundException,
        RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgAdmin, organizationId);

        get(organizationId);
        users.get(userId);

        MembershipData auditData = new MembershipData();
        auditData.setUserId(userId);
        auditData.addRole("*"); //$NON-NLS-1$
        tryAction(() -> {
            storage.deleteMemberships(userId, organizationId);
            storage.createAuditEntry(AuditUtils.membershipRevoked(organizationId, auditData, securityContext));
        });
    }

    @Transactional
    public List<MemberBean> listMembers(String organizationId) throws OrganizationNotFoundException,
        NotAuthorizedException {
        // Only members are allowed to see other members
        if (!securityContext.isMemberOf(organizationId)) {
            throw ExceptionFactory.notAuthorizedException();
        }

        get(organizationId);

        return tryAction(() -> {
            Set<RoleMembershipBean> memberships = query.getOrgMemberships(organizationId);
            TreeMap<String, MemberBean> members = new TreeMap<>();

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
        });
    }

    /**
     * Gets a policy by its id.  Also verifies that the policy really does belong to
     * the entity indicated.
     */
    private PolicyBean doGetPolicy(PolicyType type, String organizationId, String entityId,
        String entityVersion, long policyId) throws PolicyNotFoundException, StorageException, Exception {

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

    private ApiVersionBean getApiVersionFromStorage(String organizationId, String apiId, String version) throws StorageException {
        ApiVersionBean apiVersion = storage.getApiVersion(organizationId, apiId, version);
        if (apiVersion == null) {
            throw ExceptionFactory.apiVersionNotFoundException(apiId, version);
        }
        return apiVersion;
    }

    /**
     * Get the organization from storage.
     * Will throw an exception if no organization is found
     * @param organizationId the organizationId
     * @return the organization
     * @throws OrganizationNotFoundException if no organizations is found
     */
    private OrganizationBean getOrganizationFromStorage(String organizationId) throws OrganizationNotFoundException, StorageException {
        OrganizationBean organizationBean = storage.getOrganization(organizationId);
        if (organizationBean == null) {
            throw ExceptionFactory.organizationNotFoundException(organizationId);
        }
        return organizationBean;
    }


    /**
     * Checks if the api version has an api definition
     *
     * @param apiVersion the apiVersion
     * @return true if the version has a definition, else false
     */
    private boolean apiVersionHasApiDefinition(ApiVersionBean apiVersion) throws StorageException {
        // additional check if the document really exists in the storage
        return apiVersion.getDefinitionType() != null
            && apiVersion.getDefinitionType() != ApiDefinitionType.None
            && storage.getApiDefinition(apiVersion) != null;
    }

    /**
     * Try an action that may result in an exception
     */
    private <T> T tryAction(StorageSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (AbstractRestException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * Try an action that may result in an exception
     */
    private void tryAction(StorageCaller supplier) {
        try {
            supplier.call();
        } catch (AbstractRestException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    @FunctionalInterface
    private interface StorageSupplier<T> {
        T get() throws StorageException, Exception;
    }

    @FunctionalInterface
    private interface StorageCaller {
        void call() throws StorageException, Exception;
    }

}
