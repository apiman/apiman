package io.apiman.manager.api.schema.format;

import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.IGatewayLink;

import java.net.URI;
import java.util.List;

import io.apicurio.datamodels.core.models.Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;

/**
 * Replace <code>host</code> and <code>basePath</code> with gateway equivalents.
 * OAI2 does not support multiple servers, so we just use the first one.
 *
 * @see <a href="https://swagger.io/specification/v2/">OAI v2/Swagger spec</a>
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class OpenApi2 implements OAIRewriter {

    @Override
    public void rewrite(ProviderContext ctx, Document schema) throws StorageException, GatewayAuthenticationException {
        // Prepare the data we need to extract to build the servers
        ApiVersionBean avb = ctx.getAvb();
        String orgId = avb.getApi().getOrganization().getId();
        String apiId = avb.getApi().getId();
        String apiVersion = avb.getVersion();

        // Find IDs of all GWs this ApiVersion is published onto.
        String firstGateway = avb.getGateways().stream()
                                          .map(ApiGatewayBean::getGatewayId)
                                          .findFirst()
                                          .orElse("");

        GatewayBean gateway = ctx.getStorage().getGateway(firstGateway);
        IGatewayLink link = ctx.getGatewayLinkFactory().create(gateway);
        String apiEndpoint = link.getApiEndpoint(orgId, apiId, apiVersion).getEndpoint();
        URI apiEndpointUri = URI.create(apiEndpoint);

        // We can guarantee it's an OAS2.x doc (aka. Swagger v2).
        Oas20Document oas2 = (Oas20Document) schema;
        oas2.schemes = List.of(apiEndpointUri.getScheme());
        if (apiEndpointUri.getPort() == -1) {
            oas2.host = apiEndpointUri.getHost();
        } else {
            oas2.host = apiEndpointUri.getHost() + ":" + apiEndpointUri.getPort();
        }
        oas2.basePath = apiEndpointUri.getPath();
    }

}
