package io.apiman.manager.api.events;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.ApimanEvent;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;

import java.time.OffsetDateTime;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

/**
 * Send events.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class EventService {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EventService.class);
    private Event<IVersionedApimanEvent> eventDispatcher;

    @Inject
    public EventService(Event<IVersionedApimanEvent> eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public EventService() {}

    /**
     * Fire an event extending {@link IVersionedApimanEvent}. This implementation will attempt to set
     *
     * @return the event sent with event version, type, and time set.
     */
    public <E extends IVersionedApimanEvent> E fireEvent(E event) {
        // These fields may not be set.
        event.getHeaders()
             .setEventVersion(getEventVersion(event))
             .setType(getType(event))
             .setTime(OffsetDateTime.now());
        eventDispatcher.fire(event);
        LOGGER.debug("Fired event: {0}", event);
        return event;
    }

    /**
     * Use version number in priority: (1) provided in headers, (2) in @ApimanEvent annotation.
     */
    private long getEventVersion(IVersionedApimanEvent event) {
        ApimanEventHeaders headers = event.getHeaders();
        // Version was not set, get it from the annotation if possible.
        if (headers.getEventVersion() <= 0) {
            if (event.getClass().isAnnotationPresent(ApimanEvent.class)) {
                ApimanEvent ev = event.getClass().getAnnotation(ApimanEvent.class);
                return ev.version();
            } else {
                throw new IllegalStateException("No version set for @ApimanEvent event: " + event.getClass().getCanonicalName());
            }
        } else {
            return headers.getEventVersion();
        }
    }

    private String getType(IVersionedApimanEvent event) {
        String currentValue = event.getHeaders().getType();
        if (StringUtils.isEmpty(currentValue)) {
            if (event.getClass().isAnnotationPresent(ApimanEvent.class)) {
                ApimanEvent ev = event.getClass().getAnnotation(ApimanEvent.class);
                currentValue = ev.name();
            }
        }
        return Optional.ofNullable(currentValue)
                       .filter(s -> !s.isBlank())
                       .orElse(event.getClass().getCanonicalName());
    }
}
