/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.common.net.hawkular.beans;

/**
 * @author eric.wittmann@gmail.com
 */
public class MetricBean {
    
    private String id;
    private int dataRetention;
    private MetricType type;
    private String tenantId;

    /**
     * Constructor.
     */
    public MetricBean() {
        
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the dataRetention
     */
    public int getDataRetention() {
        return dataRetention;
    }

    /**
     * @param dataRetention the dataRetention to set
     */
    public void setDataRetention(int dataRetention) {
        this.dataRetention = dataRetention;
    }

    /**
     * @return the type
     */
    public MetricType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(MetricType type) {
        this.type = type;
    }

    /**
     * @return the tenantId
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId the tenantId to set
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
}
