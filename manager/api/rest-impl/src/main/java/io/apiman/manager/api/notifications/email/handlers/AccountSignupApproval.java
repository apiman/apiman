package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.AccountSignupEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.notifications.email.QteTemplateEngine;
import io.apiman.manager.api.notifications.email.SimpleMailNotificationService;
import io.apiman.manager.api.notifications.producers.NewAccountNotificationProducer;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class AccountSignupApproval implements INotificationHandler {

    private static final EmailNotificationTemplate DEFAULT_TEMPLATE = new EmailNotificationTemplate(); // TODO make a sensible default?
    private QteTemplateEngine templateEngine;
    private SimpleMailNotificationService mailNotificationService;
    private IStorage storage;

    @Inject
    public AccountSignupApproval(QteTemplateEngine templateEngine,
         SimpleMailNotificationService mailNotificationService,
         IStorage storage
    ) {
        this.templateEngine = templateEngine;
        this.mailNotificationService = mailNotificationService;
        this.storage = storage;
    }

    public AccountSignupApproval() {}

    @Override
    public void handle(NotificationDto<? extends IVersionedApimanEvent> rawNotification) {
        @SuppressWarnings("unchecked")
        NotificationDto<AccountSignupEvent> signupNotification = (NotificationDto<AccountSignupEvent>) rawNotification;
        Map<String, Object> templateMap = buildTemplateMap(signupNotification);

        EmailNotificationTemplate template = mailNotificationService
             .findTemplateFor(signupNotification.getReason())
             .or(() -> mailNotificationService.findTemplateFor(signupNotification.getCategory()))
             .orElse(DEFAULT_TEMPLATE);

        String renderedBody = templateEngine.applyTemplate(template.getNotificationTemplateBody(), templateMap);
        String renderedSubject = templateEngine.applyTemplate(template.getNotificationTemplateSubject(), templateMap);

        // Beware, for this instance, the user might not actually exist in Apiman (yet or at all) as it could have come
        // from the underlying IDM -- be careful if calling for Apiman's members, etc.
        UserDto recipient = rawNotification.getRecipient();
        mailNotificationService.sendHtml(recipient.getFullName(), renderedSubject, renderedBody, renderedSubject);
    }

    @Override
    public boolean wants(NotificationDto<? extends IVersionedApimanEvent> notification) {
        if (notification.getReason().equals(NewAccountNotificationProducer.APIMAN_ACCOUNT_APPROVAL_REQUEST)) {
            return true;
        }
        return false;
    }

    public Map<String, Object> buildTemplateMap(NotificationDto<AccountSignupEvent> notification) {
        return Map.of(
                "notification", notification,
                "event", notification.getPayload()
        );
    }
}
