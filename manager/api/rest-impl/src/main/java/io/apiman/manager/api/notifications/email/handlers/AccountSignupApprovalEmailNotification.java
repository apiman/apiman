package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.AccountSignupEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.email.SimpleEmail;
import io.apiman.manager.api.notifications.email.SimpleMailNotificationService;
import io.apiman.manager.api.notifications.producers.NewAccountNotificationProducer;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class AccountSignupApprovalEmailNotification implements INotificationHandler<AccountSignupEvent> {

    private final SimpleMailNotificationService mailNotificationService;

    @Inject
    public AccountSignupApprovalEmailNotification(SimpleMailNotificationService mailNotificationService) {
        this.mailNotificationService = mailNotificationService;
    }

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        // no-op to force eager initialization
    }

    @Override
    public void handle(NotificationDto<AccountSignupEvent> signupNotification, Map<String, Object> defaultTemplateMap) {
        // Beware, for this instance, the user might not actually exist in Apiman (yet or at all) as it could have come
        // from the underlying IDM -- be careful if calling for Apiman's members, etc.
        UserDto recipient = signupNotification.getRecipient();

        mailNotificationService
             .findTemplateFor(signupNotification.getReason(), recipient.getLocale())
             .ifPresentOrElse(
                     template -> send(recipient, template, defaultTemplateMap),
                     () -> warnOnce(recipient, signupNotification)
             );
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
        return notification.getReason().equals(NewAccountNotificationProducer.APIMAN_ACCOUNT_APPROVAL_REQUEST);
    }
}
