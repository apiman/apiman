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
package io.apiman.manager.api.beans.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for histogram beans.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class HistogramBean<T extends HistogramDataPoint> {

    private List<T> data = new ArrayList<>();

    /**
     * @return the data
     */
    public List<T> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * Adds a single data point.
     * @param point
     */
    public void addDataPoint(T point) {
        getData().add(point);
    }

}
