package io.apiman.plugins.headerallowdeny.beans.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configuration for the Header Allow/Deny Policy.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"entries"})
public class HeaderAllowDenyBean implements Serializable {
    private static final long serialVersionUID = 5141852524352883648L;

    @JsonProperty("entries")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<HeaderEntryBean> entries = new LinkedHashSet<>();

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(entries)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof HeaderAllowDenyBean)) {
            return false;
        }
        final HeaderAllowDenyBean rhs = ((HeaderAllowDenyBean) other);
        return new EqualsBuilder()
                .append(entries, rhs.entries)
                .isEquals();
    }

    public Set<HeaderEntryBean> getEntries() {
        return entries;
    }
}
