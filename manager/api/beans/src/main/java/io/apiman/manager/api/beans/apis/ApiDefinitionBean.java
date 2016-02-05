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
package io.apiman.manager.api.beans.apis;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Bean used to store an API definition.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
@Table(name = "api_defs")
public class ApiDefinitionBean implements Serializable {

    private static final long serialVersionUID = 7744514362366320690L;

    @Id
    private long id;
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="api_version_id")
    private ApiVersionBean apiVersion;
    @Lob
    private byte[] data;

    /**
     * Constructor.
     */
    public ApiDefinitionBean() {
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the apiVersion
     */
    public ApiVersionBean getApiVersion() {
        return apiVersion;
    }

    /**
     * @param apiVersion the apiVersion to set
     */
    public void setApiVersion(ApiVersionBean apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        final int maxLen = 10;
        return "ApiDefinitionBean [id=" + id + ", apiVersion=" + apiVersion + ", data="
                + (data != null ? Arrays.toString(Arrays.copyOf(data, Math.min(data.length, maxLen))) : null)
                + "]";
    }
}
