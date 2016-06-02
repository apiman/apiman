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
public class ResponseStatsDataPoint extends HistogramDataPoint {

    private long total;
    private long failures;
    private long errors;

    /**
     * Constructor.
     */
    public ResponseStatsDataPoint() {
    }

    /**
     * Constructor.
     * @param label
     * @param total
     * @param failures
     * @param errors
     */
    public ResponseStatsDataPoint(String label, long total, long failures, long errors) {
        super(label);
        setTotal(total);
        setFailures(failures);
        setErrors(errors);
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * @return the failures
     */
    public long getFailures() {
        return failures;
    }

    /**
     * @param failures the failures to set
     */
    public void setFailures(long failures) {
        this.failures = failures;
    }

    /**
     * @return the errors
     */
    public long getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(long errors) {
        this.errors = errors;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return getLabel() + "[t:" + getTotal() + " f:" + getFailures() + " e:" + getErrors() + "]";
    }

}
