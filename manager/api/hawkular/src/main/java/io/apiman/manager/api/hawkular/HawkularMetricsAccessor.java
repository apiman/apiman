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
package io.apiman.manager.api.hawkular;

import io.apiman.common.config.options.HttpConnectorOptions;
import io.apiman.common.net.hawkular.HawkularMetricsClient;
import io.apiman.common.net.hawkular.beans.BucketDataPointBean;
import io.apiman.common.net.hawkular.beans.BucketSizeType;
import io.apiman.manager.api.beans.metrics.ClientUsagePerApiBean;
import io.apiman.manager.api.beans.metrics.HistogramIntervalType;
import io.apiman.manager.api.beans.metrics.ResponseStatsDataPoint;
import io.apiman.manager.api.beans.metrics.ResponseStatsHistogramBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerClientBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerPlanBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsSummaryBean;
import io.apiman.manager.api.beans.metrics.UsageDataPoint;
import io.apiman.manager.api.beans.metrics.UsageHistogramBean;
import io.apiman.manager.api.beans.metrics.UsagePerClientBean;
import io.apiman.manager.api.beans.metrics.UsagePerPlanBean;
import io.apiman.manager.api.core.IMetricsAccessor;
import io.apiman.manager.api.core.metrics.AbstractMetricsAccessor;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;

/**
 * An elasticsearch implementation of the {@link IMetricsAccessor} interface.  This
 * implementation knows how to get metrics/analytics information out of an
 * elasticsearch store.
 *
 * @author eric.wittmann@redhat.com
 */
public class HawkularMetricsAccessor extends AbstractMetricsAccessor implements IMetricsAccessor {

    private HawkularMetricsClient client;

