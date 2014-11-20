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
import java.util.List;

/**
 * Internal representation of a service contract.  Ties together a service and
 * an application by its contract (api key).
 *
 * @author eric.wittmann@redhat.com
 */
public class ServiceContract implements Serializable {

    private static final long serialVersionUID = -4264090614804457252L;

    private String apikey;
    private Service service;
    private Application application;
    private List<Policy> policies = new ArrayList<Policy>();

    /**
     * Constructor.
     * @param apikey
     * @param service
     * @param application
     * @param policies
     */
    public ServiceContract(String apikey, Service service, Application application, List<Policy> policies) {
        setApikey(apikey);
        setService(service);
        setApplication(application);
        setPolicies(policies);
    }

    /**
     * @return the apikey
     */
    public String getApikey() {
        return apikey;
    }

    /**
     * @param apikey the apikey to set
     */
    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    /**
     * @return the service
     */
    public Service getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(Service service) {
        this.service = service;
    }

    /**
     * @return the application
     */
    public Application getApplication() {
        return application;
    }

    /**
     * @param application the application to set
     */
    public void setApplication(Application application) {
        this.application = application;
    }

    /**
     * @return the policies
     */
    public List<Policy> getPolicies() {
        return policies;
    }

    /**
     * @param policies the policies to set
     */
    public void setPolicies(List<Policy> policies) {
        this.policies = policies;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((apikey == null) ? 0 : apikey.hashCode());
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
        ServiceContract other = (ServiceContract) obj;
        if (apikey == null) {
            if (other.apikey != null)
                return false;
        } else if (!apikey.equals(other.apikey))
            return false;
        return true;
    }

}
