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
 * Bean returned for the "Overall Usage" metric.  The data returned is a
 * set of sparse data points over a histogram date range.  The period of
 * each bucket is dependent upon the granularity specified in the request.
 *
 * @author eric.wittmann@redhat.com
 */
public class UsageHistogramBean {

    private List<UsageDataPoint> data = new ArrayList<>();

    /**
     * Constructor.
     */
    public UsageHistogramBean() {
    }

    /**
     * @return the data
     */
    public List<UsageDataPoint> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(List<UsageDataPoint> data) {
        this.data = data;
    }

}
