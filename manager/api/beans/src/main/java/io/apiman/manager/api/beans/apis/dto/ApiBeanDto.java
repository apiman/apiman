/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.beans.apis.dto;

import io.apiman.manager.api.beans.apis.KeyValueTag;
import io.apiman.manager.api.beans.download.BlobReference;
import io.apiman.manager.api.beans.orgs.OrganizationBean;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * API Bean DTO
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApiBeanDto {

    private OrganizationBean organization;
    private String id;
    private String name;
    @BlobReference
    private String image;
    private String description;
    private Set<KeyValueTag> tags = new HashSet<>();
    private String createdBy;
    private Date createdOn;
    private Integer numPublished;

    public ApiBeanDto() {
    }

    public OrganizationBean getOrganization() {
        return organization;
    }

    public ApiBeanDto setOrganization(OrganizationBean organization) {
        this.organization = organization;
        return this;
    }

    public String getId() {
        return id;
    }

    public ApiBeanDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ApiBeanDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getImage() {
        return image;
    }

    public ApiBeanDto setImage(String image) {
        this.image = image;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ApiBeanDto setDescription(String description) {
        this.description = description;
        return this;
    }

    public Set<KeyValueTag> getTags() {
        return tags;
    }

    public ApiBeanDto setTags(Set<KeyValueTag> tags) {
        this.tags = tags;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public ApiBeanDto setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public ApiBeanDto setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public Integer getNumPublished() {
        return numPublished;
    }

    public ApiBeanDto setNumPublished(Integer numPublished) {
        this.numPublished = numPublished;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiBeanDto that = (ApiBeanDto) o;
        return Objects.equals(organization, that.organization) && Objects.equals(id, that.id) && Objects.equals(name, that.name)
                       && Objects.equals(image, that.image) && Objects.equals(description, that.description) && Objects.equals(tags, that.tags)
                       && Objects.equals(createdBy, that.createdBy) && Objects.equals(createdOn, that.createdOn) && Objects.equals(numPublished,
                that.numPublished);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, id, name, image, description, tags, createdBy, createdOn, numPublished);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApiBeanDto.class.getSimpleName() + "[", "]")
                .add("organization=" + organization)
                .add("id='" + id + "'")
                .add("name='" + name + "'")
                .add("image='" + image + "'")
                .add("description='" + description + "'")
                .add("tags=" + tags)
                .add("createdBy='" + createdBy + "'")
                .add("createdOn=" + createdOn)
                .add("numPublished=" + numPublished)
                .toString();
    }
}
