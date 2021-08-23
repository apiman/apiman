package io.apiman.manager.sso.keycloak.event;

import io.apiman.common.config.options.GenericOptionsParser;
import io.apiman.common.config.options.Predicates;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

/**
 * Options for Apiman's Keycloak event listener
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApimanEventListenerOptions extends GenericOptionsParser {
    private URI apiManagerUri;
    private String clientId;

    public ApimanEventListenerOptions(Map<String, String> options) {
        super(options);
    }

    @Override
    protected void parse(Map<String, String> options) {
        this.options = (TreeMap) options;
        apiManagerUri = getRequiredUri(
             keys("apimanManagerUrl"),
             this::hasRequiredUriComponents,
             "Apiman Manager endpoint must contain protocol/scheme (e.g. http), host, "
                  + "port (e.g. 8080), and any path segment (e.g. /apiman). "
                  + "Examples: https://localhost:8443/apiman or http://localhost:8080/apiman"
        );
        this.clientId = getString(
             keys("clientId"),
             "apiman-service-account",
             Predicates.noWhitespace(),
             Predicates.noWhitespaceMsg()
        );
    }

    /**
     * Get URI for the Apiman Manager API
     */
    public URI getApiManagerUri() {
        return apiManagerUri;
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

    /**
     * {@inheritDoc}
     */
    public String getClientId() {
        return clientId;
    }
}
