package io.apiman.gateway.engine.beans;

/**
 * Probe the state of a policy.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface IPolicyProbeRequest {
    boolean isPublicApi();
    String getApiKey();
    IPolicyProbeRequest setApiKey(String apiKey);
    ApiContract getContract();
    IPolicyProbeRequest setContract(ApiContract contract);
    Api getApi();
    IPolicyProbeRequest setApi(Api api);
    String getRemoteAddr();
    IPolicyProbeRequest setRemoteAddr(String remoteAddr);
}
