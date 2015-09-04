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
package io.apiman.manager.api.beans.summary;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Models a service catalog JSON file.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ServiceCatalogBean implements Serializable {

    private static final long serialVersionUID = 2410545128036209373L;

    private String name;
    private List<AvailableServiceBean> services;

    /**
     * Constructor.
     */
    public ServiceCatalogBean() {
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
     * @return the services
     */
    public List<AvailableServiceBean> getServices() {
        return services;
    }

    /**
     * @param services the services to set
     */
    public void setServices(List<AvailableServiceBean> services) {
        this.services = services;
    }

}
