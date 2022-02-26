package io.apiman.manager.api.beans.notifications;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.Hibernate;
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

    @Column(name = "user_id", nullable = false)
    @NotBlank
    @NaturalId
    private String userId; // TODO(msavy): explicitly link to user object, so we can cascade delete?

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

    public String getUserId() {
        return userId;
    }

    public NotificationPreferenceEntity setUserId(String userId) {
        this.userId = userId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        NotificationPreferenceEntity that = (NotificationPreferenceEntity) o;
        return userId != null && Objects.equals(userId, that.userId)
                       && type != null && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, type);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotificationPreferenceEntity.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("userId='" + userId + "'")
                .add("type='" + type + "'")
                .toString();
    }


}
