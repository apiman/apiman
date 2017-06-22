
package io.apiman.gateway.engine.threescale.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "state",
    "system_name",
    "end_user_registration_required",
    "created_at",
    "updated_at",
    "links"
})
public class Service_ implements Serializable
{

    @JsonProperty("id")
    private long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("state")
    private String state;
    @JsonProperty("system_name")
    private String systemName;
    @JsonProperty("end_user_registration_required")
    private boolean endUserRegistrationRequired;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("links")
    private List<Link> links = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = 6124232753563615888L;

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(long id) {
        this.id = id;
    }

    public Service_ withId(long id) {
        this.id = id;
        return this;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public Service_ withName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    public Service_ withState(String state) {
        this.state = state;
        return this;
    }

    @JsonProperty("system_name")
    public String getSystemName() {
        return systemName;
    }

    @JsonProperty("system_name")
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public Service_ withSystemName(String systemName) {
        this.systemName = systemName;
        return this;
    }

    @JsonProperty("end_user_registration_required")
    public boolean isEndUserRegistrationRequired() {
        return endUserRegistrationRequired;
    }

    @JsonProperty("end_user_registration_required")
    public void setEndUserRegistrationRequired(boolean endUserRegistrationRequired) {
        this.endUserRegistrationRequired = endUserRegistrationRequired;
    }

    public Service_ withEndUserRegistrationRequired(boolean endUserRegistrationRequired) {
        this.endUserRegistrationRequired = endUserRegistrationRequired;
        return this;
    }

    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Service_ withCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Service_ withUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @JsonProperty("links")
    public List<Link> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public Service_ withLinks(List<Link> links) {
        this.links = links;
        return this;
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

    public Service_ withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(name).append(state).append(systemName).append(endUserRegistrationRequired).append(createdAt).append(updatedAt).append(links).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Service_) == false) {
            return false;
        }
        Service_ rhs = ((Service_) other);
        return new EqualsBuilder().append(id, rhs.id).append(name, rhs.name).append(state, rhs.state).append(systemName, rhs.systemName).append(endUserRegistrationRequired, rhs.endUserRegistrationRequired).append(createdAt, rhs.createdAt).append(updatedAt, rhs.updatedAt).append(links, rhs.links).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
