package io.apiman.manager.sso.keycloak.event.processors;

import io.apiman.manager.api.beans.events.dto.NewAccountCreatedDto;
import io.apiman.manager.sso.keycloak.approval.AccountApprovalRequiredActionFactory;
import io.apiman.manager.sso.keycloak.event.ApimanEventListenerOptions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.ws.rs.core.UriBuilder;

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
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientProvider;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.utils.KeycloakModelUtils;
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
public class NewUserEventProcessor {

    private static final Logger LOGGER = Logger.getLogger(NewUserEventProcessor.class);
    private static final int ATTEMPTS_COUNT = 10;
    private static final int INTERVAL_BASE_MILLIS = 1_000;
    private final ApimanEventListenerOptions options;
    private final KeycloakSession session;
    private final HttpClientProvider httpClient;

    public NewUserEventProcessor(ApimanEventListenerOptions options, KeycloakSession session, KeycloakSessionFactory keycloakSessionFactory) {
        this.options = options;
        this.session = session;
        this.httpClient = keycloakSessionFactory.getProviderFactory(HttpClientProvider.class).create(session);
    }

    /**
     * Process a {@link org.keycloak.events.EventType#REGISTER} and send to Apiman Manager API via HTTP.
     */
    public void process(Event event) {
        RealmModel realm = session.realms().getRealm(event.getRealmId());
        UserModel newRegisteredUser = session.users().getUserById(event.getUserId(), realm);
        LOGGER.debugv("Realm: {0}, User: {1}", realm.getName(), newRegisteredUser.getUsername());

        // Push to Apiman, retry a few times.
        Retry.executeWithBackoff(
             (attempt) -> postEventToApiman(attempt, realm, event, newRegisteredUser),
             this::logRetries,
             ATTEMPTS_COUNT,
             INTERVAL_BASE_MILLIS
        );
    }

    private String generateToken(RealmModel realm) {
        ClientProvider clientProvider = session.getProvider(ClientProvider.class);
        ClientModel client = clientProvider.getClientByClientId(realm, options.getClientId());
        UserModel serviceAccount = session.getProvider(UserProvider.class).getServiceAccount(client);
        KeycloakContext ctx = session.getContext();

        long timestampNow = Time.currentTimeMillis();
        JsonWebToken token = new AccessToken()
             .id(KeycloakModelUtils.generateId())
             .type(TokenUtil.TOKEN_TYPE_BEARER)
             .subject(serviceAccount.getId())
             .issuedFor(client.getClientId())
             .audience(client.getName())
             .subject(serviceAccount.getId())
             .issuer(
                Urls.realmIssuer(
                    ctx.getUri().getBaseUri(),
                    ctx.getRealm().getName()
                )
             )
             .iat(timestampNow)
             .exp(timestampNow + Duration.ofMinutes(5).toMillis());
        // TODO(msavy): should we offer JWE? Might be nice for low-trust environments or plaintext tokenManager.encodeAndEncrypt
        return session.tokens().encode(token);
    }

    private void postEventToApiman(int attempt, RealmModel realm, Event event, UserModel user) {
        try {
            URI apimanEventEndpoint = calculateURI();

            NewAccountCreatedDto newAccount = buildApimanEvent(event, user);
            HttpEntity payload = EntityBuilder.create()
                                              .setContentType(ContentType.APPLICATION_JSON)
                                              .setText(JsonSerialization.writeValueAsPrettyString(newAccount))
                                              .build();

            LOGGER.debugv("Attempt {0} to POST {1} to {2}", attempt, newAccount, apimanEventEndpoint);

            HttpPost post = new HttpPost();
            post.setHeader("Authorization", "Bearer " + generateToken(realm));
            post.setURI(apimanEventEndpoint);
            post.setEntity(payload);

            HttpResponse response = httpClient.getHttpClient().execute(post);

            if (response.getStatusLine().getStatusCode() / 100 != 2) {
                String msg = MessageFormat.format(
                     "Retryable action {0}/{1}: Non-200 response code returned by {2}: {3}.",
                     attempt, ATTEMPTS_COUNT, apimanEventEndpoint, response.getStatusLine().getReasonPhrase()
                );
                IOException ex = new IOException(msg);
                LOGGER.error(msg, ex);
                throw ex; // Throwing exception here will trigger retry.
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

        boolean approvalRequired = user
             .getRequiredActionsStream()
             .anyMatch(n -> n.equals(AccountApprovalRequiredActionFactory.PROVIDER_ID));

        // TODO(msavy): Does this cover all role permutations? Might need to consider groups etc also.
        Set<String> roles = user.getRoleMappingsStream().map(RoleModel::getName).collect(Collectors.toSet());

        return new NewAccountCreatedDto()
             .setUserId(event.getUserId())
             .setUsername(user.getUsername())
             .setEmailAddress(user.getEmail())
             .setFirstName(user.getFirstName())
             .setSurname(user.getLastName())
             .setTime(time)
             .setRoles(roles)
             .setAttributes(user.getAttributes())
             .setApprovalRequired(approvalRequired);
    }

    private URI calculateURI() {
        return UriBuilder.fromUri(options.getApiManagerUri())
                         .path("events")
                         .path("sso")
                         .path("users")
                         .build();
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
