package io.vertx.apiman.gateway.platforms.vertx2.services;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.Iterator;

@DataObject
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

    // TODO Hack until Vert.x tidy this up - no clean & easy way of going JsonObject <-> POJO
    public VertxServiceRequest(JsonObject json) {
        this(Json.decodeValue(json.toString(), VertxServiceRequest.class));
    }

    public VertxServiceRequest(VertxServiceRequest copy) {
        this((ServiceRequest) copy);
    }

    // TODO hack until clean way of going POJO <-> JsonObject
    public JsonObject toJson() {
        return new JsonObject(Json.encode(this));
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
