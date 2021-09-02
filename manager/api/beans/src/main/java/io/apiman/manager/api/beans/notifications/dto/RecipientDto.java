package io.apiman.manager.api.beans.notifications.dto;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class RecipientDto {
    private RecipientType recipientType;
    private String recipient;

    public RecipientDto() {
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public RecipientDto setRecipientType(RecipientType recipientType) {
        this.recipientType = recipientType;
        return this;
    }

    public String getRecipient() {
        return recipient;
    }

    public RecipientDto setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RecipientDto.class.getSimpleName() + "[", "]")
             .add("recipientType=" + recipientType)
             .add("recipient='" + recipient + "'")
             .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecipientDto that = (RecipientDto) o;
        return recipientType == that.recipientType && Objects.equals(recipient, that.recipient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipientType, recipient);
    }
}