    /**
     * Constructor.
     */
    @SuppressWarnings("nls")
    public HawkularMetricsAccessor(Map<String, String> config) {
        String endpoint = config.get("hawkular.endpoint");
        Map<String, String> httpOptions = new HashMap<>();
        httpOptions.put("http.timeouts.read", config.get("http.timeouts.read"));
        httpOptions.put("http.timeouts.write", config.get("http.timeouts.write"));
        httpOptions.put("http.timeouts.connect", config.get("http.timeouts.connect"));
        httpOptions.put("http.followRedirects", config.get("http.followRedirects"));
        client = new HawkularMetricsClient(endpoint, new HttpConnectorOptions(httpOptions));
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsage(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public UsageHistogramBean getUsage(String organizationId, String apiId, String version,
            HistogramIntervalType interval, DateTime from, DateTime to) {
        String tenantId = organizationId;
        String totalCounterId = "apis." + apiId + "." + version + ".Requests.Total"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        BucketSizeType bucketSize = bucketSizeFromInterval(interval);
        List<BucketDataPointBean> data = client.getCounterData(tenantId, totalCounterId, floor(from, interval).toDate(), to.toDate(), bucketSize);
        UsageHistogramBean rval = new UsageHistogramBean();
        for (BucketDataPointBean bucket : data) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(bucket.getStart());
            String label = formatDateWithMillis(calendar);
            UsageDataPoint dataPoint = new UsageDataPoint(label, bucket.getSamples());
            rval.getData().add(dataPoint);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerClient(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public UsagePerClientBean getUsagePerClient(String organizationId, String apiId, String version,
            DateTime from, DateTime to) {
        String tenantId = organizationId;
        String totalCounterId = "apis." + apiId + "." + version + ".Requests.Total"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        Map<String, BucketDataPointBean> data = client.getCounterData(tenantId, totalCounterId, from.toDate(),
                to.toDate(), HawkularMetricsClient.tags("clientId", "*")); //$NON-NLS-1$ //$NON-NLS-2$        

        TopNSortedMap<String, Long> topFive = new TopNSortedMap<>(5);
        for (Entry<String, BucketDataPointBean> entry : data.entrySet()) {
            String key = entry.getKey().substring("clientId".length()); //$NON-NLS-1$
            topFive.put(key, entry.getValue().getSamples());
        }
        UsagePerClientBean rval = new UsagePerClientBean();
        rval.setData(topFive.toMap());
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerPlan(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public UsagePerPlanBean getUsagePerPlan(String organizationId, String apiId, String version,
            DateTime from, DateTime to) {
        String tenantId = organizationId;
        String totalCounterId = "apis." + apiId + "." + version + ".Requests.Total"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        Map<String, BucketDataPointBean> data = client.getCounterData(tenantId, totalCounterId, from.toDate(),
                to.toDate(), HawkularMetricsClient.tags("planId", "*")); //$NON-NLS-1$ //$NON-NLS-2$        

        TopNSortedMap<String, Long> topFive = new TopNSortedMap<>(5);
        for (Entry<String, BucketDataPointBean> entry : data.entrySet()) {
            String key = entry.getKey().substring("planId".length()); //$NON-NLS-1$
            topFive.put(key, entry.getValue().getSamples());
        }
        UsagePerPlanBean rval = new UsagePerPlanBean();
        rval.setData(topFive.toMap());
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStats(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsHistogramBean getResponseStats(String organizationId, String apiId, String version,
            HistogramIntervalType interval, DateTime from, DateTime to) {
        String tenantId = organizationId;
        String totalCounterId = "apis." + apiId + "." + version + ".Requests.Total"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String failureCounterId = "apis." + apiId + "." + version + ".Requests.Failed"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String errorCounterId = "apis." + apiId + "." + version + ".Requests.Errored"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        BucketSizeType bucketSize = bucketSizeFromInterval(interval);
        DateTime fromFloor = floor(from, interval);
        List<BucketDataPointBean> data = client.getCounterData(tenantId, totalCounterId, fromFloor.toDate(), to.toDate(), bucketSize);
        List<BucketDataPointBean> failureData = client.getCounterData(tenantId, failureCounterId, fromFloor.toDate(), to.toDate(), bucketSize);
        List<BucketDataPointBean> errorData = client.getCounterData(tenantId, errorCounterId, fromFloor.toDate(), to.toDate(), bucketSize);
        
        ResponseStatsHistogramBean rval = new ResponseStatsHistogramBean();
        for (int idx = 0; idx < data.size(); idx++) {
            BucketDataPointBean totalDataPoint = data.get(idx);
            BucketDataPointBean failureDataPoint = failureData.get(idx);
            BucketDataPointBean errorDataPoint = errorData.get(idx);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(totalDataPoint.getStart());
            String label = formatDateWithMillis(calendar);
            ResponseStatsDataPoint dataPoint = new ResponseStatsDataPoint(label, totalDataPoint.getSamples(),
                    failureDataPoint.getSamples(), errorDataPoint.getSamples());
            rval.getData().add(dataPoint);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsSummary(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsSummaryBean getResponseStatsSummary(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        String tenantId = organizationId;
        String totalCounterId = "apis." + apiId + "." + version + ".Requests.Total"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String failureCounterId = "apis." + apiId + "." + version + ".Requests.Failed"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String errorCounterId = "apis." + apiId + "." + version + ".Requests.Errored"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        List<BucketDataPointBean> data = client.getCounterData(tenantId, totalCounterId, from.toDate(), to.toDate(), 1);
        List<BucketDataPointBean> failureData = client.getCounterData(tenantId, failureCounterId, from.toDate(), to.toDate(), 1);
        List<BucketDataPointBean> errorData = client.getCounterData(tenantId, errorCounterId, from.toDate(), to.toDate(), 1);
        
        ResponseStatsSummaryBean rval = new ResponseStatsSummaryBean();
        if (data.size() > 0) {
            BucketDataPointBean totalDataPoint = data.get(0);
            BucketDataPointBean failureDataPoint = failureData.get(0);
            BucketDataPointBean errorDataPoint = errorData.get(0);
            rval.setTotal(totalDataPoint.getSamples());
            rval.setErrors(errorDataPoint.getSamples());
            rval.setFailures(failureDataPoint.getSamples());
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsPerClient(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsPerClientBean getResponseStatsPerClient(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        String tenantId = organizationId;
        String totalCounterId = "apis." + apiId + "." + version + ".Requests.Total"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String failureCounterId = "apis." + apiId + "." + version + ".Requests.Failed"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String errorCounterId = "apis." + apiId + "." + version + ".Requests.Errored"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        Map<String, BucketDataPointBean> totalData = client.getCounterData(tenantId, totalCounterId, from.toDate(),
                to.toDate(), HawkularMetricsClient.tags("clientId", "*")); //$NON-NLS-1$ //$NON-NLS-2$        
        Map<String, BucketDataPointBean> failureData = client.getCounterData(tenantId, failureCounterId, from.toDate(),
                to.toDate(), HawkularMetricsClient.tags("clientId", "*")); //$NON-NLS-1$ //$NON-NLS-2$        
        Map<String, BucketDataPointBean> errorData = client.getCounterData(tenantId, errorCounterId, from.toDate(),
                to.toDate(), HawkularMetricsClient.tags("clientId", "*")); //$NON-NLS-1$ //$NON-NLS-2$        

        TopNSortedMap<String, ResponseStatsDataPoint> topTen = new TopNSortedMap<>(10);
        for (Entry<String, BucketDataPointBean> entry : totalData.entrySet()) {
            String key = entry.getKey().substring("clientId".length()); //$NON-NLS-1$
            ResponseStatsDataPoint dp = new ResponseStatsDataPoint();
            dp.setLabel(key);
            dp.setTotal(entry.getValue().getSamples());
            topTen.put(key, dp);
        }
        for (Entry<String, BucketDataPointBean> entry : failureData.entrySet()) {
            String key = entry.getKey().substring("clientId".length()); //$NON-NLS-1$
            ResponseStatsDataPoint dp = topTen.get(key);
            if (dp != null) {
                dp.setFailures(entry.getValue().getSamples());
            }
        }        
        for (Entry<String, BucketDataPointBean> entry : errorData.entrySet()) {
            String key = entry.getKey().substring("clientId".length()); //$NON-NLS-1$
            ResponseStatsDataPoint dp = topTen.get(key);
            if (dp != null) {
                dp.setErrors(entry.getValue().getSamples());
            }
        }        
        
        ResponseStatsPerClientBean rval = new ResponseStatsPerClientBean();
        rval.setData(topTen.toMap());
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsPerPlan(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsPerPlanBean getResponseStatsPerPlan(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        String tenantId = organizationId;
        String totalCounterId = "apis." + apiId + "." + version + ".Requests.Total"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String failureCounterId = "apis." + apiId + "." + version + ".Requests.Failed"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String errorCounterId = "apis." + apiId + "." + version + ".Requests.Errored"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        Map<String, BucketDataPointBean> totalData = client.getCounterData(tenantId, totalCounterId, from.toDate(),
                to.toDate(), HawkularMetricsClient.tags("planId", "*")); //$NON-NLS-1$ //$NON-NLS-2$        
        Map<String, BucketDataPointBean> failureData = client.getCounterData(tenantId, failureCounterId, from.toDate(),
                to.toDate(), HawkularMetricsClient.tags("planId", "*")); //$NON-NLS-1$ //$NON-NLS-2$        
        Map<String, BucketDataPointBean> errorData = client.getCounterData(tenantId, errorCounterId, from.toDate(),
                to.toDate(), HawkularMetricsClient.tags("planId", "*")); //$NON-NLS-1$ //$NON-NLS-2$        

        TopNSortedMap<String, ResponseStatsDataPoint> topTen = new TopNSortedMap<>(10);
        for (Entry<String, BucketDataPointBean> entry : totalData.entrySet()) {
            String key = entry.getKey().substring("planId".length()); //$NON-NLS-1$
            ResponseStatsDataPoint dp = new ResponseStatsDataPoint();
            dp.setLabel(key);
            dp.setTotal(entry.getValue().getSamples());
            topTen.put(key, dp);
        }
        for (Entry<String, BucketDataPointBean> entry : failureData.entrySet()) {
            String key = entry.getKey().substring("planId".length()); //$NON-NLS-1$
            ResponseStatsDataPoint dp = topTen.get(key);
            if (dp != null) {
                dp.setFailures(entry.getValue().getSamples());
            }
        }        
        for (Entry<String, BucketDataPointBean> entry : errorData.entrySet()) {
            String key = entry.getKey().substring("planId".length()); //$NON-NLS-1$
            ResponseStatsDataPoint dp = topTen.get(key);
            if (dp != null) {
                dp.setErrors(entry.getValue().getSamples());
            }
        }
        
        ResponseStatsPerPlanBean rval = new ResponseStatsPerPlanBean();
        rval.setData(topTen.toMap());
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getClientUsagePerApi(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ClientUsagePerApiBean getClientUsagePerApi(String organizationId, String clientId, String version,
            DateTime from, DateTime to) {
        String tenantId = organizationId;
        String totalCounterId = "clients." + clientId + "." + version + ".Requests.Total"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        Map<String, BucketDataPointBean> data = client.getCounterData(tenantId, totalCounterId, from.toDate(),
                to.toDate(), HawkularMetricsClient.tags("clientId", "*")); //$NON-NLS-1$ //$NON-NLS-2$        

        TopNSortedMap<String, Long> topTen = new TopNSortedMap<>(10);
        for (Entry<String, BucketDataPointBean> entry : data.entrySet()) {
            String key = entry.getKey().substring("clientId".length()); //$NON-NLS-1$
            topTen.put(key, entry.getValue().getSamples());
        }
        ClientUsagePerApiBean rval = new ClientUsagePerApiBean();
        rval.setData(topTen.toMap());
        return rval;
    }


    /**
     * Converts an interval into a bucket size.
     * @param interval
     */
    private static BucketSizeType bucketSizeFromInterval(HistogramIntervalType interval) {
        BucketSizeType bucketSize;
        switch (interval) {
        case minute:
            bucketSize = BucketSizeType.Minute;
            break;
        case hour:
            bucketSize = BucketSizeType.Hour;
            break;
        case day:
            bucketSize = BucketSizeType.Day;
            break;
        case week:
            bucketSize = BucketSizeType.Week;
            break;
        case month:
            bucketSize = BucketSizeType.Month;
            break;
        default:
            bucketSize = BucketSizeType.Day;
            break;
        }
        return bucketSize;
    }

}
