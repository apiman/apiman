package io.apiman.plugins.uniqueheader.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Configuration for the Unique Header Policy.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"headerName"})
public class UniqueHeaderBean implements Serializable {
    /**
     * The name of the HTTP Header to set.
     */
    @JsonProperty("headerName")
    private String headerName;

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(headerName)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UniqueHeaderBean)) {
            return false;
        }
        final UniqueHeaderBean rhs = ((UniqueHeaderBean) other);
        return new EqualsBuilder()
                .append(headerName, rhs.headerName)
                .isEquals();
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
}
