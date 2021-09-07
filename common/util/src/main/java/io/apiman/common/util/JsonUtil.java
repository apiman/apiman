package io.apiman.common.util;

import java.io.UncheckedIOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

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

    public static <T> T toPojo(JsonNode payload, Class<T> klazz) {
        try {
            return OM.treeToValue(payload, klazz);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T, C extends Collection<T>> C toPojo(String payload, Class<T> klazz, Class<? extends Collection> collectionKlazz) {
        try {
            CollectionType type = OM.getTypeFactory().constructCollectionType(collectionKlazz, klazz);
            return (C) OM.readValue(payload, type);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
