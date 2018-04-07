
package io.apiman.gateway.engine.threescale.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fulmicoton.multiregexp.MultiPattern;
import com.fulmicoton.multiregexp.MultiPatternMatcher;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "tenant_id",
    "service_id",
    "endpoint",
    "deployed_at",
    "api_backend",
    "auth_app_key",
    "auth_app_id",
    "auth_user_key",
    "credentials_location",
    "error_auth_failed",
    "error_auth_missing",
    "created_at",
    "updated_at",
    "error_status_auth_failed",
    "error_headers_auth_failed",
    "error_status_auth_missing",
    "error_headers_auth_missing",
    "error_no_match",
    "error_status_no_match",
    "error_headers_no_match",
    "secret_token",
    "hostname_rewrite",
    "oauth_login_url",
    "sandbox_endpoint",
    "api_test_path",
    "api_test_success",
    "hostname_rewrite_for_sandbox",
    "endpoint_port",
    "valid?",
    "service_backend_version",
    "hosts",
    "backend",
    "proxy_rules"
})
@JsonIgnoreProperties({ "routeMatcher", "maxPayloadBufferSize" }) // @JsonIgnore being buggy
public class Proxy implements Serializable
{

    @JsonProperty("id")
    private long id;
    @JsonProperty("tenant_id")
    private long tenantId;
    @JsonProperty("service_id")
    private long serviceId;
    @JsonProperty("endpoint")
    private String endpoint;
    @JsonProperty("deployed_at")
    private String deployedAt;
    @JsonProperty("api_backend")
    private String apiBackend;
    @JsonProperty("auth_app_key")
    private String authAppKey;
    @JsonProperty("auth_app_id")
    private String authAppId;
    @JsonProperty("auth_user_key")
    private String authUserKey;
    @JsonProperty("credentials_location")
    private String credentialsLocation;
    @JsonProperty("error_auth_failed")
    private String errorAuthFailed;
    @JsonProperty("error_auth_missing")
    private String errorAuthMissing;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("error_status_auth_failed")
    private long errorStatusAuthFailed;
    @JsonProperty("error_headers_auth_failed")
    private String errorHeadersAuthFailed;
    @JsonProperty("error_status_auth_missing")
    private long errorStatusAuthMissing;
    @JsonProperty("error_headers_auth_missing")
    private String errorHeadersAuthMissing;
    @JsonProperty("error_no_match")
    private String errorNoMatch;
    @JsonProperty("error_status_no_match")
    private long errorStatusNoMatch;
    @JsonProperty("error_headers_no_match")
    private String errorHeadersNoMatch;
    @JsonProperty("secret_token")
    private String secretToken;
    @JsonProperty("hostname_rewrite")
    private String hostnameRewrite;
    @JsonProperty("oauth_login_url")
    private Object oauthLoginUrl;
    @JsonProperty("sandbox_endpoint")
    private String sandboxEndpoint;
    @JsonProperty("api_test_path")
    private String apiTestPath;
    @JsonProperty("api_test_success")
    private boolean apiTestSuccess;
    @JsonProperty("hostname_rewrite_for_sandbox")
    private String hostnameRewriteForSandbox;
    @JsonProperty("endpoint_port")
    private long endpointPort;
    @JsonProperty("valid?")
    private boolean valid;
    @JsonProperty("service_backend_version")
    private String serviceBackendVersion;
    @JsonProperty("hosts")
    private List<String> hosts = null;
    @JsonProperty("backend")
    private Backend backend;
    @JsonProperty("proxy_rules")
    private List<ProxyRule> proxyRules = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    @JsonIgnore
    private transient MultiPatternMatcher routeMatcher;
    private final static long serialVersionUID = 7319432853004376356L;
    // Avoid repeated reevaluation of Regex
    private Map<String, int[]> REGEX_MATCH_CACHE = new HashMap<>();

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(long id) {
        this.id = id;
    }

