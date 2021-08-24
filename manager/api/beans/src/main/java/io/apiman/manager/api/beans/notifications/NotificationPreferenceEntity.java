package io.apiman.manager.api.beans.notifications;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

/**
 * Which type of notification does the user want to opt into, and for which
 * types of notification.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */

@Entity
@Table(name = "notification_preferences")
public class NotificationPreferenceEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Long id;

    @Column(name = "notification_type", nullable = false)
    @NotEmpty
    private String notificationType;

    @Column(name = "notification_type", nullable = false)
    @NotEmpty
    private String notificationType;

    public NotificationPreferenceEntity() {
    }
}
