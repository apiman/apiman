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

public class Method {
    private long id;
    private long serviceId;
    private long metricId;
    private String name;
    private String systemName;
    private String friendlyName;
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
     * @return the serviceId
     */
    public long getServiceId() {
        return serviceId;
    }
    /**
     * @param serviceId the serviceId to set
     */
    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }
    /**
     * @return the metricId
     */
    public long getMetricId() {
        return metricId;
    }
    /**
     * @param metricId the metricId to set
     */
    public void setMetricId(long metricId) {
        this.metricId = metricId;
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
     * @return the friendlyName
     */
    public String getFriendlyName() {
        return friendlyName;
    }
    /**
     * @param friendlyName the friendlyName to set
     */
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
}
