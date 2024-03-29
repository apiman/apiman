package io.apiman.manager.sso.keycloak.approval;

import io.apiman.common.config.options.GenericOptionsParser;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import static io.apiman.common.config.options.Predicates.anyOk;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class AccountApprovalOptions extends GenericOptionsParser {

    public static final String DEFAULT_ADMIN_ROLE = "apiadmin";
    private String adminRole;
    private URI notApprovedUri;

    public AccountApprovalOptions(Map<String, String> options) {
        super(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void parse(Map<String, String> options) {
        this.options = (TreeMap) options;
        adminRole = getString(keys("adminRole"), DEFAULT_ADMIN_ROLE, anyOk(), "");
        notApprovedUri = getRequiredUri(keys("notApprovedRedirectUrl"), this::hasRequiredUriComponents,
             "Must provide http or https URI to redirect unapproved accounts to.");
    }

    /**
     * Get the role that represents an administrator (who should be able to bypass approval checks).
     * @return admin role name
     */
    public String getAdminRole() {
        return adminRole;
    }

    /**
     * URI to redirect browser to when their account has not yet been approved.
     * @return redirect URI
     */
    public URI getNotApprovedUri() {
        return notApprovedUri;
    }

    private boolean hasRequiredUriComponents(URI uri) {
        return uri.getScheme() != null
             && isHttpOrHttps(uri)
             && uri.getHost() != null;
    }

    private boolean isHttpOrHttps(URI uri) {
        return "http".equalsIgnoreCase(uri.getScheme())
             || "https".equalsIgnoreCase(uri.getScheme());
    }
}
