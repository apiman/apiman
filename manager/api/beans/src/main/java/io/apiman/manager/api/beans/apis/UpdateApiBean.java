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
package io.apiman.manager.api.beans.apis;

import io.apiman.manager.api.beans.apis.dto.KeyValueTagDto;
import io.apiman.manager.api.beans.download.BlobReference;

import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * Bean used when updating an API.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonInclude(Include.NON_NULL)
public class UpdateApiBean implements Serializable {

    private static final long serialVersionUID = 8811488441452291116L;

    private String description;
    @JsonIgnore
    @BlobReference
    private String image;
    private Set<KeyValueTagDto> tags;

    /**
     * Constructor.
     */
    public UpdateApiBean() {
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

    public String getImage() {
        return image;
    }

    public UpdateApiBean setImage(String imageRef) {
        this.image = imageRef;
        return this;
    }

    public Set<KeyValueTagDto> getTags() {
        return tags;
    }

    public UpdateApiBean setTags(Set<KeyValueTagDto> tags) {
        this.tags = tags;
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "UpdateApiBean [description=" + description + "]";
    }

}
