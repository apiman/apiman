package io.apiman.manager.sso.keycloak;

import io.apiman.common.config.options.GenericOptionsParser;

import java.net.URI;
import java.util.Map;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApimanProviderFactoryOptionsParser extends GenericOptionsParser {
    private URI apiManagerUri;

    public ApimanProviderFactoryOptionsParser(Map<String, String> options) {
        super(options);
    }

    @Override
    protected void parse(Map<String, String> options) {
        super.parse(options);
        apiManagerUri = getRequiredUri(
             keys(ApimanEventProviderConfigConstants.APIMAN_MANAGER_API_URI),
             this::hasRequiredUriComponents,
             "Apiman Manager endpoint must contain protocol/scheme (e.g. http), host, port (e.g. 8080), "
                  + "and any path segment (e.g. /apiman). "
                  + "Examples: https://localhost:8443/apiman or http://localhost:8080/apiman"
        );
    }

    public URI getApiManagerUri() {
        return apiManagerUri;
    }

    private boolean hasRequiredUriComponents(URI uri) {
        return uri.getScheme() != null
             && isHttpOrHttps(uri)
             && uri.getHost() != null
             && uri.getPort() != -1;
    }

    private boolean isHttpOrHttps(URI uri) {
        return "http".equalsIgnoreCase(uri.getScheme())
             || "https".equalsIgnoreCase(uri.getScheme());
    }
}
