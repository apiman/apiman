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

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.Iterator;

/**
 * Wrapped {@link VertxPolicyFailure} with Vertx specific converters
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@DataObject(generateConverter = true, inheritConverter = true)
public class VertxPolicyFailure extends PolicyFailure {
    private static final long serialVersionUID = 6303864238553908191L;
    private String rawRepresentation;

    public VertxPolicyFailure(PolicyFailureType type, int failureCode, String message) {
        super(type, failureCode, message);
    }

    public VertxPolicyFailure() {
        super();
    }

    public VertxPolicyFailure(PolicyFailure copy) {
        super();
        setType(copy.getType());
        setFailureCode(copy.getFailureCode());
        setResponseCode(copy.getResponseCode());
        setMessage(copy.getMessage());
        setHeaders(copy.getHeaders());
        setRaw(Json.encode(copy)); // TODO horrible, fixme
    }

    public VertxPolicyFailure(JsonObject json) {
        VertxPolicyFailureConverter.fromJson(json, this);
    }

    public VertxPolicyFailure(VertxPolicyFailure copy) {
        this((PolicyFailure) copy);
    }

    public JsonObject toJson() {
        JsonObject asJson = new JsonObject();
        VertxPolicyFailureConverter.toJson(this, asJson);
        return asJson;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        final int maxLen = 10;
        return "VertxPolicyFailure [toJson()=" + toJson() + ", getMessage()=" + getMessage()
                + ", getFailureCode()=" + getFailureCode() + ", getType()=" + getType() + ", getHeaders()="
                + (getHeaders() != null ? toString(getHeaders().getEntries(), maxLen) : null)
                + ", getResponseCode()=" + getResponseCode() + ", getClass()=" + getClass() + ", hashCode()="
                + hashCode() + "]";
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

    public String getRaw() {
        return rawRepresentation;
    }

    public void setRaw(String raw) {
        this.rawRepresentation = raw;
    }
}
