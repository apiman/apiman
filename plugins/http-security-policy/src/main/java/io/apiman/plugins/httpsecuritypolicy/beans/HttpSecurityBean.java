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
 * HTTP Security Headers Configuration
 * 
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "hsts", "contentSecurityPolicy", "frameOptions", "xssProtection", "contentTypeOptions" })
@SuppressWarnings("nls")
public class HttpSecurityBean {

    /**
     * HTTP Strict Transport Security
     * <p>
     * Enforce transport security when using HTTP to mitigate a range of common web vulnerabilities.
     * 
     */
    @JsonProperty("hsts")
    private HstsBean hsts;
    /**
     * Content Security Policy
     * <p>
     * A sophisticated mechanism to precisely define the types and sources of content that may be loaded, with
     * violation reporting and the ability to restrict the availability and scope of many security-sensitive
     * features.
     * 
     */
    @JsonProperty("contentSecurityPolicy")
    private ContentSecurityPolicyBean contentSecurityPolicy;
    /**
     * Frame Options
     * <p>
     * Defines if, or how, a resource should be displayed in a frame, iframe or object.
     * 
     */
    @JsonProperty("frameOptions")
    private HttpSecurityBean.FrameOptions frameOptions = HttpSecurityBean.FrameOptions.fromValue("DISABLED");
    /**
     * XSS Protection
     * <p>
     * Enable or disable XSS filtering in the UA.
     * 
     */
    @JsonProperty("xssProtection")
    private HttpSecurityBean.XssProtection xssProtection = HttpSecurityBean.XssProtection
    .fromValue("DISABLED");
    /**
     * Content Type Options
     * <p>
     * X-Content-Type-Options: Prevent MIME-sniffing to any type other than the declared Content-Type.
     * 
     */
    @JsonProperty("contentTypeOptions")
    private Boolean contentTypeOptions = false;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * HTTP Strict Transport Security
     * <p>
     * Enforce transport security when using HTTP to mitigate a range of common web vulnerabilities.
     * 
     * @return The hsts
     */
    @JsonProperty("hsts")
    public HstsBean getHsts() {
        return hsts;
    }

    /**
     * HTTP Strict Transport Security
     * <p>
     * Enforce transport security when using HTTP to mitigate a range of common web vulnerabilities.
     * 
     * @param hsts The hsts
     */
    @JsonProperty("hsts")
    public void setHsts(HstsBean hsts) {
        this.hsts = hsts;
    }

    /**
     * Content Security Policy
     * <p>
     * A sophisticated mechanism to precisely define the types and sources of content that may be loaded, with
     * violation reporting and the ability to restrict the availability and scope of many security-sensitive
     * features.
     * 
     * @return The contentSecurityPolicy
     */
    @JsonProperty("contentSecurityPolicy")
    public ContentSecurityPolicyBean getContentSecurityPolicy() {
        return contentSecurityPolicy;
    }

    /**
     * Content Security Policy
     * <p>
     * A sophisticated mechanism to precisely define the types and sources of content that may be loaded, with
     * violation reporting and the ability to restrict the availability and scope of many security-sensitive
     * features.
     * 
     * @param contentSecurityPolicy The contentSecurityPolicy
     */
    @JsonProperty("contentSecurityPolicy")
    public void setContentSecurityPolicy(ContentSecurityPolicyBean contentSecurityPolicy) {
        this.contentSecurityPolicy = contentSecurityPolicy;
    }

    /**
     * Frame Options
     * <p>
     * Defines if, or how, a resource should be displayed in a frame, iframe or object.
     * 
     * @return The frameOptions
     */
    @JsonProperty("frameOptions")
    public HttpSecurityBean.FrameOptions getFrameOptions() {
        return frameOptions;
    }

    /**
     * Frame Options
     * <p>
     * Defines if, or how, a resource should be displayed in a frame, iframe or object.
     * 
     * @param frameOptions The frameOptions
     */
    @JsonProperty("frameOptions")
    public void setFrameOptions(HttpSecurityBean.FrameOptions frameOptions) {
        this.frameOptions = frameOptions;
    }

    /**
     * XSS Protection
     * <p>
     * Enable or disable XSS filtering in the UA.
     * 
     * @return The xssProtection
     */
    @JsonProperty("xssProtection")
    public HttpSecurityBean.XssProtection getXssProtection() {
        return xssProtection;
    }

    /**
     * XSS Protection
     * <p>
     * Enable or disable XSS filtering in the UA.
     * 
     * @param xssProtection The xssProtection
     */
    @JsonProperty("xssProtection")
    public void setXssProtection(HttpSecurityBean.XssProtection xssProtection) {
        this.xssProtection = xssProtection;
    }

    /**
     * Content Type Options
     * <p>
     * X-Content-Type-Options: Prevent MIME-sniffing to any type other than the declared Content-Type.
     * 
     * @return The contentTypeOptions
     */
    @JsonProperty("contentTypeOptions")
    public Boolean getContentTypeOptions() {
        return contentTypeOptions;
    }

    /**
     * Content Type Options
     * <p>
     * X-Content-Type-Options: Prevent MIME-sniffing to any type other than the declared Content-Type.
     * 
     * @param contentTypeOptions The contentTypeOptions
     */
    @JsonProperty("contentTypeOptions")
    public void setContentTypeOptions(Boolean contentTypeOptions) {
        this.contentTypeOptions = contentTypeOptions;
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
        return new HashCodeBuilder().append(hsts).append(contentSecurityPolicy).append(frameOptions)
                .append(xssProtection).append(contentTypeOptions).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof HttpSecurityBean) == false) {
            return false;
        }
        HttpSecurityBean rhs = ((HttpSecurityBean) other);
        return new EqualsBuilder().append(hsts, rhs.hsts)
                .append(contentSecurityPolicy, rhs.contentSecurityPolicy)
                .append(frameOptions, rhs.frameOptions).append(xssProtection, rhs.xssProtection)
                .append(contentTypeOptions, rhs.contentTypeOptions)
                .append(additionalProperties, rhs.additionalProperties).isEquals();
    }

    @Generated("org.jsonschema2pojo")
    public static enum FrameOptions {

        DENY("DENY"), SAMEORIGIN("SAMEORIGIN"), DISABLED("DISABLED");
        private final String value;
        private static Map<String, HttpSecurityBean.FrameOptions> constants = new HashMap<>();

        static {
            for (HttpSecurityBean.FrameOptions c : values()) {
                constants.put(c.value, c);
            }
        }

        private FrameOptions(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static HttpSecurityBean.FrameOptions fromValue(String value) {
            HttpSecurityBean.FrameOptions constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("org.jsonschema2pojo")
    public static enum XssProtection {

        OFF("OFF", "0"), ON("ON", "1"), BLOCK("BLOCK", "1; mode=block"), DISABLED("DISABLED", "DISABLED");
        private final String value;
        private final String realValue;
        
        private static Map<String, HttpSecurityBean.XssProtection> constants = new HashMap<>();

        static {
            for (HttpSecurityBean.XssProtection c : values()) {
                constants.put(c.value, c);
            }
        }

        private XssProtection(String value, String realValue) {
            this.value = value;
            this.realValue = realValue;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.realValue;
        }

        @JsonCreator
        public static HttpSecurityBean.XssProtection fromValue(String value) {
            HttpSecurityBean.XssProtection constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
