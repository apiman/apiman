package io.apiman.manager.api.beans.notifications;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.NaturalId;

/**
 * Which type of notification does the user want to opt into, and for which types of notification.
 * <p>
 * Constraint notes: Each user can only have one {@link NotificationPreferenceEntity} entity for each type of
 * notification. For example, user 'abc' may only have one 'SMS' preference entity.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Entity
@Table(
     name = "notification_preferences",
     uniqueConstraints = {
          @UniqueConstraint(
               name = "UserAllowedOnlyOneOfEachNotificationType",
               columnNames = { "userId", "type" }
          )
     }
)
public class NotificationPreferenceEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Long id;

    @Column(name = "userId", nullable = false)
    @NotBlank
    @NaturalId
    private String userId;

    @Column(name = "type", nullable = false)
    @NotBlank
    @NaturalId
    private String notificationType;

    @Column(name = "category")
    @CollectionTable(name="notification_category_preferences")
    @ElementCollection(fetch = FetchType.EAGER, targetClass = NotificationCategory.class)
    @Enumerated(EnumType.STRING)
    private Set<NotificationCategory> notificationCategories = new HashSet<>();

    // TODO(msavy): allow enable/disable for specific notification reasons
    // Allow prefixes for more fine-grained filtering
    // Reason = account.approval.*
    // @ElementCollection(fetch = FetchType.LAZY)
    // @CollectionTable(name = "reason_notification_prefixes")
    // private Set<String> enabledForReasonPrefixes = new HashSet<>();

    public NotificationPreferenceEntity() {}

    public Long getId() {
        return id;
    }

    public NotificationPreferenceEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public NotificationPreferenceEntity setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public NotificationPreferenceEntity setNotificationType(String notificationType) {
        this.notificationType = notificationType;
        return this;
    }

    public Set<NotificationCategory> getNotificationCategories() {
        return notificationCategories;
    }

    public NotificationPreferenceEntity setNotificationCategories(
         Set<NotificationCategory> notificationCategories) {
        this.notificationCategories = notificationCategories;
        return this;
    }
}
