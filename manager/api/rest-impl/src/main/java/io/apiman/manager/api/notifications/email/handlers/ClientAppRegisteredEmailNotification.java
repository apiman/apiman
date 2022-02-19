package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.events.ClientVersionStatusEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.email.SimpleEmail;
import io.apiman.manager.api.notifications.email.SimpleMailNotificationService;
import io.apiman.manager.api.notifications.producers.ClientAppStatusNotificationProducer;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class ClientAppRegisteredEmailNotification implements INotificationHandler<ClientVersionStatusEvent> {

    private final SimpleMailNotificationService mailNotificationService;

    @Inject
    public ClientAppRegisteredEmailNotification(SimpleMailNotificationService mailNotificationService) {
        this.mailNotificationService = mailNotificationService;
    }

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        // no-op to force eager initialization
    }

    @Override
    public void handle(NotificationDto<ClientVersionStatusEvent> notification, Map<String, Object> defaultTemplateMap) {
        UserDto recipient = notification.getRecipient();

        mailNotificationService
                .findTemplateFor(notification.getReason(), recipient.getLocale())
                .ifPresentOrElse(
                        template -> send(recipient, template, defaultTemplateMap),
                        () -> warnOnce(recipient, notification)
                );
    }

    void send(UserDto recipient, EmailNotificationTemplate template, Map<String, Object> defaultTemplateMap) {
        var mail = SimpleEmail
                .builder()
                .setRecipient(recipient)
                .setTemplate(template)
                .setTemplateVariables(defaultTemplateMap)
                .build();

        mailNotificationService.send(mail);
    }

    /**
     * If a status change event && has transitioned into 'registered' state.
     */
    @Override
    public boolean wants(NotificationDto<? extends IVersionedApimanEvent> raw) {
        if (raw.getReason().equals(ClientAppStatusNotificationProducer.APIMAN_CLIENT_STATUS_CHANGE)) {
            // Get the event payload
            NotificationDto<ClientVersionStatusEvent> notification = (NotificationDto<ClientVersionStatusEvent>) raw.getPayload();
            ClientVersionStatusEvent event = notification.getPayload();
            // We only want to send a notification if the previous state was unregistered, and new state is registered (for now).
            return (event.getNewStatus() == ClientStatus.Registered && event.getPreviousStatus() == ClientStatus.AwaitingApproval);
        }
        return false;
    }

}
