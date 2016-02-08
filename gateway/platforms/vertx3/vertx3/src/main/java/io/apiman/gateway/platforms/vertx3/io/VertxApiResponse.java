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

import io.apiman.gateway.engine.beans.ApiResponse;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.Iterator;

/**
 * Wrapped {@link ApiResponse} with Vertx specific converters
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@DataObject(generateConverter = true, inheritConverter = true)
public class VertxApiResponse extends ApiResponse {

    private static final long serialVersionUID = 1205823836132916146L;

    public VertxApiResponse() {
        super();
    }

    public VertxApiResponse(ApiResponse copy) {
        super();
        setAttributes(copy.getAttributes());
        setCode(copy.getCode());
        setHeaders(copy.getHeaders());
        setMessage(copy.getMessage());
    }

    public VertxApiResponse(JsonObject json) {
        VertxApiResponseConverter.fromJson(json, this);
    }

    public VertxApiResponse(VertxApiResponse copy) {
        this((ApiResponse) copy);
    }

    public JsonObject toJson() {
        JsonObject asJson = new JsonObject();
        VertxApiResponseConverter.toJson(this, asJson);
        return asJson;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        final int maxLen = 10;
        return "VertxApiResponse [getHeaders()="
                + (getHeaders() != null ? toString(getHeaders().getEntries(), maxLen) : null) + ", getCode()="
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
