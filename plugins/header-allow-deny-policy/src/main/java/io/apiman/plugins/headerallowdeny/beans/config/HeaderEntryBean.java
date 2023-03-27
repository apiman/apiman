package io.apiman.plugins.headerallowdeny.beans.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;

/**
 * Header entry configuration bean.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"headerName", "allowIfHeaderMissing", "allowIfNoRulesMatch", "rules"})
public class HeaderEntryBean {
    @JsonProperty("headerName")
    private String headerName;

    @JsonProperty("allowIfHeaderMissing")
    private boolean allowIfHeaderMissing = false;

    @JsonProperty("allowIfNoRulesMatch")
    private boolean allowIfNoRulesMatch = false;

    @JsonProperty("rules")
    private List<HeaderRulesBean> rules;

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(headerName)
                .append(allowIfHeaderMissing)
                .append(allowIfNoRulesMatch)
                .append(rules)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof HeaderEntryBean)) {
            return false;
        }
        final HeaderEntryBean rhs = ((HeaderEntryBean) other);
        return new EqualsBuilder()
                .append(headerName, rhs.headerName)
                .append(allowIfHeaderMissing, rhs.allowIfHeaderMissing)
                .append(allowIfNoRulesMatch, rhs.allowIfNoRulesMatch)
                .append(rules, rhs.rules)
                .isEquals();
    }

    public String getHeaderName() {
        return headerName;
    }

    public boolean isAllowIfHeaderMissing() {
        return allowIfHeaderMissing;
    }

    public boolean isAllowIfNoRulesMatch() {
        return allowIfNoRulesMatch;
    }

    public List<HeaderRulesBean> getRules() {
        return rules;
    }
}
