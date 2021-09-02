package io.apiman.manager.api.events;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.JsonUtil;
import io.apiman.manager.api.beans.events.ApimanEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class EventFactory {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EventFactory.class);
    private final Multimap<String, ClazzAndMetadata> klazzez = ArrayListMultimap.create();

    @Inject
    public EventFactory() {
    }

    @PostConstruct
    public void postConstruct() {
        findEvents();
    }

    private void findEvents() {
        Reflections reflections = new Reflections("io.apiman.manager.api");
        Set<Class<? extends IVersionedApimanEvent>> eventKlazzes = reflections.getSubTypesOf(IVersionedApimanEvent.class);
        for (Class<? extends IVersionedApimanEvent> klazz : eventKlazzes) {
            String name = calculateName(klazz);
            ApimanEvent metadata = getAnnotation(klazz);
            klazzez.put(name, new ClazzAndMetadata(metadata, klazz));
            LOGGER.debug("Calculated name for {0} as {1}", klazz, name);
        }
    }

    private ApimanEvent getAnnotation(Class<? extends IVersionedApimanEvent> klazz) {
        if (klazz.isAnnotationPresent(ApimanEvent.class)) {
            return klazz.getAnnotation(ApimanEvent.class);
        }
        return null;
    }

    private String calculateName(Class<? extends IVersionedApimanEvent> klazz) {
        if (klazz.isAnnotationPresent(ApimanEvent.class)) {
            ApimanEvent anno = klazz.getAnnotation(ApimanEvent.class);
            LOGGER.debug("Found annotation {0} for {1}", anno, klazz.getCanonicalName());
            if (!"".equals(anno.name())) {
                return anno.name();
            } else {
                return klazz.getCanonicalName();
            }
        } else {
            return klazz.getCanonicalName();
        }
    }

    /**
     * Turn JsonNode structure into an @ApimanEvent extending IVersionedApimanEvent
     */
    public <E extends IVersionedApimanEvent> E toEventPojo(JsonNode payload) {
        return (E) JsonUtil.toPojo(payload, getKlazz(payload));
    }

    public Class<? extends IVersionedApimanEvent> getKlazz(JsonNode payload) {
        JsonNode possibleHeaders = payload.at("/headers");
        if (!possibleHeaders.isMissingNode() && possibleHeaders.isObject()) {
            String type = possibleHeaders.get("type").asText();
            int version = possibleHeaders.get("version").asInt();
            if (StringUtils.isBlank(type)) {
                throw new IllegalStateException("'type' field missing from headers");
            }
            ClazzAndMetadata result = klazzez.get(type).stream()
                             .filter(k -> k.metadata.version() == version)
                             .findFirst()
                             .orElseThrow(() -> new NoSuchElementException("Can't find an event class for " + payload));
            LOGGER.debug("Found class for event payload: {0}", result);
            return result.klazz;
        }
        throw new IllegalArgumentException("Provided JsonNode structure does not appear to be an ApimanEvent: " + payload);
    }

    private static final class ClazzAndMetadata {
        private final ApimanEvent metadata;
        private final Class<? extends IVersionedApimanEvent> klazz;

        public ClazzAndMetadata(ApimanEvent metadata, Class<? extends IVersionedApimanEvent> klazz) {
            this.metadata = metadata;
            this.klazz = klazz;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ClazzAndMetadata.class.getSimpleName() + "[", "]")
                 .add("metadata=" + metadata)
                 .add("klazz=" + klazz)
                 .toString();
        }
    }
}
