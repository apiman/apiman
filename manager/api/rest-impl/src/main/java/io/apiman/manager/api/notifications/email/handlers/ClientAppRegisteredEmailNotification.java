package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.events.ClientVersionStatusEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.notifications.email.QteTemplateEngine;
import io.apiman.manager.api.notifications.email.SimpleMailNotificationService;
import io.apiman.manager.api.notifications.producers.ClientAppStatusNotificationProducer;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class ClientAppRegisteredEmailNotification implements INotificationHandler {

    private IStorage storage;
    private SimpleMailNotificationService mailNotificationService;
    private QteTemplateEngine templateEngine;

    @Inject
    public ClientAppRegisteredEmailNotification(QteTemplateEngine templateEngine,
         SimpleMailNotificationService mailNotificationService,
         IStorage storage
    ) {
        this.templateEngine = templateEngine;
        this.mailNotificationService = mailNotificationService;
        this.storage = storage;
    }

    public ClientAppRegisteredEmailNotification() {}

    @Override
    public void handle(NotificationDto<? extends IVersionedApimanEvent> raw) {
        NotificationDto<ClientVersionStatusEvent> notification = (NotificationDto<ClientVersionStatusEvent>) raw;

        Map<String, Object> templateMap = buildTemplateMap(notification);
        ClientVersionStatusEvent event = notification.getPayload();

        EmailNotificationTemplate template = mailNotificationService
             .findTemplateFor(notification.getReason())
             .or(() -> mailNotificationService.findTemplateFor(notification.getCategory()))
             .orElseThrow();

        String renderedBody = templateEngine.applyTemplate(template.getNotificationTemplateBody(), templateMap);
        String renderedSubject = templateEngine.applyTemplate(template.getNotificationTemplateSubject(), templateMap);

        UserDto recipient = notification.getRecipient();
        mailNotificationService.sendHtml(recipient.getEmail(), recipient.getFullName(), renderedSubject, renderedBody,
             "");
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

    public Map<String, Object> buildTemplateMap(NotificationDto<ClientVersionStatusEvent> notification) {
        return Map.of(
             "notification", notification,
             "event", notification.getPayload()
        );
    }
}
