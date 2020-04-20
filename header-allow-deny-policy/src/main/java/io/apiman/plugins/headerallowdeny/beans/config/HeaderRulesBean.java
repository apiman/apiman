package io.apiman.plugins.headerallowdeny.beans.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Header entry rules bean.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"allowRequest", "headerValueRegex"})
public class HeaderRulesBean {
    @JsonProperty("headerValueRegex")
    private String headerValueRegex;

    @JsonProperty("allowRequest")
    private boolean allowRequest = false;

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(headerValueRegex)
                .append(allowRequest)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof HeaderRulesBean)) {
            return false;
        }
        final HeaderRulesBean rhs = ((HeaderRulesBean) other);
        return new EqualsBuilder()
                .append(headerValueRegex, rhs.headerValueRegex)
                .append(allowRequest, rhs.allowRequest)
                .isEquals();
    }

    public String getHeaderValueRegex() {
        return headerValueRegex;
    }

    public boolean isAllowRequest() {
        return allowRequest;
    }
}
