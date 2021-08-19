package io.apiman.manager.sso.keycloak.event.processors;

import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Event;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderFactory;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface EventProcessor {
    void onEvent(KeycloakSession session, Event event,
         ProviderFactory<HttpClientProvider> httpClientFactory);
}
