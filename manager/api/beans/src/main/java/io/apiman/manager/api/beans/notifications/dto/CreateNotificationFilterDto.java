package io.apiman.manager.api.beans.notifications.dto;

import io.apiman.manager.api.beans.notifications.NotificationType;

import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nullable;
import javax.persistence.Column;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class CreateNotificationFilterDto {
    private String source;
    private NotificationType notificationType;
    private String expression;
    private boolean enabled = true;
    private String message;

    public CreateNotificationFilterDto() {}

    public String getSource() {
        return source;
    }

    public CreateNotificationFilterDto setSource(String source) {
        this.source = source;
        return this;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public CreateNotificationFilterDto setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
        return this;
    }

    public String getExpression() {
        return expression;
    }

    public CreateNotificationFilterDto setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public CreateNotificationFilterDto setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CreateNotificationFilterDto setMessage(String message) {
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
        CreateNotificationFilterDto that = (CreateNotificationFilterDto) o;
        return Objects.equals(source, that.source) && notificationType == that.notificationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, notificationType);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CreateNotificationFilterDto.class.getSimpleName() + "[", "]")
                .add("source='" + source + "'")
                .add("notificationType=" + notificationType)
                .add("expression='" + expression + "'")
                .add("enabled=" + enabled)
                .add("message='" + message + "'")
                .toString();
    }
}
