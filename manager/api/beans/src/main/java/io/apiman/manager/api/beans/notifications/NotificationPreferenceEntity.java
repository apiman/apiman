package io.apiman.manager.api.beans.notifications;

import io.apiman.manager.api.beans.idm.UserBean;

import java.util.LinkedHashSet;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

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
               columnNames = { "user_id", "type" }
          )
     }
)
public class NotificationPreferenceEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NaturalId
    private UserBean user;

    @Column(name = "type", nullable = false)
    @NotNull
    @NaturalId
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "notification_rules")
    private Set<NotificationFilterEntity> rules = new LinkedHashSet<>();

    public NotificationPreferenceEntity() {}

    public Long getId() {
        return id;
    }

    public NotificationPreferenceEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public UserBean getUser() {
        return user;
    }

    public NotificationPreferenceEntity setUser(UserBean user) {
        this.user = user;
        return this;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationPreferenceEntity setType(NotificationType type) {
        this.type = type;
        return this;
    }

    public Set<NotificationFilterEntity> getRules() {
        return rules;
    }

    public NotificationPreferenceEntity setRules(Set<NotificationFilterEntity> filters) {
        this.rules = filters;
        return this;
    }

}
