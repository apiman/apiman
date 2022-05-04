package io.apiman.gateway.engine.policies.probe;

import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.policies.config.RateLimitingConfig;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class RateLimitingProbeResponse implements IPolicyProbeResponse {

    private String probeType = "RateLimitingProbeResponse";
    private RateLimitingConfig config;
    private RateLimitResponse status;

    public RateLimitingProbeResponse() {
    }

    public RateLimitingConfig getConfig() {
        return config;
    }

    public RateLimitingProbeResponse setConfig(RateLimitingConfig config) {
        this.config = config;
        return this;
    }

    public RateLimitResponse getStatus() {
        return status;
    }

    public RateLimitingProbeResponse setStatus(RateLimitResponse status) {
        this.status = status;
        return this;
    }

    @Override
    public String getProbeType() {
        return probeType;
    }
}
