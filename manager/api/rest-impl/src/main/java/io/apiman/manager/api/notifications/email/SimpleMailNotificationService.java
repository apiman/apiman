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
import java.util.Collection;
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
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.common.util.LocaleUtils;
import org.jetbrains.annotations.NotNull;

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
public class SimpleMailNotificationService {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(SimpleMailNotificationService.class);
    private static final Map<String, String> DEFAULT_HEADERS = Map.of("X-Notification-Producer", "Apiman");

    private IEmailSender emailSender;
    private ApiManagerConfig config;
    private QteTemplateEngine templateEngine;
    // String -> Map<Locale, EmailNotificationTemplate>
    private final PatriciaTrie<Map<Locale,EmailNotificationTemplate>> reasonTrie = new PatriciaTrie<>();
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
    public void sendPlaintext(@NotNull String toEmail, @NotNull String toName, @NotNull String subject, @NotNull String body) {
        emailSender.sendPlaintext(toEmail, toName, subject, body, DEFAULT_HEADERS);
    }

    /**
     * Send a plaintext email
     */
    public void sendPlaintext(@NotNull String toEmail, @NotNull String toName, @NotNull  String subject, @NotNull  String body, @NotNull Map<String, String> headers) {
        var copy = Maps.newHashMap(headers);
        copy.putAll(DEFAULT_HEADERS);
        emailSender.sendPlaintext(toEmail, toName, subject, body, copy);
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
        var copy = Maps.newHashMap(headers);
        copy.putAll(DEFAULT_HEADERS);
        emailSender.sendHtml(toEmail, toName, subject, htmlBody, plainBody, headers);
    }

    public Optional<EmailNotificationTemplate> findTemplateFor(@NotNull String reasonKey, @NotNull String localeTag) {
        Locale bestMatchingLocale = Locale.lookup(Locale.LanguageRange.parse(localeTag), supportedLocales);
        return findTemplateFor(reasonKey, bestMatchingLocale);
    }

    /**
     * Find an email template for a provided reason (longest prefix match wins).
     *
     * <p>For example, if we have a template for both <code>a.b.c.d</code> and <code>a.b.c</code>, the former will be
     * returned (it is the longest match).
     *
     * <p>Correspondingly, if an exact template match is not found, the prefix will be shortened until a matching
     * template is found, or we determine there is no suitable template.
     *
     * @param reasonKey the reason to find a template for
     * @param locale the locale to resolve
     * @return template, if a suitable one is found, otherwise empty
     */
    public Optional<EmailNotificationTemplate> findTemplateFor(@NotNull String reasonKey, @NotNull Locale locale) {
        Entry<String, Map<Locale,EmailNotificationTemplate>> selected = reasonTrie.select(reasonKey);
        if (selected == null || selected.getValue() == null) {
            LOGGER.debug("No email template found for reason {0}, including shorter paths", reasonKey);
            return Optional.empty();
        } else {
            LOGGER.debug("Found template for reason {0} as {1}, shorter matching reasons templates "
                              + "may also exist", selected.getKey(), reasonKey);

            Map<Locale, EmailNotificationTemplate> localeMap = selected.getValue();
            if (localeMap == null) {
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
    public Map<Locale, List<EmailNotificationTemplate>> findAllTemplatesFor(@NotNull String reasonKey) {
        String fromKey = null;
        String[] elems = reasonKey.split("\\.");
        if (elems.length == 1) {
            fromKey = reasonKey.substring(0, 1);
        } else {
            fromKey = elems[0];
        }

        List<Map<Locale, EmailNotificationTemplate>> subMapCollection = new ArrayList<>(reasonTrie.subMap(fromKey, reasonKey.substring(0, reasonKey.length() - 1)).values());
        Map<Locale, EmailNotificationTemplate> selectMap = reasonTrie.select(reasonKey).getValue();
        subMapCollection.add(selectMap);

        Map<Locale, List<EmailNotificationTemplate>> results = new HashMap<>();

        for (Map<Locale, EmailNotificationTemplate> map : subMapCollection) {
            for (Entry<Locale, EmailNotificationTemplate> entry : map.entrySet()) {
                results.computeIfAbsent(entry.getKey(), value -> new ArrayList<>()).add(entry.getValue());
            }
        }

        return results;
    }

    private void readEmailNotificationTemplatesFromFile() {
        Path file = config.getConfigDirectory().resolve("email-notification-templates.json5");
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

            localeTplMap.forEach(this::processReasonMap);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private void processReasonMap(String localeTag, ReasonMap reasonMap) {
        reasonMap.forEach((String reason, EmailNotificationTemplate tpl) -> {
            tpl.setNotificationReason(reason);
            tpl.setLocale(localeTag);
            Locale locale = Locale.forLanguageTag(localeTag);
            reasonTrie.computeIfAbsent(reason, (val) -> new HashMap<>(5)).put(locale, tpl);
            // For each category we now have multiple locales, so we have an extra layer before we get to our list of notification templates.
            Map<Locale, List<EmailNotificationTemplate>> catLocales = categoryToTemplateMap.computeIfAbsent(tpl.getCategory(), val -> new HashMap<>());
            // Instantiate the list if not already there. We have: category -> locale -> list<templates>
            catLocales.computeIfAbsent(locale, val -> new ArrayList<>()).add(tpl);
            LOGGER.trace("Adding template ({0}): reason {1} -> {2}", locale, tpl.getNotificationReason(), tpl);
            LOGGER.trace("Adding template ({0}): category {1} -> {2}", locale, tpl.getCategory(), tpl);
        });
    }

    /**
     * Map: Reason code -> email notification template.
     * Helpful for making Jackson deserialization a bit easier.
     */
    private static final class ReasonMap extends HashMap<String, EmailNotificationTemplate> { }
}
