package io.apiman.manager.api.beans.notifications.dto;

import io.apiman.manager.api.beans.notifications.NotificationType;

import java.util.Objects;
import java.util.StringJoiner;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class CreateNotificationFilterDto {
    private String source;
    private NotificationType notificationType;
    private String expression;

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

    public String getexpression() {
        return expression;
    }

    public CreateNotificationFilterDto setexpression(String expression) {
        this.expression = expression;
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
        return Objects.equals(source, that.source) && notificationType == that.notificationType && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, notificationType, expression);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CreateNotificationFilterDto.class.getSimpleName() + "[", "]")
                .add("source='" + source + "'")
                .add("notificationType=" + notificationType)
                .add("expression='" + expression + "'")
                .toString();
    }
}
