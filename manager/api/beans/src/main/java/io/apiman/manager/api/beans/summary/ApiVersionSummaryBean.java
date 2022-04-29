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
package io.apiman.manager.api.beans.summary;

import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.dto.KeyValueTagDto;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * A summary of an individual API version.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiVersionSummaryBean implements Serializable {

    private static final long serialVersionUID = -5287420561319780618L;

    private String organizationId;
    private String organizationName;
    private String id;
    private String name;
    private String description;
    private String extendedDescription;
    private ApiStatus status;
    private String version;
    private boolean publicAPI;
    private DiscoverabilityLevel publicDiscoverability = DiscoverabilityLevel.ORG_MEMBERS;
    private Set<KeyValueTagDto> apiTags;

    /**
     * Constructor.
     */
    public ApiVersionSummaryBean() {
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
     * @return the organizationName
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * @param organizationName the organizationName to set
     */
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the status
     */
    public ApiStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(ApiStatus status) {
        this.status = status;
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

    public String getExtendedDescription() {
        return extendedDescription;
    }

    public ApiVersionSummaryBean setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
        return this;
    }

    public DiscoverabilityLevel getPublicDiscoverability() {
        return publicDiscoverability;
    }

    public ApiVersionSummaryBean setPublicDiscoverability(DiscoverabilityLevel publicDiscoverability) {
        this.publicDiscoverability = publicDiscoverability;
        return this;
    }

    public Set<KeyValueTagDto> getApiTags() {
        return apiTags;
    }

    public ApiVersionSummaryBean setApiTags(Set<KeyValueTagDto> apiTags) {
        this.apiTags = apiTags;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApiVersionSummaryBean.class.getSimpleName() + "[", "]")
                .add("organizationId='" + organizationId + "'")
                .add("organizationName='" + organizationName + "'")
                .add("id='" + id + "'")
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .add("extendedDescription='" + extendedDescription + "'")
                .add("status=" + status)
                .add("version='" + version + "'")
                .add("publicAPI=" + publicAPI)
                .add("publicDiscoverability=" + publicDiscoverability)
                .add("apiTags=" + apiTags)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiVersionSummaryBean that = (ApiVersionSummaryBean) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
