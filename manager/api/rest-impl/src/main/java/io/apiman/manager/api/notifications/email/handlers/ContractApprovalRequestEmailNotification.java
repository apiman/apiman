package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.ContractCreatedEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.email.SimpleEmail;
import io.apiman.manager.api.notifications.email.SimpleMailNotificationService;
import io.apiman.manager.api.notifications.producers.ContractApprovalRequestNotificationProducer;
import io.apiman.manager.api.providers.eager.EagerLoaded;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@EagerLoaded
@ApplicationScoped
public class ContractApprovalRequestEmailNotification implements INotificationHandler {

    private SimpleMailNotificationService mailNotificationService;

    @Inject
    public ContractApprovalRequestEmailNotification(SimpleMailNotificationService mailNotificationService) {
        this.mailNotificationService = mailNotificationService;
    }

    public ContractApprovalRequestEmailNotification() {
    }

    @Override
    public void handle(NotificationDto<? extends IVersionedApimanEvent> rawNotification) {
        @SuppressWarnings("unchecked")
        NotificationDto<ContractCreatedEvent> signupNotification = (NotificationDto<ContractCreatedEvent>) rawNotification;
        ContractCreatedEvent event = signupNotification.getPayload();
        if (event.isApprovalRequired()) {
            approvalRequiredNotification(signupNotification);
        }
    }

    private void approvalRequiredNotification(NotificationDto<ContractCreatedEvent> signupNotification) {
        UserDto recipient = signupNotification.getRecipient();
        EmailNotificationTemplate template = mailNotificationService
             .findTemplateFor(signupNotification.getReason(), recipient.getLocale())
             .or(() -> mailNotificationService.findTemplateFor(signupNotification.getCategory(), recipient.getLocale()))
             .orElseThrow();

        Map<String, Object> templateMap = buildTemplateMap(signupNotification);

        var mail = SimpleEmail
             .builder()
             .setRecipient(signupNotification.getRecipient()) // Or can set each field manually
             .setTemplate(template)
             .setTemplateVariables(templateMap)
             .build();

        mailNotificationService.send(mail);
    }

    @Override
    public boolean wants(NotificationDto<? extends IVersionedApimanEvent> notification) {
        return notification.getReason().equals(ContractApprovalRequestNotificationProducer.APIMAN_CLIENT_CONTRACT_REASON);
    }

    public Map<String, Object> buildTemplateMap(NotificationDto<ContractCreatedEvent> notification) {
        return Map.of(
             "notification", notification,
             "event", notification.getPayload()
        );
    }
}
