package io.apiman.manager.api.events;

import io.apiman.common.util.Preconditions;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.apache.commons.lang3.Validate;

/**
 * Uses cloud events-style fields
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonDeserialize(builder = ApimanEventHeaders.Builder.class)
public class ApimanEventHeaders {
    private String id;
    private URI source;
    private String type;
    private String subject;
    private OffsetDateTime time;
    private long eventVersion;
    private Map<String, Object> otherProperties;

    ApimanEventHeaders(String id, URI source, String type, String subject, OffsetDateTime time,
         long eventVersion, Map<String, Object> otherProperties) {
        this.id = id;
        this.source = source;
        this.type = type;
        this.subject = subject;
        this.time = time;
        this.eventVersion = eventVersion;
        this.otherProperties = otherProperties;
    }

    ApimanEventHeaders(){}

    public String getId() {
        return id;
    }

    public URI getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public long getEventVersion() {
        return eventVersion;
    }

    public Map<String, Object> getOtherProperties() {
        return otherProperties;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private String id;
        private URI source;
        private String type;
        private String subject;
        private OffsetDateTime time = OffsetDateTime.now();
        private long eventVersion;
        private final Map<String, Object> otherProperties = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setSource(URI source) {
            this.source = source;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder setTime(OffsetDateTime time) {
            this.time = time;
            return this;
        }

        public Builder setEventVersion(long eventVersion) {
            this.eventVersion = eventVersion;
            return this;
        }

        public Builder addProperty(String key, String value) {
            Objects.requireNonNull(key, "Header property key must not be null");
            this.otherProperties.put(key, value);
            return this;
        }

        public Builder setProperties(Map<String, Object> propertiesIn) {
            Objects.requireNonNull(propertiesIn);
            if (propertiesIn.containsKey(null)) {
                throw new IllegalArgumentException("Header property key must not be null");
            }
            this.otherProperties.putAll(propertiesIn);
            return this;
        }

        public ApimanEventHeaders build() {
            Validate.notBlank(id, "ID field must be set and not blank");
            Objects.requireNonNull(source, "Source must be set");
            Objects.requireNonNull(type, "Type must be set");
            Objects.requireNonNull(subject, "Subject must be set");
            Objects.requireNonNull(time, "Time must be set");
            Preconditions.checkArgument(eventVersion > 0, "Version must be > 0");

            return new ApimanEventHeaders(
                 id,
                 source,
                 type,
                 subject,
                 time,
                 eventVersion,
                 otherProperties
            );
        }
    }
}
