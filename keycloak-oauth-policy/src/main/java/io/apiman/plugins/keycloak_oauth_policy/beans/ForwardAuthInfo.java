/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.plugins.keycloak_oauth_policy.beans;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Header to token property mappings.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({ "headers", "field" })
public class ForwardAuthInfo {

    @JsonProperty("headers")
    private String headers;

    @JsonProperty("field")
    private ForwardAuthInfo.Field field;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * @return The headers
     */
    @JsonProperty("headers")
    public String getHeader() {
        return headers;
    }

    /**
     * @param headers The headers
     */
    @JsonProperty("headers")
    public void setHeaders(String headers) {
        this.headers = headers;
    }

    /**
     * @return The field
     */
    @JsonProperty("field")
    public ForwardAuthInfo.Field getField() {
        return field;
    }

    /**
     * @param field The field
     */
    @JsonProperty("field")
    public void setField(ForwardAuthInfo.Field field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(headers).append(field).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ForwardAuthInfo) == false) {
            return false;
        }
        ForwardAuthInfo rhs = ((ForwardAuthInfo) other);
        return new EqualsBuilder().append(headers, rhs.headers).append(field, rhs.field)
                .append(additionalProperties, rhs.additionalProperties).isEquals();
    }

    @Generated("org.jsonschema2pojo")
    public static enum Field {

        SUBJECT("subject"), //$NON-NLS-1$
        USERNAME("username"), //$NON-NLS-1$
        EMAIL("email"), //$NON-NLS-1$
        NAME("name"), //$NON-NLS-1$
        ACCESS_TOKEN("access_token"); //$NON-NLS-1$
        private final String value;
        private static Map<String, ForwardAuthInfo.Field> constants = new HashMap<>();

        static {
            for (ForwardAuthInfo.Field c : values()) {
                constants.put(c.value, c);
            }
        }

        private Field(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static ForwardAuthInfo.Field fromValue(String value) {
            ForwardAuthInfo.Field constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
