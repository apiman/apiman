package io.apiman.gateway.engine.policies.probe;

import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class PlaceholderProbeResponse implements IPolicyProbeResponse {

    private RateLimitResponse rateLimitResponse;

    public PlaceholderProbeResponse() {
    }

    public PlaceholderProbeResponse setRateLimitResponse(RateLimitResponse rateLimitResponse) {
        this.rateLimitResponse = rateLimitResponse;
        return this;
    }

    public RateLimitResponse getRateLimitResponse() {
        return rateLimitResponse;
    }

    @Override
    public String getProbeType() {
        return PlaceholderProbeResponse.class.getName();
    }
}
