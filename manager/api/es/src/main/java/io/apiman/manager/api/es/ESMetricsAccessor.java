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
package io.apiman.manager.api.es;

import io.apiman.common.logging.IApimanLogger;
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
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.metrics.AbstractMetricsAccessor;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.DateHistogramAggregation;
import io.searchbox.core.search.aggregation.DateHistogramAggregation.DateHistogram;
import io.searchbox.core.search.aggregation.FilterAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.joda.time.DateTime;

/**
 * An elasticsearch implementation of the {@link IMetricsAccessor} interface.  This
 * implementation knows how to get metrics/analytics information out of an
 * elasticsearch store.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class ESMetricsAccessor extends AbstractMetricsAccessor implements IMetricsAccessor {

    private static final String INDEX_NAME = "apiman_metrics"; //$NON-NLS-1$

    @Inject @ApimanLogger(ESMetricsAccessor.class)
    IApimanLogger log;

    @Inject @Named("metrics")
    private JestClient esClient;

    /**
     * Constructor.
     */
    public ESMetricsAccessor() {
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsage(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @SuppressWarnings("nls")
    @Override
    public UsageHistogramBean getUsage(String organizationId, String apiId, String version,
            HistogramIntervalType interval, DateTime from, DateTime to) {
        UsageHistogramBean rval = new UsageHistogramBean();
        Map<String, UsageDataPoint> index = generateHistogramSkeleton(rval, from, to, interval, UsageDataPoint.class);

        try {
            String query =
                    "{" +
                    "    \"query\": {" +
                    "        \"bool\": {" +
                    "            \"filter\": [{" +
                    "                    \"term\": {" +
                    "                        \"apiOrgId\": \"${apiOrgId}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiId\": \"${apiId}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiVersion\": \"${apiVersion}\"" +
                    "                    }" +
                    "                }," +
                    "                {" +
                    "                    \"range\": {" +
                    "                        \"requestStart\": {" +
                    "                            \"gte\": \"${from}\"," +
                    "                            \"lte\": \"${to}\"" +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            ]" +
                    "        }" +
                    "    }," +
                    "    \"size\": 0," +
                    "    \"aggs\": {" +
                    "        \"histogram\": {" +
                    "            \"date_histogram\": {" +
                    "                \"field\": \"requestStart\"," +
                    "                \"interval\": \"${interval}\"" +
                    "            }" +
                    "        }" +
                    "    }" +
                    "}";

            Map<String, String> params = new HashMap<>();
            params.put("from", formatDate(from));
            params.put("to", formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));
            params.put("interval", interval.name());
            StrSubstitutor ss = new StrSubstitutor(params);
            query = ss.replace(query);

            Search search = new Search.Builder(query).addIndex(INDEX_NAME).addType("request").build();
            SearchResult response = getEsClient().execute(search);
            MetricAggregation aggregations = response.getAggregations();
            DateHistogramAggregation aggregation = aggregations.getDateHistogramAggregation("histogram");
            if (aggregation != null) {
                List<DateHistogram> buckets = aggregation.getBuckets();
                for (DateHistogram entry : buckets) {
                    String keyAsString = entry.getTimeAsString();
                    if (index.containsKey(keyAsString)) {
                        index.get(keyAsString).setCount(entry.getCount());
                    }
                }
            }
        } catch (IOException e) {
            log.error(e);
        }

        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerClient(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @SuppressWarnings("nls")
    @Override
    public UsagePerClientBean getUsagePerClient(String organizationId, String apiId, String version,
            DateTime from, DateTime to) {
        UsagePerClientBean rval = new UsagePerClientBean();

        try {
            String query =
                    "{" +
                    "    \"query\": {" +
                    "        \"bool\": {" +
                    "            \"filter\": [{" +
                    "                \"term\": {" +
                    "                    \"apiOrgId\": \"${apiOrgId}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiId\": \"${apiId}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiVersion\": \"${apiVersion}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"range\": {" +
                    "                    \"requestStart\": {" +
                    "                        \"gte\": \"${from}\"," +
                    "                        \"lte\": \"${to}\"" +
                    "                    }" +
                    "                }" +
                    "            }]" +
                    "        }" +
                    "    }," +
                    "    \"size\": 0," +
                    "    \"aggs\": {" +
                    "        \"usage_by_client\": {" +
                    "            \"terms\": {" +
                    "                \"field\": \"clientId\"" +
                    "            }" +
                    "        }" +
                    "    }" +
                    "}";

            Map<String, String> params = new HashMap<>();
            params.put("from", formatDate(from));
            params.put("to", formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));
            StrSubstitutor ss = new StrSubstitutor(params);
            query = ss.replace(query);

            Search search = new Search.Builder(query).addIndex(INDEX_NAME).addType("request").build();
            SearchResult response = getEsClient().execute(search);
            MetricAggregation aggregations = response.getAggregations();
            ApimanTermsAggregation aggregation = aggregations.getAggregation("usage_by_client", ApimanTermsAggregation.class); //$NON-NLS-1$
            if (aggregation != null) {
                List<ApimanTermsAggregation.Entry> buckets = aggregation.getBuckets();
                int counter = 0;
                for (ApimanTermsAggregation.Entry entry : buckets) {
                    rval.getData().put(entry.getKey(), entry.getCount());
                    counter++;
                    if (counter > 5) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            log.error(e);
        }

        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerPlan(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @SuppressWarnings("nls")
    @Override
    public UsagePerPlanBean getUsagePerPlan(String organizationId, String apiId, String version,
            DateTime from, DateTime to) {
        UsagePerPlanBean rval = new UsagePerPlanBean();

        try {
            String query =
                    "{" +
                    "    \"query\": {" +
                    "        \"bool\": {" +
                    "            \"filter\": [{" +
                    "                    \"term\": {" +
                    "                        \"apiOrgId\": \"${apiOrgId}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiId\": \"${apiId}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiVersion\": \"${apiVersion}\"" +
                    "                    }" +
                    "                }," +
                    "                {" +
                    "                    \"range\": {" +
                    "                        \"requestStart\": {" +
                    "                            \"gte\": \"${from}\"," +
                    "                            \"lte\": \"${to}\"" +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            ]" +
                    "        }" +
                    "    }," +
                    "    \"size\": 0," +
                    "    \"aggs\": {" +
                    "        \"usage_by_plan\": {" +
                    "            \"terms\": {" +
                    "                \"field\": \"planId\"" +
                    "            }" +
                    "        }" +
                    "    }" +
                    "}";

            Map<String, String> params = new HashMap<>();
            params.put("from", formatDate(from));
            params.put("to", formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));
            StrSubstitutor ss = new StrSubstitutor(params);
            query = ss.replace(query);

            Search search = new Search.Builder(query).addIndex(INDEX_NAME).addType("request").build();
            SearchResult response = getEsClient().execute(search);
            MetricAggregation aggregations = response.getAggregations();
            ApimanTermsAggregation aggregation = aggregations.getAggregation("usage_by_plan", ApimanTermsAggregation.class); //$NON-NLS-1$
            if (aggregation != null) {
                List<ApimanTermsAggregation.Entry> buckets = aggregation.getBuckets();
                for (ApimanTermsAggregation.Entry entry : buckets) {
                    rval.getData().put(entry.getKey(), entry.getCount());
                }
            }
        } catch (IOException e) {
            log.error(e);
        }

        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStats(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @SuppressWarnings("nls")
    @Override
    public ResponseStatsHistogramBean getResponseStats(String organizationId, String apiId,
            String version, HistogramIntervalType interval, DateTime from, DateTime to) {
        ResponseStatsHistogramBean rval = new ResponseStatsHistogramBean();
        Map<String, ResponseStatsDataPoint> index = generateHistogramSkeleton(rval, from, to, interval, ResponseStatsDataPoint.class);

        try {
            String query =
                    "{" +
                    "    \"query\": {" +
                    "        \"bool\": {" +
                    "            \"filter\": [{" +
                    "                \"term\": {" +
                    "                    \"apiOrgId\": \"${apiOrgId}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiId\": \"${apiId}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiVersion\": \"${apiVersion}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"range\": {" +
                    "                    \"requestStart\": {" +
                    "                        \"gte\": \"${from}\"," +
                    "                        \"lte\": \"${to}\"" +
                    "                    }" +
                    "                }" +
                    "            }]" +
                    "        }" +
                    "    }," +
                    "    \"size\": 0," +
                    "    \"aggs\": {" +
                    "        \"histogram\": {" +
                    "            \"date_histogram\": {" +
                    "                \"field\": \"requestStart\"," +
                    "                \"interval\": \"${interval}\"" +
                    "            }," +
                    "            \"aggs\": {" +
                    "                \"total_failures\": {" +
                    "                    \"filter\": {" +
                    "                        \"term\": {" +
                    "                            \"failure\": true" +
                    "                        }" +
                    "                    }" +
                    "                }," +
                    "                \"total_errors\": {" +
                    "                    \"filter\": {" +
                    "                        \"term\": {" +
                    "                            \"error\": true" +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            }" +
                    "        }" +
                    "    }" +
                    "}";

            Map<String, String> params = new HashMap<>();
            params.put("from", formatDate(from));
            params.put("to", formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));
            params.put("interval", interval.name());
            StrSubstitutor ss = new StrSubstitutor(params);
            query = ss.replace(query);

            Search search = new Search.Builder(query).addIndex(INDEX_NAME).addType("request").build();
            SearchResult response = getEsClient().execute(search);
            MetricAggregation aggregations = response.getAggregations();
            DateHistogramAggregation aggregation = aggregations.getDateHistogramAggregation("histogram");
            if (aggregation != null) {
                List<DateHistogram> buckets = aggregation.getBuckets();
                for (DateHistogram entry : buckets) {
                    String keyAsString = entry.getTimeAsString();
                    if (index.containsKey(keyAsString)) {
                        FilterAggregation totalFailuresAgg = entry.getFilterAggregation("total_failures");
                        FilterAggregation totalErrorsAgg = entry.getFilterAggregation("total_errors");
                        long failures = totalFailuresAgg.getCount();
                        long errors = totalErrorsAgg.getCount();
                        ResponseStatsDataPoint point = index.get(keyAsString);
                        point.setTotal(entry.getCount());
                        point.setFailures(failures);
                        point.setErrors(errors);
                    }
                }
            }
        } catch (IOException e) {
            log.error(e);
        }

        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsSummary(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    @SuppressWarnings("nls")
    public ResponseStatsSummaryBean getResponseStatsSummary(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        ResponseStatsSummaryBean rval = new ResponseStatsSummaryBean();

        try {
            String query =
                    "{" +
                    "    \"query\": {" +
                    "        \"bool\": {" +
                    "            \"filter\": [{" +
                    "                \"term\": {" +
                    "                    \"apiOrgId\": \"${apiOrgId}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiId\": \"${apiId}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiVersion\": \"${apiVersion}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"range\": {" +
                    "                    \"requestStart\": {" +
                    "                        \"gte\": \"${from}\"," +
                    "                        \"lte\": \"${to}\"" +
                    "                    }" +
                    "                }" +
                    "            }]" +
                    "        }" +
                    "    }," +
                    "    \"size\": 0," +
                    "    \"aggs\": {" +
                    "        \"total_failures\": {" +
                    "            \"filter\": {" +
                    "                \"term\": {" +
                    "                    \"failure\": true" +
                    "                }" +
                    "            }" +
                    "        }," +
                    "        \"total_errors\": {" +
                    "            \"filter\": {" +
                    "                \"term\": {" +
                    "                    \"error\": true" +
                    "                }" +
                    "            }" +
                    "        }" +
                    "    }" +
                    "}";

            Map<String, String> params = new HashMap<>();
            params.put("from", formatDate(from));
            params.put("to", formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));
            StrSubstitutor ss = new StrSubstitutor(params);
            query = ss.replace(query);

            Search search = new Search.Builder(query).addIndex(INDEX_NAME).addType("request").build();
            SearchResult response = getEsClient().execute(search);

            rval.setTotal(response.getTotal());
            rval.setFailures(response.getAggregations().getFilterAggregation("total_failures").getCount());
            rval.setErrors(response.getAggregations().getFilterAggregation("total_errors").getCount());
        } catch (IOException e) {
            log.error(e);
        }

        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsPerClient(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    @SuppressWarnings("nls")
    public ResponseStatsPerClientBean getResponseStatsPerClient(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        ResponseStatsPerClientBean rval = new ResponseStatsPerClientBean();

        try {
            String query =
                    "{" +
                    "    \"query\": {" +
                    "        \"bool\": {" +
                    "            \"filter\": [{" +
                    "                    \"term\": {" +
                    "                        \"apiOrgId\": \"${apiOrgId}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiId\": \"${apiId}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiVersion\": \"${apiVersion}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"range\": {" +
                    "                        \"requestStart\": {" +
                    "                            \"gte\": \"${from}\"," +
                    "                            \"lte\": \"${to}\"" +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            ]" +
                    "        }" +
                    "    }," +
                    "    \"aggs\": {" +
                    "        \"by_client\": {" +
                    "            \"terms\": {" +
                    "                \"field\": \"clientId\"" +
                    "            }," +
                    "            \"aggs\": {" +
                    "                \"total_failures\": {" +
                    "                    \"filter\": {" +
                    "                        \"term\": {" +
                    "                            \"failure\": true" +
                    "                        }" +
                    "                    }" +
                    "                }," +
                    "                \"total_errors\": {" +
                    "                    \"filter\": {" +
                    "                        \"term\": {" +
                    "                            \"error\": true" +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            }" +
                    "        }" +
                    "    }," +
                    "    \"size\": 0" +
                    "}";

            Map<String, String> params = new HashMap<>();
            params.put("from", formatDate(from));
            params.put("to", formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));
            StrSubstitutor ss = new StrSubstitutor(params);
            query = ss.replace(query);

            Search search = new Search.Builder(query).addIndex(INDEX_NAME).addType("request").build();
            SearchResult response = getEsClient().execute(search);
            MetricAggregation aggregations = response.getAggregations();
            ApimanTermsAggregation aggregation = aggregations.getAggregation("by_client", ApimanTermsAggregation.class); //$NON-NLS-1$
            if (aggregation != null) {
                List<ApimanTermsAggregation.Entry> buckets = aggregation.getBuckets();
                int counter = 0;
                for (ApimanTermsAggregation.Entry entry : buckets) {
                    rval.addDataPoint(entry.getKey(), entry.getCount(), entry.getFilterAggregation("total_failures").getCount(),
                            entry.getFilterAggregation("total_errors").getCount());
                    counter++;
                    if (counter > 10) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            log.error(e);
        }

        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsPerPlan(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    @SuppressWarnings("nls")
    public ResponseStatsPerPlanBean getResponseStatsPerPlan(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        ResponseStatsPerPlanBean rval = new ResponseStatsPerPlanBean();

        try {
            String query =
                    "{" +
                    "    \"query\": {" +
                    "        \"bool\": {" +
                    "            \"filter\": [{" +
                    "                \"term\": {" +
                    "                    \"apiOrgId\": \"${apiOrgId}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiId\": \"${apiId}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiVersion\": \"${apiVersion}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"range\": {" +
                    "                    \"requestStart\": {" +
                    "                        \"gte\": \"${from}\"," +
                    "                        \"lte\": \"${to}\"" +
                    "                    }" +
                    "                }" +
                    "            }]" +
                    "        }" +
                    "    }," +
                    "    \"size\": 0," +
                    "    \"aggs\": {" +
                    "        \"by_plan\": {" +
                    "            \"terms\": {" +
                    "                \"field\": \"planId\"" +
                    "            }," +
                    "            \"aggs\": {" +
                    "                \"total_failures\": {" +
                    "                    \"filter\": {" +
                    "                        \"term\": {" +
                    "                            \"failure\": true" +
                    "                        }" +
                    "                    }" +
                    "                }," +
                    "                \"total_errors\": {" +
                    "                    \"filter\": {" +
                    "                        \"term\": {" +
                    "                            \"error\": true" +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            }" +
                    "        }" +
                    "    }" +
                    "}";

            Map<String, String> params = new HashMap<>();
            params.put("from", formatDate(from));
            params.put("to", formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));
            StrSubstitutor ss = new StrSubstitutor(params);
            query = ss.replace(query);

            Search search = new Search.Builder(query).addIndex(INDEX_NAME).addType("request").build();
            SearchResult response = getEsClient().execute(search);
            MetricAggregation aggregations = response.getAggregations();
            ApimanTermsAggregation aggregation = aggregations.getAggregation("by_plan", ApimanTermsAggregation.class); //$NON-NLS-1$
            if (aggregation != null) {
                List<ApimanTermsAggregation.Entry> buckets = aggregation.getBuckets();
                int counter = 0;
                for (ApimanTermsAggregation.Entry entry : buckets) {
                    rval.addDataPoint(entry.getKey(), entry.getCount(), entry.getFilterAggregation("total_failures").getCount(),
                            entry.getFilterAggregation("total_errors").getCount());
                    counter++;
                    if (counter > 10) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            log.error(e);
        }

        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getClientUsagePerApi(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    @SuppressWarnings("nls")
    public ClientUsagePerApiBean getClientUsagePerApi(String organizationId, String clientId,
            String version, DateTime from, DateTime to) {
        ClientUsagePerApiBean rval = new ClientUsagePerApiBean();

        try {
            String query =
                    "{" +
                    "    \"query\": {" +
                    "        \"bool\": {" +
                    "            \"filter\": [{" +
                    "                    \"term\": {" +
                    "                        \"clientOrgId\": \"${clientOrgId}\"" +
                    "                    }" +
                    "                }," +
                    "                {" +
                    "                    \"term\": {" +
                    "                        \"clientId\": \"${clientId}\"" +
                    "                    }" +
                    "                }," +
                    "                {" +
                    "                    \"term\": {" +
                    "                        \"clientVersion\": \"${clientVersion}\"" +
                    "                    }" +
                    "                }," +
                    "                {" +
                    "                    \"range\": {" +
                    "                        \"requestStart\": {" +
                    "                            \"gte\": \"${from}\"," +
                    "                            \"lte\": \"${to}\"" +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            ]" +
                    "        }" +
                    "    }," +
                    "    \"size\": 0," +
                    "    \"aggs\": {" +
                    "        \"usage_by_api\": {" +
                    "            \"terms\": {" +
                    "                \"field\": \"apiId\"" +
                    "            }" +
                    "        }" +
                    "    }" +
                    "}";

            Map<String, String> params = new HashMap<>();
            params.put("from", formatDate(from));
            params.put("to", formatDate(to));
            params.put("clientOrgId", organizationId.replace('"', '_'));
            params.put("clientId", clientId.replace('"', '_'));
            params.put("clientVersion", version.replace('"', '_'));
            StrSubstitutor ss = new StrSubstitutor(params);
            query = ss.replace(query);

            Search search = new Search.Builder(query).addIndex(INDEX_NAME).addType("request").build();
            SearchResult response = getEsClient().execute(search);
            MetricAggregation aggregations = response.getAggregations();
            ApimanTermsAggregation aggregation = aggregations.getAggregation("usage_by_api", ApimanTermsAggregation.class); //$NON-NLS-1$
            if (aggregation != null) {
                List<ApimanTermsAggregation.Entry> buckets = aggregation.getBuckets();
                for (ApimanTermsAggregation.Entry entry : buckets) {
                    rval.getData().put(entry.getKey(), entry.getCount());
                }
            }
        } catch (IOException e) {
            log.error(e);
        }

        return rval;

    }

    /**
     * @return the esClient
     */
    public JestClient getEsClient() {
        return esClient;
    }

    /**
     * @param esClient the esClient to set
     */
    public void setEsClient(JestClient esClient) {
        this.esClient = esClient;
    }

}
