package io.apiman.gateway.engine.policy;

import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.IPolicyProbeRequest;
import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface IPolicyProbe<C, P extends IPolicyProbeRequest> extends IPolicy {
    ObjectMapper mapper = JsonMapper
            .builder()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .findAndAddModules()
            .build();

    /**
     * Return the class to unmarshall the raw probe request JSON configuration into.
     * <p>
     * For compatibility reasons this is not abstract, but all classes who provide probe capabilities should implement it.
     */
    Class<P> getProbeRequestClass();

    Class<C> getConfigurationClass();

    /**
     * This version uses Jackson to parse the probe configuration into the class specified by
     * {@link #getProbeRequestClass()}, and the policy config specified by {@link #getConfigurationClass()}.
     * <p>
     * Most implementors should override the abstract
     * {@link #probe(IPolicyProbeRequest, Object, ProbeContext, IPolicyContext, IAsyncResultHandler)}  method rather than this one,
     * unless they are doing something more exotic (e.g. not using JSON).
     */
    default void probe(String rawProbeConfiguration, String rawPolicyConfiguration, ProbeContext probeContext, IPolicyContext policyContext,
                       IAsyncResultHandler<IPolicyProbeResponse> resultHandler) {
        try {
            P probeConfig = mapper.readValue(rawProbeConfiguration, getProbeRequestClass());
            C policyConfig = mapper.readValue(rawPolicyConfiguration, getConfigurationClass());
            probe(probeConfig, policyConfig, probeContext, policyContext, resultHandler);
        } catch (Exception e) {
            throw new ConfigurationParseException(e);
        }
    }

    void probe(P probeRequest, C policyConfig, ProbeContext probeContext, IPolicyContext policyContext, IAsyncResultHandler<IPolicyProbeResponse> resultHandler);
}
