package io.apiman.manager.sso.keycloak.event;

import io.apiman.manager.sso.keycloak.event.processors.IEventProcessorFactory;

import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

/**
 * Pushes events to Apiman Manager that it needs to know about.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApimanEventListenerProvider implements EventListenerProvider {

    private static final Logger LOGGER = Logger.getLogger(ApimanEventListenerProvider.class);
    private final Map<EventType, IEventProcessorFactory> handlers;
    private final KeycloakSession session;

    public ApimanEventListenerProvider(Map<EventType, IEventProcessorFactory> handlers,
         KeycloakSession session) {
        this.handlers = handlers;
        this.session = session;
    }

    /**
     * Accept Keycloak event and then if a handler exists for that event type, invoke it.
     */
    @Override
    public void onEvent(Event event) {
        if (handlers.containsKey(event.getType())) {
            IEventProcessorFactory handler = handlers.get(event.getType());
            LOGGER.debugv("Invoking a handler {0} for event: {1}", handler, event);
            handler.onEvent(session, event);
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
    }

    @Override
    public void close() {

    }
}
