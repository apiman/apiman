package io.apiman.manager.api.security.impl;

import io.apiman.manager.api.beans.idm.UserDto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class KeycloakAdminClient {

    public static final String APIMAN_CLIENT = "apiman";
    private final KeycloakDeployment keycloakDeployment;

    public KeycloakAdminClient(KeycloakDeployment keycloakDeployment) {
        this.keycloakDeployment = keycloakDeployment;
    }

    /**
     * Please be careful when using this not to ask for roles that may have a massive number of users.
     * <p>
     * If that is required, then a paginated approach may be necessary.
     * <p>
     * Ensure that the APIMAN_CLIENT has 'service accounts enabled' set to true. This allows Apiman to speak to Keycloak
     * using the client name + secret to interact with the Keycloak API.
     * <p>
     * Ignores users who are not enabled.
     *
     * @param roleName the name of the role.
     * @return the user IDs of users for a given role.
     */
    public List<UserDto> getUsersForRole(String roleName) {
        Keycloak client = getClient();
        Set<UserRepresentation> users = client.realm(keycloakDeployment.getRealm())
                                              .clients().get(APIMAN_CLIENT)
                                              .roles().get(roleName)
                                              .getRoleUserMembers();
        return users.stream()
                    .filter(UserRepresentation::isEnabled)
                    // .filter(UserRepresentation::isEmailVerified)
                    .map(this::toUserBean)
                    .collect(Collectors.toList());
    }

    private UserDto toUserBean(UserRepresentation userRepresentation) {
        return new UserDto()
             .setId(userRepresentation.getId())
             .setUsername(userRepresentation.getUsername())
             .setEmail(userRepresentation.getEmail())
             .setFullName(userRepresentation.getFirstName() + " " + userRepresentation.getLastName());
    }

    private Keycloak getClient() {
        String secret = (String) keycloakDeployment.getResourceCredentials().get("secret");

        if (secret == null) {
            throw new IllegalArgumentException("No client secret defined in Keycloak config");
        }

        return Keycloak.getInstance(
             keycloakDeployment.getAuthServerBaseUrl(),
             keycloakDeployment.getRealm(),
             APIMAN_CLIENT, // TODO can I get this from the deployment somehow?
             secret,
             APIMAN_CLIENT
        );
    }
}
