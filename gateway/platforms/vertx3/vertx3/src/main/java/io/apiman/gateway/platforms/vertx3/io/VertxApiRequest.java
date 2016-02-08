/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.gateway.platforms.vertx3.io;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

/**
 * Wrapped {@link ApiRequest} with Vertx specific converters
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@DataObject(generateConverter = true, inheritConverter = true)
public class VertxApiRequest extends ApiRequest {

    private static final long serialVersionUID = 4847852261198092180L;

    public VertxApiRequest() {
        super();
    }

    public VertxApiRequest(ApiRequest copy) {
        super();
        setApiKey(copy.getApiKey());
        setContract(copy.getContract());
        setUrl(copy.getUrl());
        setDestination(copy.getDestination());
        setHeaders(copy.getHeaders());
        setQueryParameters(copy.getQueryParameters());
        setRawRequest(copy.getRawRequest());
        setRemoteAddr(copy.getRemoteAddr());
        setApiId(copy.getApiId());
        setApiOrgId(copy.getApiOrgId());
        setApiVersion(copy.getApiVersion());
        setTransportSecure(copy.isTransportSecure());
        setType(copy.getType());
    }

    public VertxApiRequest(JsonObject json) {
        VertxApiRequestConverter.fromJson(json, this);
    }

    public VertxApiRequest(VertxApiRequest copy) {
        this((ApiRequest) copy);
    }

    public JsonObject toJson() {
        JsonObject asJson = new JsonObject();
        VertxApiRequestConverter.toJson(this, asJson);
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
                + ", getContract()=" + getContract() + ", getServiceOrgId()=" + getApiOrgId()
                + ", getServiceId()=" + getApiId() + ", getServiceVersion()=" + getApiVersion()
                + ", getQueryParams()="
                + (getQueryParameters() != null ? toString(getQueryParameters().entrySet(), maxLen) : null)
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

    int uuid = UUID.randomUUID().hashCode();

    @Override
    public int hashCode() {
        return uuid;
    }

}
