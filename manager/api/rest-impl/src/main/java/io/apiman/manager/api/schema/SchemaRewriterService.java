package io.apiman.manager.api.schema;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.schema.format.ApiDefinitionProvider;
import io.apiman.manager.api.schema.format.OpenApiProvider;
import io.apiman.manager.api.schema.format.ProviderContext;
import io.apiman.manager.api.schema.format.WsdlRewriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;
import javax.inject.Inject;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.io.CharStreams;
import com.google.common.io.FileBackedOutputStream;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.nullness.qual.PolyNull;

/**
 * Rewrite elements of schemas, such as OpenAPI and WSDL, to point to the Apiman managed endpoint rather than the
 * original endpoint(s) if there are any.
 * <p>
 * The specifics of what the transformation is entirely dependent on the schema supports.
 *
 * <h2>Implementation Details</h2>
 * <ul>
 *     <li>This class performs actions that are required by all providers</li>
 *     <li>A map of ApiDefinitionType to providers is maintained. These are called automatically for a given API Version</li>
 *     <li>A successfully rewritten API definition is cached for 5 minutes to prevent repeatedly transforming the same document, which can be expensive.
 *     Also, in theory, a gateway can return arbitrary URI patterns (this is pluggable behaviour, and could be dynamic to some extent).
 *     Hence, we can't entirely guarantee this URI will be stable/immutable. As a compromise, caching preserves good performance.
 *     This also helps with situations where a gateway goes down temporarily, or is being reconfigured (the change will be picked up after a while).</li>
 *     <li>The cached schemas are stored in a {@link FileBackedOutputStream}, which uses a 256KB size limit before spilling over to temp file storage.
 *     This prevents memory bloat in the case of very large schemas.</li>
 *     <li>If no provider is available for a given format, </li>
 * </ul>
 *
 * @see io.apiman.manager.api.schema.format.OpenApi2
 * @see io.apiman.manager.api.schema.format.OpenApi3
 * @see io.apiman.manager.api.schema.format.WsdlRewriter
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */

public class SchemaRewriterService {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(SchemaRewriterService.class);
    private static final ApiDefinitionProvider PASSTHROUGH_PROVIDER = new NoOpProvider();
    private static final Cache<SchemaCacheKey, FileBackedOutputStream> SCHEMA_CACHE = Caffeine.newBuilder()
                                                                    .expireAfterWrite(Duration.ofMinutes(5)) // TODO(msavy): make configurable
                                                                    .build();
    private static final Map<ApiDefinitionType, ApiDefinitionProvider> SCHEMA_HANDLERS;

    static {
        var swaggerProvider = new OpenApiProvider();
        SCHEMA_HANDLERS = Map.of(
                ApiDefinitionType.SwaggerJSON, swaggerProvider,
                ApiDefinitionType.SwaggerYAML, swaggerProvider,
                ApiDefinitionType.WSDL, new WsdlRewriter()
        );
    }

    private IStorage storage;
    private IStorageQuery query;
    private IGatewayLinkFactory gatewayLinkFactory;

    @Inject
    public SchemaRewriterService(IStorage storage, IStorageQuery query, IGatewayLinkFactory gatewayLinkFactory) {
        this.storage = storage;
        this.query = query;
        this.gatewayLinkFactory = gatewayLinkFactory;
    }

    public SchemaRewriterService() {
    }

    public FileBackedOutputStream rewrite(ApiVersionBean avb, InputStream is, ApiDefinitionType type) throws Exception {
        var ctx = new ProviderContext(
                storage,
                query,
                avb,
                gatewayLinkFactory
        );
        var cacheKey = new SchemaCacheKey(ctx.getAvb().getId(), type);
        // Due to checked exceptions, can't use the `#get(key, (cache) -> { lambda });` form
        @PolyNull FileBackedOutputStream cachedEntry = SCHEMA_CACHE.getIfPresent(cacheKey);
        if (cachedEntry != null) {
            return cachedEntry;
        } else {
            ApiDefinitionProvider handler = SCHEMA_HANDLERS.getOrDefault(type, PASSTHROUGH_PROVIDER);
            String rewritten = handler.rewrite(ctx, is, type);
            FileBackedOutputStream fbos = new FileBackedOutputStream(Math.toIntExact(FileUtils.ONE_KB * 256));
            fbos.write(rewritten.getBytes(StandardCharsets.UTF_8));
            SCHEMA_CACHE.put(cacheKey, fbos);
            return fbos;
        }
    }
    private static final class NoOpProvider implements ApiDefinitionProvider {

        @Override
        public String rewrite(ProviderContext providerCtx, InputStream is, ApiDefinitionType apiDefinitionType)
                throws IOException, StorageException, GatewayAuthenticationException {
            LOGGER.debug("No basepath/hostname rewriter for API Definition Type {0}. Will just read into a string.", apiDefinitionType);
            // Do nothing, this is some kind of schema we have no rewriting capability for.
            try (Reader reader = new InputStreamReader(is)) {
                return CharStreams.toString(reader);
            }
        }
    }

    private static class SchemaCacheKey {
        private final long apiVersionId;
        private final ApiDefinitionType apiDefinitionType;

        private SchemaCacheKey(long apiVersionId, ApiDefinitionType apiDefinitionType) {
            this.apiVersionId = apiVersionId;
            this.apiDefinitionType = apiDefinitionType;
        }

        public long getApiVersionId() {
            return apiVersionId;
        }

        public ApiDefinitionType getApiDefinitionType() {
            return apiDefinitionType;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", SchemaCacheKey.class.getSimpleName() + "[", "]")
                           .add("apiVersionId=" + apiVersionId)
                           .add("apiDefinitionType=" + apiDefinitionType)
                           .toString();
        }
    }
}
