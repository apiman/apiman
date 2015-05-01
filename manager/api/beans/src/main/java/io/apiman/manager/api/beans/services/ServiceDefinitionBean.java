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
package io.apiman.manager.api.beans.services;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Bean used to store a service definition.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
@Table(name = "service_defs")
public class ServiceDefinitionBean implements Serializable {

    private static final long serialVersionUID = 7744514362366320690L;

    @Id
    private long id;
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="serviceVersionId")
    private ServiceVersionBean serviceVersion;
    @Lob
    private byte[] data;

    /**
     * Constructor.
     */
    public ServiceDefinitionBean() {
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
     * @return the serviceVersion
     */
    public ServiceVersionBean getServiceVersion() {
        return serviceVersion;
    }

    /**
     * @param serviceVersion the serviceVersion to set
     */
    public void setServiceVersion(ServiceVersionBean serviceVersion) {
        this.serviceVersion = serviceVersion;
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
}
