package io.apiman.manager.api.beans.notifications.dto;

import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.notifications.NotificationCategory;

import java.util.List;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class CreateNotificationDto {
    private NotificationCategory category;
    private String reason;
    private String reasonMessage;
    private List<RecipientDto> recipient;
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

    public List<RecipientDto> getRecipient() {
        return recipient;
    }

    public CreateNotificationDto setRecipient(
         List<RecipientDto> recipient) {
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
}
