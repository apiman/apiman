package io.apiman.manager.api.notifications.producers;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.ContractCreatedEvent;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.notifications.NotificationCategory;
import io.apiman.manager.api.beans.notifications.dto.CreateNotificationDto;
import io.apiman.manager.api.beans.notifications.dto.RecipientDto;
import io.apiman.manager.api.beans.notifications.dto.RecipientType;
import io.apiman.manager.api.notifications.INotificationProducer;
import io.apiman.manager.api.providers.eager.EagerLoaded;
import io.apiman.manager.api.service.NotificationService;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Accept a {@link ContractCreatedEvent} and produce a {@link #APIMAN_CLIENT_CONTRACT_REASON} notification.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@EagerLoaded
@ApplicationScoped
public class ContractApprovalRequestNotificationProducer implements INotificationProducer {

    private final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ContractApprovalRequestNotificationProducer.class);
    public static final String APIMAN_CLIENT_CONTRACT_REASON = "apiman.client.contract.approval.request";
    private NotificationService notificationService;

    @Inject
    public ContractApprovalRequestNotificationProducer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public ContractApprovalRequestNotificationProducer() {}

    public void processEvent(@Observes ContractCreatedEvent signupEvent) {
        LOGGER.debug("Processing signup event {0}", signupEvent);
        if (signupEvent.isApprovalRequired()) {
            CreateNotificationDto newNotification = new CreateNotificationDto();
            String orgId = signupEvent.getApiOrgId();

            RecipientDto planAdmins = new RecipientDto()
                 .setRecipient(PermissionType.planAdmin.name())
                 .setOrgId(orgId)
                 .setRecipientType(RecipientType.PERMISSION);

            newNotification.setRecipient(List.of(planAdmins))
                           .setReason(APIMAN_CLIENT_CONTRACT_REASON)
                           .setReasonMessage("Signup request for API")
                           .setCategory(NotificationCategory.API_ADMINISTRATION)
                           .setSource("/apiman/notifications/contracts/approvals")
                           .setPayload(signupEvent);

            LOGGER.debug("Sending notification for approval of contract {0} from client {1} version {2}",
                 signupEvent.getContractId(), signupEvent.getClientId(), signupEvent.getClientVersion());

            notificationService.sendNotification(newNotification);
        }
    }
}
