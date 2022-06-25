package io.apiman.manager.sso.keycloak.approval;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;

import org.jboss.logging.Logger;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

/**
 * A user is required to have their account approved (perhaps manually or by an external process).
 *
 * <p>This is achieved by testing whether the {@link AccountApprovalRequiredActionProvider#APIMAN_APPROVAL_ATTRIBUTE}
 * is present on a user's account.
 *
 * <p>If it is not, the user is redirected to a config-specified error URI, with some additional query
 * parameters attached which are likely of use to implementors of that page
 * (see: {@link #buildURI(URI, UserModel)}).
 *
 * <p>As a failsafe in the case of a Keycloak misconfiguration, anyone with a configuration-specified
 * administrator role automatically bypasses the check. See {@link AccountApprovalOptions#getAdminRole()}.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class AccountApprovalRequiredActionProvider implements RequiredActionProvider {
    public static final String APIMAN_APPROVAL_ATTRIBUTE = "apiman-account-approved";

    private static final Logger LOGGER = Logger.getLogger(AccountApprovalRequiredActionProvider.class);

    private final AccountApprovalOptions approvalConfig;

    public AccountApprovalRequiredActionProvider(AccountApprovalOptions approvalConfig) {
        this.approvalConfig = approvalConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    /**
     * A user is required to have their account approved (perhaps manually or by an external process).
     *
     * {@inheritDoc}
     *
     * @see AccountApprovalOptions options for URI redirect, admin role name, etc.
     */
    @Override
    public void requiredActionChallenge(RequiredActionContext requiredActionCtx) {
        RoleModel adminRole = requiredActionCtx.getRealm().getRole(approvalConfig.getAdminRole());
        UserModel user = requiredActionCtx.getUser();
        boolean isApproved = user.getAttributes().containsKey(APIMAN_APPROVAL_ATTRIBUTE);

        if (isApproved || hasAdminRole(user, adminRole)) {
            LOGGER.debugv("Found approved account attribute {0} on account {1} (or has admin role {2}) ✅",
                 APIMAN_APPROVAL_ATTRIBUTE, user.getUsername(), approvalConfig.getAdminRole());
            requiredActionCtx.success();
        } else {
            LOGGER.debugv("Account {0} failed the approval required action challenge (attribute {1}). ⛔️",
                 user.getUsername(), APIMAN_APPROVAL_ATTRIBUTE);
            requiredActionCtx
                 .getAuthenticationSession()
                 .setRedirectUri(buildURI(approvalConfig.getNotApprovedUri(), user));
            requiredActionCtx.failure();
        }
    }

    private boolean hasAdminRole(UserModel user, RoleModel adminRole) {
        if (adminRole == null) {
            LOGGER.warnv("Admin role {0} specified in configuration does not seem to exist",
                 approvalConfig.getAdminRole());
            return false;
        }
        return user.hasRole(adminRole);
    }

    private String buildURI(URI baseURI, UserModel user) {
        return UriBuilder.fromUri(baseURI)
                         .queryParam("username", user.getUsername())
                         .queryParam("userId", user.getId())
                         .queryParam("emailVerified", user.isEmailVerified())
                         .build()
                         .toString();
    }

    @Override
    public void processAction(RequiredActionContext context) {

    }

    @Override
    public void close() {

    }
}
