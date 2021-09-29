package io.apiman.gateway.engine.policies.probe;

import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.policies.config.RateLimitingConfig;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class RateLimitingProbeResponse implements IPolicyProbeResponse {

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
        return RateLimitingProbeResponse.class.getSimpleName();
    }
}
