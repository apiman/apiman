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
 * Internal representation of a API contract.  Ties together a API and
 * a client by its contract (api key).
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiContract implements Serializable {

    private static final long serialVersionUID = -4264090614804457252L;

    private String apikey;
    private Api api;
    private Client client;
    private String plan;
    private List<Policy> policies = new ArrayList<>();

    /**
     * Constructor.
     */
    public ApiContract() {
    }

    /**
     * Constructor.
     * @param apikey the api key
     * @param api the api
     * @param client the client
     * @param policies the list of policies
     */
    public ApiContract(String apikey, Api api, Client client, String plan, List<Policy> policies) {
        setApikey(apikey);
        setApi(api);
        setClient(client);
        setPlan(plan);
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
     * @return the api
     */
    public Api getApi() {
        return api;
    }

    /**
     * @param api the api to set
     */
    public void setApi(Api api) {
        this.api = api;
    }

    /**
     * @return the client
     */
    public Client getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(Client client) {
        this.client = client;
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
        result = prime * result + (apikey == null ? 0 : apikey.hashCode());
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
        ApiContract other = (ApiContract) obj;
        if (apikey == null) {
            if (other.apikey != null)
                return false;
        } else if (!apikey.equals(other.apikey))
            return false;
        return true;
    }

    /**
     * @return the plan
     */
    public String getPlan() {
        return plan;
    }

    /**
     * @param plan the plan to set
     */
    public void setPlan(String plan) {
        this.plan = plan;
    }

}
