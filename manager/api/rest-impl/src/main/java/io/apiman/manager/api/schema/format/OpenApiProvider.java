package io.apiman.manager.api.schema.format;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.core.models.Document;
import io.apicurio.datamodels.core.models.DocumentType;

/**
 * Parse and rewrite OpenAPI/Swagger v2 and OpenAPI v3 schemas to point to the Apiman managed gateway endpoints.
 * <p>
 * This implementation uses Jackson to parse JSON and YAML representations into the generic {@link JsonNode} structure,
 * which can be handled by the underlying OAI libraries we use to parse and rewrite the schemas (apicurio-oai-libaries).
 * <p>
 * Actual rewriting is dispatched to the specific handlers for that version as they work very differently.
 *
 * @see OpenApi2 OpenApi v2 and Swagger
 * @see OpenApi3 OpenApi v3
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class OpenApiProvider implements ApiDefinitionProvider {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(OpenApiProvider.class);
    private static final ObjectMapper JSON_OM = new ObjectMapper()
                                                        .findAndRegisterModules()
                                                        .enable(SerializationFeature.INDENT_OUTPUT);

    private static final ObjectMapper YAML_OM = new ObjectMapper(new YAMLFactory())
                                                        .findAndRegisterModules()
                                                        .enable(SerializationFeature.INDENT_OUTPUT);

    private static final OpenApi2 OPEN_API_2 = new OpenApi2();
    private static final OpenApi3 OPEN_API_3 = new OpenApi3();

    @Override
    public String rewrite(ProviderContext providerCtx, InputStream is, ApiDefinitionType apiDefinitionType)
            throws IOException, StorageException, GatewayAuthenticationException {
        JsonNode root = om(apiDefinitionType).readTree(is);
        Document parsedLib = Library.readDocument(root);
        DocumentType docType = parsedLib.getDocumentType();
        switch (docType) {
            case openapi2:
                OPEN_API_2.rewrite(providerCtx, parsedLib);
                break;
            case openapi3:
                OPEN_API_3.rewrite(providerCtx, parsedLib);
                break;
            case asyncapi2:
            default:
                LOGGER.warn("We don't know how to handle {0} (yet), returning schema without doing anything", docType);
        }
        Object writeOut = Library.writeNode(parsedLib);
        return om(apiDefinitionType).writeValueAsString(writeOut);
    }

    private ObjectMapper om(ApiDefinitionType apiDefinitionType) {
        if (apiDefinitionType == ApiDefinitionType.SwaggerJSON) {
            return JSON_OM;
        }
        if (apiDefinitionType == ApiDefinitionType.SwaggerYAML) {
            return YAML_OM;
        }
        throw new IllegalArgumentException("This rewriter only supports SwaggerJSON or SwaggerYAML: " + apiDefinitionType);
    }
}
