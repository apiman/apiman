package io.apiman.manager.api.notifications.producers;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.events.ClientVersionStatusEvent;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.notifications.NotificationCategory;
import io.apiman.manager.api.beans.notifications.dto.CreateNotificationDto;
import io.apiman.manager.api.beans.notifications.dto.RecipientDto;
import io.apiman.manager.api.beans.notifications.dto.RecipientType;
import io.apiman.manager.api.notifications.INotificationProducer;
import io.apiman.manager.api.service.NotificationService;

import java.util.List;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Send notification when the client is registered (could be auto-registered after approval).
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ClientAppStatusNotificationProducer implements INotificationProducer {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ContractApprovalNotificationProducer.class);
    public static final String APIMAN_CLIENT_STATUS_CHANGE = "apiman.client.status_change";
    private NotificationService notificationService;

    @Inject
    public ClientAppStatusNotificationProducer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public ClientAppStatusNotificationProducer() {}

    public void processEvent(@Observes ClientVersionStatusEvent signupEvent) {
        // For now, we'll only send a message if there's a register?
        if (signupEvent.getNewStatus() == ClientStatus.Registered) {
            LOGGER.debug("Processing client app registered event {0}", signupEvent);
            CreateNotificationDto newNotification = new CreateNotificationDto();
            String clientOrg = signupEvent.getClientOrgId();

            RecipientDto clientAdmins = new RecipientDto()
                 .setRecipient(PermissionType.clientAdmin.name())
                 .setOrgId(clientOrg)
                 .setRecipientType(RecipientType.PERMISSION);

            newNotification.setRecipient(List.of(clientAdmins))
                           .setReason(APIMAN_CLIENT_STATUS_CHANGE)
                           .setReasonMessage("Client was published")
                           .setCategory(NotificationCategory.API_ADMINISTRATION)
                           .setSource("/resources/something/here/")
                           .setPayload(signupEvent);

            LOGGER.debug("Sending notification for client app registration: {0}", newNotification);
            notificationService.sendNotification(newNotification);
        }
    }
}
