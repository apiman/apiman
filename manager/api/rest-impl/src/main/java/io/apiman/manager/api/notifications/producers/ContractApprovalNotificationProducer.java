package io.apiman.manager.api.notifications.producers;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.ContractApprovalEvent;
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
 * When a contract has been approved, send notification(s).
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@EagerLoaded
@ApplicationScoped
public class ContractApprovalNotificationProducer implements INotificationProducer {

    private final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ContractApprovalNotificationProducer.class);
    public static final String APIMAN_CONTRACT_APPROVED_REASON = "apiman.client.contract.approval.granted";
    public static final String APIMAN_CONTRACT_REJECTED_REASON = "apiman.client.contract.approval.rejected";

    private NotificationService notificationService;

    @Inject
    public ContractApprovalNotificationProducer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public ContractApprovalNotificationProducer() {}

    public void processEvent(@Observes ContractApprovalEvent approvalEvent) {
        LOGGER.debug("Processing contract approval event: {0}", approvalEvent);
        String orgId = approvalEvent.getApiOrgId();

        RecipientDto planAdmins = new RecipientDto()
             .setRecipient(PermissionType.clientAdmin.name())
             .setOrgId(approvalEvent.getClientOrgId())
             .setRecipientType(RecipientType.PERMISSION);

        CreateNotificationDto newNotification = new CreateNotificationDto()
             .setRecipient(List.of(planAdmins))
             .setCategory(NotificationCategory.API_ADMINISTRATION)
             .setSource("http://somepage/here/")
             .setPayload(approvalEvent);

        if (approvalEvent.isApproved()) {
            newNotification.setReason(APIMAN_CONTRACT_APPROVED_REASON);
            newNotification.setReasonMessage("Signup was approved!");
        } else {
            newNotification.setReason(APIMAN_CONTRACT_REJECTED_REASON);
            newNotification.setReasonMessage("Signup was rejected: " + approvalEvent.getRejectionReason());
        }

        notificationService.sendNotification(newNotification);
    }
}
