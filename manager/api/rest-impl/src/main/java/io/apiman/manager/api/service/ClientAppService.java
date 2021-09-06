package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.audit.data.EntityUpdatedData;
import io.apiman.manager.api.beans.clients.ApiKeyBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.clients.NewClientBean;
import io.apiman.manager.api.beans.clients.NewClientVersionBean;
import io.apiman.manager.api.beans.clients.UpdateClientBean;
import io.apiman.manager.api.beans.contracts.NewContractBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.policies.NewPolicyBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyChainBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.policies.UpdatePolicyBean;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.ClientVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.core.IApiKeyGenerator;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.exceptions.ClientAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ClientNotFoundException;
import io.apiman.manager.api.rest.exceptions.ClientVersionAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ClientVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.EntityStillActiveException;
import io.apiman.manager.api.rest.exceptions.InvalidClientStatusException;
import io.apiman.manager.api.rest.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.exceptions.InvalidVersionException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.rest.impl.util.FieldValidator;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;

import static java.util.stream.Collectors.toList;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class ClientAppService implements DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ClientAppService.class);

    private IStorage storage;
    private IStorageQuery query;
    private ISecurityContext securityContext;
    private OrganizationService organizationService;
    private ContractService contractService;
    private PolicyService policyService;
    private IApiKeyGenerator apiKeyGenerator;

    @Inject
    public ClientAppService(
        IStorage storage,
        IStorageQuery query,
        ISecurityContext securityContext,
        OrganizationService organizationService,
        ContractService contractService,
        PolicyService policyService, IApiKeyGenerator apiKeyGenerator
    ) {
        this.storage = storage;
        this.query = query;
        this.securityContext = securityContext;
        this.organizationService = organizationService;
        this.contractService = contractService;
        this.policyService = policyService;
        this.apiKeyGenerator = apiKeyGenerator;
    }

    public ClientAppService() {
    }

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
            OrganizationBean org = organizationService.getOrg(organizationId);
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
    
    public ClientBean getClient(String organizationId, String clientId)
        throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        ClientBean clientBean = tryAction(() -> getClientFromStorage(organizationId, clientId));
        LOGGER.debug(String.format("Got client %s: %s", clientBean.getName(), clientBean)); //$NON-NLS-1$
        return clientBean;
    }
    
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
            LOGGER.debug("Deleted ClientApp: {0}", client.getName()); //$NON-NLS-1$
        });
    }
    
    public List<ClientSummaryBean> listClients(String organizationId) throws OrganizationNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgView, organizationId);
        return tryAction(() -> query.getClientsInOrg(organizationId));
    }
    
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

    public ClientVersionBean createClientVersion(String organizationId, String clientId,
        NewClientVersionBean bean) throws ClientNotFoundException, NotAuthorizedException,
        InvalidVersionException, ClientVersionAlreadyExistsException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        FieldValidator.validateVersion(bean.getVersion());

        ClientVersionBean newVersion;
        try {
            ClientBean client = getClientFromStorage(organizationId, clientId);
            if (storage.getClientVersion(organizationId, clientId, bean.getVersion()) != null) {
                throw ExceptionFactory.clientVersionAlreadyExistsException(clientId, bean.getVersion());
            }

            newVersion = createClientVersionInternal(bean, client);
        } catch (AbstractRestException e) {
            throw e;
        } catch (Exception e) {
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
                    contractService.createContract(organizationId, clientId, newVersion.getVersion(), ncb);
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

    public ApiKeyBean getClientApiKey(String organizationId, String clientId, String version)
        throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

        ClientVersionBean client = tryAction(() -> getClientVersionInternal(organizationId, clientId, version));
        ApiKeyBean apiKeyBean = new ApiKeyBean();
        apiKeyBean.setApiKey(client.getApikey());
        return apiKeyBean;
    }
    
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

    
    public ClientVersionBean getClientVersion(String organizationId, String clientId, String version)
        throws ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        return getClientVersionInternal(organizationId, clientId, version);
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
        throws ClientVersionNotFoundException {
        ClientVersionBean clientVersion = tryAction(() -> getClientVersionFromStorage(organizationId, clientId, version));
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
    
    public SearchResultsBean<AuditEntryBean> getClientVersionActivity(String organizationId,
        String clientId, String version, int page, int pageSize)
        throws ClientVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

        PagingBean paging = PagingBean.create(page, pageSize);
        return tryAction(() -> query.auditEntity(organizationId, clientId, version, ClientBean.class, paging));
    }
    
    public SearchResultsBean<AuditEntryBean> getClientActivity(String organizationId, String clientId,
        int page, int pageSize) throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);

        PagingBean paging = PagingBean.create(page, pageSize);
        return tryAction(() -> query.auditEntity(organizationId, clientId, null, ClientBean.class, paging));
    }

    public List<ClientVersionSummaryBean> listClientVersions(String organizationId, String clientId)
        throws ClientNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        // Try to get the client first - will throw a ClientNotFoundException if not found.
        getClient(organizationId, clientId);

        return tryAction(() -> query.getClientVersions(organizationId, clientId));
    }
    
    public List<ContractSummaryBean> getClientVersionContracts(String organizationId, String clientId, String version)
        throws ClientNotFoundException, NotAuthorizedException {

        securityContext.checkPermissions(PermissionType.clientView, organizationId);
        // Try to get the client first - will throw a ClientNotFoundException if not found.
        getClientVersionInternal(organizationId, clientId, version);

        return tryAction(() -> query.getClientContracts(organizationId, clientId, version));
    }

    @Transactional
    public PolicyBean createClientPolicy(String organizationId, String clientId, String version,
        NewPolicyBean bean) throws OrganizationNotFoundException, ClientVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientEdit, organizationId);

        return tryAction(() -> {
            // Make sure the Client exists
            ClientVersionBean cvb = getClientVersionInternal(organizationId, clientId, version);

            PolicyBean policy = policyService.createPolicy(organizationId, clientId, version, bean, PolicyType.Client);

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

        return policyService.getPolicy(PolicyType.Client, organizationId, clientId, version, policyId);
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


    private ClientBean getClientFromStorage(String organizationId, String clientId) throws StorageException {
        ClientBean client = storage.getClient(organizationId, clientId);
        if (client == null) {
            throw ExceptionFactory.clientNotFoundException(clientId);
        }
        return client;
    }
}
