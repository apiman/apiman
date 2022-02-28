package io.apiman.manager.api.notifications.email;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;

import java.util.Map;

/**
 * A mock email sender, just prints the message to the logger and drops it.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class MockEmailSender implements IEmailSender {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(MockEmailSender.class);

    @Override
    public void sendPlaintext(String toEmail, String toName, String subject, String body, Map<String, String> headers) throws EmailException {
        LOGGER.debug("Mock plaintext email (nothing will be sent). \n"
                          + "to: {0} \n"
                          + "toName: {1} \n"
                          + "subject: {2} \n"
                          + "body: {3} \n"
                          + "headers: {4}",
             toEmail, toName, subject, body, headers);
    }

    @Override
    public void sendHtml(String toEmail, String toName, String subject, String htmlBody, String plainBody, Map<String, String> headers) throws EmailException {
        LOGGER.debug("Mock HTML email (nothing will be sent). \n"
                          + "to: {0} \n"
                          + "toName: {1} \n"
                          + "subject: {2} \n"
                          + "HTML body: {3} \n"
                          + "plain body: {4} \n"
                          + "headers: {5}",
             toEmail, toName, subject, htmlBody, plainBody, headers);
    }
}
