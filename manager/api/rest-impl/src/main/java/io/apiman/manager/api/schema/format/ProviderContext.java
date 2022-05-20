package io.apiman.manager.api.schema.format;

import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;

import java.util.StringJoiner;

/**
 * Context provided to all {@link ApiDefinitionProvider}s.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ProviderContext {
    private final IStorage storage;
    private final IStorageQuery query;
    private final ApiVersionBean avb;
    private final IGatewayLinkFactory gatewayLinkFactory;

    public ProviderContext(IStorage storage, IStorageQuery query, ApiVersionBean avb, IGatewayLinkFactory gatewayLinkFactory) {
        this.storage = storage;
        this.query = query;
        this.avb = avb;
        this.gatewayLinkFactory = gatewayLinkFactory;
    }

    public IStorage getStorage() {
        return storage;
    }

    public IStorageQuery getQuery() {
        return query;
    }

    public ApiVersionBean getAvb() {
        return avb;
    }

    public IGatewayLinkFactory getGatewayLinkFactory() {
        return gatewayLinkFactory;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ProviderContext.class.getSimpleName() + "[", "]")
                       .add("storage=" + storage)
                       .add("query=" + query)
                       .add("avb=" + avb)
                       .add("gatewayLinkFactory=" + gatewayLinkFactory)
                       .toString();
    }
}
