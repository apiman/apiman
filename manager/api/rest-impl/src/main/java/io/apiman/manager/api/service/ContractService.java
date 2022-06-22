package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.Preconditions;
import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiPlanBean;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.ContractStatus;
import io.apiman.manager.api.beans.contracts.NewContractBean;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.ContractCreatedEvent;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.idm.UserMapper;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.core.IClientValidator;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.events.EventService;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.exceptions.ApiNotFoundException;
import io.apiman.manager.api.rest.exceptions.ClientNotFoundException;
import io.apiman.manager.api.rest.exceptions.ContractAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ContractNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidClientStatusException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanNotFoundException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.security.ISecurityContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import com.google.common.collect.Lists;

import static io.apiman.manager.api.beans.contracts.ContractStatus.Created;
import static io.apiman.manager.api.beans.idm.PermissionType.planAdmin;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class ContractService implements DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ContractService.class);

    private IStorage storage;
    private IStorageQuery query;
    private EventService eventService;
    private ClientAppService clientAppService;
    private PlanService planService;
    private ISecurityContext securityContext;
    private IClientValidator clientValidator;
    private IGatewayLinkFactory gatewayLinkFactory;
    private ActionService actionService;


    @Inject
    public ContractService(IStorage storage,
                           IStorageQuery query,
                           EventService eventService,
                           ClientAppService clientAppService,
                           PlanService planService,
                           ISecurityContext securityContext,
                           IClientValidator clientValidator,
                           IGatewayLinkFactory gatewayLinkFactory,
                           ActionService actionService) {
        this.storage = storage;
        this.query = query;
        this.eventService = eventService;
        this.clientAppService = clientAppService;
        this.planService = planService;
        this.securityContext = securityContext;
        this.clientValidator = clientValidator;
        this.gatewayLinkFactory = gatewayLinkFactory;
        this.actionService = actionService;
    }

    public ContractService() {
    }

    public ContractBean createContract(String organizationId, String clientId, String version,
        NewContractBean bean) throws OrganizationNotFoundException, ClientNotFoundException,
        ApiNotFoundException, PlanNotFoundException, ContractAlreadyExistsException,
        NotAuthorizedException {

        ContractBean contract = tryAction(() -> createContractInternal(organizationId, clientId, version, bean));
        LOGGER.debug("Created new contract {0}: {1}", contract.getId(), contract); //$NON-NLS-1$
        if (contract.getStatus() == ContractStatus.AwaitingApproval) {
            fireContractApprovalRequest(securityContext.getCurrentUser(), contract);
        }
        return contract;
    }

    /**
     * Creates a contract.
     */
    protected ContractBean createContractInternal(String clientOrgId, String clientId, String clientVersion, NewContractBean bean) throws Exception {
        ClientVersionBean cvb = clientAppService.getClientVersion(clientOrgId, clientId, clientVersion);

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
        Set<ApiPlanBean> plans = Optional.ofNullable(avb.getPlans())
                                         .orElse(Collections.emptySet());

        ApiPlanBean apiPlanBean = plans.stream()
             .filter(apb -> apb.getPlanId().equals(bean.getPlanId()))
             .findFirst()
             .orElseThrow(() -> ExceptionFactory.planNotFoundException(bean.getPlanId()));

        PlanVersionBean pvb = planService.getPlanVersion(bean.getApiOrgId(), bean.getPlanId(), apiPlanBean.getVersion());
        if (pvb.getStatus() != PlanStatus.Locked) {
            throw ExceptionFactory.invalidPlanStatusException();
        }

        ContractBean contract = new ContractBean();
        contract.setClient(cvb);
        contract.setApi(avb);
        contract.setPlan(pvb);
        contract.setCreatedBy(securityContext.getCurrentUser());
        contract.setCreatedOn(new Date());

        OrganizationBean planOrg = pvb.getPlan().getOrganization();

        if (!apiPlanBean.isRequiresApproval() || securityContext.hasPermission(planAdmin, planOrg.getId())) {
            LOGGER.debug("Contract valid immediately ✅: {0}", contract);
            contract.setStatus(Created);
        } else {
            LOGGER.debug("Contract requires approval ✋: {0}", contract);
            contract.setStatus(ContractStatus.AwaitingApproval);
        }

        try {
            storage.createContract(contract);
        } catch (IllegalStateException ise) {
            throw ExceptionFactory.contractDuplicateException();
        }
        storage.createAuditEntry(AuditUtils.contractCreatedFromClient(contract, securityContext));
        storage.createAuditEntry(AuditUtils.contractCreatedToApi(contract, securityContext));

        // Determine what status of CVB should be now
        ClientStatus oldStatus = cvb.getStatus();
        ClientStatus newStatus = clientValidator.determineStatus(cvb);
        if (oldStatus != newStatus) {
            cvb.setStatus(newStatus);
            clientAppService.fireClientStatusChangeEvent(cvb, oldStatus);
        }

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

    public List<ContractSummaryBean> getClientContractSummaries(String orgId, String clientId, String version)
         throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException, StorageException {
        return query.getClientContracts(orgId, clientId, version);
    }

    @Transactional
    public ContractBean getContract(Long contractId) throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException {
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

        ArrayList<ContractBean> contractsToDelete = Lists.newArrayList(tryAction(() -> storage.getAllContracts(organizationId, clientId, version)));
        try {
            actionService.unregisterClient(organizationId, clientId, version);
            deleteContractsInternal(organizationId, clientId, version, contractsToDelete, contractsToDelete);
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    @Transactional
    public void deleteContract(String organizationId, String clientId, String version, Long contractId)
        throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException,
        InvalidClientStatusException {

        try {
            ArrayList<ContractBean> allContracts = Lists.newArrayList(tryAction(() -> storage.getAllContracts(organizationId, clientId, version)));
            ContractBean contractToDelete = storage.getContract(contractId);
            if (allContracts.size() <= 1) {
                // If we are deleting the only/last contract, then we can unregister.
                actionService.unregisterClient(organizationId, clientId, version);
                deleteContractsInternal(organizationId, clientId, version, allContracts, List.of(contractToDelete));
            } else {
                // If we still have contracts left, we should re-register the existing client.
                ClientStatus newStatus = deleteContractsInternal(organizationId, clientId, version, allContracts, List.of(contractToDelete));
                if (newStatus == ClientStatus.Registered || newStatus == ClientStatus.AwaitingApproval) {
                    actionService.registerClient(organizationId, clientId, version);
                }
            }
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    private ClientStatus deleteContractsInternal(String organizationId, String clientId, String clientVersion, List<ContractBean> allContracts, List<ContractBean> contractsToDelete)
            throws Exception {
        Preconditions.checkArgument(allContracts.size() > 0, "Must have at least 1 contract if you want to delete");
        Preconditions.checkArgument(contractsToDelete.size() > 0, "Must nominate at least 1 contract to delete");
        for (ContractBean contract : contractsToDelete) {
            Long contractId = contract.getId();
            if (!contract.getClient().getClient().getOrganization().getId().equals(organizationId)) {
                throw ExceptionFactory.contractNotFoundException(contractId);
            }
            if (!contract.getClient().getClient().getId().equals(clientId)) {
                throw ExceptionFactory.contractNotFoundException(contractId);
            }
            if (!contract.getClient().getVersion().equals(clientVersion)) {
                throw ExceptionFactory.contractNotFoundException(contractId);
            }
            // if (contract.getClient().getStatus() == ClientStatus.Retired) {
            //     throw ExceptionFactory.invalidClientStatusException();
            // }
            storage.deleteContract(contract);
            storage.createAuditEntry(AuditUtils.contractBrokenFromClient(contract, securityContext));
            storage.createAuditEntry(AuditUtils.contractBrokenToApi(contract, securityContext));
        }

        // Update the version with new meta-data (e.g. modified-by)
        ClientVersionBean clientV = clientAppService.getClientVersion(organizationId, clientId, clientVersion);
        clientV.setModifiedBy(securityContext.getCurrentUser());
        clientV.setModifiedOn(new Date());

        ClientStatus newStatus = clientValidator.determineStatus(clientV, allContracts);
        LOGGER.debug("New status for client version {0} is: {1}", clientV, newStatus);
        clientV.setStatus(newStatus);
        storage.updateClientVersion(clientV);
        LOGGER.debug("Deleted contract(s): {0}", contractsToDelete); //$NON-NLS-1$
        return newStatus;
    }

    // TODO make properly optimised query for this
    public List<IPolicyProbeResponse> probePolicy(Long contractId, long policyId, String rawPayload)
            throws ClientNotFoundException, ContractNotFoundException {
        ContractBean contract = getContract(contractId);
        ApiVersionBean avb = contract.getApi();
        OrganizationBean apiOrg = avb.getApi().getOrganization();
        String apiKey = contract.getClient().getApikey();
        Set<String> gatewayIds = contract.getApi()
                .getGateways()
                .stream()
                .map(ApiGatewayBean::getGatewayId)
                .collect(Collectors.toSet());
        if (gatewayIds.size() == 0) {
            return List.of();
        }

        List<PolicyBean> policyChain = aggregateContractPolicies(contract);

        int idxFound = -1;
        for (int i = 0, policyChainSize = policyChain.size(); i < policyChainSize; i++) {
            PolicyBean policy = policyChain.get(i);
            if (policy.getId().equals(policyId)) {
                idxFound = i;
            }
        }
        if (idxFound == -1) {
            throw new IllegalArgumentException("Provided policy ID not found in contract " + policyId);
        }

        List<GatewayBean> gateways = tryAction(() -> storage.getGateways(gatewayIds));
        LOGGER.debug("Gateways for contract {0}: {1}", contractId, gateways);

        List<IPolicyProbeResponse> probeResponses = new ArrayList<>(gateways.size());
        for (GatewayBean gateway : gateways) {
            IGatewayLink link = gatewayLinkFactory.create(gateway);
            try {
                probeResponses.add(link.probe(apiOrg.getId(), avb.getApi().getId(), avb.getVersion(), idxFound, apiKey, rawPayload));
            } catch (GatewayAuthenticationException e) {
                throw new SystemErrorException(e);
            }
        }
        LOGGER.debug("Probe responses for contract {0}: {1}", contractId, probeResponses);
        return probeResponses;
    }

    public List<PolicyBean> aggregateContractPolicies(ContractBean contractBean) {
        try {
            List<PolicyBean> policies = new ArrayList<>();
            PolicyType [] types = new PolicyType[] {
                    PolicyType.Client, PolicyType.Plan, PolicyType.Api
            };
            for (PolicyType policyType : types) {
                String org, id, ver;
                switch (policyType) {
                    case Client: {
                        org = contractBean.getClient().getClient().getOrganization().getId();
                        id = contractBean.getClient().getClient().getId();
                        ver = contractBean.getClient().getVersion();
                        break;
                    }
                    case Plan: {
                        org = contractBean.getPlan().getPlan().getOrganization().getId();
                        id = contractBean.getPlan().getPlan().getId();
                        ver = contractBean.getPlan().getVersion();
                        break;
                    }
                    case Api: {
                        org = contractBean.getApi().getApi().getOrganization().getId();
                        id = contractBean.getApi().getApi().getId();
                        ver = contractBean.getApi().getVersion();
                        break;
                    }
                    default: {
                        throw new RuntimeException("Missing case for switch!"); //$NON-NLS-1$
                    }
                }

                List<PolicySummaryBean> clientPolicies = query.getPolicies(org, id, ver, policyType);
                for (PolicySummaryBean policySummaryBean : clientPolicies) {
                    policies.add(storage.getPolicy(policyType, org, id, ver, policySummaryBean.getId()));
                }
            }
            return policies;
        } catch (Exception e) {
            throw ExceptionFactory.actionException(
                    Messages.i18n.format("ErrorAggregatingPolicies", e)); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Aggregates the API, client, and plan policies into a single ordered list.
     */
    public List<Policy> aggregateContractPolicies(ContractSummaryBean contractBean) {
        try {
            List<Policy> policies = new ArrayList<>();
            PolicyType [] types = new PolicyType[] {
                    PolicyType.Client, PolicyType.Plan, PolicyType.Api
            };
            for (PolicyType policyType : types) {
                String org, id, ver;
                switch (policyType) {
                    case Client: {
                        org = contractBean.getClientOrganizationId();
                        id = contractBean.getClientId();
                        ver = contractBean.getClientVersion();
                        break;
                    }
                    case Plan: {
                        org = contractBean.getApiOrganizationId();
                        id = contractBean.getPlanId();
                        ver = contractBean.getPlanVersion();
                        break;
                    }
                    case Api: {
                        org = contractBean.getApiOrganizationId();
                        id = contractBean.getApiId();
                        ver = contractBean.getApiVersion();
                        break;
                    }
                    default: {
                        throw new RuntimeException("Missing case for switch!"); //$NON-NLS-1$
                    }
                }

                List<PolicySummaryBean> clientPolicies = query.getPolicies(org, id, ver, policyType);
                for (PolicySummaryBean policySummaryBean : clientPolicies) {
                    PolicyBean policyBean = storage.getPolicy(policyType, org, id, ver, policySummaryBean.getId());
                    Policy policy = new Policy();
                    policy.setPolicyJsonConfig(policyBean.getConfiguration());
                    policy.setPolicyImpl(policyBean.getDefinition().getPolicyImpl());
                    policies.add(policy);
                }
            }
            return policies;
        } catch (Exception e) {
            throw ExceptionFactory.actionException(
                    Messages.i18n.format("ErrorAggregatingPolicies", contractBean.getClientId() + "->" + contractBean.getApiDescription()), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private void fireContractApprovalRequest(String requesterId, ContractBean contract) {
        LOGGER.debug("Firing contract approval request from requester {0} on contract {1}", requesterId, contract);
        UserDto requester = UserMapper.INSTANCE.toDto(tryAction(() -> storage.getUser(requesterId)));

        ApimanEventHeaders headers = ApimanEventHeaders
             .builder()
             .setId(UUID.randomUUID().toString())
             .setSource(URI.create("/apiman/events/contracts/approvals"))
             .setSubject("request")
             .build();

        PlanVersionBean plan = contract.getPlan();
        ClientVersionBean cvb = contract.getClient();
        ApiVersionBean avb = contract.getApi();
        OrganizationBean orgA = avb.getApi().getOrganization();
        OrganizationBean orgC = cvb.getClient().getOrganization();

        var approvalRequestEvent = ContractCreatedEvent
             .builder()
             .setHeaders(headers)
             .setUser(requester)
             .setClientOrgId(orgC.getId())
             .setClientId(cvb.getClient().getId())
             .setClientVersion(cvb.getVersion())
             .setApiOrgId(orgA.getId())
             .setApiId(avb.getApi().getId())
             .setApiVersion(avb.getVersion())
             .setContractId(String.valueOf(contract.getId()))
             .setPlanId(plan.getPlan().getId())
             .setPlanVersion(plan.getVersion())
             .setApprovalRequired(true)
             .build();

        LOGGER.debug("Sending approval request event {0}", approvalRequestEvent);
        eventService.fireEvent(approvalRequestEvent);
    }
}
