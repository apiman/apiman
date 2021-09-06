package io.apiman.common.util;

import java.io.UncheckedIOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_JAVA_COMMENTS;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_TRAILING_COMMA;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class JsonUtil {
    private static final ObjectMapper OM = JsonMapper
         .builder()
         .enable(ALLOW_UNQUOTED_FIELD_NAMES)
         .enable(ALLOW_TRAILING_COMMA)
         .enable(ALLOW_SINGLE_QUOTES)
         .enable(ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
         .enable(ALLOW_NON_NUMERIC_NUMBERS)
         .enable(ALLOW_JAVA_COMMENTS)
         .enable(ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS)
         .enable(ALLOW_UNESCAPED_CONTROL_CHARS)
         .build();

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

    @SuppressWarnings("unchecked")
    public static <T, C extends Collection<T>> C toPojo(String payload, Class<T> klazz, Class<? extends Collection> collectionKlazz) {
        try {
            CollectionType type = OM.getTypeFactory().constructCollectionType(collectionKlazz, klazz);
            return (C) OM.readValue(payload, type);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
