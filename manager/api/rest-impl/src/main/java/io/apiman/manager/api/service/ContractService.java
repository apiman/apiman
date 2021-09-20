package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
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
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.idm.UserMapper;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.core.IClientValidator;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.events.EventService;
import io.apiman.manager.api.rest.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.exceptions.ApiNotFoundException;
import io.apiman.manager.api.rest.exceptions.ClientNotFoundException;
import io.apiman.manager.api.rest.exceptions.ContractAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ContractNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidClientStatusException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanNotFoundException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.security.ISecurityContext;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

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

    @Inject
    public ContractService(IStorage storage,
        IStorageQuery query,
        EventService eventService,
        ClientAppService clientAppService,
        PlanService planService,
        ISecurityContext securityContext,
        IClientValidator clientValidator) {
        this.storage = storage;
        this.query = query;
        this.eventService = eventService;
        this.clientAppService = clientAppService;
        this.planService = planService;
        this.securityContext = securityContext;
        this.clientValidator = clientValidator;
    }

    public ContractService() {
    }

    public ContractBean createContract(String organizationId, String clientId, String version,
        NewContractBean bean) throws OrganizationNotFoundException, ClientNotFoundException,
        ApiNotFoundException, PlanNotFoundException, ContractAlreadyExistsException,
        NotAuthorizedException {

        try {
            ContractBean contract = createContractInternal(organizationId, clientId, version, bean);
            LOGGER.debug("Created new contract {0}: {1}", contract.getId(), contract); //$NON-NLS-1$
            return contract;
        } catch (AbstractRestException e) {
            throw e;
        } catch (Exception e) {
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
        ClientVersionBean cvb = clientAppService.getClientVersion(organizationId, clientId, version);

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

        boolean approvalRequired = false;

        if (!apiPlanBean.isRequiresApproval()
                 || securityContext.isAdmin()
                 || securityContext.hasPermission(planAdmin, organizationId)) {
            LOGGER.debug("Contract valid immediately ✅: {0}", contract);
            contract.setStatus(ContractStatus.Created);
        } else {
            LOGGER.debug("Contract requires approval ✋: {0}", contract);
            contract.setStatus(ContractStatus.AwaitingApproval);
            approvalRequired = true;
        }

        // Move the client to the "Ready" state if necessary.
        if (cvb.getStatus() == ClientStatus.Created && clientValidator.isReady(cvb, true)) {
            ClientStatus oldStatus = cvb.getStatus();
            if (approvalRequired) {
                cvb.setStatus(ClientStatus.AwaitingApproval);
                fireContractApprovalRequest(securityContext.getCurrentUser(), contract);
            } else {
                cvb.setStatus(ClientStatus.Ready);
            }
            clientAppService.fireClientStatusChangeEvent(cvb, oldStatus);
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

        List<ContractSummaryBean> contracts = clientAppService.getClientVersionContracts(organizationId, clientId, version);
        for (ContractSummaryBean contract : contracts) {
            deleteContract(organizationId, clientId, version, contract.getContractId());
        }
    }

    @Transactional
    public void deleteContract(String organizationId, String clientId, String version, Long contractId)
        throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException,
        InvalidClientStatusException {

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
            ClientVersionBean clientV = clientAppService.getClientVersion(organizationId, clientId, version);
            clientV.setModifiedBy(securityContext.getCurrentUser());
            clientV.setModifiedOn(new Date());
            storage.updateClientVersion(clientV);

            LOGGER.debug(String.format("Deleted contract: %s", contract)); //$NON-NLS-1$
        });
    }

    private void fireContractApprovalRequest(String requesterId, ContractBean contract) {
        UserDto requester = UserMapper.toDto(tryAction(() -> storage.getUser(requesterId)));

        ApimanEventHeaders headers = ApimanEventHeaders
             .builder()
             .setId(UUID.randomUUID().toString())
             .setSource(URI.create("/a/b/c"))
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
