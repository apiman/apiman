package io.apiman.gateway.engine.policies.probe;

import io.apiman.gateway.engine.beans.IPolicyProbeRequest;

import java.util.StringJoiner;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
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

    @Override public String toString() {
        return new StringJoiner(", ", RateLimitingProbeConfig.class.getSimpleName() + "[", "]")
                .add("user='" + user + "'")
                .add("apiKey='" + apiKey + "'")
                .add("callerIp='" + callerIp + "'")
                .toString();
    }
}
