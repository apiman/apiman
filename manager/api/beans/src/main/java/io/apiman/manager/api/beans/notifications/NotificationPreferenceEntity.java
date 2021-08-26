package io.apiman.manager.api.beans.notifications;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;

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
               columnNames = { "userId", "notification_type" }
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
    private String userId;

    @Column(name = "notification_type", nullable = false)
    @NotBlank
    private String notificationType;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "notification_category_options")
    private Set<NotificationCategory> notificationCategories;

    // TODO(msavy): allow enable/disable for specific notification reasons
    // Allow prefixes for more fine-grained filtering
    // Reason = account.approval.*
    // @ElementCollection(fetch = FetchType.LAZY)
    // @CollectionTable(name = "reason_notification_prefixes")
    // private Set<String> enabledForReasonPrefixes = new HashSet<>();

    public NotificationPreferenceEntity() {
    }
}
