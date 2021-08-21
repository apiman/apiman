package io.apiman.manager.sso.keycloak.event.processors;

import io.apiman.manager.api.beans.events.dto.NewAccountCreatedDto;
import io.apiman.manager.sso.keycloak.event.ApimanEventListenerOptions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.StringJoiner;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.jboss.logging.Logger;
import org.keycloak.common.util.Retry;
import org.keycloak.common.util.Time;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientProvider;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.TokenManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.Urls;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.TokenUtil;

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
    public void onEvent(KeycloakSession session, Event event,
         ProviderFactory<HttpClientProvider> httpClientFactory) {

        typeCheck(event);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Processing a REGISTER event: {0}", ToStringBuilder.reflectionToString(event));
        }

        RealmModel realm = session.realms().getRealm(event.getRealmId());
        UserModel newRegisteredUser = session.users().getUserById(event.getUserId(), realm);
        LOGGER.debugv("Realm: {0}, User: {1}", realm.getName(), newRegisteredUser.getUsername());

        // Push to Apiman, retry a few times.
        HttpClientProvider clientProvider = httpClientFactory.create(session);
        Retry.executeWithBackoff(
             (attempt) -> postEventToApiman(attempt, session, realm, clientProvider, event,
                  newRegisteredUser),
             this::logRetries,
             ATTEMPTS_COUNT,
             INTERVAL_BASE_MILLIS
        );
    }

    private String generateToken(RealmModel realm, KeycloakSession session) {
        ClientProvider clientProvider = session.getProvider(ClientProvider.class);
        ClientModel client = clientProvider.getClientByClientId(realm, options.getClientId());
        UserModel serviceAccount = session.getProvider(UserProvider.class).getServiceAccount(client);
        KeycloakContext ctx = session.getContext();

        long issuedAt = Time.currentTime();
        JsonWebToken token = new AccessToken()
             .id(KeycloakModelUtils.generateId())
             .type(TokenUtil.TOKEN_TYPE_BEARER)
             .subject(serviceAccount.getId())
             .issuedNow()
             .issuedFor(client.getClientId())
             .audience(client.getName())
             .subject(serviceAccount.getId())
             .issuer(
                Urls.realmIssuer(
                    ctx.getUri().getBaseUri(),
                    ctx.getRealm().getName()
                )
             )
             .iat(issuedAt)
             .exp(issuedAt + 240L);

        TokenManager tokenManager = session.tokens();
        // TODO(msavy): should we offer JWE? Might be nice for low-trust environments or plaintext tokenManager.encodeAndEncrypt
        return tokenManager.encode(token);
    }

    private void postEventToApiman(int attempt, KeycloakSession session, RealmModel realm,
         HttpClientProvider client, Event event, UserModel user) {
        try {
            URI apimanEventEndpoint = calculateURI();

            NewAccountCreatedDto newAccount = buildApimanEvent(event, user);
            HttpEntity payload = EntityBuilder.create()
                                              .setContentType(ContentType.APPLICATION_JSON)
                                              .setText(JsonSerialization.writeValueAsPrettyString(newAccount))
                                              .build();

            LOGGER.debugv("Attempt {0} to POST {1} to {2}",
                 attempt, newAccount, apimanEventEndpoint);

            HttpPost post = new HttpPost();
            post.setHeader("Authorization", "Bearer " + generateToken(realm, session));
            post.setURI(apimanEventEndpoint);
            post.setEntity(payload);

            HttpResponse response = client.getHttpClient().execute(post);

            if (response.getStatusLine().getStatusCode() / 100 != 2) {
                String msg = MessageFormat.format("Non-200 response code returned by {0}: {1}.",
                     apimanEventEndpoint, response.getStatusLine().getReasonPhrase());
                LOGGER.error(msg);
                throw new IOException(msg); // Throwing exception here will trigger retry.
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private NewAccountCreatedDto buildApimanEvent(Event event, UserModel user) {
        OffsetDateTime time = OffsetDateTime.ofInstant(
             Instant.ofEpochMilli(user.getCreatedTimestamp()),
             ZoneId.of("UTC")
        );
        return new NewAccountCreatedDto()
             .setUserId(event.getUserId())
             .setEmailAddress(user.getEmail())
             .setFirstName(user.getFirstName())
             .setSurname(user.getLastName())
             .setTime(time);
    }

    private URI calculateURI() {
        return UriBuilder.fromUri(options.getApiManagerUri())
                         .path("notifications")
                         .path("system")
                         .path("users")
                         .build();
    }

    private void typeCheck(Event event) {
        if (!event.getType().equals(EventType.REGISTER)) {
            String msg = MessageFormat.format("Expected to process {0} but got {1}", EventType.REGISTER,
                 event.getType());
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
