package io.apiman.gateway.platforms.vertx2.io;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.Iterator;

@DataObject(generateConverter = true, inheritConverter = true)
public class VertxServiceRequest extends ServiceRequest {

    private static final long serialVersionUID = 4847852261198092180L;

    public VertxServiceRequest() {
        super();
    }

    public VertxServiceRequest(ServiceRequest copy) {
        super();
        setApiKey(copy.getApiKey());
        setContract(copy.getContract());
        setDestination(copy.getDestination());
        setHeaders(copy.getHeaders());
        setQueryParams(copy.getQueryParams());
        setRawRequest(copy.getRawRequest());
        setRemoteAddr(copy.getRemoteAddr());
        setServiceId(copy.getServiceId());
        setServiceOrgId(copy.getServiceOrgId());
        setServiceVersion(copy.getServiceVersion());
        setTransportSecure(copy.isTransportSecure());
        setType(copy.getType());
    }

    public VertxServiceRequest(JsonObject json) {
        VertxServiceRequestConverter.fromJson(json, this);
    }

    public VertxServiceRequest(VertxServiceRequest copy) {
        this((ServiceRequest) copy);
    }

    public JsonObject toJson() {
        JsonObject asJson = new JsonObject();
        VertxServiceRequestConverter.toJson(this, asJson);
        return asJson;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        final int maxLen = 10;
        return "VertxServiceRequest [getApiKey()=" + getApiKey() + ", getRawRequest()=" + getRawRequest()
                + ", getType()=" + getType() + ", getHeaders()="
                + (getHeaders() != null ? toString(getHeaders().entrySet(), maxLen) : null)
                + ", getDestination()=" + getDestination() + ", getRemoteAddr()=" + getRemoteAddr()
                + ", getContract()=" + getContract() + ", getServiceOrgId()=" + getServiceOrgId()
                + ", getServiceId()=" + getServiceId() + ", getServiceVersion()=" + getServiceVersion()
                + ", getQueryParams()="
                + (getQueryParams() != null ? toString(getQueryParams().entrySet(), maxLen) : null)
                + ", isTransportSecure()=" + isTransportSecure() + ", getClass()=" + getClass()
                + ", hashCode()=" + hashCode() + "]";
    }

    @SuppressWarnings("nls")
    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }
}
