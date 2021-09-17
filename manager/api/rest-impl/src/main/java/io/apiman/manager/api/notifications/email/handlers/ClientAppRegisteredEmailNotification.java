package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.ClientVersionStatusEvent;
import io.apiman.manager.api.beans.events.ContractApprovalEvent;
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

    @Override
    public boolean wants(NotificationDto<? extends IVersionedApimanEvent> notification) {
        return notification.getReason().equals(ClientAppStatusNotificationProducer.APIMAN_CLIENT_STATUS_CHANGE);
    }

    public Map<String, Object> buildTemplateMap(NotificationDto<ContractApprovalEvent> notification) {
        return Map.of(
             "notification", notification,
             "event", notification.getPayload()
        );
    }
}
