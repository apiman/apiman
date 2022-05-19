package io.apiman.manager.api.schema.format;

import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;

import com.fasterxml.jackson.databind.JsonNode;
import io.apicurio.datamodels.core.models.Document;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface OAIRewriter {
    void rewrite(ProviderContext ctx, Document schema) throws StorageException, GatewayAuthenticationException;
}
