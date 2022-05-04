package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.ContractCreatedEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.email.SimpleEmail;
import io.apiman.manager.api.notifications.email.SimpleMailNotificationService;
import io.apiman.manager.api.notifications.producers.ContractApprovalRequestNotificationProducer;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class ContractApprovalRequestEmailNotification implements INotificationHandler<ContractCreatedEvent> {

    private final SimpleMailNotificationService mailNotificationService;

    @Inject
    public ContractApprovalRequestEmailNotification(SimpleMailNotificationService mailNotificationService) {
        this.mailNotificationService = mailNotificationService;
    }

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        // no-op to force eager initialization
    }

    @Override
    public void handle(NotificationDto<ContractCreatedEvent> notification, Map<String, Object> defaultTemplateMap) {
        ContractCreatedEvent event = notification.getPayload();

        if (event.isApprovalRequired()) {
            UserDto recipient = notification.getRecipient();

            mailNotificationService
                    .findTemplateFor(notification.getReason(), recipient.getLocale())
                    .ifPresentOrElse(
                            template -> send(recipient, template, defaultTemplateMap),
                            () -> warnOnce(recipient, notification)
                    );
        }
    }

    private void send(UserDto recipient, EmailNotificationTemplate template, Map<String, Object> defaultTemplateMap) {
        var mail = SimpleEmail
             .builder()
             .setRecipient(recipient)
             .setTemplate(template)
             .setTemplateVariables(defaultTemplateMap)
             .build();

        mailNotificationService.send(mail);
    }

    @Override
    public boolean wants(NotificationDto<? extends IVersionedApimanEvent> notification) {
        return notification.getReason().equals(ContractApprovalRequestNotificationProducer.APIMAN_CLIENT_CONTRACT_REASON);
    }
}
