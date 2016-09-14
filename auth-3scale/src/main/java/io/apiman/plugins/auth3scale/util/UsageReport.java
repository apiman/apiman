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
package io.apiman.plugins.auth3scale.util;

/**
 * Usage information for an AuthRep or Authorize
 */
public class UsageReport {

    private String metric = "";
    private String period = "";
    private String periodStart = "";
    private String periodEnd = "";
    private String currentValue = "";
    private String maxValue = "";
    private boolean hasExceeded = false;

    /**
     * Create a UsageReport
     *
     * @param metric
     * @param period
     * @param periodStart
     * @param periodEnd
     * @param currentValue
     * @param maxValue
     * @param hasExceeded
     */
    public UsageReport(String metric, String period, String periodStart, String periodEnd, String currentValue, String maxValue, String hasExceeded) {
        this.metric = metric;
        this.period = period;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.currentValue = currentValue;
        this.maxValue = maxValue;
        setHasExceeded(hasExceeded);
    }

    private void setHasExceeded(String hasExceeded) {
        if (hasExceeded.toLowerCase().equals("true")) {
            this.hasExceeded = true;
        } else {
            this.hasExceeded = false;
        }
    }

    /**
     * Get the Name of the Metric
     *
     * @return name
     */
    public String getMetric() {
        return metric;
    }

    /**
     * Get the period of the metric
     *
     * @return
     */
    public String getPeriod() {
        return period;
    }

    /**
     * Get the current value of the metric
     *
     * @return
     */
    public String getCurrentValue() {
        return currentValue;
    }

    /**
     * Get the maximum value of the metric
     *
     * @return
     */
    public String getMaxValue() {
        return maxValue;
    }

    /**
     * Get the start of period as a String
     *
     * @return YYYY-MM-DD HH:mm:SS +NNNN  Year-Month-Day Hour:Minute:second Offset from UTC
     */
    public String getPeriodStart() {
        return periodStart;
    }

    /**
     * Get the end of period as a String
     *
     * @return YYYY-MM-DD HH:mm:SS +NNNN  Year-Month-Day Hour:Minute:second Offset from UTC
     */
    public String getPeriodEnd() {
        return periodEnd;
    }

    /**
     * Returns the hasExceeded flag
     *
     * @return true if the metrics have been exceeded.
     */
    public boolean hasExceeded() {
        return hasExceeded;
    }
}
