package io.apiman.manager.sso.keycloak.processors;

import org.keycloak.events.Event;
import org.keycloak.models.KeycloakSession;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface EventProcessor {
    void onEvent(KeycloakSession session, Event event);
}
