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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * HTTP Strict Transport Security
 * <p>
 * Enforce transport security when using HTTP to mitigate a range of common web vulnerabilities.
 * 
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "enabled", "includeSubdomains", "maxAge", "preload" })
public class HstsBean {

    /**
     * Enable HTTP Strict Transport (HSTS)
     */
    @JsonProperty("enabled")
    private Boolean enabled = false;
    /**
     * Include Subdomains
     */
    @JsonProperty("includeSubdomains")
    private Boolean includeSubdomains = false;
    /**
     * Maximum Age
     * <p>
     * Delta seconds user agents should cache HSTS status for.
     */
    @JsonProperty("maxAge")
    private Integer maxAge = 0;
    /**
     * Enable HSTS Preload Flag
     * <p>
     * Flag to verify HSTS preload status. Popular browsers contain a hard-coded (pinned) list of domains and
     * certificates, which they always connect securely with. This mitigates a wide range of identity and
     * MIITM attacks, and is particularly useful for high-profile domains. Users must submit a request for
     * their domain to be included in the scheme.
     * 
     */
    @JsonProperty("preload")
    private Boolean preload = false;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private String headerValue;

    /**
     * Enable HTTP Strict Transport (HSTS)
     * 
     * @return The enabled
     */
    @JsonProperty("enabled")
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable HTTP Strict Transport (HSTS)
     * 
     * @param enabled The enabled
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Include Subdomains
     *
     * @return The includeSubdomains
     */
    @JsonProperty("includeSubdomains")
    public Boolean getIncludeSubdomains() {
        return includeSubdomains;
    }

    /**
     * Include Subdomains
     * 
     * @param includeSubdomains The includeSubdomains
     */
    @JsonProperty("includeSubdomains")
    public void setIncludeSubdomains(Boolean includeSubdomains) {
        this.includeSubdomains = includeSubdomains;
    }

    /**
     * Maximum Age
     * <p>
     * Delta seconds user agents should cache HSTS status for.
     * 
     * @return The maxAge
     */
    @JsonProperty("maxAge")
    public Integer getMaxAge() {
        return maxAge;
    }

    /**
     * Maximum Age
     * <p>
     * Delta seconds user agents should cache HSTS status for.
     * 
     * @param maxAge The maxAge
     */
    @JsonProperty("maxAge")
    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Enable HSTS Preload Flag
     * <p>
     * Flag to verify HSTS preload status. Popular browsers contain a hard-coded (pinned) list of domains and
     * certificates, which they always connect securely with. This mitigates a wide range of identity and
     * MIITM attacks, and is particularly useful for high-profile domains. Users must submit a request for
     * their domain to be included in the scheme.
     * 
     * @return The preload
     */
    @JsonProperty("preload")
    public Boolean getPreload() {
        return preload;
    }

    /**
     * Enable HSTS Preload Flag
     * <p>
     * Flag to verify HSTS preload status. Popular browsers contain a hard-coded (pinned) list of domains and
     * certificates, which they always connect securely with. This mitigates a wide range of identity and
     * MIITM attacks, and is particularly useful for high-profile domains. Users must submit a request for
     * their domain to be included in the scheme.
     * 
     * @param preload The preload
     */
    @JsonProperty("preload")
    public void setPreload(Boolean preload) {
        this.preload = preload;
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
        return new HashCodeBuilder().append(enabled).append(includeSubdomains).append(maxAge).append(preload)
                .append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof HstsBean) == false) {
            return false;
        }
        HstsBean rhs = ((HstsBean) other);
        return new EqualsBuilder().append(enabled, rhs.enabled)
                .append(includeSubdomains, rhs.includeSubdomains).append(maxAge, rhs.maxAge)
                .append(preload, rhs.preload).append(additionalProperties, rhs.additionalProperties)
                .isEquals();
    }

    @SuppressWarnings("nls")
    public String getHeaderValue() {
        if (headerValue == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("max-age=" + getMaxAge().toString());

            if (getIncludeSubdomains()) {
                sb.append("; includeSubDomains");
            }
            if (getPreload()) {
                sb.append("; preload");
            }

            headerValue = sb.toString();
        }
        return headerValue;
    }
}
