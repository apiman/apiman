package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.ContractCreatedEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.email.QteTemplateEngine;
import io.apiman.manager.api.notifications.email.SimpleMailNotificationService;
import io.apiman.manager.api.notifications.producers.ContractApprovalRequestNotificationProducer;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class ContractApprovalRequestEmailNotification implements INotificationHandler  {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ContractApprovalRequestEmailNotification.class);
    private final QteTemplateEngine templateEngine;
    private final SimpleMailNotificationService mailNotificationService;

    @Inject
    public ContractApprovalRequestEmailNotification(QteTemplateEngine templateEngine,
         SimpleMailNotificationService mailNotificationService
    ) {
        this.templateEngine = templateEngine;
        this.mailNotificationService = mailNotificationService;
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
        Map<String, Object> templateMap = buildTemplateMap(signupNotification);

        EmailNotificationTemplate template = mailNotificationService
             .findTemplateFor(signupNotification.getReason())
             .or(() -> mailNotificationService.findTemplateFor(signupNotification.getCategory()))
             .orElseThrow();

        String renderedBody = templateEngine.applyTemplate(template.getNotificationTemplateBody(), templateMap);
        String renderedSubject = templateEngine.applyTemplate(template.getNotificationTemplateSubject(), templateMap);

        UserDto recipient = signupNotification.getRecipient();
        mailNotificationService.sendHtml(recipient.getEmail(), recipient.getFullName(), renderedSubject, renderedBody, renderedSubject);
    }

    @Override
    public boolean wants(NotificationDto<? extends IVersionedApimanEvent> notification) {
        if (notification.getReason().equals(ContractApprovalRequestNotificationProducer.APIMAN_CLIENT_CONTRACT_REASON)) {
            return true;
        }
        return false;
    }

    public Map<String, Object> buildTemplateMap(NotificationDto<ContractCreatedEvent> notification) {
        return Map.of(
             "notification", notification,
             "event", notification.getPayload()
        );
    }
}
