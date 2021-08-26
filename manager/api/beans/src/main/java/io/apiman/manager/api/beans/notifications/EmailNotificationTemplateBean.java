package io.apiman.manager.api.beans.notifications;

import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * A very simple global template system for various email notifications.
 *
 * For event X.Y.Z = use this body and subject line.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Entity
@Table(name = "email_notification_templates")
public class EmailNotificationTemplateBean {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "notification_template_body", nullable = false, length = 10000)
    @NotBlank
    private String notificationTemplateBody;

    @Column(name = "notification_template_subject", nullable = false)
    @NotBlank
    private String notificationTemplateSubject;

    @Column(name = "notification_reason", nullable = false)
    @NotBlank
    private String notificationReason;

    public EmailNotificationTemplateBean() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNotificationTemplateBody() {
        return notificationTemplateBody;
    }

    public EmailNotificationTemplateBean setNotificationTemplateBody(String notificationTemplateBody) {
        this.notificationTemplateBody = notificationTemplateBody;
        return this;
    }

    public String getNotificationTemplateSubject() {
        return notificationTemplateSubject;
    }

    public EmailNotificationTemplateBean setNotificationTemplateSubject(
         String notificationTemplateSubject) {
        this.notificationTemplateSubject = notificationTemplateSubject;
        return this;
    }

    public String getNotificationReason() {
        return notificationReason;
    }

    public EmailNotificationTemplateBean setNotificationReason(String notificationType) {
        this.notificationReason = notificationType;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EmailNotificationTemplateBean.class.getSimpleName() + "[", "]")
             .add("id=" + id)
             .add("notificationTemplateBody='" + notificationTemplateBody + "'")
             .add("notificationTemplateSubject='" + notificationTemplateSubject + "'")
             .add("notificationReason='" + notificationReason + "'")
             .toString();
    }
}
