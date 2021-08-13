package io.apiman.manager.api.beans.notifications;

import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */

@Entity
@Table(name = "notification_preferences")
public class NotificationPreferenceBean {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Long id;

    @Column(name = "notification_type", nullable = false)
    @NotEmpty
    private String notificationType;

    @Column(name = "user_id", nullable = false)
    @NotEmpty
    private String userId;

    public NotificationPreferenceBean() {
    }

    public Long getId() {
        return id;
    }

    public NotificationPreferenceBean setId(Long id) {
        this.id = id;
        return this;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public NotificationPreferenceBean setNotificationType(String notificationType) {
        this.notificationType = notificationType;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public NotificationPreferenceBean setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotificationPreferenceBean.class.getSimpleName() + "[", "]")
             .add("id=" + id)
             .add("notificationType='" + notificationType + "'")
             .add("userId='" + userId + "'")
             .toString();
    }
}
