package io.apiman.plugins.urlwhitelist.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"regex", "methodGet", "methodPost", "methodPut", "methodPatch", "methodDelete"})
public class WhitelistEntryBean {
    @JsonProperty("regex")
    private String regex;

    @JsonProperty("methodGet")
    private boolean methodGet = false;

    @JsonProperty("methodPost")
    private boolean methodPost = false;

    @JsonProperty("methodPut")
    private boolean methodPut = false;

    @JsonProperty("methodPatch")
    private boolean methodPatch = false;

    @JsonProperty("methodDelete")
    private boolean methodDelete = false;

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(regex).append(methodGet).append(methodPost).append(methodDelete)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WhitelistEntryBean)) {
            return false;
        }
        WhitelistEntryBean rhs = ((WhitelistEntryBean) other);
        return new EqualsBuilder().append(regex, rhs.regex).append(methodGet, rhs.methodGet)
                .append(methodPost, rhs.methodPost).append(methodDelete, rhs.methodDelete).isEquals();
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public boolean isMethodGet() {
        return methodGet;
    }

    public void setMethodGet(boolean methodGet) {
        this.methodGet = methodGet;
    }

    public boolean isMethodPost() {
        return methodPost;
    }

    public void setMethodPost(boolean methodPost) {
        this.methodPost = methodPost;
    }

    public boolean isMethodPut() {
        return methodPut;
    }

    public void setMethodPut(boolean methodPut) {
        this.methodPut = methodPut;
    }

    public boolean isMethodPatch() {
        return methodPatch;
    }

    public void setMethodPatch(boolean methodPatch) {
        this.methodPatch = methodPatch;
    }

    public boolean isMethodDelete() {
        return methodDelete;
    }

    public void setMethodDelete(boolean methodDelete) {
        this.methodDelete = methodDelete;
    }
}
