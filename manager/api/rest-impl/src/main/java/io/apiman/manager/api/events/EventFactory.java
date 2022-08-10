package io.apiman.manager.api.events;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.JsonUtil;
import io.apiman.manager.api.beans.events.ApimanEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;

import java.util.NoSuchElementException;
import java.util.StringJoiner;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.apache.commons.lang3.StringUtils;

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
        try (ScanResult scanResult = new ClassGraph()
                                             .enableAnnotationInfo()
                                             .enableClassInfo()
                                             .acceptPackages("io.apiman.manager.api")
                                             .scan()) {
            for (ClassInfo klazzInfo : scanResult.getClassesImplementing(IVersionedApimanEvent.class)) {
                try {
                    Class<IVersionedApimanEvent> klazz = (Class<IVersionedApimanEvent>) klazzInfo.loadClass();
                    String name = calculateName(klazz);
                    ApimanEvent metadata = getAnnotation(klazz);
                    klazzez.put(name, new ClazzAndMetadata(metadata, klazz));
                    LOGGER.debug("Calculated name for {0} as {1}", klazz, name);
                } catch (Throwable t) {
                    LOGGER.warn("Ignoring exception during event class load: {0}", t);
                }
            }
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
            long version = possibleHeaders.get("eventVersion").asLong();
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
