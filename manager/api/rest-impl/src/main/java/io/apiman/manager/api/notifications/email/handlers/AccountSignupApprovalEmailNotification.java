package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.AccountSignupEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.email.SimpleEmail;
import io.apiman.manager.api.notifications.email.SimpleMailNotificationService;
import io.apiman.manager.api.notifications.producers.NewAccountNotificationProducer;
import io.apiman.manager.api.providers.eager.EagerLoaded;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@EagerLoaded
@ApplicationScoped
public class AccountSignupApprovalEmailNotification implements INotificationHandler {

    private SimpleMailNotificationService mailNotificationService;

    @Inject
    public AccountSignupApprovalEmailNotification(SimpleMailNotificationService mailNotificationService) {
        this.mailNotificationService = mailNotificationService;
    }

    public AccountSignupApprovalEmailNotification() {
    }

    @Override
    public void handle(NotificationDto<? extends IVersionedApimanEvent> rawNotification) {
        @SuppressWarnings("unchecked")
        NotificationDto<AccountSignupEvent> signupNotification = (NotificationDto<AccountSignupEvent>) rawNotification;
        Map<String, Object> templateMap = buildTemplateMap(signupNotification);

        // Beware, for this instance, the user might not actually exist in Apiman (yet or at all) as it could have come
        // from the underlying IDM -- be careful if calling for Apiman's members, etc.
        UserDto recipient = rawNotification.getRecipient();

        EmailNotificationTemplate template = mailNotificationService
             .findTemplateFor(signupNotification.getReason(), recipient.getLocale())
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
        return notification.getReason().equals(NewAccountNotificationProducer.APIMAN_ACCOUNT_APPROVAL_REQUEST);
    }

    public Map<String, Object> buildTemplateMap(NotificationDto<AccountSignupEvent> notification) {
        return Map.of(
             "notification", notification,
             "event", notification.getPayload()
        );
    }
}
