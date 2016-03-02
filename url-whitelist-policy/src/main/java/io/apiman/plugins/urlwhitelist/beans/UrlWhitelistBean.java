package io.apiman.plugins.urlwhitelist.beans;

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
 * Configuration for the URL Whitelist Policy.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"removePathPrefix", "whitelist"})
public class UrlWhitelistBean implements Serializable {
    @JsonProperty("whitelist")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<WhitelistEntryBean> whitelist = new LinkedHashSet<>();

    @JsonProperty("removePathPrefix")
    private boolean removePathPrefix = true;

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(whitelist).append(removePathPrefix)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UrlWhitelistBean)) {
            return false;
        }
        UrlWhitelistBean rhs = ((UrlWhitelistBean) other);
        return new EqualsBuilder().append(whitelist, rhs.whitelist).append(removePathPrefix, rhs.removePathPrefix).isEquals();
    }

    public Set<WhitelistEntryBean> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(Set<WhitelistEntryBean> whitelist) {
        this.whitelist = whitelist;
    }

    public boolean isRemovePathPrefix() {
        return removePathPrefix;
    }

    public void setRemovePathPrefix(boolean removePathPrefix) {
        this.removePathPrefix = removePathPrefix;
    }
}
