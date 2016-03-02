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
     * @param api the api
     * @param client the client
     * @param policies the list of policies
     */
    public ApiContract(Api api, Client client, String plan, List<Policy> policies) {
        setApi(api);
        setClient(client);
        setPlan(plan);
        setPolicies(policies);
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
