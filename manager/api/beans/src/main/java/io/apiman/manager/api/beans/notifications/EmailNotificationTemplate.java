package io.apiman.manager.api.beans.notifications;

import java.util.StringJoiner;

/**
 * A very simple global template system for various email notifications.
 *
 * By notification reason or category. Category might be more useful for more generic notifications.
 *
 * TODO: HTML + Plaintext
 * TODO: Language-specific templates (need per-user language preference)
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
    private String notificationTemplateBody;

    // @Column(name = "notification_template_subject", nullable = false)
    // @NotBlank
    private String notificationTemplateSubject;

    // @Column(name = "notification_reason", nullable = true)
    private String notificationReason;

    // @Column(name = "notification_category", nullable = true)
    // @Enumerated(EnumType.STRING)
    private NotificationCategory notificationCategory;

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

    public String getNotificationTemplateBody() {
        return notificationTemplateBody;
    }

    public EmailNotificationTemplate setNotificationTemplateBody(String notificationTemplateBody) {
        this.notificationTemplateBody = notificationTemplateBody;
        return this;
    }

    public String getNotificationTemplateSubject() {
        return notificationTemplateSubject;
    }

    public EmailNotificationTemplate setNotificationTemplateSubject(
         String notificationTemplateSubject) {
        this.notificationTemplateSubject = notificationTemplateSubject;
        return this;
    }

    public String getNotificationReason() {
        return notificationReason;
    }

    public EmailNotificationTemplate setNotificationReason(String notificationType) {
        this.notificationReason = notificationType;
        return this;
    }

    public NotificationCategory getNotificationCategory() {
        return notificationCategory;
    }

    public EmailNotificationTemplate setNotificationCategory(
         NotificationCategory notificationCategory) {
        this.notificationCategory = notificationCategory;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EmailNotificationTemplate.class.getSimpleName() + "[", "]")
             //.add("id=" + id)
             .add("notificationTemplateBody='" + notificationTemplateBody + "'")
             .add("notificationTemplateSubject='" + notificationTemplateSubject + "'")
             .add("notificationReason='" + notificationReason + "'")
             .add("notificationCategory=" + notificationCategory)
             .toString();
    }
}
