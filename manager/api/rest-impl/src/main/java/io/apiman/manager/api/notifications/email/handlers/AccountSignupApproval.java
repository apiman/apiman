package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.AccountSignupEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.email.EmailSender;
import io.apiman.manager.api.notifications.email.QteTemplateEngine;
import io.apiman.manager.api.notifications.impl.NewAccountNotificationProducer;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class AccountSignupApproval implements INotificationHandler {

    private QteTemplateEngine templateEngine;
    private static final String temp_msg = "Dear {notification.recipient}, an account needs your approval.";

    @Inject
    public AccountSignupApproval(QteTemplateEngine templateEngine, EmailSender) {
        this.templateEngine = templateEngine;
    }

    public AccountSignupApproval() {}

    @Override
    public void handle(NotificationDto<? extends IVersionedApimanEvent> rawNotification) {
        @SuppressWarnings("unchecked")
        NotificationDto<AccountSignupEvent> signupNotification = (NotificationDto<AccountSignupEvent>) rawNotification;
        Map<String, Object> templateMap = buildTemplateMap(signupNotification);
        String rendered = templateEngine.applyTemplate(temp_msg, templateMap);

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
                "notification", notification
        );
    }
}
