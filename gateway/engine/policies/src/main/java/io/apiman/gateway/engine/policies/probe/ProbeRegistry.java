package io.apiman.gateway.engine.policies.probe;

import io.apiman.gateway.engine.beans.IPolicyProbeResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ProbeRegistry {
    private static final ObjectMapper OM = new ObjectMapper()
            .findAndRegisterModules();

    public ProbeRegistry() {
    }

    static {
        register(RateLimitingProbeResponse.class);
    }

    public static void register(Class<? extends IPolicyProbeResponse> response) {
        OM.registerSubtypes(response);
    }

    public static String serialize(IPolicyProbeResponse response) {
        try {
            return OM.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static IPolicyProbeResponse deserialize(String raw)  {
        try {
            return OM.readValue(raw, IPolicyProbeResponse.class);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static IPolicyProbeResponse deserialize(InputStream is)  {
        try {
            return OM.readValue(is, IPolicyProbeResponse.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
