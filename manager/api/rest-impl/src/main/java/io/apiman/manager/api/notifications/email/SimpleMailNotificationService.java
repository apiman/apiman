package io.apiman.manager.api.notifications.email;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.JsonUtil;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.NotificationCategory;
import io.apiman.manager.api.core.config.ApiManagerConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.jetbrains.annotations.NotNull;

/**
 * Send notification emails as configured via file in {@link ApiManagerConfig#getNotificationProperties()}.
 * @see SmtpEmailConfiguration
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class SimpleMailNotificationService {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(SimpleMailNotificationService.class);
    private EmailSender emailSender;
    private ApiManagerConfig config;
    private PatriciaTrie<EmailNotificationTemplate> reasonTrie = new PatriciaTrie<>();
    private MultiValuedMap<NotificationCategory, EmailNotificationTemplate> categoryToTemplateMap = new ArrayListValuedHashMap();

    @Inject
    public SimpleMailNotificationService(ApiManagerConfig config) {
        this.config = config;
        var smtpConfig = new SmtpEmailConfiguration(config.getNotificationProperties());
        this.emailSender = new EmailSender(smtpConfig);
        readEmailNotificationTemplatesFromFile();
    }

    public SimpleMailNotificationService() {}

    /**
     * Send a plaintext email
     */
    public void sendPlaintext(@NotNull String to, @NotNull String subject, @NotNull String body) {
        emailSender.sendPlaintext(to, subject, body, Collections.emptyMap());
    }

    /**
     * Send a plaintext email
     */
    public void sendPlaintext(@NotNull  String to, @NotNull  String subject, @NotNull  String body, @NotNull Map<String, String> headers) {
        LOGGER.debug("Sending plaintext email. To: {0}, Subject: {1}, Body: {2}, Extra headers: {3}.",
             to, subject, body, headers);
        LOGGER.debug("Sender settings: {0}", emailSender);
        emailSender.sendPlaintext(to, subject, body, headers);
    }

    /**
     * Send an HTML email with a plaintext fallback/alternative.
     */
    public void sendHtml(@NotNull String to, @NotNull String subject, @NotNull String htmlBody, @NotNull String plainBody) {
        emailSender.sendHtml(to, subject, htmlBody, plainBody, Collections.emptyMap());
    }

    /**
     * Send an HTML email with a plaintext fallback/alternative.
     */
    public void sendHtml(@NotNull String to, @NotNull String subject, @NotNull String htmlBody, @NotNull String plainBody, @NotNull Map<String, String> headers) {
        LOGGER.debug("Sending HTML email. To: {0}, Subject: {1}, HTML body: {2}, Plaintext Body: {3}, "
                          + "Extra headers: {4}.", to, subject, htmlBody, plainBody, headers);
        LOGGER.debug("Sender settings: {0}", emailSender);
        emailSender.sendHtml(to, subject, htmlBody, plainBody, headers);
    }

    /**
     * Find all valid email templates for a provided reason.
     */
    public SortedMap<String, EmailNotificationTemplate> findAllTemplatesFor(@NotNull String reasonKey) {
        return reasonTrie.headMap(reasonKey + "*");
    }

    /**
     * Find an email template for a provided reason (longest match wins).
     *
     * <p>For example, if we have a template for both <code>a.b.c.d</code> and <code>a.b.c</code>, the former will be
     * returned.
     *
     * <p>Correspondingly, if an exact template match is not found, the prefix will be shortened until a matching
     * template is found, or we determine there is no suitable template. In the latter case, use of
     * {@link #findTemplateFor(NotificationCategory)} may provide a suitable alternative (or a very generic template).
     *
     * @param reasonKey the reason to find a template for
     * @return template, if a suitable one is found, otherwise empty
     */
    public Optional<EmailNotificationTemplate> findTemplateFor(@NotNull String reasonKey) {
        Entry<String, EmailNotificationTemplate> selected = reasonTrie.select(reasonKey);
        if (selected == null || selected.getValue() == null) {
            LOGGER.debug("Found template for reason {0} as {1}, shorter matching reasons templates "
                              + "may also exist", selected.getKey(), reasonKey);
            return Optional.empty();
        } else {
            LOGGER.debug("No email template found for reason {0}, including shorter paths", reasonKey);
            return Optional.of(selected.getValue());
        }

        // String[] splitReason = reason.split("\\.");
        // Deque<String> stack = new ArrayDeque<>(splitReason.length);
        // stack.addAll(Arrays.asList(splitReason));
        // // Start full path, try to find match, shorten with #pop, etc. Could be done more efficiently but this is fine for now.
        // while (!stack.isEmpty()) {
        //     String candidate = String.join(".", stack);
        //     LOGGER.trace("Searching for matching template for reason {0}. Trying substring candidate {1}",
        //          reason, candidate);
        //
        //     stack.pop();
        // }
        // return Optional.empty();
    }

    /**
     * Get a template for a reason category (generally more coarse-grained).
     *
     * @param category the category to get a template for
     * @return template, if a suitable one is found, otherwise empty
     */
    public Optional<EmailNotificationTemplate> findTemplateFor(@NotNull NotificationCategory category) {
        return Optional.empty();
    }

    private void readEmailNotificationTemplatesFromFile() {
        Path file = config.getConfigDirectory().resolve("email-notification-templates.json");
        if (Files.notExists(file)) {
            LOGGER.warn("No email notification templates found at {0}", file);
            return;
        }
        try {
            List<EmailNotificationTemplate> tpls = JsonUtil.toPojo(Files.readString(file),
                 EmailNotificationTemplate.class, List.class);
            for (EmailNotificationTemplate tpl : tpls) {
                reasonTrie.put(tpl.getNotificationReason(), tpl);
                LOGGER.trace("Adding template: reason {0} -> {1}", tpl.getNotificationReason(), tpl);
                LOGGER.trace("Adding template: category {0} -> {1}", tpl.getNotificationCategory(), tpl);
                categoryToTemplateMap.put(tpl.getNotificationCategory(), tpl);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
}
