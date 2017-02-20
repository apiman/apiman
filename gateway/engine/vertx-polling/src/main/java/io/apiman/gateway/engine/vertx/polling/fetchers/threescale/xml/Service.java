/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.engine.vertx.polling.fetchers.threescale.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class Service {
    private long id;
    private long accountId;
    private String name;
    private String state;
    private String systemName;
    private boolean endUserRegistrationRequired;
    private List<Metric> metrics = new ArrayList<>();
    private List<Method> methods = new ArrayList<>();
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
    /**
     * @return the accountId
     */
    public long getAccountId() {
        return accountId;
    }
    /**
     * @param accountId the accountId to set
     */
    public void setAccountId(long accountId) {
        this.accountId = accountId;
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
     * @return the state
     */
    public String getState() {
        return state;
    }
    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }
    /**
     * @return the systemName
     */
    public String getSystemName() {
        return systemName;
    }
    /**
     * @param systemName the systemName to set
     */
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
    /**
     * @return the endUserRegistrationRequired
     */
    public boolean isEndUserRegistrationRequired() {
        return endUserRegistrationRequired;
    }
    /**
     * @param endUserRegistrationRequired the endUserRegistrationRequired to set
     */
    public void setEndUserRegistrationRequired(boolean endUserRegistrationRequired) {
        this.endUserRegistrationRequired = endUserRegistrationRequired;
    }
    /**
     * @return the metrics
     */
    public List<Metric> getMetrics() {
        return metrics;
    }
    /**
     * @param metrics the metrics to set
     */
    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }
    /**
     * @return the methods
     */
    public List<Method> getMethods() {
        return methods;
    }
    /**
     * @param methods the methods to set
     */
    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }
}
