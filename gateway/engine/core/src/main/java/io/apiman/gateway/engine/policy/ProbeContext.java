package io.apiman.gateway.engine.policy;

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;

import java.util.StringJoiner;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ProbeContext {
    private String apiKey;
    private ApiContract contract;
    private Api api;
    private String url;

    public String getApiKey() {
        return apiKey;
    }

    public ProbeContext setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public ApiContract getContract() {
        return contract;
    }

    public ProbeContext setContract(ApiContract contract) {
        this.contract = contract;
        return this;
    }

    public Api getApi() {
        return api;
    }

    public ProbeContext setApi(Api api) {
        this.api = api;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ProbeContext setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ProbeContext.class.getSimpleName() + "[", "]")
                .add("apiKey='" + apiKey + "'")
                .add("contract=" + contract)
                .add("api=" + api)
                .add("url='" + url + "'")
                .toString();
    }
}
