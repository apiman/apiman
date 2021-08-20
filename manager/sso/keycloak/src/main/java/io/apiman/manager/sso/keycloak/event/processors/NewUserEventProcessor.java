package io.apiman.manager.sso.keycloak.event.processors;

import io.apiman.manager.sso.keycloak.event.ApimanEventListenerOptions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.StringJoiner;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jboss.logging.Logger;
import org.keycloak.common.util.Retry;
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
 * <p>Pushes the "New User" registered event to Apiman (via HTTP only, currently).
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class NewUserEventProcessor implements IEventProcessor {

    private static final Logger LOGGER = Logger.getLogger(NewUserEventProcessor.class);
    private static final int ATTEMPTS_COUNT = 10;
    private static final int INTERVAL_BASE_MILLIS = 1_000;
    private final ApimanEventListenerOptions options;

    public NewUserEventProcessor(ApimanEventListenerOptions options) {
        this.options = options;
    }

    /**
     * Process a {@link org.keycloak.events.EventType#REGISTER} and send to Apiman Manager API via HTTP.
     */
    @Override
    public void onEvent(KeycloakSession session, Event event, ProviderFactory<HttpClientProvider> httpClientFactory) {
        typeCheck(event);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Processing a REGISTER event: {0}", ToStringBuilder.reflectionToString(event));
        }

        RealmModel realm = session.realms().getRealm(event.getRealmId());
        UserModel newRegisteredUser = session.users().getUserById(event.getUserId(), realm);
        LOGGER.debugv("Realm: {0}, User: {1}", realm.getName(), newRegisteredUser);

        // Push to Apiman, retry a few times.
        HttpClientProvider client = httpClientFactory.create(session);
        Retry.executeWithBackoff(
             (attempt) -> postEventToApiman(attempt, client, event),
             this::logRetries,
             ATTEMPTS_COUNT,
             INTERVAL_BASE_MILLIS
        );
    }

    private void postEventToApiman(int attempt, HttpClientProvider client, Event event) {
        try {
            URI apimanEventEndpoint = calculateURI();
            LOGGER.debugv("Attempt {0} to POST {1} event to {2}", attempt, EventType.REGISTER.name(), apimanEventEndpoint);
            String json = JsonSerialization.writeValueAsPrettyString(event);
            int responseCode = client.postText(apimanEventEndpoint.toString(), json);
            if (responseCode / 100 != 2) {
                String msg = MessageFormat.format("Non-200 response code returned by {0}.", apimanEventEndpoint);
                LOGGER.error(msg);
                throw new IOException(msg);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private URI calculateURI() {
        return UriBuilder.fromUri(options.getApiManagerUri())
                         .path("new-user-event")
                         .build();
    }

    private void typeCheck(Event event) {
        if (!event.getType().equals(EventType.REGISTER)) {
            String msg = MessageFormat.format("Expected to process {0} but got {1}", EventType.REGISTER, event.getType());
            throw new IllegalArgumentException(msg);
        }
    }

    private void logRetries(int attempt, Throwable throwable) {
        LOGGER.errorv(throwable, "Attempt {0} to POST event failed. "
             + "A maximum of {1} retries will be attempted.", attempt, ATTEMPTS_COUNT);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NewUserEventProcessor.class.getSimpleName() + "[", "]")
             .toString();
    }
}
