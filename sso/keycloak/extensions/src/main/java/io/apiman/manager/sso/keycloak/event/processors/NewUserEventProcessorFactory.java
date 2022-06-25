package io.apiman.manager.sso.keycloak.event.processors;

import io.apiman.manager.sso.keycloak.event.ApimanEventListenerOptions;

import java.text.MessageFormat;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class NewUserEventProcessorFactory implements IEventProcessorFactory {

    private static final Logger LOGGER = Logger.getLogger(NewUserEventProcessorFactory.class);
    private final ApimanEventListenerOptions options;
    private final KeycloakSessionFactory keycloakSessionFactory;

    public NewUserEventProcessorFactory(ApimanEventListenerOptions options,
         KeycloakSessionFactory keycloakSessionFactory) {
         this.options = options;
         this.keycloakSessionFactory = keycloakSessionFactory;
    }

    @Override
    public void onEvent(KeycloakSession session, Event event) {
        typeCheck(event);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Processing a REGISTER event: {0}", ToStringBuilder.reflectionToString(event));
        }

        new NewUserEventProcessor(options, session, keycloakSessionFactory).process(event);
    }


    private void typeCheck(Event event) {
        if (!event.getType().equals(EventType.REGISTER)) {
            String msg = MessageFormat.format("Expected to process {0} but got {1}", EventType.REGISTER,
                 event.getType());
            throw new IllegalArgumentException(msg);
        }
    }
}
