package io.apiman.manager.api.notifications.producers;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.ContractApprovalRequestEvent;
import io.apiman.manager.api.beans.notifications.NotificationCategory;
import io.apiman.manager.api.beans.notifications.dto.CreateNotificationDto;
import io.apiman.manager.api.beans.notifications.dto.RecipientDto;
import io.apiman.manager.api.beans.notifications.dto.RecipientType;
import io.apiman.manager.api.notifications.INotificationProducer;
import io.apiman.manager.api.service.NotificationService;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Accept a {@link ContractApprovalRequestEvent} and produce a {@link #APIMAN_API_APPROVAL_REQUEST} notification.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class ContractApprovalRequestNotificationProducer implements INotificationProducer {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ContractApprovalRequestNotificationProducer.class);
    public static final String APIMAN_API_APPROVAL_REQUEST = "apiman.api.approval.request";
    private NotificationService notificationService;

    @Inject
    public ContractApprovalRequestNotificationProducer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public ContractApprovalRequestNotificationProducer() {}

    public void processEvent(@Observes ContractApprovalRequestEvent signupEvent) {
        LOGGER.debug("Processing signup event {0}", signupEvent);
        if (signupEvent.isApprovalRequired()) {
            CreateNotificationDto newNotification = new CreateNotificationDto();
            String orgId = signupEvent.getOrgId();

            RecipientDto planAdmins = new RecipientDto()
                 .setRecipient("planAdmin")
                 .setOrgId(orgId)
                 .setRecipientType(RecipientType.PERMISSION);

            newNotification.setRecipient(List.of(planAdmins))
                           .setReason(APIMAN_API_APPROVAL_REQUEST)
                           .setReasonMessage("Signup request for API")
                           .setCategory(NotificationCategory.API_ADMINISTRATION)
                           .setPayload(signupEvent);

            LOGGER.debug("Sending notification for approval of contract {0} from client {1} version {2}",
                 signupEvent.getContractId(), signupEvent.getClientId(), signupEvent.getClientVersion());

            notificationService.sendNotification(newNotification);
        }
    }
}
