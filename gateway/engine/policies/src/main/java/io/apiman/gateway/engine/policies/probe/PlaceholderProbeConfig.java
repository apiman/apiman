package io.apiman.gateway.engine.policies.probe;

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.IPolicyProbeRequest;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class PlaceholderProbeConfig implements IPolicyProbeRequest {

    @Override
    public boolean isPublicApi() {
        return false;
    }

    @Override
    public String getApiKey() {
        return null;
    }

    @Override
    public IPolicyProbeRequest setApiKey(String apiKey) {
        return null;
    }

    @Override
    public ApiContract getContract() {
        return null;
    }

    @Override
    public IPolicyProbeRequest setContract(ApiContract contract) {
        return null;
    }

    @Override
    public Api getApi() {
        return null;
    }

    @Override
    public IPolicyProbeRequest setApi(Api api) {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public IPolicyProbeRequest setRemoteAddr(String remoteAddr) {
        return null;
    }
}
