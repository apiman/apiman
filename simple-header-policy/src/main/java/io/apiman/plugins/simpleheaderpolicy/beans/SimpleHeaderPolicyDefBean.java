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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Generated;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author Marc Savy <msavy@redhat.com>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "addHeaders", "stripHeaders" })
public class SimpleHeaderPolicyDefBean {

    /**
     * Add Headers
     */
    @JsonProperty("addHeaders")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<AddHeaderBean> addHeaders = new LinkedHashSet<>();
    /**
     * Strip Headers
     * <p>
     * Removes header key and value pairs when pattern matches.
     */
    @JsonProperty("stripHeaders")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<StripHeaderBean> stripHeaders = new LinkedHashSet<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private Pattern stripKeyRegex;
    private Pattern stripValueRegex;

    /**
     * Add Headers
     * 
     * @return The addHeaders
     */
    @JsonProperty("addHeaders")
    public Set<AddHeaderBean> getAddHeaders() {
        return addHeaders;
    }

    /**
     * Add Headers
     * 
     * @param addHeaders The addHeaders
     */
    @JsonProperty("addHeaders")
    public void setAddHeaders(Set<AddHeaderBean> addHeaders) {
        this.addHeaders = addHeaders;
    }

    /**
     * Strip Headers
     * <p>
     * Removes header key and value pairs when pattern matches.
     * 
     * @return The stripHeaders
     */
    @JsonProperty("stripHeaders")
    public Set<StripHeaderBean> getStripHeaders() {
        return stripHeaders;
    }

    /**
     * Strip Headers
     * <p>
     * Removes header key and value pairs when pattern matches.
     * 
     * @param stripHeaders The stripHeaders
     */
    @JsonProperty("stripHeaders")
    public void setStripHeaders(Set<StripHeaderBean> stripHeaders) {
        this.stripHeaders = stripHeaders;
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

    @SuppressWarnings("nls")
    private Pattern buildRegex(List<StripHeaderBean> itemList) {
        StringBuilder sb = new StringBuilder();
        String divider = "";

        for (StripHeaderBean stripHeader : itemList) {
            String pattern = StringUtils.strip(stripHeader.getPattern());
            sb.append(divider);
            sb.append(pattern);
            divider = "|";
        }

        return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(addHeaders).append(stripHeaders).append(additionalProperties)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SimpleHeaderPolicyDefBean) == false) {
            return false;
        }
        SimpleHeaderPolicyDefBean rhs = ((SimpleHeaderPolicyDefBean) other);
        return new EqualsBuilder().append(addHeaders, rhs.addHeaders).append(stripHeaders, rhs.stripHeaders)
                .append(additionalProperties, rhs.additionalProperties).isEquals();
    }

    /**
     * @return the keyRegex
     */
    public Pattern getKeyRegex() {
        if (stripKeyRegex == null) {
            List<StripHeaderBean> keys = new ArrayList<>();

            for (StripHeaderBean bean : stripHeaders) {
                if (bean.getStripType() == StripHeaderBean.StripType.KEY) {
                    keys.add(bean);
                }
            }

            stripKeyRegex = buildRegex(keys);
        }
        return stripKeyRegex;
    }

    /**
     * @return the keyRegex
     */
    public Pattern getValueRegex() {
        if (stripValueRegex == null) {
            List<StripHeaderBean> values = new ArrayList<>();

            for (StripHeaderBean bean : stripHeaders) {
                if (bean.getStripType() == StripHeaderBean.StripType.VALUE) {
                    values.add(bean);
                }
            }

            stripValueRegex = buildRegex(values);
        }
        return stripValueRegex;
    }
}
