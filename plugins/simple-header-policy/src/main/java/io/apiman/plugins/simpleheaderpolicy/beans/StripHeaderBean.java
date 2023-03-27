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
package io.apiman.plugins.simpleheaderpolicy.beans;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Strip Headers
 * 
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "stripType", "with", "pattern", "applyTo" })
@SuppressWarnings("nls")
public class StripHeaderBean {

    /**
     * Strip Header(s) That Match
     */
    @JsonProperty("stripType")
    private StripHeaderBean.StripType stripType;
    /**
     * With Matcher Type
     */
    @JsonProperty("with")
    private StripHeaderBean.With with;
    /**
     * Pattern
     */
    @JsonProperty("pattern")
    private String pattern;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Apply To
     */
    @JsonProperty("applyTo")
    private AddHeaderBean.ApplyTo applyTo = AddHeaderBean.ApplyTo.BOTH;

    /**
     * Strip Header(s) That Match
     * 
     * @return The stripType
     */
    @JsonProperty("stripType")
    public StripHeaderBean.StripType getStripType() {
        return stripType;
    }

    /**
     * Strip Header(s) That Match
     * 
     * @param stripType The stripType
     */
    @JsonProperty("stripType")
    public void setStripType(StripHeaderBean.StripType stripType) {
        this.stripType = stripType;
    }

    /**
     * With Matcher Type
     * 
     * @return The with
     */
    @JsonProperty("with")
    public StripHeaderBean.With getWith() {
        return with;
    }

    /**
     * With Matcher Type
     * 
     * @param with The with
     */
    @JsonProperty("with")
    public void setWith(StripHeaderBean.With with) {
        this.with = with;
    }

    /**
     * Pattern
     * 
     * @return The pattern
     */
    @JsonProperty("pattern")
    public String getPattern() {
        return pattern;
    }

    /**
     * Pattern
     * 
     * @param pattern The pattern
     */
    @JsonProperty("pattern")
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Apply To
     * <p>
     *
     *
     * @return The applyTo
     */
    @JsonProperty("applyTo")
    public AddHeaderBean.ApplyTo getApplyTo() {
        return applyTo;
    }

    /**
     * Apply To
     * <p>
     *
     *
     * @param applyTo The applyTo
     */
    @JsonProperty("applyTo")
    public void setApplyTo(AddHeaderBean.ApplyTo applyTo) {
        this.applyTo = applyTo;
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
        return new HashCodeBuilder().append(stripType).append(with).append(pattern)
                .append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StripHeaderBean) == false) {
            return false;
        }
        StripHeaderBean rhs = ((StripHeaderBean) other);
        return new EqualsBuilder().append(stripType, rhs.stripType).append(with, rhs.with)
                .append(pattern, rhs.pattern).append(additionalProperties, rhs.additionalProperties)
                .isEquals();
    }

    @Generated("org.jsonschema2pojo")
    public static enum StripType {

        KEY("Key"), VALUE("Value");
        private final String value;
        private static Map<String, StripHeaderBean.StripType> constants = new HashMap<>();

        static {
            for (StripHeaderBean.StripType c : values()) {
                constants.put(c.value, c);
            }
        }

        private StripType(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static StripHeaderBean.StripType fromValue(String value) {
            StripHeaderBean.StripType constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("org.jsonschema2pojo")
    public static enum With {

        STRING("String"), REGEX("Regex");
        private final String value;
        private static Map<String, StripHeaderBean.With> constants = new HashMap<>();

        static {
            for (StripHeaderBean.With c : values()) {
                constants.put(c.value, c);
            }
        }

        private With(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static StripHeaderBean.With fromValue(String value) {
            StripHeaderBean.With constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
