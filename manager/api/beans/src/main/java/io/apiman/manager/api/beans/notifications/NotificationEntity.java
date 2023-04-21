package io.apiman.manager.api.beans.notifications;

import io.apiman.common.util.JsonUtil;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Models a notification, which can also contain a payload.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Entity
@TypeDef(name = "json", typeClass = JsonType.class)
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @NotNull
    private NotificationCategory category;

    @Column(name = "reason", nullable = false)
    @NotBlank
    private String reason;

    @Column(name = "reason_message", nullable = false)
    @NotBlank
    private String reasonMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private NotificationStatus status;

    // TODO(msavy): consider tracking dismissal reason? for example, old or irrelevant, etc?
    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Column(name = "recipient", nullable = false)
    @NotBlank
    private String recipient;

    @Column(name = "source", nullable = false)
    @NotBlank
    private String source;

    @Type(type = "json")
    @Column(name = "payload", columnDefinition = "json", nullable = false)
    @NotNull
    private JsonNode payload;

    public NotificationEntity() {
    }

    public Long getId() {
        return id;
    }

    public NotificationEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public NotificationEntity setCategory(NotificationCategory category) {
        this.category = category;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public NotificationEntity setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public String getReasonMessage() {
        return reasonMessage;
    }

    public NotificationEntity setReasonMessage(String reasonMessage) {
        this.reasonMessage = reasonMessage;
        return this;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public NotificationEntity setStatus(NotificationStatus status) {
        this.status = status;
        return this;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public NotificationEntity setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public NotificationEntity setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
        return this;
    }

    public String getRecipient() {
        return recipient;
    }

    public NotificationEntity setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public String getSource() {
        return source;
    }

    public NotificationEntity setSource(String source) {
        this.source = source;
        return this;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public NotificationEntity setPayload(JsonNode payload) {
        this.payload = payload;
        return this;
    }

    public NotificationEntity setPayload(IVersionedApimanEvent payload) {
        this.payload = JsonUtil.toJsonTree(payload);
        return this;
    }

    /**
     * Pulls the payload type out of its headers segment.
     *
     * @return the type of the payload via its headers, and "missing" if unset.
     */
    @JsonIgnore
    public String getPayloadType() {
        return this.payload.at("/headers/type").asText("missing");
    }

    @JsonIgnore
    public Optional<ApimanEventHeaders> getHeaders() {
        JsonNode possibleHeaders = this.payload.at("/headers");
        if (!possibleHeaders.isMissingNode() && possibleHeaders.isObject()) {
            return Optional.of(JsonUtil.toPojo(possibleHeaders, ApimanEventHeaders.class));
        }
        return Optional.empty();
    }

    // TODO(msavy): Look in headers -> type, then pass to factory to reify it
    //
    // public <P extends IVersionedApimanEvent> P getEventPayload() {
    //     return JsonUtil.toPojo(payload, IVersionedApimanEvent.class);
    // }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotificationEntity.class.getSimpleName() + "[", "]")
             .add("id=" + id)
             .add("category=" + category)
             .add("reason='" + reason + "'")
             .add("reasonMessage='" + reasonMessage + "'")
             .add("status=" + status)
             .add("createdOn=" + createdOn)
             .add("modifiedOn=" + modifiedOn)
             .add("recipient='" + recipient + "'")
             .add("source='" + source + "'")
             .add("payload=" + payload)
             .toString();
    }
}
