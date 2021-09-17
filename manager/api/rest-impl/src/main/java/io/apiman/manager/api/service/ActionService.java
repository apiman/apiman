package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.actions.ContractActionDto;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.ContractStatus;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.ContractApprovalEvent;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.idm.UserMapper;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.core.IClientValidator;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.events.EventService;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.transaction.Transactional;

import com.google.common.collect.Streams;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Transactional
public class ActionService implements DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ActionService.class);
    private IStorage storage;
    private IStorageQuery query;
    private EventService eventService;
    private ContractService contractService;
    private IClientValidator clientValidator;

    @Inject
    public ActionService(IStorage storage,
         IStorageQuery query,
         ContractService contractService,
         EventService eventService,
         IClientValidator clientValidator) {
        this.storage = storage;
        this.query = query;
        this.eventService = eventService;
        this.contractService = contractService;
        this.clientValidator = clientValidator;
    }

    public ActionService() {
    }

    public void approveContract(ContractActionDto action, String approverId) {

        // Must exist
        ContractBean contract = tryAction(() -> storage.getContract(action.getContractId()));
        if (contract == null) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ContractDoesNotExist"));
        }

        // Must be in AwaitingApproval state
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
        fireContractApprovedEvent(approver, contract, orgC, cvb, orgA, avb, plan);
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
                    LOGGER.debug("Client set to ready as all contracts have been approved");
                    avb.setStatus(ApiStatus.Ready);
                    fireClientStatus(approver);
                }
            });
        }
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
}
