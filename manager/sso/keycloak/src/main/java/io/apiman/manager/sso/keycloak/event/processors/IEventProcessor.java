package io.apiman.manager.sso.keycloak.event.processors;

import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Event;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderFactory;

/**
 * Process a Keycloak event.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface IEventProcessor {

    /**
     * Handle a Keycloak event.
     */
    void onEvent(KeycloakSession session, Event event, ProviderFactory<HttpClientProvider> httpClientFactory);
}
