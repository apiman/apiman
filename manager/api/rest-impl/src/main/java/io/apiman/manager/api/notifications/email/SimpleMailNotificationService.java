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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.jetbrains.annotations.NotNull;

/**
 * Simple email related actions, with a focus on notifications.
 *
 * <ul>
 *     <li>Send notification emails as configured via file in {@link ApiManagerConfig#getNotificationProperties()}.</li>
 *     <li>Get email notification templates (from a static file only, at present) by reason (or prefix thereof) or
 *     category.</li>
 * </ul>
 *
 * @see SmtpEmailConfiguration
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class SimpleMailNotificationService {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(SimpleMailNotificationService.class);
    private static final Map DEFAULT_HEADERS = Map.of("X-Notification-Producer", "Apiman");

    private EmailSender emailSender;
    private ApiManagerConfig config;

    private PatriciaTrie<EmailNotificationTemplate> reasonTrie = new PatriciaTrie<>();
    private ArrayListValuedHashMap<NotificationCategory, EmailNotificationTemplate> categoryToTemplateMap = new ArrayListValuedHashMap();

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
    public void sendPlaintext(@NotNull String toEmail, @NotNull String toName, @NotNull String subject, @NotNull String body) {
        emailSender.sendPlaintext(toEmail, toName, subject, body, DEFAULT_HEADERS);
    }

    /**
     * Send a plaintext email
     */
    public void sendPlaintext(@NotNull String toEmail, @NotNull String toName, @NotNull  String subject, @NotNull  String body, @NotNull Map<String, String> headers) {
        LOGGER.debug("Sending plaintext email. To: {0}, Subject: {1}, Body: {2}, Extra headers: {3}.",
             toEmail, subject, body, headers);
        LOGGER.debug("Sender settings: {0}", emailSender);
        emailSender.sendPlaintext(toEmail, toName, subject, body, headers);
    }

    /**
     * Send an HTML email with a plaintext fallback/alternative.
     */
    public void sendHtml(@NotNull String toEmail, @NotNull String toName, @NotNull String subject, @NotNull String htmlBody, @NotNull String plainBody) {
        emailSender.sendHtml(toEmail, toName, subject, htmlBody, plainBody, DEFAULT_HEADERS);
    }

    /**
     * Send an HTML email with a plaintext fallback/alternative.
     */
    public void sendHtml(@NotNull String toEmail, @NotNull String toName, @NotNull String subject, @NotNull String htmlBody, @NotNull String plainBody, @NotNull Map<String, String> headers) {
        LOGGER.debug("Sending HTML email. To: {0}, Subject: {1}, HTML body: {2}, Plaintext Body: {3}, "
                          + "Extra headers: {4}.", toEmail, subject, htmlBody, plainBody, headers);
        LOGGER.debug("Sender settings: {0}", emailSender);
        emailSender.sendHtml(toEmail, toName, subject, htmlBody, plainBody, headers);
    }

    /**
     * Find an email template for a provided reason (longest prefix match wins).
     *
     * <p>For example, if we have a template for both <code>a.b.c.d</code> and <code>a.b.c</code>, the former will be
     * returned (it is the longest match).
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
            LOGGER.debug("No email template found for reason {0}, including shorter paths", reasonKey);
            return Optional.empty();
        } else {
            LOGGER.debug("Found template for reason {0} as {1}, shorter matching reasons templates "
                              + "may also exist", selected.getKey(), reasonKey);
            return Optional.of(selected.getValue());
        }
    }

    /**
     * Find all valid email templates for a provided reason. This is a prefix search, so any smaller or equal matching
     * prefix will be returned.
     *
     * <p>For example, if we have template mappings <code>a.b.c.d=tpl1</code> and <code>a.b.c=tpl2</code>, both
     * templates will be returned with a reasonKey of <code>a.b.c.d</code> (common prefix).
     *
     * @return Sorted map of prefix and corresponding template. Empty if no matches are found.
     */
    public SortedMap<String, EmailNotificationTemplate> findAllTemplatesFor(@NotNull String reasonKey) {
        return reasonTrie.headMap(reasonKey + "*"); // Add a character, as headMap only matches prefixes shorter
    }

    /**
     * Get first template for a reason category (generally more coarse-grained).
     *
     * @param category the category to get a template for
     * @return template, if a suitable one is found, otherwise empty
     */
    public Optional<EmailNotificationTemplate> findTemplateFor(@NotNull NotificationCategory category) {
        if (categoryToTemplateMap.containsKey(category)) {
            return Optional.of(categoryToTemplateMap.get(category).get(0));
        }
        return Optional.empty();
    }

    /**
     * Get all templates for a reason category (generally more coarse-grained).
     *
     * @param category the category to get a template for
     * @return template, if a suitable one is found, otherwise empty
     */
    public List<EmailNotificationTemplate> findAllTemplatesFor(@NotNull NotificationCategory category) {
        return categoryToTemplateMap.get(category);
    }

    private void readEmailNotificationTemplatesFromFile() {
        Path file = config.getConfigDirectory().resolve("email-notification-templates.json");
        if (Files.notExists(file)) {
            LOGGER.warn("No email notification templates found at {0}", file);
            return;
        }
        try {
            // So pithy!
            List<EmailNotificationTemplate> tpls = JsonUtil.<EmailNotificationTemplate, List<EmailNotificationTemplate>>toPojo(Files.readString(file), EmailNotificationTemplate.class, List.class);
            LOGGER.debug("{0} email notification templates read from {1}", tpls.size(), file.toAbsolutePath());
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
