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

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.apache.commons.lang3.Validate;

/**
 * Account signup event.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonDeserialize(builder = AccountSignupEvent.Builder.class)
@ApimanEvent(version = 1)
public class AccountSignupEvent implements IVersionedApimanEvent {
    private ApimanEventHeaders headers;
    private String userId;
    private String username;
    private String emailAddress;
    private String firstName;
    private String surname;
    private boolean approvalRequired;
    // TODO decide on all the attributes to use here, would be nice to capture everything that's not password

    private Map<String, Object> attributes; // TODO check for most appropriate type here
    // What details in here, hmm!

    AccountSignupEvent(ApimanEventHeaders headers, String userId, String username, String emailAddress,
         String firstName, String surname, boolean approvalRequired, Map<String, Object> attributes) {
        this.headers = headers;
        this.userId = userId;
        this.username = username;
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.surname = surname;
        this.approvalRequired = approvalRequired;
        this.attributes = attributes;
    }

    AccountSignupEvent() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public ApimanEventHeaders getHeaders() {
        return headers;
    }

    /**
     * Accountholder's user ID
     */
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private ApimanEventHeaders headers;
        private String userId;
        private String username;
        private String emailAddress;
        private String firstName;
        private String surname;
        private boolean approvalRequired;
        private final Map<String, Object> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        public Builder setHeaders(ApimanEventHeaders headers) {
            this.headers = headers;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder addAttribute(String name, Object value) {
            Validate.notBlank(name, "Attribute key must not be null or blank");
            this.attributes.put(name, value);
            return this;
        }

        public Builder setAttributes(Map<String, Object> attributesIn) {
            Objects.requireNonNull(attributesIn);
            if (attributesIn.containsKey(null)) {
                throw new IllegalArgumentException("Attribute key must not be null");
            }
            this.attributes.putAll(attributesIn);
            return this;
        }

        public Builder setApprovalRequired(boolean approvalRequired) {
            this.approvalRequired = approvalRequired;
            return this;
        }

        public AccountSignupEvent build() {
            Objects.requireNonNull(headers, "headers must be set");
            Validate.notBlank(userId, "userId must be set");
            Validate.notBlank(username, "Username must be set");
            Validate.notBlank(emailAddress, "email address must be set");
            Validate.notBlank(firstName, "firstName must be set");
            Validate.notBlank(surname, "surname must be set");

            return new AccountSignupEvent(headers, userId, username, emailAddress, firstName, surname, approvalRequired, attributes);
        }
    }
}
