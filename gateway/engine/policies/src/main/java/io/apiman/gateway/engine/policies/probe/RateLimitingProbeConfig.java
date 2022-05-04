package io.apiman.gateway.engine.policies.probe;

import io.apiman.gateway.engine.beans.IPolicyProbeRequest;

import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonInclude(Include.NON_NULL)
public class RateLimitingProbeConfig implements IPolicyProbeRequest {

    private String user;
    private String apiKey;
    private String callerIp;

    public RateLimitingProbeConfig() {
    }

    public String getUser() {
        return user;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getCallerIp() {
        return callerIp;
    }

    public RateLimitingProbeConfig setUser(String user) {
        this.user = user;
        return this;
    }

    public RateLimitingProbeConfig setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public RateLimitingProbeConfig setCallerIp(String callerIp) {
        this.callerIp = callerIp;
        return this;
    }

    @Override public String toString() {
        return new StringJoiner(", ", RateLimitingProbeConfig.class.getSimpleName() + "[", "]")
                .add("user='" + user + "'")
                .add("apiKey='" + apiKey + "'")
                .add("callerIp='" + callerIp + "'")
                .toString();
    }
}
