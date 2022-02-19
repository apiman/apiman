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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.collect.Maps;

/**
 * Simple email related actions, with a focus on notifications.
 *
 * <ul>
 *     <li>Send notification emails as configured via file in {@link ApiManagerConfig#getEmailNotificationProperties()}}.</li>
 *     <li>Get email notification templates (from a static file only, at present) by reason (or prefix thereof) or
 *     category.</li>
 *     <li>A convenient builder is available with {@link SimpleEmail#builder()}.</li>
 * </ul>
 *
 * TODO(msavy): consider refactoring to allow only full word matches on keys
 *
 * @see SmtpEmailConfiguration
 * @see SimpleEmail
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@ParametersAreNonnullByDefault
public class SimpleMailNotificationService {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(SimpleMailNotificationService.class);
    private static final Map<String, String> DEFAULT_HEADERS = Map.of("X-Notification-Producer", "Apiman");

    private IEmailSender emailSender;
    private ApiManagerConfig config;
    private QteTemplateEngine templateEngine;
    // String -> Map<Locale, EmailNotificationTemplate>
    private final Map<String, Map<Locale,EmailNotificationTemplate>> reasonMap = new HashMap<>();
    // NotificationCategory -> List<Map<Locale, EmailNotificationTemplate>>
    private final Map<NotificationCategory, Map<Locale, List<EmailNotificationTemplate>>> categoryToTemplateMap = new HashMap<>();
    // All locales we have a template for (e.g. en, fr, de, etc).
    private Set<Locale> supportedLocales;

    @Inject
    public SimpleMailNotificationService(ApiManagerConfig config, QteTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.config = config;
        if (!config.getEmailNotificationProperties().isEmpty()) {
            var smtpConfig = new SmtpEmailConfiguration(config.getEmailNotificationProperties());
            this.emailSender = new EmailSender(smtpConfig);
            if (smtpConfig.isMock()) {
                emailSender = new MockEmailSender();
            }
        } else {
            emailSender = new MockEmailSender();
        }
        readEmailNotificationTemplatesFromFile();
    }

    public SimpleMailNotificationService() {}

    /**
     * Send an email
     */
    public void send(SimpleEmail email) {
        LOGGER.debug("Sending email: {0}", email);
        EmailNotificationTemplate template = email.getTemplate();

        String html = templateEngine.applyTemplate(template.getHtmlBody(), email.getTemplateVariables());
        boolean htmlBlank = html.isBlank();

        String plain = templateEngine.applyTemplate(template.getPlainBody(), email.getTemplateVariables());
        boolean plainBlank = html.isBlank();

        String subject = templateEngine.applyTemplate(template.getSubject(), email.getTemplateVariables());
        boolean subjectBlank = subject.isBlank();

        if (subjectBlank) {
            throw new IllegalArgumentException("Non-blank subject is required for notification emails " + email);
        } else if (htmlBlank && plainBlank) {
            throw new IllegalArgumentException("Both HTML and plain templates are blank in notification mail " + email);
        } else if (htmlBlank) {
            sendPlaintext(email.getToEmail(), email.getToName(), subject, plain, email.getHeaders());
        } else if (plainBlank) {
            LOGGER.warn("Sending an HTML mail without a plaintext version is not recommended: {0}", email);
            sendHtml(email.getToEmail(), email.getToName(), subject, html, "", email.getHeaders());
        } else {
            sendHtml(email.getToEmail(), email.getToName(), subject, html, plain, email.getHeaders());
        }
    }

    /**
     * Send a plaintext email
     */
    public void sendPlaintext(String toEmail, String toName, String subject, String body) {
        emailSender.sendPlaintext(toEmail, toName, subject, body, DEFAULT_HEADERS);
    }

    /**
     * Send a plaintext email
     */
    public void sendPlaintext(String toEmail, String toName,  String subject, String body, Map<String, String> headers) {
        var copy = Maps.newHashMap(headers);
        copy.putAll(DEFAULT_HEADERS);
        emailSender.sendPlaintext(toEmail, toName, subject, body, copy);
    }

    /**
     * Send an HTML email with a plaintext fallback/alternative.
     */
    public void sendHtml(String toEmail, String toName, String subject, String htmlBody, String plainBody) {
        emailSender.sendHtml(toEmail, toName, subject, htmlBody, plainBody, DEFAULT_HEADERS);
    }

    /**
     * Send an HTML email with a plaintext fallback/alternative.
     */
    public void sendHtml(String toEmail, String toName, String subject, String htmlBody, String plainBody, Map<String, String> headers) {
        var copy = Maps.newHashMap(headers);
        copy.putAll(DEFAULT_HEADERS);
        emailSender.sendHtml(toEmail, toName, subject, htmlBody, plainBody, headers);
    }

    public Optional<EmailNotificationTemplate> findTemplateFor(String reasonKey, String localeTag) {
        Locale bestMatchingLocale = Locale.lookup(Locale.LanguageRange.parse(localeTag), supportedLocales);
        return findTemplateFor(reasonKey, bestMatchingLocale);
    }

    /**
     * Find an email template for a provided reason (longest prefix match wins).
     *
     * @param reasonKey the reason to find a template for
     * @param locale the locale to resolve
     * @return template, if a suitable one is found, otherwise empty
     */
    public Optional<EmailNotificationTemplate> findTemplateFor(String reasonKey, Locale locale) {
        Map<Locale, EmailNotificationTemplate> localeMap = reasonMap.get(reasonKey);
        if (localeMap == null || localeMap.isEmpty()) {
            LOGGER.debug("No email template found for reason {0}, including shorter paths", reasonKey);
            return Optional.empty();
        } else {
            List<Locale> bestMatchingLocale = List.of(
                    Locale.lookup(LanguageRange.parse(locale.toLanguageTag()), supportedLocales),
                    Locale.getDefault(),
                    new Locale("en") // TODO(msavy): config-based default language?
            );
            return bestMatchingLocale.stream()
                    .map(localeMap::get)
                    .filter(Objects::nonNull)
                    .findFirst();
        }
    }

    private void readEmailNotificationTemplatesFromFile() {
        Path file = config.getConfigDirectory().resolve("notifications/email/notification-template-index.json");
        if (Files.notExists(file)) {
            LOGGER.warn("No email notification templates found at {0}", file);
            return;
        }
        try {
            // So pithy!
            Map<String, ReasonMap> localeTplMap = JsonUtil.toPojo(Files.readString(file), String.class, ReasonMap.class, HashMap.class);
            LOGGER.debug("Email notification templates read from {1}", file.toAbsolutePath());

            supportedLocales = localeTplMap.keySet().stream()
                    .map(Locale::forLanguageTag)
                    .collect(Collectors.toSet());

            localeTplMap.forEach((k, v) -> processReasonMap(file, k, v));
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private void processReasonMap(Path root, String localeTag, ReasonMap reasonMapFromFile) {
            String reason = "";
        try {
            for (Entry<String, EmailTemplateFileEntry> entry : reasonMapFromFile.entrySet()) {
                reason = entry.getKey();
                EmailTemplateFileEntry fileEntry = entry.getValue();
                EmailNotificationTemplate tpl = new EmailNotificationTemplate();
                tpl.setNotificationReason(reason);
                tpl.setLocale(localeTag);
                tpl.setHtmlBody(Files.readString(root.getParent().resolve(fileEntry.html())));
                tpl.setPlainBody(Files.readString(root.getParent().resolve(fileEntry.plain())));
                tpl.setCategory(fileEntry.category());
                Locale locale = Locale.forLanguageTag(localeTag);
                reasonMap.computeIfAbsent(reason, (val) -> new HashMap<>(5)).put(locale, tpl);
                // For each category we now have multiple locales, so we have an extra layer before we get to our list of notification templates.
                Map<Locale, List<EmailNotificationTemplate>> catLocales = categoryToTemplateMap.computeIfAbsent(tpl.getCategory(), val -> new HashMap<>());
                // Instantiate the list if not already there. We have: category -> locale -> list<templates>
                catLocales.computeIfAbsent(locale, val -> new ArrayList<>()).add(tpl);
                LOGGER.trace("Adding template ({0}): reason {1} -> {2}", locale, tpl.getNotificationReason(), tpl);
                LOGGER.trace("Adding template ({0}): category {1} -> {2}", locale, tpl.getCategory(), tpl);
            }
        } catch (IOException ioe) {
            LOGGER.error(ioe, "An IO exception occurred attempting to process an email template. Reason code: {0}. Locale: {1}.", reason, localeTag);
            throw new UncheckedIOException(ioe);
        }
    }

    /**
     * Map: Reason code -> email notification template.
     * Helpful for making Jackson deserialization a bit easier.
     */
    private static final class ReasonMap extends HashMap<String, EmailTemplateFileEntry> { }
}
