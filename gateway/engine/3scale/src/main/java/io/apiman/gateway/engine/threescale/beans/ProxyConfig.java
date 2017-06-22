
package io.apiman.gateway.engine.threescale.beans;

import java.io.Serializable;
import java.util.HashMap;
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
    "version",
    "environment",
    "content"
})
public class ProxyConfig implements Serializable
{

    @JsonProperty("version")
    private long version;
    @JsonProperty("environment")
    private String environment;
    @JsonProperty("content")
    private BackendConfiguration content;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = 4009643270463245312L;

    @JsonProperty("version")
    public long getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(long version) {
        this.version = version;
    }

    public ProxyConfig withVersion(long version) {
        this.version = version;
        return this;
    }

    @JsonProperty("environment")
    public String getEnvironment() {
        return environment;
    }

    @JsonProperty("environment")
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public ProxyConfig withEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    @JsonProperty("content")
    public BackendConfiguration getBackendConfig() {
        return content;
    }

    @JsonProperty("content")
    public void setBackendConfig(BackendConfiguration content) {
        this.content = content;
    }

    public ProxyConfig withBackendConfig(BackendConfiguration content) {
        this.content = content;
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

    public ProxyConfig withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(version).append(environment).append(content).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProxyConfig) == false) {
            return false;
        }
        ProxyConfig rhs = ((ProxyConfig) other);
        return new EqualsBuilder().append(version, rhs.version).append(environment, rhs.environment).append(content, rhs.content)
                .append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
