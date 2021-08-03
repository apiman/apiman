package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.audit.data.EntityUpdatedData;
import io.apiman.manager.api.beans.audit.data.MembershipData;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.GrantRolesBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.members.MemberBean;
import io.apiman.manager.api.beans.members.MemberRoleBean;
import io.apiman.manager.api.beans.orgs.NewOrganizationBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.orgs.UpdateOrganizationBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiEntryBean;
import io.apiman.manager.api.beans.summary.ApiRegistryBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.config.ApiManagerConfig;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.IRoleResource;
import io.apiman.manager.api.rest.IUserResource;
import io.apiman.manager.api.rest.exceptions.ClientVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.EntityStillActiveException;
import io.apiman.manager.api.rest.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.RoleNotFoundException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.UserNotFoundException;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.rest.impl.util.FieldValidator;
import io.apiman.manager.api.rest.impl.util.RestHelper;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Organization services
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class OrganizationService implements DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(OrganizationService.class);

    private ApiManagerConfig config;
    private IStorage storage;
    private IStorageQuery query;
    private IUserResource users;
    private IRoleResource roles;
    private ISecurityContext securityContext;
    private IGatewayLinkFactory gatewayLinkFactory;
    private ClientAppService clientService;

    /**
     * Constructor.
     */
    @Inject
    public OrganizationService(
        ApiManagerConfig config,
        IStorage storage,
        IStorageQuery query,
        IUserResource users,
        IRoleResource roles,
        ISecurityContext securityContext,
        IGatewayLinkFactory gatewayLinkFactory,
        ClientAppService clientService
    ) {
        this.config = config;
        this.storage = storage;
        this.query = query;
        this.users = users;
        this.roles = roles;
        this.securityContext = securityContext;
        this.gatewayLinkFactory = gatewayLinkFactory;
        this.clientService = clientService;
    }

    public OrganizationService() {
    }

    public OrganizationBean createOrg(
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
                RoleMembershipBean membership = RoleMembershipBean.create(currentUser, roleBean.getId(), orgId);
                membership.setCreatedOn(new Date());
                storage.createMembership(membership);
            }

            LOGGER.debug(String.format("Created organization %s: %s", orgBean.getName(), orgBean)); //$NON-NLS-1$

            return orgBean;
        });
    }

    public void deleteOrg(String organizationId)
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
    
    public OrganizationBean getOrg(String organizationId) throws OrganizationNotFoundException {
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

    public void updateOrg(String organizationId, UpdateOrganizationBean bean)
        throws OrganizationNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgEdit, organizationId);

        OrganizationBean orgForUpdate = tryAction(() -> getOrganizationFromStorage(organizationId));

        EntityUpdatedData auditData = new EntityUpdatedData();
        if (AuditUtils.valueChanged(orgForUpdate.getDescription(), bean.getDescription())) {
            auditData.addChange("description", orgForUpdate.getDescription(), bean.getDescription()); //$NON-NLS-1$
            orgForUpdate.setDescription(bean.getDescription());
        }

        tryAction(() -> {
            storage.updateOrganization(orgForUpdate);
            storage.createAuditEntry(AuditUtils.organizationUpdated(orgForUpdate, auditData, securityContext));
        });

        LOGGER.debug(String.format("Updated organization %s: %s", orgForUpdate.getName(), orgForUpdate)); //$NON-NLS-1$
    }
    
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

    /**
     * Gets the API registry.
     */
    public ApiRegistryBean getApiRegistry(String organizationId, String clientId, String version) throws ClientVersionNotFoundException {
        // Try to get the client first - will throw a ClientVersionNotFoundException if not found.
        ClientVersionBean clientVersion = clientService.getClientVersion(organizationId, clientId, version);

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

    public void grant(String organizationId, GrantRolesBean bean) throws OrganizationNotFoundException,
        RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgAdmin, organizationId);

        // Verify that the references are valid.
        getOrg(organizationId);
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

    public void revoke(String organizationId, String roleId, String userId)
        throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgAdmin, organizationId);

        getOrg(organizationId);
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

    public void revokeAll(String organizationId, String userId) throws OrganizationNotFoundException,
        RoleNotFoundException, UserNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgAdmin, organizationId);

        getOrg(organizationId);
        users.get(userId);

        MembershipData auditData = new MembershipData();
        auditData.setUserId(userId);
        auditData.addRole("*"); //$NON-NLS-1$
        tryAction(() -> {
            storage.deleteMemberships(userId, organizationId);
            storage.createAuditEntry(AuditUtils.membershipRevoked(organizationId, auditData, securityContext));
        });
    }

    public List<MemberBean> listMembers(String organizationId) throws OrganizationNotFoundException,
        NotAuthorizedException {
        // Only members are allowed to see other members
        if (!securityContext.isMemberOf(organizationId)) {
            throw ExceptionFactory.notAuthorizedException();
        }

        getOrg(organizationId);

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

}
