package io.apiman.manager.sso.keycloak.event;

import io.apiman.manager.sso.keycloak.KeycloakOptsMapShim;
import io.apiman.manager.sso.keycloak.event.processors.IEventProcessor;
import io.apiman.manager.sso.keycloak.event.processors.NewUserEventProcessor;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApimanEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger LOGGER = Logger.getLogger(ApimanEventListenerProviderFactory.class);
    private final Map<EventType, IEventProcessor> eventProcessorMap = new HashMap<>();

    private ProviderFactory<HttpClientProvider> httpClientFactory;
    private ApimanEventListenerOptions options;

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new ApimanEventListenerProvider(eventProcessorMap, keycloakSession, httpClientFactory);
    }

    @Override
    public void init(Scope scope) {
        options = new ApimanEventListenerOptions(new KeycloakOptsMapShim(scope));
        initProcessors();
        LOGGER.debugv("Apiman SPI {0} initialised", getId());
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        httpClientFactory = keycloakSessionFactory.getProviderFactory(HttpClientProvider.class);
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "apiman-push-events";
    }

    private void initProcessors() {
        eventProcessorMap.put(EventType.REGISTER, new NewUserEventProcessor(options));
    }

}
