package io.apiman.gateway.engine.policies.probe;

import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.components.rate.RateLimitResponse;
import io.apiman.gateway.engine.policies.config.TransferQuotaConfig;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class TransferQuotaProbeResponse implements IPolicyProbeResponse {

    private String probeType = "TransferQuotaProbeResponse";
    private TransferQuotaConfig config;
    private RateLimitResponse status;

    public TransferQuotaProbeResponse() {
    }

    public TransferQuotaConfig getConfig() {
        return config;
    }

    public TransferQuotaProbeResponse setConfig(TransferQuotaConfig config) {
        this.config = config;
        return this;
    }

    public RateLimitResponse getStatus() {
        return status;
    }

    public TransferQuotaProbeResponse setStatus(RateLimitResponse status) {
        this.status = status;
        return this;
    }

    @Override
    public String getProbeType() {
        return probeType;
    }
}
