package io.apiman.manager.api.beans.notifications;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Notifications types that we know about, so users can
 * opt into and out of these.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Entity
@Table(name = "notification_types")
public class NotificationType {

    @Id
    @Column(name = "type", unique = true, nullable = false)
    private String type;

    public NotificationType(String type) {
        this.type = type;
    }

    public NotificationType() {

    }
}
