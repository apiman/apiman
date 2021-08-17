package io.apiman.manager.sso.keycloak;

import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderFactory;

import io.apiman.manager.sso.keycloak.processors.EventProcessor;

/**
 * Pushes events to Apiman Manager that it needs to know about.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApimanEventListenerProvider implements EventListenerProvider {

    private static final Logger LOGGER = Logger.getLogger(ApimanEventListenerProvider.class);
    private final Map<EventType, EventProcessor> handlers;
    private final KeycloakSession session;
    private final ProviderFactory<HttpClientProvider> httpClientFactory;

    public ApimanEventListenerProvider(Map<EventType, EventProcessor> handlers,
         KeycloakSession session,
         ProviderFactory<HttpClientProvider> httpClientFactory) {
        this.handlers = handlers;
        this.session = session;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void onEvent(Event event) {
        if (handlers.containsKey(event.getType())) {
            EventProcessor handler = handlers.get(event.getType());
            LOGGER.debugv("Found a handler {0} for event: {1}", handler, event);
            handler.onEvent(session, event);
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // Do what?
    }

    @Override
    public void close() {

    }
}
