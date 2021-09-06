package io.apiman.manager.api.beans.notifications.dto;

import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.notifications.NotificationCategory;
import io.apiman.manager.api.beans.notifications.NotificationStatus;

import java.time.OffsetDateTime;
import java.util.StringJoiner;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class NotificationDto<P extends IVersionedApimanEvent> {
    public Long id;
    private NotificationCategory category;
    private String reason;
    private String reasonMessage;
    private NotificationStatus notificationStatus;
    private OffsetDateTime createdOn;
    private OffsetDateTime modifiedOn;
    private String recipient;
    private String source;
    private P payload;

    public NotificationDto() {
    }

    public Long getId() {
        return id;
    }

    public NotificationDto<P> setId(Long id) {
        this.id = id;
        return this;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public NotificationDto<P> setCategory(NotificationCategory category) {
        this.category = category;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public NotificationDto<P> setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public String getReasonMessage() {
        return reasonMessage;
    }

    public NotificationDto<P> setReasonMessage(String reasonMessage) {
        this.reasonMessage = reasonMessage;
        return this;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public NotificationDto<P> setNotificationStatus(
         NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
        return this;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public NotificationDto<P> setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public NotificationDto<P> setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
        return this;
    }

    public String getRecipient() {
        return recipient;
    }

    public NotificationDto<P> setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public String getSource() {
        return source;
    }

    public NotificationDto<P> setSource(String source) {
        this.source = source;
        return this;
    }

    public P getPayload() {
        return payload;
    }

    public NotificationDto<P> setPayload(P payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotificationDto.class.getSimpleName() + "[", "]")
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
