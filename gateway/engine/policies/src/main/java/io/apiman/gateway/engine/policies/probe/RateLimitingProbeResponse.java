package io.apiman.gateway.engine.policies.probe;

import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.policies.config.RateLimitingConfig;

import com.fasterxml.jackson.annotation.JsonTypeId;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class RateLimitingProbeResponse implements IPolicyProbeResponse {

    private RateLimitingConfig rateLimitConfig;
    private RateLimitResponse rateLimitResponse;

    public RateLimitingProbeResponse() {
    }

    public RateLimitingConfig getRateLimitConfig() {
        return rateLimitConfig;
    }

    public RateLimitingProbeResponse setRateLimitConfig(RateLimitingConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
        return this;
    }

    public RateLimitingProbeResponse setRateLimitResponse(RateLimitResponse rateLimitResponse) {
        this.rateLimitResponse = rateLimitResponse;
        return this;
    }

    public RateLimitResponse getRateLimitResponse() {
        return rateLimitResponse;
    }

    @Override
    public String getProbeType() {
        return RateLimitingProbeResponse.class.getSimpleName();
    }
}
