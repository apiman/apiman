package io.apiman.gateway.engine.policies.probe;

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.IPolicyProbeRequest;

import java.util.StringJoiner;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class RateLimitingProbeConfig implements IPolicyProbeRequest {

    private String user;
    private String apiKey;
    private ApiContract contract;
    private Api api;
    private String remoteAddr;
    private String callerIp;

    @Override
    public boolean isPublicApi() {
       return contract != null;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public RateLimitingProbeConfig setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    @Override
    public ApiContract getContract() {
        return contract;
    }

    @Override
    public RateLimitingProbeConfig setContract(ApiContract contract) {
        this.contract = contract;
        return this;
    }

    @Override
    public Api getApi() {
        return api;
    }

    @Override
    public RateLimitingProbeConfig setApi(Api api) {
        this.api = api;
        return this;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public RateLimitingProbeConfig setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
        return this;
    }

    public String getUser() {
        return user;
    }

    public RateLimitingProbeConfig setUser(String user) {
        this.user = user;
        return this;
    }

    public String getCallerIp() {
        return callerIp;
    }

    public void setCallerIp(String callerIp) {
        this.callerIp = callerIp;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RateLimitingProbeConfig.class.getSimpleName() + "[", "]")
             .add("user='" + user + "'")
             .add("apiKey='" + apiKey + "'")
             .add("contract=" + contract)
             .add("api=" + api)
             .add("remoteAddr='" + remoteAddr + "'")
             .add("callerIp='" + callerIp + "'")
             .toString();
    }
}
