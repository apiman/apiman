package io.apiman.manager.api.schema.format;

import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface ApiDefinitionProvider {
    /**
     * Parse the input stream from the provider's format into a Jackson JsonNode structure (returning the root node).
     */
    String rewrite(ProviderContext providerCtx, InputStream is, ApiDefinitionType apiDefinitionType)
            throws IOException, StorageException, GatewayAuthenticationException, Exception;
}
