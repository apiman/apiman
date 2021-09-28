package io.apiman.gateway.engine.policies.probe;

import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class RateLimitingProbeResponse implements IPolicyProbeResponse {

    private RateLimitResponse rateLimitResponse;

    public RateLimitingProbeResponse() {
    }

    public RateLimitingProbeResponse setRateLimitResponse(RateLimitResponse rateLimitResponse) {
        this.rateLimitResponse = rateLimitResponse;
        return this;
    }

    public RateLimitResponse getRateLimitResponse() {
        return rateLimitResponse;
    }
}
