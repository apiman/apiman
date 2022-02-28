package io.apiman.manager.api.beans.notifications;

import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;

/**
 * <p>Filters for notifications, using a limited subset of SpEL to exclude a notification.
 *
 * <ul>
 *     <li>source - target notification type (expression can refer to arbitrary fields, so must be constrained appropriately)</li>
 *     <li>expression - SpEL expression</li>
 * </ul>
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Embeddable
public class NotificationFilterEntity {

    @NotBlank
    @Column(name = "source")
    private String source;

    @NotBlank
    @Column(name = "expression")
    private String expression;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Nullable
    @Column(name = "message")
    private String message;

    public NotificationFilterEntity() {
    }

    public String getSource() {
        return source;
    }

    public NotificationFilterEntity setSource(String source) {
        this.source = source;
        return this;
    }

    public String getExpression() {
        return expression;
    }

    public NotificationFilterEntity setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NotificationFilterEntity setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    public NotificationFilterEntity setMessage(@Nullable String message) {
        this.message = message;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationFilterEntity that = (NotificationFilterEntity) o;
        return Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotificationFilterEntity.class.getSimpleName() + "[", "]")
                .add("source='" + source + "'")
                .add("expression='" + expression + "'")
                .add("enabled=" + enabled)
                .add("message='" + message + "'")
                .toString();
    }
}