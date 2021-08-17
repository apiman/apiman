package io.apiman.manager.sso.keycloak.event.processors;

import io.apiman.manager.sso.keycloak.event.ApimanEventListenerOptions;

import org.keycloak.events.Event;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

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
