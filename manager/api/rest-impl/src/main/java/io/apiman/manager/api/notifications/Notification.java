package io.apiman.manager.api.notifications;

import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.notifications.NotificationCategory;
import io.apiman.manager.api.beans.notifications.NotificationStatus;

import java.util.Date;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class Notification<P extends IVersionedApimanEvent> {
    private Long id;
    private NotificationCategory category;
    private String reason;
    private String reasonMessage;
    private NotificationStatus notificationStatus;
    private Date createdOn;
    private Date modifiedOn;
    private String recipient;
    private String source;
    private P payload;

    public Notification() {
    }

    public Long getId() {
        return id;
    }

    public Notification<P> setId(Long id) {
        this.id = id;
        return this;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public Notification<P> setCategory(NotificationCategory category) {
        this.category = category;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public Notification<P> setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public String getReasonMessage() {
        return reasonMessage;
    }

    public Notification<P> setReasonMessage(String reasonMessage) {
        this.reasonMessage = reasonMessage;
        return this;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public Notification<P> setNotificationStatus(
         NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public Notification<P> setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public Notification<P> setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
        return this;
    }

    public String getRecipient() {
        return recipient;
    }

    public Notification<P> setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public String getSource() {
        return source;
    }

    public Notification<P> setSource(String source) {
        this.source = source;
        return this;
    }

    public P getPayload() {
        return payload;
    }

    public Notification<P> setPayload(P payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Notification.class.getSimpleName() + "[", "]")
             .add("id=" + id)
             .add("category=" + category)
             .add("reason='" + reason + "'")
             .add("reasonMessage='" + reasonMessage + "'")
             .add("notificationStatus=" + notificationStatus)
             .add("createdOn=" + createdOn)
             .add("modifiedOn=" + modifiedOn)
             .add("recipient='" + recipient + "'")
             .add("source='" + source + "'")
             .add("payload=" + payload)
             .toString();
    }
}
