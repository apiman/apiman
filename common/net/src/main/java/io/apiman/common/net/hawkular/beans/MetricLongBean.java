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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author eric.wittmann@gmail.com
 */
public class MetricLongBean {
    
    private String id;
    private List<DataPointLongBean> dataPoints = new ArrayList<>();
    private MetricType type = MetricType.counter;
    private Map<String, String> tags;
    
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
    public DataPointLongBean addDataPoint(Date timestamp, long value) {
        DataPointLongBean point = new DataPointLongBean(timestamp, value);
        dataPoints.add(point);
        return point;
    }

    /**
     * Adds a single data point to the metric.
     * @param timestamp
     * @param value
     * @param tags
     */
    public DataPointLongBean addDataPoint(Date timestamp, long value, Map<String, String> tags) {
        DataPointLongBean point = new DataPointLongBean(timestamp, value);
        for (Entry<String, String> entry : tags.entrySet()) {
            point.addTag(entry.getKey(), entry.getValue());
        }
        dataPoints.add(point);
        return point;
    }

    /**
     * @param name
     * @param value
     */
    public void addTag(String name, String value) {
        if (this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.put(name, value);
    }
    
    /**
     * Removes a single tag.
     * @param name
     */
    public void removeTag(String name) {
        if (this.tags != null) {
            this.tags.remove(name);
        }
    }
    
    /**
     * Clear the tags for the data point.
     */
    public void clearTags() {
        if (this.tags != null) {
            this.tags.clear();
        }
    }

    /**
     * @return the tags
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
    
}
