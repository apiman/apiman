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

/**
 * A single data point in the usage histogram.
 *
 * @author eric.wittmann@redhat.com
 */
public class UsageDataPoint extends HistogramDataPoint {

    private long count;

    /**
     * Constructor.
     */
    public UsageDataPoint() {
    }

    /**
     * Constructor.
     * @param label
     * @param count
     */
    public UsageDataPoint(String label, long count) {
        super(label);
        setCount(count);
    }

    /**
     * @return the count
     */
    public long getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(long count) {
        this.count = count;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return getLabel() + "[" + getCount() + "]";
    }

}
