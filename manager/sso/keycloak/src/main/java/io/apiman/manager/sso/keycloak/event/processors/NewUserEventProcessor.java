package io.apiman.manager.sso.keycloak.event.processors;

import io.apiman.manager.sso.keycloak.event.ApimanEventListenerOptions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.StringJoiner;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jboss.logging.Logger;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.util.JsonSerialization;

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
    private final ApimanEventListenerOptions options;

    public NewUserEventProcessor(ApimanEventListenerOptions options) {
        this.options = options;
    }

    public void onEvent(KeycloakSession session, Event event, ProviderFactory<HttpClientProvider> httpClientFactory) {
        typeCheck(event);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Processing a REGISTER event: {0}", ToStringBuilder.reflectionToString(event));
        }

        RealmModel realm = session.realms().getRealm(event.getRealmId());
        UserModel newRegisteredUser = session.users().getUserById(event.getUserId(), realm);
        LOGGER.debugv("Realm: {0}, User: {1}", realm.getName(), newRegisteredUser);
        // Push to Apiman
        //httpClient.post(options.getApiManagerUri(), new NewUserCreatedDto(...), );

        HttpClientProvider client = httpClientFactory.create(session);

        RetryUtils
        try {
            client.postText(options.getApiManagerUri().toString() + "/new-user-event", JsonSerialization.writeValueAsPrettyString(event));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void typeCheck(Event event) {
        if (!event.getType().equals(EventType.REGISTER)) {
            String msg = MessageFormat.format("Expected to process {0} but got {1}", EventType.REGISTER, event.getType());
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NewUserEventProcessor.class.getSimpleName() + "[", "]")
             .toString();
    }
}
