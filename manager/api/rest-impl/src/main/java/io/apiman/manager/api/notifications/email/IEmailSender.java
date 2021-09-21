package io.apiman.manager.api.notifications.email;

import java.util.Map;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface IEmailSender {

    /**
     * Send a plaintext email.
     */
    void sendPlaintext(String toEmail, String toName, String subject, String body, Map<String, String> headers)
         throws EmailException;

    /**
     * Send an HTML email. Best practice is to send a plaintext body also.
     */
    void sendHtml(String toEmail, String toName, String subject, String htmlBody, String plainBody,
         Map<String, String> headers) throws EmailException;

    /**
     * Wrap in our own exception in case we change impls.
     */
    final class EmailException extends RuntimeException {

        public EmailException(Throwable cause) {
            super(cause);
        }
    }
}
