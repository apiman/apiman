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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
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
    
    // BOTH
    private Pattern stripBothKeyRegex;
    private Pattern stripBothValueRegex;
    
    // REQUEST
    private Pattern stripRequestKeyRegex;
    private Pattern stripRequestValueRegex;

    // RESPONSE
    private Pattern stripKeyResponseRegex;
    private Pattern stripValueResponseRegex;

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
    private Pattern getRequestKeyRegex() {
        if (stripRequestKeyRegex == null) {
            List<StripHeaderBean> keys = new ArrayList<>();

            for (StripHeaderBean bean : stripHeaders) {
                if (bean.getStripType() == StripHeaderBean.StripType.KEY &&
                        bean.getApplyTo() == AddHeaderBean.ApplyTo.REQUEST ||
                        bean.getApplyTo() == AddHeaderBean.ApplyTo.BOTH) {
                    keys.add(bean);
                }
            }

            stripRequestKeyRegex = buildRegex(keys);
        }
        return stripRequestKeyRegex;
    }

    /**
     * @return the keyRegex
     */
    private Pattern getRequestValueRegex() {
        if (stripRequestValueRegex == null) {
            List<StripHeaderBean> values = new ArrayList<>();

            for (StripHeaderBean bean : stripHeaders) {
                if (bean.getStripType() == StripHeaderBean.StripType.VALUE &&
                        bean.getApplyTo() == AddHeaderBean.ApplyTo.REQUEST ||
                        bean.getApplyTo() == AddHeaderBean.ApplyTo.BOTH) {
                    values.add(bean);
                }
            }

            stripRequestValueRegex = buildRegex(values);
        }
        return stripRequestValueRegex;
    }


    /**
     * @return the keyRegex
     */
    private Pattern getResponseKeyRegex() {
        if (stripKeyResponseRegex == null) {
            List<StripHeaderBean> keys = new ArrayList<>();

            for (StripHeaderBean bean : stripHeaders) {
                if (bean.getStripType() == StripHeaderBean.StripType.KEY &&
                        bean.getApplyTo() == AddHeaderBean.ApplyTo.RESPONSE ||
                        bean.getApplyTo() == AddHeaderBean.ApplyTo.BOTH) {
                    keys.add(bean);
                }
            }

            stripKeyResponseRegex = buildRegex(keys);
        }
        return stripKeyResponseRegex;
    }

    /**
     * @return the keyRegex
     */
    private Pattern getResponseValueRegex() {
        if (stripValueResponseRegex == null) {
            List<StripHeaderBean> values = new ArrayList<>();

            for (StripHeaderBean bean : stripHeaders) {
                if (bean.getStripType() == StripHeaderBean.StripType.VALUE &&
                        bean.getApplyTo() == AddHeaderBean.ApplyTo.RESPONSE ||
                        bean.getApplyTo() == AddHeaderBean.ApplyTo.BOTH) {
                    values.add(bean);
                }
            }

            stripValueResponseRegex = buildRegex(values);
        }
        return stripValueResponseRegex;
    }

    public Pattern getKeyRegex(AddHeaderBean.ApplyTo applyTo) {
        if (applyTo == AddHeaderBean.ApplyTo.REQUEST) {
            return getRequestKeyRegex();
        }
        return getResponseKeyRegex();
    }

    public Pattern getValueRegex(AddHeaderBean.ApplyTo applyTo) {
        if (applyTo == AddHeaderBean.ApplyTo.REQUEST) {
            return getRequestValueRegex();
        }
        return getResponseValueRegex();
    }
}
