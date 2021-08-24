package io.apiman.manager.api.beans.notifications;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * Different types of notification, such as SMS, etc.
 *
 * Different notification handlers should register themselves.
 *
 * In theory, this should allow Apiman to support any number of notification
 * types.
 *
 * TODO(msavy): this could just be in-memory possibly? Each handler just registers itself
 * TODO(msavy): at startup and we store the types as map. This could cause some weirdness
 * TODO(msavy): if one of the handlers goes away.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Entity
@Table(name = "notification_types")
public class NotificationTypeEntity {

    @Column(name = "type", nullable = false)
    @NotBlank
    @Id
    private String type; // e.g. email, SMS etc

    @Column(name = "description", nullable = false)
    @NotBlank
    private String description;

    public NotificationTypeEntity() {
    }

    public String getType() {
        return type;
    }

    public NotificationTypeEntity setType(String type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public NotificationTypeEntity setDescription(String description) {
        this.description = description;
        return this;
    }
}
