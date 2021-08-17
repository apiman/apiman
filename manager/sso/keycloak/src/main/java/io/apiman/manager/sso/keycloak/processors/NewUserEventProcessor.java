package io.apiman.manager.sso.keycloak.processors;

import io.apiman.manager.sso.keycloak.ApimanProviderFactoryOptionsParser;

import java.text.MessageFormat;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * Process a new user registration event {@link org.keycloak.events.EventType#REGISTER}
 *
 * <p>
 * <p>Push the "New User" event to Apiman
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class NewUserEventProcessor implements EventProcessor {
    private static final Logger LOGGER = Logger.getLogger(NewUserEventProcessor.class);
    private final ApimanProviderFactoryOptionsParser options;

    public NewUserEventProcessor(ApimanProviderFactoryOptionsParser options) {
        this.options = options;
    }

    public void onEvent(KeycloakSession session, Event event) {
        typeCheck(event);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Processing a REGISTER event: {0}", ToStringBuilder.reflectionToString(event));
        }

        RealmModel realm = session.realms().getRealm(event.getRealmId());
        UserModel newRegisteredUser = session.users().getUserById(event.getUserId(), realm);

        LOGGER.debugv("Realm: {0}, User: {1}", realm.getName());
    }

    private void typeCheck(Event event) {
        if (!event.getType().equals(EventType.REGISTER)) {
            var msg = MessageFormat.format("Expected to process {0} but got {1}", EventType.REGISTER, event.getType());
            throw new IllegalArgumentException(msg);
        }
    }
}
