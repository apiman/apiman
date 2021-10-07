package io.apiman.common.util;

import java.io.UncheckedIOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

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
    public JsonUtil() {
    }

    private static final ObjectMapper OM = JsonMapper
         .builder()
         // (mostly) match the JSON5 spec.
         .enable(ALLOW_UNQUOTED_FIELD_NAMES)
         .enable(ALLOW_TRAILING_COMMA)
         .enable(ALLOW_SINGLE_QUOTES)
         .enable(ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
         .enable(ALLOW_NON_NUMERIC_NUMBERS)
         .enable(ALLOW_JAVA_COMMENTS)
         .enable(ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS)
         .enable(ALLOW_UNESCAPED_CONTROL_CHARS)
         // Avoid weird floating point timestamps
         .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
         // Enable various Java 8 and library data structures to be serialized
         .addModule(new JavaTimeModule())
         .addModule(new ParameterNamesModule())
         .addModule(new Jdk8Module())
         .addModule(new GuavaModule())
         .addModule(new JaxbAnnotationModule())
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

    public static ObjectMapper getObjectMapper() {
        return OM;
    }
}
