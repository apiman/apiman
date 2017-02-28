/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.engine.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Models an API published to the API Management runtime.
 *
 * @author eric.wittmann@redhat.com
 */
public class Api implements Serializable {

    private static final long serialVersionUID = -294764695917891050L;

    private boolean publicAPI;
    private String organizationId;
    private String apiId;
    private String version;
    private String endpoint;
    private String endpointType;
    private String endpointContentType;
    private Map<String, String> endpointProperties = new HashMap<>();
    private boolean parsePayload;
    @JsonIgnore
    private transient long maxPayloadBufferSize;
    private List<Policy> apiPolicies = new ArrayList<>();
    @JsonIgnore
    private transient Map<String, Object> cache = new HashMap<>();

    /**
     * Constructor.
     */
    public Api() {
    }

    /**
     * @return the organizationId
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * @param organizationId the organizationId to set
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * @return the apiId
     */
    public String getApiId() {
        return apiId;
    }

    /**
     * @param apiId the apiId to set
     */
    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    /**
     * @return the endpointType
     */
    public String getEndpointType() {
        return endpointType;
    }

    /**
     * @param endpointType the endpointType to set
     */
    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    /**
     * @return the endpointContentType
     */
    public String getEndpointContentType() {
        return endpointContentType;
    }

    /**
     * @param endpointContentType the endpointContentType to set
     */
    public void setEndpointContentType(String endpointContentType) {
        this.endpointContentType = endpointContentType;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return the endpointProperties
     */
    public Map<String, String> getEndpointProperties() {
        return endpointProperties;
    }

    /**
     * @param endpointProperties the endpointProperties to set
     */
    public void setEndpointProperties(Map<String, String> endpointProperties) {
        this.endpointProperties = endpointProperties;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the publicAPI
     */
    public boolean isPublicAPI() {
        return publicAPI;
    }

    /**
     * @param publicAPI the publicAPI to set
     */
    public void setPublicAPI(boolean publicAPI) {
        this.publicAPI = publicAPI;
    }

    /**
     * @return the apiPolicies
     */
    public List<Policy> getApiPolicies() {
        return apiPolicies;
    }

    /**
     * @param apiPolicies the apiPolicies to set
     */
    public void setApiPolicies(List<Policy> apiPolicies) {
        this.apiPolicies = apiPolicies;
    }

    /**
     * @return the parsePayload
     */
    public boolean isParsePayload() {
        return parsePayload;
    }

    /**
     * @param parsePayload the parsePayload to set
     */
    public void setParsePayload(boolean parsePayload) {
        this.parsePayload = parsePayload;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((organizationId == null) ? 0 : organizationId.hashCode());
        result = prime * result + ((apiId == null) ? 0 : apiId.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Api other = (Api) obj;
        if (organizationId == null) {
            if (other.organizationId != null)
                return false;
        } else if (!organizationId.equals(other.organizationId))
            return false;
        if (apiId == null) {
            if (other.apiId != null)
                return false;
        } else if (!apiId.equals(other.apiId))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        final int maxLen = 10;
        return "API [publicAPI=" + publicAPI + ", organizationId=" + organizationId
                + ", apiId=" + apiId + ", version=" + version + ", endpointType=" + endpointType
                + ", endpoint=" + endpoint + ", endpointProperties="
                + (endpointProperties != null ? toString(endpointProperties.entrySet(), maxLen) : null)
                + ", apiPolicies=" + (apiPolicies != null ? toString(apiPolicies, maxLen) : null)
                + ", parsePayload=" + parsePayload
                + "]";
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

    /**
     * @return the maxPayloadBufferSize
     */
    public long getMaxPayloadBufferSize() {
        return maxPayloadBufferSize;
    }

    /**
     * @param maxPayloadBufferSize the maxPayloadBufferSize to set
     */
    public void setMaxPayloadBufferSize(long maxPayloadBufferSize) {
        this.maxPayloadBufferSize = maxPayloadBufferSize;
    }

    /**
     * @return the cache
     */
    public Map<String, Object> getCache() {
        return cache;
    }

    /**
     * @param cache the cache to set
     */
    public void setCache(Map<String, Object> cache) {
        this.cache = cache;
    }

}
