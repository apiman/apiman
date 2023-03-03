package io.apiman.gateway.engine.policies.probe;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.beans.IPolicyProbeResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ProbeRegistry {
    private static final ObjectMapper OM = new ObjectMapper()
            .findAndRegisterModules();

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ProbeRegistry.class);

    public ProbeRegistry() {
    }

    static {
        try (ScanResult scanResult = new ClassGraph()
                                             .enableAnnotationInfo()
                                             .enableClassInfo()
                                             .scan()) {
            for (ClassInfo klazzInfo : scanResult.getClassesImplementing(IPolicyProbeResponse.class)) {
                try {
                    Class<IPolicyProbeResponse> klazz = (Class<IPolicyProbeResponse>) klazzInfo.loadClass();
                    register(klazz);
                    LOGGER.info("Found policy probe: {0}", klazz.getCanonicalName());
                } catch (Throwable t) {
                    LOGGER.warn("Ignoring exception during event class load: {0}", t);
                }
            }
        }
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
