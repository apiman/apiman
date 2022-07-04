package io.apiman.manager.sso.keycloak.event.processors;

import org.keycloak.events.Event;
import org.keycloak.models.KeycloakSession;

/**
 * Process a Keycloak event.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface IEventProcessorFactory {
    /**
     * Handle a Keycloak event.
     */
    void onEvent(KeycloakSession session, Event event);
}
