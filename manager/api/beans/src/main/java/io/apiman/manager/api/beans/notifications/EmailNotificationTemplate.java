package io.apiman.manager.api.beans.notifications;

import java.util.StringJoiner;

/**
 * A very simple global template system for various email notifications.
 *
 * By notification reason or category. Category might be more useful for more generic notifications.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
// This will be an entity later...
// @Entity
// @Table(name = "email_notification_templates")
public class EmailNotificationTemplate {

    // @Id
    // @Column(name = "id", nullable = false)
    // private Long id;

    // @Column(name = "notification_template_body", nullable = false, length = 10000)
    // @NotBlank

    private String locale;

    private String htmlBody;

    private String plainBody;

    // @Column(name = "notification_template_subject", nullable = false)
    // @NotBlank
    private String subject;

    // @Column(name = "notification_reason", nullable = true)
    private String notificationReason;

    // @Column(name = "notification_category", nullable = true)
    // @Enumerated(EnumType.STRING)
    private NotificationCategory category;

    public EmailNotificationTemplate() {
    }
    //
    // public Long getId() {
    //     return id;
    // }
    //
    // public void setId(Long id) {
    //     this.id = id;
    // }


    public String getHtmlBody() {
        return htmlBody;
    }

    public EmailNotificationTemplate setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
        return this;
    }

    public String getPlainBody() {
        return plainBody;
    }

    public EmailNotificationTemplate setPlainBody(String plainBody) {
        this.plainBody = plainBody;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public EmailNotificationTemplate setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getNotificationReason() {
        return notificationReason;
    }

    public EmailNotificationTemplate setNotificationReason(String notificationReason) {
        this.notificationReason = notificationReason;
        return this;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public EmailNotificationTemplate setCategory(
         NotificationCategory category) {
        this.category = category;
        return this;
    }

    public String getLocale() {
        return locale;
    }

    public EmailNotificationTemplate setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EmailNotificationTemplate.class.getSimpleName() + "[", "]")
             .add("htmlBody='" + htmlBody + "'")
             .add("plainBody='" + plainBody + "'")
             .add("subject='" + subject + "'")
             .add("notificationReason='" + notificationReason + "'")
             .add("category=" + category)
             .toString();
    }
}
