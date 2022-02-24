package io.apiman.manager.api.beans.notifications;

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;

/**
 *
 */
@Embeddable
public class NotificationFilterEntity {

    @NotBlank
    @Column(name = "source")
    private String source;

    @NotBlank
    @Column(name = "expression")
    private String expression;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationFilterEntity that = (NotificationFilterEntity) o;
        return Objects.equals(source, that.source) && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, expression);
    }
}