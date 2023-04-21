package io.apiman.manager.api.jpa.model.outbox;

import java.time.OffsetDateTime;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

/**
 * Models an entry in a transactional outbox.
 *
 * <p>Thanks, @vladmihalcea for <a href="https://github.com/vladmihalcea/hibernate-types">JSONB support in Hibernate</a>
 * <p>Thanks, @gunnarmorling for
 *  <a href="https://debezium.io/blog/2019/02/19/reliable-microservices-data-exchange-with-the-outbox-pattern/">
 *     Debezium Outbox pattern blog
 *  </a>
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Entity
@TypeDef(name = "json", typeClass = JsonType.class)
@Table(name = "outbox")
public class OutboxEventEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source", nullable = false)
    @NotBlank
    private String source;

    @Column(name = "type", nullable = false)
    @NotBlank
    private String type;

    @Column(name = "subject", nullable = false)
    @NotBlank
    private String subject;

    // @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time", nullable = false)
    @NotNull
    private OffsetDateTime time;

    @Column(name = "event_version", nullable = false)
    @NotNull
    private Long eventVersion;

    @Type(type = "json")
    @Column(name = "payload", columnDefinition = "json", nullable = false)
    @NotNull
    private JsonNode payload;

    public OutboxEventEntity() {
    }

    public Long getId() {
        return id;
    }

    public OutboxEventEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getSource() {
        return source;
    }

    public OutboxEventEntity setSource(String source) {
        this.source = source;
        return this;
    }

    public String getType() {
        return type;
    }

    public OutboxEventEntity setType(String type) {
        this.type = type;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public OutboxEventEntity setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public OutboxEventEntity setTime(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    public Long getEventVersion() {
        return eventVersion;
    }

    public OutboxEventEntity setEventVersion(Long eventVersion) {
        this.eventVersion = eventVersion;
        return this;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public OutboxEventEntity setPayload(JsonNode payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OutboxEventEntity.class.getSimpleName() + "[", "]")
             .add("id=" + id)
             .add("source='" + source + "'")
             .add("type='" + type + "'")
             .add("subject='" + subject + "'")
             .add("time=" + time)
             .add("eventVersion=" + eventVersion)
             .add("payload=" + payload)
             .toString();
    }
}
