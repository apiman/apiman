package io.vertx.apiman.gateway.platforms.vertx2.services;

import io.apiman.gateway.engine.beans.ServiceResponse;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.Iterator;

@DataObject
public class VertxServiceResponse extends ServiceResponse {

    private static final long serialVersionUID = 1205823836132916146L;

    public VertxServiceResponse() {
        super();
    }

    public VertxServiceResponse(ServiceResponse copy) {
        super();
        setAttributes(copy.getAttributes());
        setCode(copy.getCode());
        setHeaders(copy.getHeaders());
        setMessage(copy.getMessage());
    }

    // TODO Hack until Vert.x tidy this up - no clean & easy way of going JsonObject <-> POJO
    public VertxServiceResponse(JsonObject json) {
        this(Json.decodeValue(json.toString(), VertxServiceResponse.class));
    }

    public VertxServiceResponse(VertxServiceResponse copy) {
        this((ServiceResponse) copy);
    }

    // TODO hack until clean way of going POJO <-> JsonObject
    public JsonObject toJson() {
        return new JsonObject(Json.encode(this));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        final int maxLen = 10;
        return "VertxServiceResponse [getHeaders()="
                + (getHeaders() != null ? toString(getHeaders().entrySet(), maxLen) : null) + ", getCode()="
                + getCode() + ", getMessage()=" + getMessage() + ", getAttributes()="
                + (getAttributes() != null ? toString(getAttributes().entrySet(), maxLen) : null)
                + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + "]";
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
