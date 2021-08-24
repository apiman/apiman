package io.apiman.common.util;

import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class JsonUtil {
    private static final ObjectMapper OM = new ObjectMapper();

    public static JsonNode toJsonTree(Object o) {
        return OM.valueToTree(o);
    }

    public static String toJsonString(Object o) {
        try {
            return OM.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
