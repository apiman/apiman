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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author eric.wittmann@gmail.com
 */
public class MetricLongBean {
    
    private String id;
    private List<DataPointLongBean> dataPoints = new ArrayList<>();
    private MetricType type = MetricType.counter;
    
    /**
     * Constructor.
     */
    public MetricLongBean() {
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
     * @return the dataPoints
     */
    public List<DataPointLongBean> getDataPoints() {
        return dataPoints;
    }

    /**
     * @param dataPoints the dataPoints to set
     */
    public void setDataPoints(List<DataPointLongBean> dataPoints) {
        this.dataPoints = dataPoints;
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
     * Adds a single data point to the metric.
     * @param timestamp
     * @param value
     */
    public void addDataPoint(Date timestamp, long value) {
        DataPointLongBean point = new DataPointLongBean(timestamp, value);
        dataPoints.add(point);
    }

}
