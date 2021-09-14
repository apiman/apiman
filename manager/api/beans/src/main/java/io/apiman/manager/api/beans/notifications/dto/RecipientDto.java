package io.apiman.manager.api.beans.notifications.dto;

import java.util.Objects;
import java.util.StringJoiner;

import org.jetbrains.annotations.Nullable;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class RecipientDto {
    private RecipientType recipientType;
    private String recipient;
    @Nullable
    private String orgId;

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

    public String getOrgId() {
        return orgId;
    }

    public RecipientDto setOrgId(String orgId) {
        this.orgId = orgId;
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
