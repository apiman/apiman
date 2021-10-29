package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.manager.api.beans.actions.ContractActionDto;
import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.ContractStatus;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.ContractApprovalEvent;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.UserBean;
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
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.exceptions.ActionException;
import io.apiman.manager.api.rest.exceptions.ApiVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.ClientVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.security.ISecurityContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import com.google.common.collect.Streams;

/**
 * Actions like publish, register, re-register, etc.
 *
 * NB: Might fold this into respective services later.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Transactional
@ApplicationScoped
public class ActionService implements DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ActionService.class);
    private IStorage storage;
    private ISecurityContext securityContext;
    private ApiService apiService;
    private PlanService planService;
    private ContractService contractService;
    private IStorageQuery query;
    private EventService eventService;
    private ClientAppService clientAppService;
    private IClientValidator clientValidator;
    private IGatewayLinkFactory gatewayLinkFactory;

    @Inject
    public ActionService(IStorage storage,
         ISecurityContext securityContext,
         ApiService apiService,
         PlanService planService,
         IStorageQuery query,
         ContractService contractService,
         EventService eventService,
         ClientAppService clientAppService,
         IClientValidator clientValidator,
         IGatewayLinkFactory gatewayLinkFactory) {
        this.storage = storage;
        this.securityContext = securityContext;
        this.apiService = apiService;
        this.planService = planService;
        this.query = query;
        this.contractService = contractService;
        this.eventService = eventService;
        this.clientAppService = clientAppService;
        this.clientValidator = clientValidator;
        this.gatewayLinkFactory = gatewayLinkFactory;
    }

    public ActionService() {
    }

    public void approveContract(ContractActionDto action, String approverId) {
        // Must exist
        ContractBean contract = tryAction(() -> storage.getContract(action.getContractId()));
        if (contract == null) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ContractDoesNotExist"));
        }

        // Must be in AwaitingApproval state (no need to approve otherwise!)
        if (contract.getStatus() != ContractStatus.AwaitingApproval) {
            throw ExceptionFactory.invalidContractStatus(ContractStatus.AwaitingApproval, contract.getStatus());
        }

        // We probably need an optimised query :-).
        ClientVersionBean cvb = contract.getClient();
        ApiVersionBean avb = contract.getApi();
        OrganizationBean org = avb.getApi().getOrganization();
        PlanVersionBean plan = contract.getPlan();
        OrganizationBean orgA = avb.getApi().getOrganization();
        OrganizationBean orgC = cvb.getClient().getOrganization();
        UserBean approver = tryAction(() -> storage.getUser(approverId));

        // Set the contract to approved state and send approved event.
        contract.setStatus(ContractStatus.Created);
        LOGGER.debug("{0} approved a contract: {1} -> {2}", approverId, contract, action);

        // In the second phase we need to check the other contracts to see whether they are all in the 'ready' state
        // If so, then it's ready to publish.
        List<ContractBean> contracts = tryAction(
             () -> Streams.stream((storage.getAllContracts(org.getId(), cvb.getClient().getId(), cvb.getVersion())
             ))).collect(Collectors.toList());

        List<ContractBean> awaitingApprovalList = contracts
             .stream()
             .filter(c -> c.getStatus().equals(ContractStatus.AwaitingApproval))
             .collect(Collectors.toList());

        if (awaitingApprovalList.size() > 0) {
            LOGGER.debug("A contract was approved, but {0} other contracts are still awaiting approval, "
                      + "so client version {1} will remain in its pending state until the remaining contract approvals "
                      + "are granted: {2}.", awaitingApprovalList.size(), cvb.getVersion(), awaitingApprovalList);
        } else {
            LOGGER.debug("All contracts for {0} have been approved", cvb.getVersion());
            tryAction(() -> {
                if (clientValidator.isReady(cvb)) {
                    // Set client to ready status and fire change status event
                    LOGGER.debug("Client set to ready as all contracts have been approved");
                    cvb.setStatus(ClientStatus.Ready);
                    clientAppService.fireClientStatusChangeEvent(cvb, ClientStatus.AwaitingApproval);
                    // If auto-promote, then we immediately register.
                    if (action.isAutoPromote()) {
                        LOGGER.debug("Auto approving: {0}", cvb);
                        registerClient(orgC.getId(), cvb.getClient().getId(), cvb.getVersion());
                    }
                }
            });
        }
        //storage.flush();
        fireContractApprovedEvent(approver, contract, orgC, cvb, orgA, avb, plan);
    }

    private void fireContractApprovedEvent(UserBean approver, ContractBean contract, OrganizationBean orgC,
         ClientVersionBean cvb, OrganizationBean orgA, ApiVersionBean avb, PlanVersionBean plan) {
        ApimanEventHeaders headers = ApimanEventHeaders
             .builder()
             .setId(UUID.randomUUID().toString().substring(8))
             .setSource(URI.create("/a/b/c"))
             .setSubject("approval")
             .build();

        var event = ContractApprovalEvent
             .builder()
             .setApprover(UserMapper.toDto(approver))
             .setApproved(true)
             .setHeaders(headers)
             .setClientOrgId(orgC.getId())
             .setClientId(cvb.getClient().getId())
             .setClientVersion(cvb.getVersion())
             .setApiOrgId(orgA.getId())
             .setApiId(avb.getApi().getId())
             .setApiVersion(avb.getVersion())
             .setContractId(String.valueOf(contract.getId()))
             .setPlanId(plan.getPlan().getId())
             .setPlanVersion(plan.getVersion())
             .build();

        LOGGER.debug("Sending contract approval response event: {0}", event);
        eventService.fireEvent(event);
    }

    /**
     * Publishes an API to the gateway.
     */
    public void publishApi(String orgId, String apiId, String apiVersion) throws ActionException, NotAuthorizedException {
        ApiVersionBean versionBean;
        try {
            versionBean = apiService.getApiVersion(orgId, apiId, apiVersion);
        } catch (ApiVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ApiNotFound")); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - API must be Ready.
        if (!versionBean.isPublicAPI() && versionBean.getStatus() != ApiStatus.Ready) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidApiStatus")); //$NON-NLS-1$
        }

        if (versionBean.isPublicAPI()) {
            if (versionBean.getStatus() == ApiStatus.Retired || versionBean.getStatus() == ApiStatus.Created) {
                throw ExceptionFactory.actionException(Messages.i18n.format("InvalidApiStatus")); //$NON-NLS-1$
            }
            if (versionBean.getStatus() == ApiStatus.Published) {
                Date modOn = versionBean.getModifiedOn();
                Date publishedOn = versionBean.getPublishedOn();
                int c = modOn.compareTo(publishedOn);
                if (c <= 0) {
                    throw ExceptionFactory.actionException(Messages.i18n.format("ApiRePublishNotRequired")); //$NON-NLS-1$
                }
            }
        }

        Api gatewayApi = new Api();
        gatewayApi.setEndpoint(versionBean.getEndpoint());
        gatewayApi.setEndpointType(versionBean.getEndpointType().toString());
        if (versionBean.getEndpointContentType() != null) {
            gatewayApi.setEndpointContentType(versionBean.getEndpointContentType().toString());
        }
        gatewayApi.setEndpointProperties(versionBean.getEndpointProperties());
        gatewayApi.setOrganizationId(versionBean.getApi().getOrganization().getId());
        gatewayApi.setApiId(versionBean.getApi().getId());
        gatewayApi.setVersion(versionBean.getVersion());
        gatewayApi.setPublicAPI(versionBean.isPublicAPI());
        gatewayApi.setParsePayload(versionBean.isParsePayload());
        gatewayApi.setKeysStrippingDisabled(versionBean.getDisableKeysStrip());

        try {
            if (versionBean.isPublicAPI()) {
                List<Policy> policiesToPublish = new ArrayList<>();
                List<PolicySummaryBean> apiPolicies = query.getPolicies(orgId,
                     apiId, apiVersion, PolicyType.Api);
                for (PolicySummaryBean policySummaryBean : apiPolicies) {
                    PolicyBean apiPolicy = storage.getPolicy(PolicyType.Api, orgId,
                         apiId, apiVersion, policySummaryBean.getId());
                    Policy policyToPublish = new Policy();
                    policyToPublish.setPolicyJsonConfig(apiPolicy.getConfiguration());
                    policyToPublish.setPolicyImpl(apiPolicy.getDefinition().getPolicyImpl());
                    policiesToPublish.add(policyToPublish);
                }
                gatewayApi.setApiPolicies(policiesToPublish);
            }
        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError"), e); //$NON-NLS-1$
        }

        // Publish the API to all relevant gateways
        try {
            Set<ApiGatewayBean> gateways = versionBean.getGateways();
            if (gateways == null) {
                throw new PublishingException("No gateways specified for API!"); //$NON-NLS-1$
            }
            for (ApiGatewayBean apiGatewayBean : gateways) {
                IGatewayLink gatewayLink = createGatewayLink(apiGatewayBean.getGatewayId());
                gatewayLink.publishApi(gatewayApi);
                gatewayLink.close();
            }

            versionBean.setStatus(ApiStatus.Published);
            versionBean.setPublishedOn(new Date());

            ApiBean api = storage.getApi(orgId, apiId);
            if (api == null) {
                throw new PublishingException("Error: could not find API - " + orgId + "=>" + apiId); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (api.getNumPublished() == null) {
                api.setNumPublished(1);
            } else {
                api.setNumPublished(api.getNumPublished() + 1);
            }

            storage.updateApi(api);
            storage.updateApiVersion(versionBean);
            storage.createAuditEntry(AuditUtils.apiPublished(versionBean, securityContext));
        } catch (Exception e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError"), e); //$NON-NLS-1$
        }

        LOGGER.debug(String.format("Successfully published API %s on specified gateways: %s", //$NON-NLS-1$
             versionBean.getApi().getName(), versionBean.getApi()));
    }

    /**
     * Creates a gateway link given a gateway id.
     */
    private IGatewayLink createGatewayLink(String gatewayId) throws PublishingException {
        try {
            GatewayBean gateway = storage.getGateway(gatewayId);
            if (gateway == null) {
                throw new GatewayNotFoundException();
            }
            IGatewayLink link = gatewayLinkFactory.create(gateway);
            return link;
        } catch (GatewayNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new PublishingException(e.getMessage(), e);
        }
    }

    /**
     * Retires an API that is currently published to the Gateway.
     */
    public void retireApi(String orgId, String apiId, String apiVersion) throws ActionException, NotAuthorizedException {
        ApiVersionBean versionBean;
        try {
            versionBean = apiService.getApiVersion(orgId, apiId, apiVersion);
        } catch (ApiVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ApiNotFound")); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - API must be Published.
        if (versionBean.getStatus() != ApiStatus.Published) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidApiStatus")); //$NON-NLS-1$
        }

        Api gatewayApi = new Api();
        gatewayApi.setOrganizationId(versionBean.getApi().getOrganization().getId());
        gatewayApi.setApiId(versionBean.getApi().getId());
        gatewayApi.setVersion(versionBean.getVersion());

        // Retire the API from all relevant gateways
        try {
            Set<ApiGatewayBean> gateways = versionBean.getGateways();
            if (gateways == null) {
                throw new PublishingException("No gateways specified for API!"); //$NON-NLS-1$
            }
            for (ApiGatewayBean apiGatewayBean : gateways) {
                IGatewayLink gatewayLink = createGatewayLink(apiGatewayBean.getGatewayId());
                gatewayLink.retireApi(gatewayApi);
                gatewayLink.close();
            }

            versionBean.setStatus(ApiStatus.Retired);
            versionBean.setRetiredOn(new Date());

            ApiBean api = storage.getApi(orgId, apiId);
            if (api == null) {
                throw new PublishingException("Error: could not find API - " + orgId + "=>" + apiId); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (api.getNumPublished() == null || api.getNumPublished() == 0) {
                api.setNumPublished(0);
            } else {
                api.setNumPublished(api.getNumPublished() - 1);
            }

            storage.updateApi(api);
            storage.updateApiVersion(versionBean);
            storage.createAuditEntry(AuditUtils.apiRetired(versionBean, securityContext));
        } catch (Exception e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("RetireError"), e); //$NON-NLS-1$
        }

        LOGGER.debug(String.format("Successfully retired API %s on specified gateways: %s", //$NON-NLS-1$
             versionBean.getApi().getName(), versionBean.getApi()));
    }

    /**
     * Registers a client (along with all of its contracts) to the gateway.
     */
    public void registerClient(String orgId, String clientId, String clientVersion) throws ActionException, NotAuthorizedException {
        ClientVersionBean versionBean;
        List<ContractSummaryBean> contractBeans;
        try {
            versionBean = clientAppService.getClientVersion(orgId, clientId, clientVersion);
        } catch (ClientVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("clientVersionDoesNotExist", clientId, clientVersion)); //$NON-NLS-1$
        }
        try {
            contractBeans = query.getClientContracts(orgId, clientId, clientVersion);
            // Any awaiting approval then don't let them republish.
            List<ContractSummaryBean> awaitingApproval = contractBeans.stream()
                         .filter(f -> f.getStatus() == ContractStatus.AwaitingApproval)
                         .collect(Collectors.toList());
            if (!awaitingApproval.isEmpty()) {
                throw ExceptionFactory.contractNotYetApprovedException(awaitingApproval);
            }
        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ClientNotFound"), e); //$NON-NLS-1$
        }

        boolean isReregister = false;

        // Validate that it's ok to perform this action
        if (versionBean.getStatus() == ClientStatus.Registered) {
            Date modOn = versionBean.getModifiedOn();
            Date publishedOn = versionBean.getPublishedOn();
            int c = modOn.compareTo(publishedOn);
            if (c <= 0) {
                throw ExceptionFactory.actionException(Messages.i18n.format("ClientReRegisterNotRequired")); //$NON-NLS-1$
            }
            isReregister = true;
        }

        Client client = new Client();
        client.setOrganizationId(versionBean.getClient().getOrganization().getId());
        client.setClientId(versionBean.getClient().getId());
        client.setVersion(versionBean.getVersion());
        client.setApiKey(versionBean.getApikey());

        Set<Contract> contracts = new HashSet<>();
        for (ContractSummaryBean contractBean : contractBeans) {
            Contract contract = new Contract();
            contract.setPlan(contractBean.getPlanId());
            contract.setApiId(contractBean.getApiId());
            contract.setApiOrgId(contractBean.getApiOrganizationId());
            contract.setApiVersion(contractBean.getApiVersion());
            contract.getPolicies().addAll(contractService.aggregateContractPolicies(contractBean));
            contracts.add(contract);
        }
        client.setContracts(contracts);

        // Next, register the client with *all* relevant gateways.  This is done by
        // looking up all referenced APIs and getting the gateway information for them.
        // Each of those gateways must be told about the client.
        try {
            Map<String, IGatewayLink> links = new HashMap<>();
            for (Contract contract : client.getContracts()) {
                ApiVersionBean svb = storage.getApiVersion(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
                Set<ApiGatewayBean> gateways = svb.getGateways();
                if (gateways == null) {
                    throw new PublishingException("No gateways specified for API: " + svb.getApi().getName()); //$NON-NLS-1$
                }
                for (ApiGatewayBean apiGatewayBean : gateways) {
                    if (!links.containsKey(apiGatewayBean.getGatewayId())) {
                        IGatewayLink gatewayLink = createGatewayLink(apiGatewayBean.getGatewayId());
                        links.put(apiGatewayBean.getGatewayId(), gatewayLink);
                    }
                }
            }
            if (isReregister) {
                // Once we figure out which gateways to register with, make sure we also "unregister"
                // the client app from all other gateways.  This is necessary because we may have broken
                // contracts we previously had on APIs that are published to other gateways.  And thus
                // it's possible we need to remove a contract from a Gateway that is not otherwise/currently
                // referenced.
                //
                // This is a fix for:  https://issues.jboss.org/browse/APIMAN-895

                Iterator<GatewayBean> gateways = storage.getAllGateways();
                while (gateways.hasNext()) {
                    GatewayBean gbean = gateways.next();
                    if (!links.containsKey(gbean.getId())) {
                        IGatewayLink gatewayLink = createGatewayLink(gbean.getId());

                        try {
                            gatewayLink.unregisterClient(client);
                        } catch (Exception e) {
                            // We need to catch the error, but ignore it,
                            // in the event that the gateway is invalid.
                        }

                        gatewayLink.close();
                    }
                }
            }
            for (IGatewayLink gatewayLink : links.values()) {
                gatewayLink.registerClient(client);
                gatewayLink.close();
            }
        } catch (Exception e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("RegisterError"), e); //$NON-NLS-1$
        }

        ClientStatus oldStatus = versionBean.getStatus();
        versionBean.setStatus(ClientStatus.Registered);
        versionBean.setPublishedOn(new Date());

        try {
            storage.updateClientVersion(versionBean);
            storage.createAuditEntry(AuditUtils.clientRegistered(versionBean, securityContext));
            clientAppService.fireClientStatusChangeEvent(versionBean, oldStatus);
        } catch (Exception e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("RegisterError"), e); //$NON-NLS-1$
        }

        LOGGER.debug(String.format("Successfully registered Client %s on specified gateways: %s", //$NON-NLS-1$
             versionBean.getClient().getName(), versionBean.getClient()));
    }

    /**
     * De-registers an client that is currently registered with the gateway.
     */
    public void unregisterClient(String orgId, String clientId, String clientVersion) throws ActionException, NotAuthorizedException {
        ClientVersionBean versionBean;
        List<ContractSummaryBean> contractBeans;
        try {
            versionBean = clientAppService.getClientVersion(orgId, clientId, clientVersion);
        } catch (ClientVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ClientNotFound")); //$NON-NLS-1$
        }
        try {
            contractBeans = query.getClientContracts(orgId, clientId, clientVersion)
                    .stream()
                    .peek(c -> {
                        if (c.getStatus() != ContractStatus.Created) {
                            LOGGER.debug("Will not try to delete contract {0} from gateway(s) as it is not in 'Created' state", c);
                        }
                    })
                    .filter(c -> c.getStatus() == ContractStatus.Created)
                    .collect(Collectors.toList());

        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ClientNotFound"), e); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - client must be either registered or awaiting approval (or there's nothing to unregister)
        if (versionBean.getStatus() != ClientStatus.Registered && versionBean.getStatus() != ClientStatus.AwaitingApproval) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidClientStatus")); //$NON-NLS-1$
        }

        Client client = new Client();
        client.setOrganizationId(versionBean.getClient().getOrganization().getId());
        client.setClientId(versionBean.getClient().getId());
        client.setVersion(versionBean.getVersion());

        // Next, unregister the client from *all* relevant gateways.  This is done by
        // looking up all referenced APIs and getting the gateway information for them.
        // Each of those gateways must be told about the client.
        try {
            Map<String, IGatewayLink> links = new HashMap<>();
            for (ContractSummaryBean contractBean : contractBeans) {
                ApiVersionBean svb = storage.getApiVersion(contractBean.getApiOrganizationId(), contractBean.getApiId(), contractBean.getApiVersion());
                Set<ApiGatewayBean> gateways = svb.getGateways();
                if (gateways == null) {
                    throw new PublishingException("No gateways specified for API: " + svb.getApi().getName()); //$NON-NLS-1$
                }
                for (ApiGatewayBean apiGatewayBean : gateways) {
                    if (!links.containsKey(apiGatewayBean.getGatewayId())) {
                        IGatewayLink gatewayLink = createGatewayLink(apiGatewayBean.getGatewayId());
                        links.put(apiGatewayBean.getGatewayId(), gatewayLink);
                    }
                }
            }
            for (IGatewayLink gatewayLink : links.values()) {
                gatewayLink.unregisterClient(client);
                gatewayLink.close();
            }
        } catch (Exception e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("UnregisterError"), e); //$NON-NLS-1$
        }

        ClientStatus oldStatus = versionBean.getStatus();
        versionBean.setStatus(ClientStatus.Retired);
        versionBean.setRetiredOn(new Date());
        clientAppService.fireClientStatusChangeEvent(versionBean, oldStatus);

        try {
            storage.updateClientVersion(versionBean);
            storage.createAuditEntry(AuditUtils.clientUnregistered(versionBean, securityContext));
        } catch (Exception e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("UnregisterError"), e); //$NON-NLS-1$
        }

        LOGGER.debug(String.format("Successfully registered Client %s on specified gateways: %s", //$NON-NLS-1$
             versionBean.getClient().getName(), versionBean.getClient()));
    }

    /**
     * Locks the plan.
     */
    public void lockPlan(String orgId, String planId, String planVersion) throws ActionException, NotAuthorizedException {
        PlanVersionBean versionBean;
        try {
            versionBean = planService.getPlanVersion(orgId, planId, planVersion);
        } catch (PlanVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PlanNotFound")); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - plan must not already be locked
        if (versionBean.getStatus() == PlanStatus.Locked) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidPlanStatus")); //$NON-NLS-1$
        }

        versionBean.setStatus(PlanStatus.Locked);
        versionBean.setLockedOn(new Date());

        try {
            storage.updatePlanVersion(versionBean);
            storage.createAuditEntry(AuditUtils.planLocked(versionBean, securityContext));
        } catch (Exception e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("LockError"), e); //$NON-NLS-1$
        }

        LOGGER.debug(String.format("Successfully locked Plan %s: %s", //$NON-NLS-1$
             versionBean.getPlan().getName(), versionBean.getPlan()));
    }
}
