package io.apiman.manager.api.notifications.dto;

import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.notifications.NotificationCategory;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class CreateNotificationDto {
    private NotificationCategory category;
    private String reason;
    private String reasonMessage;
    private RecipientType recipientType;
    private String recipient;
    private String source;
    private IVersionedApimanEvent payload;

    public CreateNotificationDto() {
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public CreateNotificationDto setCategory(NotificationCategory category) {
        this.category = category;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public CreateNotificationDto setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public String getReasonMessage() {
        return reasonMessage;
    }

    public CreateNotificationDto setReasonMessage(String reasonMessage) {
        this.reasonMessage = reasonMessage;
        return this;
    }

    public String getRecipient() {
        return recipient;
    }

    public CreateNotificationDto setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public String getSource() {
        return source;
    }

    public CreateNotificationDto setSource(String source) {
        this.source = source;
        return this;
    }

    public IVersionedApimanEvent getPayload() {
        return payload;
    }

    public CreateNotificationDto setPayload(IVersionedApimanEvent payload) {
        this.payload = payload;
        return this;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public CreateNotificationDto setRecipientType(RecipientType recipientType) {
        this.recipientType = recipientType;
        return this;
    }
}
