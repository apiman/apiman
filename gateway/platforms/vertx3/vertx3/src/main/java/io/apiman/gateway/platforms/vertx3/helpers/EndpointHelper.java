package io.apiman.gateway.platforms.vertx3.helpers;

import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

/**
 * Helper for Gateway and Api Endpoints (Reading Endpoint from Vertx Config)
 */
public class EndpointHelper {

    private String scheme;
    private int port;
    private String host;
    private String path;

    /**
     * Get the scheme
     * @return scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Get the port
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the host
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the path
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Reads the gateway endpoint data from the vertx configuration
     * @param apimanConfig  vertx configuration
     */
    public EndpointHelper(VertxEngineConfig apimanConfig) {
        scheme = apimanConfig.preferSecure() ? "https" : "http";
        port = apimanConfig.getPort(scheme);
        host = "localhost";
        path = "";
        // If endpoint was manually specified
        if (apimanConfig.getPublicEndpoint() != null) {
            URI publicEndpoint = URI.create(apimanConfig.getPublicEndpoint());

            if (publicEndpoint.getPort() != -1) {
                port = publicEndpoint.getPort();
            }
            if (publicEndpoint.getScheme() != null && !publicEndpoint.getScheme().isEmpty()) {
                scheme = publicEndpoint.getScheme();
            }
            if (publicEndpoint.getPath() != null && !publicEndpoint.getPath().isEmpty()) {
                path = publicEndpoint.getPath();
            }
            if (publicEndpoint.getHost() != null && !publicEndpoint.getHost().isEmpty()) {
                host = publicEndpoint.getHost();
            }
        }
    }


    /**
     * Builds the gateway endpoint
     * @return gateway endpoint
     */
    public String getGatewayEndpoint() {
        StringBuilder endpoint = new StringBuilder();
        endpoint.append(scheme);
        endpoint.append("://");
        endpoint.append(host);

        if (port != 443 && port != 80) {
            endpoint.append(":");
            endpoint.append(port);
        }

        if (path.isEmpty()) {
            endpoint.append("/");
        } else {
            endpoint.append(path);
            if (!StringUtils.endsWith(path, "/"))
                endpoint.append("/");
        }
        return endpoint.toString();
    }

    /**
     * Builds the api endpoint based on the gateway endpoint data
     * @param organizationId Organization Id
     * @param apiId Api Id
     * @param apiVersion Api version
     * @return api endpoint
     */
    public String getApiEndpoint(String organizationId, String apiId, String apiVersion) {
        StringBuilder gatewayEndpoint = new StringBuilder(this.getGatewayEndpoint());
        StringBuilder apiEndpoint = gatewayEndpoint.append(String.join("/", organizationId, apiId, apiVersion));
        return apiEndpoint.toString();
    }

}
