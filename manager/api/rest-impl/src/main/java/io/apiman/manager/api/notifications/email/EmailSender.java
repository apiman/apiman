package io.apiman.manager.api.notifications.email;

import io.apiman.manager.api.beans.notifications.dto.NotificationDto;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class EmailSender {

    private final SmtpEmailConfiguration emailConfiguration;

    public EmailSender(SmtpEmailConfiguration emailConfiguration) {
        this.emailConfiguration = emailConfiguration;
    }

    public void sendEmail(String bodyTpl, String subjectTpl, NotificationDto<?> notification) {

    }
}
