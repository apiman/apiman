package io.apiman.manager.api.notifications.email;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.notifications.email.SmtpEmailConfiguration.StartTLSEnum;

import java.util.Map;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class EmailSender implements IEmailSender {

    private final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EmailSender.class);
    private final SmtpEmailConfiguration emailConfiguration;

    public EmailSender(SmtpEmailConfiguration emailConfiguration) {
        this.emailConfiguration = emailConfiguration;
        LOGGER.debug("EmailSender config: {0}", emailConfiguration);
    }

    @Override
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
        LOGGER.debug("Plaintext-only email notification sent. \n"
                         + "to: {0} \n"
                         + "toName: {1} \n"
                         + "subject: {2} \n"
                         + "body: {3} \n"
                         + "headers: {4}",
             toEmail, toName, subject, body, headers);
    }

    @Override
    public void sendHtml(String toEmail, String toName, String subject, String htmlBody, String plainBody,
         Map<String, String> headers) throws EmailException {
        try {
            HtmlEmail email = new HtmlEmail();
            setCommonConfigElements(email, toName, toEmail, subject, headers);
            email.setHtmlMsg(htmlBody);
            email.setTextMsg(plainBody);
            email.send();
        } catch (org.apache.commons.mail.EmailException e) {
            throw new EmailException(e);
        }
        LOGGER.debug("HTML email notification sent. \n"
                         + "to: {0} \n"
                         + "toName: {1} \n"
                         + "subject: {2} \n"
                         + "HTML body: {3} \n"
                         + "plain body: {4} \n"
                         + "headers: {5}",
             toEmail, toName, subject, htmlBody, plainBody, headers);
    }

    private void setCommonConfigElements(Email email, String toName, String toEmail, String subject, Map<String, String> headers)
         throws org.apache.commons.mail.EmailException {
        email.setCharset(EmailConstants.UTF_8);
        email.addTo(toEmail, toName);
        email.setFrom(emailConfiguration.getFromEmail(), emailConfiguration.getFromName());
        email.setSubject(subject);
        email.setHostName(emailConfiguration.getHost());
        email.setSmtpPort(emailConfiguration.getPort());
        email.setAuthenticator(new DefaultAuthenticator(emailConfiguration.getUsername(), emailConfiguration.getPassword()));
        email.getHeaders().putAll(headers);

        if (emailConfiguration.getStartTLSMode() == StartTLSEnum.REQUIRED || emailConfiguration.getStartTLSMode() == StartTLSEnum.OPTIONAL) {
            email.setStartTLSEnabled(true);
            if (emailConfiguration.getStartTLSMode() == StartTLSEnum.REQUIRED) {
                // https://issues.apache.org/jira/browse/EMAIL-105
                // mail.smtp.starttls.required in addition to mail.smtp.starttls.enable, to make the client disconnect if the server doesn't support STARTTLS.
                email.setStartTLSRequired(true);
            }
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
}
