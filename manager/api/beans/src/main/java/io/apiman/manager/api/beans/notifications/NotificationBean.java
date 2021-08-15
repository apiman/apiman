package io.apiman.manager.api.beans.notifications;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */

@Entity
@Table(name = "notifications")
public class NotificationBean  {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Long id;

    @Column(name = "created_on", updatable=false, nullable=false)
    private Date createdOn;

    @Column(name = "created_on", nullable=false)
    private Date modifiedOn;

    @Column(name = "recipient", nullable = false)
    @NotEmpty
    private String recipient;

    @Column(name = "source", nullable = false)
    @NotEmpty
    private String source;

    public NotificationBean() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public NotificationBean setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public NotificationBean setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
        return this;
    }

    public String getRecipient() {
        return recipient;
    }

    public NotificationBean setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public String getSource() {
        return source;
    }

    public NotificationBean setSource(String source) {
        this.source = source;
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
        NotificationBean that = (NotificationBean) o;
        return Objects.equals(createdOn, that.createdOn) && Objects.equals(modifiedOn,
             that.modifiedOn) && Objects.equals(recipient, that.recipient) && Objects.equals(
             source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdOn, modifiedOn, recipient, source);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotificationBean.class.getSimpleName() + "[", "]")
             .add("id=" + id)
             .add("createdOn=" + createdOn)
             .add("modifiedOn=" + modifiedOn)
             .add("recipient='" + recipient + "'")
             .add("source='" + source + "'")
             .toString();
    }
}
