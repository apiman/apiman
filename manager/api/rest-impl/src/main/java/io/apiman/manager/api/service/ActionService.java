package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.ContractApprovalRequestEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.idm.UserMapper;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.events.EventService;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.inject.Inject;
import javax.transaction.Transactional;

import static io.apiman.manager.api.notifications.producers.ContractApprovalRequestNotificationProducer.APIMAN_CLIENT_CONTRACT_REASON;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Transactional
public class ActionService implements DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ActionService.class);
    private IStorage storage;
    private EventService eventService;

    @Inject
    public ActionService(IStorage storage, EventService eventService) {
        this.storage = storage;
        this.eventService = eventService;
    }

    public ActionService() {}

    public void sendContractApprovalRequest(String requestorId, String clientId, String apiId, Long contractId) {
        UserDto requester = UserMapper.toDto(tryAction(() -> storage.getUser(requestorId)));

        ApimanEventHeaders headers = ApimanEventHeaders
             .builder()
             .setId(UUID.randomUUID().toString())
             .setSource(URI.create("/a/b/c"))
             .setSubject(APIMAN_CLIENT_CONTRACT_REASON)
             .setTime(OffsetDateTime.now())
             .build();

        var approvalRequestEvent = ContractApprovalRequestEvent
             .builder()
             .setHeaders(headers)
             .setUser(requester)
             .setClientId(clientId)
             .setApiId(apiId)
             .setContractId(Long.toString(contractId))
             .setApprovalRequired(true)
             .build();
        LOGGER.debug("Sending approval request event {0}", approvalRequestEvent);
        eventService.fireEvent(approvalRequestEvent);
    }
}
