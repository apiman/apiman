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
package io.apiman.plugins.httpsecuritypolicy.beans;

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
 * Content Security Policy
 * <p>
 * A sophisticated mechanism to precisely define the types and sources of content that may be loaded, with
 * violation reporting and the ability to restrict the availability and scope of many security-sensitive
 * features.
 * 
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "mode", "csp" })
@SuppressWarnings("nls")
public class ContentSecurityPolicyBean {

    /**
     * CSP Mode
     */
    @JsonProperty("mode")
    private ContentSecurityPolicyBean.Mode mode = ContentSecurityPolicyBean.Mode.fromValue("DISABLED");
    /**
     * Content Security Policy Definition
     * <p>
     * Valid CSP definition must be provided.
     * 
     */
    @JsonProperty("csp")
    private String csp;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * CSP Mode
     * <p>
     * 
     * 
     * @return The mode
     */
    @JsonProperty("mode")
    public ContentSecurityPolicyBean.Mode getMode() {
        return mode;
    }

    /**
     * CSP Mode
     * <p>
     * 
     * 
     * @param mode The mode
     */
    @JsonProperty("mode")
    public void setMode(ContentSecurityPolicyBean.Mode mode) {
        this.mode = mode;
    }

    /**
     * Content Security Policy Definition
     * <p>
     * Valid CSP definition must be provided.
     * 
     * @return The csp
     */
    @JsonProperty("csp")
    public String getCsp() {
        return csp;
    }

    /**
     * Content Security Policy Definition
     * <p>
     * Valid CSP definition must be provided.
     * 
     * @param csp The csp
     */
    @JsonProperty("csp")
    public void setCsp(String csp) {
        this.csp = csp;
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
        return new HashCodeBuilder().append(mode).append(csp).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ContentSecurityPolicyBean) == false) {
            return false;
        }
        ContentSecurityPolicyBean rhs = ((ContentSecurityPolicyBean) other);
        return new EqualsBuilder().append(mode, rhs.mode).append(csp, rhs.csp)
                .append(additionalProperties, rhs.additionalProperties).isEquals();
    }

    @Generated("org.jsonschema2pojo")
    public static enum Mode {

        ENABLED("ENABLED"), REPORT_ONLY("REPORT_ONLY"), DISABLED("DISABLED");
        private final String value;
        private static Map<String, ContentSecurityPolicyBean.Mode> constants = new HashMap<>();

        static {
            for (ContentSecurityPolicyBean.Mode c : values()) {
                constants.put(c.value, c);
            }
        }

        private Mode(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static ContentSecurityPolicyBean.Mode fromValue(String value) {
            ContentSecurityPolicyBean.Mode constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