    public Proxy withId(long id) {
        this.id = id;
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

    public Proxy withTenantId(long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @JsonProperty("service_id")
    public long getServiceId() {
        return serviceId;
    }

    @JsonProperty("service_id")
    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public Proxy withServiceId(long serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    @JsonProperty("endpoint")
    public String getEndpoint() {
        return endpoint;
    }

    @JsonProperty("endpoint")
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Proxy withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    @JsonProperty("deployed_at")
    public String getDeployedAt() {
        return deployedAt;
    }

    @JsonProperty("deployed_at")
    public void setDeployedAt(String deployedAt) {
        this.deployedAt = deployedAt;
    }

    public Proxy withDeployedAt(String deployedAt) {
        this.deployedAt = deployedAt;
        return this;
    }

    @JsonProperty("api_backend")
    public String getApiBackend() {
        return apiBackend;
    }

    @JsonProperty("api_backend")
    public void setApiBackend(String apiBackend) {
        this.apiBackend = apiBackend;
    }

    public Proxy withApiBackend(String apiBackend) {
        this.apiBackend = apiBackend;
        return this;
    }

    @JsonProperty("auth_app_key")
    public String getAuthAppKey() {
        return authAppKey;
    }

    @JsonProperty("auth_app_key")
    public void setAuthAppKey(String authAppKey) {
        this.authAppKey = authAppKey;
    }

    public Proxy withAuthAppKey(String authAppKey) {
        this.authAppKey = authAppKey;
        return this;
    }

    @JsonProperty("auth_app_id")
    public String getAuthAppId() {
        return authAppId;
    }

    @JsonProperty("auth_app_id")
    public void setAuthAppId(String authAppId) {
        this.authAppId = authAppId;
    }

    public Proxy withAuthAppId(String authAppId) {
        this.authAppId = authAppId;
        return this;
    }

    @JsonProperty("auth_user_key")
    public String getAuthUserKey() {
        return authUserKey;
    }

    @JsonProperty("auth_user_key")
    public void setAuthUserKey(String authUserKey) {
        this.authUserKey = authUserKey;
    }

    public Proxy withAuthUserKey(String authUserKey) {
        this.authUserKey = authUserKey;
        return this;
    }

    @JsonProperty("credentials_location")
    public String getCredentialsLocation() {
        return credentialsLocation;
    }

    @JsonProperty("credentials_location")
    public void setCredentialsLocation(String credentialsLocation) {
        this.credentialsLocation = credentialsLocation;
    }

    public Proxy withCredentialsLocation(String credentialsLocation) {
        this.credentialsLocation = credentialsLocation;
        return this;
    }

    @JsonProperty("error_auth_failed")
    public String getErrorAuthFailed() {
        return errorAuthFailed;
    }

    @JsonProperty("error_auth_failed")
    public void setErrorAuthFailed(String errorAuthFailed) {
        this.errorAuthFailed = errorAuthFailed;
    }

    public Proxy withErrorAuthFailed(String errorAuthFailed) {
        this.errorAuthFailed = errorAuthFailed;
        return this;
    }

    @JsonProperty("error_auth_missing")
    public String getErrorAuthMissing() {
        return errorAuthMissing;
    }

    @JsonProperty("error_auth_missing")
    public void setErrorAuthMissing(String errorAuthMissing) {
        this.errorAuthMissing = errorAuthMissing;
    }

    public Proxy withErrorAuthMissing(String errorAuthMissing) {
        this.errorAuthMissing = errorAuthMissing;
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

    public Proxy withCreatedAt(String createdAt) {
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

    public Proxy withUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @JsonProperty("error_status_auth_failed")
    public long getErrorStatusAuthFailed() {
        return errorStatusAuthFailed;
    }

    @JsonProperty("error_status_auth_failed")
    public void setErrorStatusAuthFailed(long errorStatusAuthFailed) {
        this.errorStatusAuthFailed = errorStatusAuthFailed;
    }

    public Proxy withErrorStatusAuthFailed(long errorStatusAuthFailed) {
        this.errorStatusAuthFailed = errorStatusAuthFailed;
        return this;
    }

    @JsonProperty("error_headers_auth_failed")
    public String getErrorHeadersAuthFailed() {
        return errorHeadersAuthFailed;
    }

    @JsonProperty("error_headers_auth_failed")
    public void setErrorHeadersAuthFailed(String errorHeadersAuthFailed) {
        this.errorHeadersAuthFailed = errorHeadersAuthFailed;
    }

    public Proxy withErrorHeadersAuthFailed(String errorHeadersAuthFailed) {
        this.errorHeadersAuthFailed = errorHeadersAuthFailed;
        return this;
    }

    @JsonProperty("error_status_auth_missing")
    public long getErrorStatusAuthMissing() {
        return errorStatusAuthMissing;
    }

    @JsonProperty("error_status_auth_missing")
    public void setErrorStatusAuthMissing(long errorStatusAuthMissing) {
        this.errorStatusAuthMissing = errorStatusAuthMissing;
    }

    public Proxy withErrorStatusAuthMissing(long errorStatusAuthMissing) {
        this.errorStatusAuthMissing = errorStatusAuthMissing;
        return this;
    }

    @JsonProperty("error_headers_auth_missing")
    public String getErrorHeadersAuthMissing() {
        return errorHeadersAuthMissing;
    }

    @JsonProperty("error_headers_auth_missing")
    public void setErrorHeadersAuthMissing(String errorHeadersAuthMissing) {
        this.errorHeadersAuthMissing = errorHeadersAuthMissing;
    }

    public Proxy withErrorHeadersAuthMissing(String errorHeadersAuthMissing) {
        this.errorHeadersAuthMissing = errorHeadersAuthMissing;
        return this;
    }

    @JsonProperty("error_no_match")
    public String getErrorNoMatch() {
        return errorNoMatch;
    }

    @JsonProperty("error_no_match")
    public void setErrorNoMatch(String errorNoMatch) {
        this.errorNoMatch = errorNoMatch;
    }

    public Proxy withErrorNoMatch(String errorNoMatch) {
        this.errorNoMatch = errorNoMatch;
        return this;
    }

    @JsonProperty("error_status_no_match")
    public long getErrorStatusNoMatch() {
        return errorStatusNoMatch;
    }

    @JsonProperty("error_status_no_match")
    public void setErrorStatusNoMatch(long errorStatusNoMatch) {
        this.errorStatusNoMatch = errorStatusNoMatch;
    }

    public Proxy withErrorStatusNoMatch(long errorStatusNoMatch) {
        this.errorStatusNoMatch = errorStatusNoMatch;
        return this;
    }

    @JsonProperty("error_headers_no_match")
    public String getErrorHeadersNoMatch() {
        return errorHeadersNoMatch;
    }

    @JsonProperty("error_headers_no_match")
    public void setErrorHeadersNoMatch(String errorHeadersNoMatch) {
        this.errorHeadersNoMatch = errorHeadersNoMatch;
    }

    public Proxy withErrorHeadersNoMatch(String errorHeadersNoMatch) {
        this.errorHeadersNoMatch = errorHeadersNoMatch;
        return this;
    }

    @JsonProperty("secret_token")
    public String getSecretToken() {
        return secretToken;
    }

    @JsonProperty("secret_token")
    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    public Proxy withSecretToken(String secretToken) {
        this.secretToken = secretToken;
        return this;
    }

    @JsonProperty("hostname_rewrite")
    public String getHostnameRewrite() {
        return hostnameRewrite;
    }

    @JsonProperty("hostname_rewrite")
    public void setHostnameRewrite(String hostnameRewrite) {
        this.hostnameRewrite = hostnameRewrite;
    }

    public Proxy withHostnameRewrite(String hostnameRewrite) {
        this.hostnameRewrite = hostnameRewrite;
        return this;
    }

    @JsonProperty("oauth_login_url")
    public Object getOauthLoginUrl() {
        return oauthLoginUrl;
    }

    @JsonProperty("oauth_login_url")
    public void setOauthLoginUrl(Object oauthLoginUrl) {
        this.oauthLoginUrl = oauthLoginUrl;
    }

    public Proxy withOauthLoginUrl(Object oauthLoginUrl) {
        this.oauthLoginUrl = oauthLoginUrl;
        return this;
    }

    @JsonProperty("sandbox_endpoint")
    public String getSandboxEndpoint() {
        return sandboxEndpoint;
    }

    @JsonProperty("sandbox_endpoint")
    public void setSandboxEndpoint(String sandboxEndpoint) {
        this.sandboxEndpoint = sandboxEndpoint;
    }

    public Proxy withSandboxEndpoint(String sandboxEndpoint) {
        this.sandboxEndpoint = sandboxEndpoint;
        return this;
    }

    @JsonProperty("api_test_path")
    public String getApiTestPath() {
        return apiTestPath;
    }

    @JsonProperty("api_test_path")
    public void setApiTestPath(String apiTestPath) {
        this.apiTestPath = apiTestPath;
    }

    public Proxy withApiTestPath(String apiTestPath) {
        this.apiTestPath = apiTestPath;
        return this;
    }

    @JsonProperty("api_test_success")
    public boolean isApiTestSuccess() {
        return apiTestSuccess;
    }

    @JsonProperty("api_test_success")
    public void setApiTestSuccess(boolean apiTestSuccess) {
        this.apiTestSuccess = apiTestSuccess;
    }

    public Proxy withApiTestSuccess(boolean apiTestSuccess) {
        this.apiTestSuccess = apiTestSuccess;
        return this;
    }

    @JsonProperty("hostname_rewrite_for_sandbox")
    public String getHostnameRewriteForSandbox() {
        return hostnameRewriteForSandbox;
    }

    @JsonProperty("hostname_rewrite_for_sandbox")
    public void setHostnameRewriteForSandbox(String hostnameRewriteForSandbox) {
        this.hostnameRewriteForSandbox = hostnameRewriteForSandbox;
    }

    public Proxy withHostnameRewriteForSandbox(String hostnameRewriteForSandbox) {
        this.hostnameRewriteForSandbox = hostnameRewriteForSandbox;
        return this;
    }

    @JsonProperty("endpoint_port")
    public long getEndpointPort() {
        return endpointPort;
    }

    @JsonProperty("endpoint_port")
    public void setEndpointPort(long endpointPort) {
        this.endpointPort = endpointPort;
    }

    public Proxy withEndpointPort(long endpointPort) {
        this.endpointPort = endpointPort;
        return this;
    }

    @JsonProperty("valid?")
    public boolean isValid() {
        return valid;
    }

    @JsonProperty("valid?")
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Proxy withValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    @JsonProperty("service_backend_version")
    public String getServiceBackendVersion() {
        return serviceBackendVersion;
    }

    @JsonProperty("service_backend_version")
    public void setServiceBackendVersion(String serviceBackendVersion) {
        this.serviceBackendVersion = serviceBackendVersion;
    }

    public Proxy withServiceBackendVersion(String serviceBackendVersion) {
        this.serviceBackendVersion = serviceBackendVersion;
        return this;
    }

    @JsonProperty("hosts")
    public List<String> getHosts() {
        return hosts;
    }

    @JsonProperty("hosts")
    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public Proxy withHosts(List<String> hosts) {
        this.hosts = hosts;
        return this;
    }

    @JsonProperty("backend")
    public Backend getBackend() {
        return backend;
    }

    @JsonProperty("backend")
    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    public Proxy withBackend(Backend backend) {
        this.backend = backend;
        return this;
    }

    @JsonProperty("proxy_rules")
    public List<ProxyRule> getProxyRules() {
        return proxyRules;
    }

    private void  buildRegex() {
        List<String> patterns = proxyRules.stream()
                .map(ProxyRule::getRegex)
                .map(Pattern::toString)
                .collect(Collectors.toList());
        this.routeMatcher = MultiPattern.of(patterns).matcher();
    }

    public int[] match(String path) {
        if (REGEX_MATCH_CACHE.containsKey(path)) {
            return REGEX_MATCH_CACHE.get(path);
        }
        int[] match = routeMatcher.match(path);
        REGEX_MATCH_CACHE.put(path, match);
        return match;
    }

    // TODO consider storing cache of metricName -> index mappings for O(1) performance.
    public boolean match(String destination, String metricName) {
        int[] matches = match(destination);
        if (matches == null || matches.length == 0) {
            return false;
        }
        for (int match : matches) {
            String candidateMetricName = proxyRules.get(match).getMetricSystemName();
            if (metricName.equals(candidateMetricName)) {
                return true;
            }
        }
        return false;
    }

    @JsonProperty("proxy_rules")
    public void setProxyRules(List<ProxyRule> proxyRules) {
        this.proxyRules = proxyRules;
        buildRegex();
    }

    public Proxy withProxyRules(List<ProxyRule> proxyRules) {
        this.proxyRules = proxyRules;
        buildRegex();
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

    public Proxy withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(tenantId).append(serviceId).append(endpoint).append(deployedAt).append(apiBackend)
                .append(authAppKey).append(authAppId).append(authUserKey).append(credentialsLocation).append(errorAuthFailed).append(errorAuthMissing)
                .append(createdAt).append(updatedAt).append(errorStatusAuthFailed).append(errorHeadersAuthFailed).append(errorStatusAuthMissing)
                .append(errorHeadersAuthMissing).append(errorNoMatch).append(errorStatusNoMatch).append(errorHeadersNoMatch).append(secretToken)
                .append(hostnameRewrite).append(oauthLoginUrl).append(sandboxEndpoint).append(apiTestPath).append(apiTestSuccess)
                .append(hostnameRewriteForSandbox).append(endpointPort).append(valid).append(serviceBackendVersion).append(hosts).append(backend)
                .append(proxyRules).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Proxy) == false) {
            return false;
        }
        Proxy rhs = ((Proxy) other);
        return new EqualsBuilder().append(id, rhs.id).append(tenantId, rhs.tenantId).append(serviceId, rhs.serviceId).append(endpoint, rhs.endpoint)
                .append(deployedAt, rhs.deployedAt).append(apiBackend, rhs.apiBackend).append(authAppKey, rhs.authAppKey)
                .append(authAppId, rhs.authAppId).append(authUserKey, rhs.authUserKey).append(credentialsLocation, rhs.credentialsLocation)
                .append(errorAuthFailed, rhs.errorAuthFailed).append(errorAuthMissing, rhs.errorAuthMissing).append(createdAt, rhs.createdAt)
                .append(updatedAt, rhs.updatedAt).append(errorStatusAuthFailed, rhs.errorStatusAuthFailed)
                .append(errorHeadersAuthFailed, rhs.errorHeadersAuthFailed).append(errorStatusAuthMissing, rhs.errorStatusAuthMissing)
                .append(errorHeadersAuthMissing, rhs.errorHeadersAuthMissing).append(errorNoMatch, rhs.errorNoMatch)
                .append(errorStatusNoMatch, rhs.errorStatusNoMatch).append(errorHeadersNoMatch, rhs.errorHeadersNoMatch)
                .append(secretToken, rhs.secretToken).append(hostnameRewrite, rhs.hostnameRewrite).append(oauthLoginUrl, rhs.oauthLoginUrl)
                .append(sandboxEndpoint, rhs.sandboxEndpoint).append(apiTestPath, rhs.apiTestPath).append(apiTestSuccess, rhs.apiTestSuccess)
                .append(hostnameRewriteForSandbox, rhs.hostnameRewriteForSandbox).append(endpointPort, rhs.endpointPort).append(valid, rhs.valid)
                .append(serviceBackendVersion, rhs.serviceBackendVersion).append(hosts, rhs.hosts).append(backend, rhs.backend)
                .append(proxyRules, rhs.proxyRules).append(additionalProperties, rhs.additionalProperties).isEquals();
    }


}
