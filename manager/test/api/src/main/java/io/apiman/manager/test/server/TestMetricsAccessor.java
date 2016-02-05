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
package io.apiman.manager.test.server;

import io.apiman.manager.api.beans.metrics.ClientUsagePerApiBean;
import io.apiman.manager.api.beans.metrics.HistogramIntervalType;
import io.apiman.manager.api.beans.metrics.ResponseStatsHistogramBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerClientBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerPlanBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsSummaryBean;
import io.apiman.manager.api.beans.metrics.UsageDataPoint;
import io.apiman.manager.api.beans.metrics.UsageHistogramBean;
import io.apiman.manager.api.beans.metrics.UsagePerClientBean;
import io.apiman.manager.api.beans.metrics.UsagePerPlanBean;
import io.apiman.manager.api.core.IMetricsAccessor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.joda.time.DateTime;

/**
 * Simple metrics accessor with hard coded data.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
@SuppressWarnings("nls")
public class TestMetricsAccessor implements IMetricsAccessor {

    /**
     * Constructor.
     */
    public TestMetricsAccessor() {
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsage(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, java.util.Date, java.util.Date)
     */
    @Override
    public UsageHistogramBean getUsage(String organizationId, String apiId, String version,
            HistogramIntervalType interval, DateTime from, DateTime to) {
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
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerClient(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)
     */
    @Override
    public UsagePerClientBean getUsagePerClient(String organizationId, String apiId, String version,
            DateTime from, DateTime to) {
        UsagePerClientBean rval = new UsagePerClientBean();
        rval.getData().put("my-client", 120384L);
        rval.getData().put("foo-client", 1263L);
        rval.getData().put("bar-client", 726392L);
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerPlan(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)
     */
    @Override
    public UsagePerPlanBean getUsagePerPlan(String organizationId, String apiId, String version,
            DateTime from, DateTime to) {
        UsagePerPlanBean rval = new UsagePerPlanBean();
        rval.getData().put("Gold", 120384L);
        rval.getData().put("Silver", 921263L);
        rval.getData().put("Platinum", 726392L);
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStats(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsHistogramBean getResponseStats(String organizationId, String apiId,
            String version, HistogramIntervalType interval, DateTime from, DateTime to) {
        ResponseStatsHistogramBean rval = new ResponseStatsHistogramBean();
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsSummary(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsSummaryBean getResponseStatsSummary(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        ResponseStatsSummaryBean rval = new ResponseStatsSummaryBean();
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsPerClient(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsPerClientBean getResponseStatsPerClient(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        ResponseStatsPerClientBean rval = new ResponseStatsPerClientBean();
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsPerPlan(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsPerPlanBean getResponseStatsPerPlan(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        ResponseStatsPerPlanBean rval = new ResponseStatsPerPlanBean();
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getClientUsagePerApi(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ClientUsagePerApiBean getClientUsagePerApi(String organizationId, String clientId,
            String version, DateTime from, DateTime to) {
        ClientUsagePerApiBean rval = new ClientUsagePerApiBean();
        return rval;
    }

}
