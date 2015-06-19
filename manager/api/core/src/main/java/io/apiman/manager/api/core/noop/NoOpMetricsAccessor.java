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
package io.apiman.manager.api.core.noop;

import io.apiman.manager.api.beans.metrics.UsageDataPoint;
import io.apiman.manager.api.beans.metrics.UsageHistogramBean;
import io.apiman.manager.api.beans.metrics.UsageHistogramIntervalType;
import io.apiman.manager.api.beans.metrics.UsagePerAppBean;
import io.apiman.manager.api.beans.metrics.UsagePerPlanBean;
import io.apiman.manager.api.core.IMetricsAccessor;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

/**
 * A no-op implementaiton of {@link IMetricsAccessor}.  Useful for situations where
 * no metrics are available.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class NoOpMetricsAccessor implements IMetricsAccessor {

    /**
     * Constructor.
     */
    public NoOpMetricsAccessor() {
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsage(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.UsageHistogramIntervalType, java.util.Date, java.util.Date)
     */
    @Override
    public UsageHistogramBean getUsage(String organizationId, String serviceId, String version,
            UsageHistogramIntervalType interval, Date from, Date to) {
        UsageHistogramBean rval = new UsageHistogramBean();
        rval.getData().add(new UsageDataPoint("2015-06-01T00:00:00.000Z", 17));
        rval.getData().add(new UsageDataPoint("2015-06-01T01:00:00.000Z", 1));
        rval.getData().add(new UsageDataPoint("2015-06-01T02:00:00.000Z", 1));
        rval.getData().add(new UsageDataPoint("2015-06-01T03:00:00.000Z", 29));
        rval.getData().add(new UsageDataPoint("2015-06-01T04:00:00.000Z", 19));
        rval.getData().add(new UsageDataPoint("2015-06-01T05:00:00.000Z", 52));
        rval.getData().add(new UsageDataPoint("2015-06-01T06:00:00.000Z", 6));
        rval.getData().add(new UsageDataPoint("2015-06-01T07:00:00.000Z", 4));
        rval.getData().add(new UsageDataPoint("2015-06-01T08:00:00.000Z", 5));
        rval.getData().add(new UsageDataPoint("2015-06-01T09:00:00.000Z", 27));
        rval.getData().add(new UsageDataPoint("2015-06-01T10:00:00.000Z", 19));
        rval.getData().add(new UsageDataPoint("2015-06-01T11:00:00.000Z", 52));
        rval.getData().add(new UsageDataPoint("2015-06-01T12:00:00.000Z", 6));
        rval.getData().add(new UsageDataPoint("2015-06-01T13:00:00.000Z", 4));
        rval.getData().add(new UsageDataPoint("2015-06-01T14:00:00.000Z", 2));
        rval.getData().add(new UsageDataPoint("2015-06-01T15:00:00.000Z", 17));
        rval.getData().add(new UsageDataPoint("2015-06-01T16:00:00.000Z", 1));
        rval.getData().add(new UsageDataPoint("2015-06-01T17:00:00.000Z", 1));
        rval.getData().add(new UsageDataPoint("2015-06-01T18:00:00.000Z", 29));
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerApp(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)
     */
    @Override
    public UsagePerAppBean getUsagePerApp(String organizationId, String serviceId, String version,
            Date fromDate, Date toDate) {
        UsagePerAppBean rval = new UsagePerAppBean();
        rval.getData().put("my-app", 120384L);
        rval.getData().put("foo-app", 1263L);
        rval.getData().put("bar-app", 726392L);
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerPlan(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)
     */
    @Override
    public UsagePerPlanBean getUsagePerPlan(String organizationId, String serviceId, String version,
            Date fromDate, Date toDate) {
        UsagePerPlanBean rval = new UsagePerPlanBean();
        rval.getData().put("Gold", 120384L);
        rval.getData().put("Silver", 921263L);
        rval.getData().put("Platinum", 726392L);
        return rval;
    }

}
