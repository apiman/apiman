package io.apiman.manager.api.notifications.email;

import io.apiman.manager.api.notifications.email.SmtpEmailConfiguration.StartTLSEnum;

import java.util.Map;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class EmailSender {

    private final SmtpEmailConfiguration emailConfiguration;

    public EmailSender(SmtpEmailConfiguration emailConfiguration) {
        this.emailConfiguration = emailConfiguration;
    }

    public void sendPlaintext(String toEmail, String toName, String subject, String body, Map<String, String> headers)
         throws EmailException {
        try {
            SimpleEmail email = new SimpleEmail();
            setCommonConfigElements(email, toName, toEmail, subject, headers);
            email.setMsg(body);
            email.send();
        } catch (org.apache.commons.mail.EmailException e) {
            throw new EmailException(e);
        }
    }

    public void sendHtml(String toEmail, String toName, String subject, String htmlBody, String plainBody, Map<String, String> headers)
         throws EmailException {
        try {
            HtmlEmail email = new HtmlEmail();
            setCommonConfigElements(email, toName, toEmail, subject, headers);
            email.setHtmlMsg(htmlBody);
            email.setTextMsg(plainBody);
            email.send();
        } catch (org.apache.commons.mail.EmailException e) {
            throw new EmailException(e);
        }
    }

    private void setCommonConfigElements(Email email, String toName, String toEmail, String subject, Map<String, String> headers)
         throws org.apache.commons.mail.EmailException {
        email.addTo(toEmail, toName);
        email.setFrom(emailConfiguration.getFrom());
        email.setSubject(subject);
        email.setHostName(emailConfiguration.getHost());
        email.setSmtpPort(emailConfiguration.getPort());
        email.setAuthenticator(new DefaultAuthenticator(emailConfiguration.getUsername(), emailConfiguration.getPassword()));
        email.getHeaders().putAll(headers);

        if (emailConfiguration.getStartTLSMode() == StartTLSEnum.REQUIRED) {
            email.setStartTLSRequired(true);
        } else if (emailConfiguration.getStartTLSMode() == StartTLSEnum.DISABLED) {
            email.setStartTLSRequired(false);
        }
        if (emailConfiguration.isSsl()) {
            email.setSslSmtpPort(Integer.toString(emailConfiguration.getPort()));
            email.setSSLOnConnect(true);
        } else {
            email.setSSLOnConnect(false);
        }
    }

    // Wrap in our own exception in case we change impls
    public static final class EmailException extends RuntimeException {
        public EmailException(Throwable cause) {
            super(cause);
        }
    }
}
