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
package io.apiman.manager.api.beans.clients;

import io.apiman.manager.api.beans.download.BlobReference;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Bean used when updating an client.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonInclude(Include.NON_NULL)
public class UpdateClientBean implements Serializable {

    private static final long serialVersionUID = 5549391329361400489L;

    private String description;
    @JsonIgnore
    @BlobReference
    private String image;

    /**
     * Constructor.
     */
    public UpdateClientBean() {
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

    public UpdateClientBean setImage(String image) {
        this.image = image;
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "UpdateClientBean [description=" + description + "]";
    }

}
