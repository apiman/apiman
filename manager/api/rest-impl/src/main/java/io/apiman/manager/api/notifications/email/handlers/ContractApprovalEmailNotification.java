package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.ContractApprovalEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.email.SimpleEmail;
import io.apiman.manager.api.notifications.email.SimpleMailNotificationService;
import io.apiman.manager.api.notifications.producers.ContractApprovalNotificationProducer;
import io.apiman.manager.api.providers.eager.EagerLoaded;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
//@EagerLoaded
@ApplicationScoped
public class ContractApprovalEmailNotification implements INotificationHandler {

    private final SimpleMailNotificationService mailNotificationService;

    @Inject
    public ContractApprovalEmailNotification(SimpleMailNotificationService mailNotificationService) {
        this.mailNotificationService = mailNotificationService;
    }

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        // no-op to force eager initialization
    }

    @Override
    public void handle(@Observes NotificationDto<?> notif) {
        NotificationDto<ContractApprovalEvent> notification = (NotificationDto<ContractApprovalEvent>) notif;
        Map<String, Object> templateMap = buildTemplateMap(notification);
        UserDto recipient = notification.getRecipient();

        EmailNotificationTemplate template = mailNotificationService
             .findTemplateFor(notification.getReason(), recipient.getLocale())
             .orElseThrow();

        var mail = SimpleEmail
             .builder()
             .setRecipient(recipient)
             .setTemplate(template)
             .setTemplateVariables(templateMap)
             .build();
        mailNotificationService.send(mail);
    }

    @Override
    public boolean wants(NotificationDto<? extends IVersionedApimanEvent> notification) {
        String reason = notification.getReason();
        return reason.equals(ContractApprovalNotificationProducer.APIMAN_CONTRACT_APPROVED_REASON)
                    || reason.equals(ContractApprovalNotificationProducer.APIMAN_CONTRACT_REJECTED_REASON);
    }

    public Map<String, Object> buildTemplateMap(NotificationDto<ContractApprovalEvent> notification) {
        return Map.of(
             "notification", notification,
             "event", notification.getPayload()
        );
    }
}
