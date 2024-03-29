/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.beans.events;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.apache.commons.lang3.Validate;

/**
 * Uses cloud events-style fields.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonDeserialize(builder = ApimanEventHeaders.Builder.class)
public class ApimanEventHeaders {
    private String id;
    private URI source;
    private String type; // FQCN
    private String subject; // apiman.blah?
    private OffsetDateTime time;
    private long eventVersion;
    private Map<String, Object> otherProperties = Collections.emptyMap();

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

    public ApimanEventHeaders setEventVersion(long eventVersion) {
        this.eventVersion = eventVersion;
        return this;
    }

    public ApimanEventHeaders setTime(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    public ApimanEventHeaders setType(String type) {
        this.type = type;
        return this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ApimanEventHeaders original) {
        return new Builder(original);
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private String id;
        private URI source;
        private String type;
        private String subject;
        private OffsetDateTime time = OffsetDateTime.now();
        private long eventVersion;
        private Map<String, Object> otherProperties = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        public Builder(ApimanEventHeaders original) {
            this.id = original.id;
            this.source = original.source;
            this.type = original.type;
            this.subject = original.subject;
            this.time = original.time;
            this.eventVersion = original.eventVersion;
            this.otherProperties = original.otherProperties;
        }

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

        /**
         * If you do not set this then the class' FQN name will be used.
         */
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

        /**
         * Version of the event these headers are associated with. If you do not set this then the
         * class' <tt>@EventVersion</tt> annotation will be used.
         */
        public Builder setEventVersion(long eventVersion) {
            this.eventVersion = eventVersion;
            return this;
        }

        public Builder addOtherProperty(String key, String value) {
            Objects.requireNonNull(key, "Header property key must not be null");
            this.otherProperties.put(key, value);
            return this;
        }

        public Builder setOtherProperties(Map<String, Object> propertiesIn) {
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
            //Objects.requireNonNull(type, "Type must be set");
            //Objects.requireNonNull(subject, "Subject must be set");
            Objects.requireNonNull(time, "Time must be set");
            // Preconditions.checkArgument(eventVersion > 0, "Version must be > 0");

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
