package io.apiman.manager.sso.keycloak.event;

import io.apiman.manager.sso.keycloak.KeycloakOptsMapShim;
import io.apiman.manager.sso.keycloak.event.processors.IEventProcessorFactory;
import io.apiman.manager.sso.keycloak.event.processors.NewUserEventProcessorFactory;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Creates {@link ApimanEventListenerProvider}
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApimanEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger LOGGER = Logger.getLogger(ApimanEventListenerProviderFactory.class);
    private final Map<EventType, IEventProcessorFactory> eventProcessorMap = new HashMap<>();
    private ApimanEventListenerOptions options;

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new ApimanEventListenerProvider(eventProcessorMap, keycloakSession);
    }

    @Override
    public void init(Scope scope) {
        options = new ApimanEventListenerOptions(new KeycloakOptsMapShim(scope));
        LOGGER.debugv("Apiman SPI {0} initialised", getId());
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        initProcessors(keycloakSessionFactory);
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "apiman-push-events";
    }

    private void initProcessors(KeycloakSessionFactory keycloakSessionFactory) {
        eventProcessorMap.put(
             EventType.REGISTER,
             new NewUserEventProcessorFactory(options, keycloakSessionFactory)
        );
    }

}
