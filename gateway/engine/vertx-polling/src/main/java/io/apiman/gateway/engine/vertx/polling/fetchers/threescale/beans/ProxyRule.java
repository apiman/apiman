
package io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans;

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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "proxy_id",
    "http_method",
    "pattern",
    "metric_id",
    "metric_system_name",
    "delta",
    "tenant_id",
    "created_at",
    "updated_at",
    "redirect_url",
    "parameters",
    "querystring_parameters"
})
public class ProxyRule implements Serializable
{

    @JsonProperty("id")
    private long id;
    @JsonProperty("proxy_id")
    private long proxyId;
    @JsonProperty("http_method")
    private String httpMethod;
    @JsonProperty("pattern")
    private String pattern;
    @JsonProperty("metric_id")
    private long metricId;
    @JsonProperty("metric_system_name")
    private String metricSystemName;
    @JsonProperty("delta")
    private long delta;
    @JsonProperty("tenant_id")
    private long tenantId;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("redirect_url")
    private Object redirectUrl;
    @JsonProperty("parameters")
    private List<String> parameters = null;
    @JsonProperty("querystring_parameters")
    private QuerystringParameters querystringParameters;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = 5993748206678997809L;

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(long id) {
        this.id = id;
    }

    public ProxyRule withId(long id) {
        this.id = id;
        return this;
    }

    @JsonProperty("proxy_id")
    public long getProxyId() {
        return proxyId;
    }

    @JsonProperty("proxy_id")
    public void setProxyId(long proxyId) {
        this.proxyId = proxyId;
    }

    public ProxyRule withProxyId(long proxyId) {
        this.proxyId = proxyId;
        return this;
    }

    @JsonProperty("http_method")
    public String getHttpMethod() {
        return httpMethod;
    }

    @JsonProperty("http_method")
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public ProxyRule withHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    @JsonProperty("pattern")
    public String getPattern() {
        return pattern;
    }

    @JsonProperty("pattern")
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public ProxyRule withPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    @JsonProperty("metric_id")
    public long getMetricId() {
        return metricId;
    }

    @JsonProperty("metric_id")
    public void setMetricId(long metricId) {
        this.metricId = metricId;
    }

    public ProxyRule withMetricId(long metricId) {
        this.metricId = metricId;
        return this;
    }

    @JsonProperty("metric_system_name")
    public String getMetricSystemName() {
        return metricSystemName;
    }

    @JsonProperty("metric_system_name")
    public void setMetricSystemName(String metricSystemName) {
        this.metricSystemName = metricSystemName;
    }

    public ProxyRule withMetricSystemName(String metricSystemName) {
        this.metricSystemName = metricSystemName;
        return this;
    }

    @JsonProperty("delta")
    public long getDelta() {
        return delta;
    }

    @JsonProperty("delta")
    public void setDelta(long delta) {
        this.delta = delta;
    }

    public ProxyRule withDelta(long delta) {
        this.delta = delta;
        return this;
    }

    @JsonProperty("tenant_id")
    public long getTenantId() {
        return tenantId;
    }

    @JsonProperty("tenant_id")
    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public ProxyRule withTenantId(long tenantId) {
        this.tenantId = tenantId;
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

    public ProxyRule withCreatedAt(String createdAt) {
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

    public ProxyRule withUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @JsonProperty("redirect_url")
    public Object getRedirectUrl() {
        return redirectUrl;
    }

    @JsonProperty("redirect_url")
    public void setRedirectUrl(Object redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public ProxyRule withRedirectUrl(Object redirectUrl) {
        this.redirectUrl = redirectUrl;
        return this;
    }

    @JsonProperty("parameters")
    public List<String> getParameters() {
        return parameters;
    }

    @JsonProperty("parameters")
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public ProxyRule withParameters(List<String> parameters) {
        this.parameters = parameters;
        return this;
    }

    @JsonProperty("querystring_parameters")
    public QuerystringParameters getQuerystringParameters() {
        return querystringParameters;
    }

    @JsonProperty("querystring_parameters")
    public void setQuerystringParameters(QuerystringParameters querystringParameters) {
        this.querystringParameters = querystringParameters;
    }

    public ProxyRule withQuerystringParameters(QuerystringParameters querystringParameters) {
        this.querystringParameters = querystringParameters;
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

    public ProxyRule withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(proxyId).append(httpMethod).append(pattern).append(metricId).append(metricSystemName).append(delta).append(tenantId).append(createdAt).append(updatedAt).append(redirectUrl).append(parameters).append(querystringParameters).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProxyRule) == false) {
            return false;
        }
        ProxyRule rhs = ((ProxyRule) other);
        return new EqualsBuilder().append(id, rhs.id).append(proxyId, rhs.proxyId).append(httpMethod, rhs.httpMethod).append(pattern, rhs.pattern).append(metricId, rhs.metricId).append(metricSystemName, rhs.metricSystemName).append(delta, rhs.delta).append(tenantId, rhs.tenantId).append(createdAt, rhs.createdAt).append(updatedAt, rhs.updatedAt).append(redirectUrl, rhs.redirectUrl).append(parameters, rhs.parameters).append(querystringParameters, rhs.querystringParameters).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
