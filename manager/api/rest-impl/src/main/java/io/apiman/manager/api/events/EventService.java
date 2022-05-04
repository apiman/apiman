package io.apiman.manager.api.events;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.ApimanEvent;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

/**
 * Send Apiman events.
 * <p>
 * The event service can be disabled at runtime by using the {@link #deactivate()} method; this is for use in situations such as system initialisation and import, where
 * it can cause a coincidental replay of history that is undesirable (e.g. causing dismissed events and notifications to be resurrected). The service can be reactivated
 * using {@link #activate()}, and is active by default.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class EventService {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EventService.class);
    private Event<IVersionedApimanEvent> eventDispatcher;
    private boolean active = true;
    private final ReentrantLock lock = new ReentrantLock();

    @Inject
    public EventService(Event<IVersionedApimanEvent> eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public EventService() {}

    /**
     * If the event service is <strong>active</strong>, then events will be accepted and <strong>dispatched/fired</strong>.
     * <p>
     * If the event service is <strong>inactive</strong>, then events will be accepted and <strong>dropped</strong> (silently).
     *
     * @return true if event service is active, otherwise false.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Activate the service.
     * <p>
     * NB: If the service is locked, this call will block until unlocked.
     */
    public EventService activate() {
        lock.lock();
        active = true;
        lock.unlock();
        return this;
    }

    /**
     * Deactivate the event service.
     * <p>
     * NB: If the service is locked, this call will block until unlocked.
     */
    public EventService deactivate() {
        lock.lock();
        active = false;
        lock.unlock();
        return this;
    }

    /**
     * Acquire an exclusive lock for this service that prevents anyone else activating or deactivating the service until {@link #unlock()} has been called.
     * For example, during an import, it may be desirable to stop anyone else re-activating the service, which may cause event unintended resurrection.
     * <p>
     * Caller <em>must</em> manually release this lock with {@link #unlock()}, otherwise the EventService will remain stuck in its current state.
     */
    public EventService lock() {
        lock.lock();
        return this;
    }

    /**
     * Release any lock held by the calling thread.
     */
    public EventService unlock() {
        lock.unlock();
        return this;
    }

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
        if (isActive()) {
            eventDispatcher.fire(event);
            LOGGER.debug("Fired event: {0}", event);
        } else {
            LOGGER.debug("EventService is deactivated. Event will be discarded: {0}");
        }
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
