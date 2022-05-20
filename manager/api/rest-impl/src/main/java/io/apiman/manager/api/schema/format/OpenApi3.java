package io.apiman.manager.api.schema.format;

import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.IGatewayLink;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.apicurio.datamodels.core.models.Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import org.apache.commons.lang.StringUtils;

/**
 * Remove existing <code>servers</code> sections and instead insert with Apiman Gateway URLs.
 * Multiple gateways are allowed, so we can insert all of them.
 * Name and description are incorporated into the description field of the server.
 *
 * @see <a href="https://swagger.io/specification/">OpenAPI v3 specification</a>
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class OpenApi3 implements OAIRewriter {

    @Override
    public void rewrite(ProviderContext ctx, Document schema) throws StorageException, GatewayAuthenticationException {
        // Prepare the data we need to extract to build the servers
        ApiVersionBean avb = ctx.getAvb();
        String orgId = avb.getApi().getOrganization().getId();
        String apiId = avb.getApi().getId();
        String apiVersion = avb.getVersion();

        // Find IDs of all GWs this ApiVersion is published onto.
        Set<String> gatewayIds = avb.getGateways().stream()
                                          .map(ApiGatewayBean::getGatewayId)
                                          .collect(Collectors.toUnmodifiableSet());

        Set<ApiEndpointWithDescription> endpoints = new HashSet<>(gatewayIds.size());

        for (GatewayBean gateway : ctx.getStorage().getGateways(gatewayIds)) {
            IGatewayLink link = ctx.getGatewayLinkFactory().create(gateway);
            endpoints.add(new ApiEndpointWithDescription(
                    link.getApiEndpoint(orgId, apiId, apiVersion).getEndpoint(),
                    gateway.getName(),
                    gateway.getDescription()
            ));
        }

        // We can guarantee it's an OAS3.x doc.
        Oas30Document oas3 = (Oas30Document) schema;
        // For now, we just ditch the inbuilt servers and list ours. Later we may do something more intelligent with the user's input.
        if (oas3.servers != null) {
            oas3.servers.clear();
        }
        // Generate new server endpoints with each GW the API Version is published into inserted into the list.
        for (ApiEndpointWithDescription endpoint : endpoints) {
            String nameAndDesc = endpoint.getName();
            if (StringUtils.isNotBlank(endpoint.getDescription())) {
                nameAndDesc += ": " + endpoint.getDescription();
            }
            oas3.addServer(endpoint.getEndpoint(), nameAndDesc);
        }
    }

    private static final class ApiEndpointWithDescription {
        private final String endpoint;
        private final String name;
        private final String description;

        public ApiEndpointWithDescription(String endpoint, String name, String description) {
            this.endpoint = endpoint;
            this.name = name;
            this.description = description;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
