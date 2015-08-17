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
 * Header
 * 
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "headerName", "headerValue", "valueType", "applyTo", "overwrite" })
@SuppressWarnings("nls")
public class AddHeaderBean {

    /**
     * Header Name
     */
    @JsonProperty("headerName")
    private String headerName;

    /**
     * Header Value
     */
    @JsonProperty("headerValue")
    private String headerValue;

    /**
     * Value Type
     */
    @JsonProperty("valueType")
    private AddHeaderBean.ValueType valueType;

    /**
     * Apply To
     */
    @JsonProperty("applyTo")
    private AddHeaderBean.ApplyTo applyTo;
    /**
     * Overwrite Existing
     */
    @JsonProperty("overwrite")
    private Boolean overwrite = false;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Header Name
     * <p>
     * 
     * 
     * @return The headerName
     */
    @JsonProperty("headerName")
    public String getHeaderName() {
        return headerName;
    }

    /**
     * Header Name
     * <p>
     * 
     * 
     * @param headerName The headerName
     */
    @JsonProperty("headerName")
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * Header Value
     * <p>
     * 
     * 
     * @return The headerValue
     */
    @JsonProperty("headerValue")
    public String getHeaderValue() {
        return headerValue;
    }

    /**
     * Header Value
     * <p>
     * 
     * 
     * @param headerValue The headerValue
     */
    @JsonProperty("headerValue")
    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }

    /**
     * Value Type
     * <p>
     * 
     * 
     * @return The valueType
     */
    @JsonProperty("valueType")
    public AddHeaderBean.ValueType getValueType() {
        return valueType;
    }

    /**
     * Value Type
     * <p>
     * 
     * 
     * @param valueType The valueType
     */
    @JsonProperty("valueType")
    public void setValueType(AddHeaderBean.ValueType valueType) {
        this.valueType = valueType;
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

    /**
     * Overwrite Existing
     * <p>
     * 
     * 
     * @return The overwrite
     */
    @JsonProperty("overwrite")
    public Boolean getOverwrite() {
        return overwrite;
    }

    /**
     * Overwrite Existing
     * <p>
     * 
     * 
     * @param overwrite The overwrite
     */
    @JsonProperty("overwrite")
    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
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
        return new HashCodeBuilder().append(headerName).append(headerValue).append(applyTo).append(overwrite)
                .append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AddHeaderBean) == false) {
            return false;
        }
        AddHeaderBean rhs = ((AddHeaderBean) other);
        return new EqualsBuilder().append(headerName, rhs.headerName).append(headerValue, rhs.headerValue)
                .append(applyTo, rhs.applyTo).append(overwrite, rhs.overwrite)
                .append(additionalProperties, rhs.additionalProperties).isEquals();
    }

    @Generated("org.jsonschema2pojo")
    public static enum ApplyTo {

        REQUEST("Request"), RESPONSE("Response"), BOTH("Both");
        private final String value;
        private static Map<String, AddHeaderBean.ApplyTo> constants = new HashMap<>();

        static {
            for (AddHeaderBean.ApplyTo c : values()) {
                constants.put(c.value, c);
            }
        }

        private ApplyTo(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static AddHeaderBean.ApplyTo fromValue(String value) {
            AddHeaderBean.ApplyTo constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public static enum ValueType {

        STRING("String"), ENV("Env"), SYS("System Properties");
        private final String value;
        private static Map<String, AddHeaderBean.ValueType> constants = new HashMap<>();

        static {
            for (AddHeaderBean.ValueType c : values()) {
                constants.put(c.value, c);
            }
        }

        private ValueType(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static AddHeaderBean.ValueType fromValue(String value) {
            AddHeaderBean.ValueType constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public String getResolvedHeaderValue() {
        String rVal = null;

        switch (getValueType()) {
        case ENV:
            rVal = System.getenv(headerValue);
            break;
        case SYS:
            rVal = System.getProperty(headerValue);
            break;
        case STRING:
            rVal = headerValue;
        }

        return (rVal == null) ? "" : rVal;
    }
}
