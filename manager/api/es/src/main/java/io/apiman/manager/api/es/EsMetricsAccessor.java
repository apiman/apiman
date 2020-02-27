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

import io.apiman.common.es.util.EsConstants;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.es.util.AbstractEsComponent;
import io.apiman.manager.api.beans.metrics.*;
import io.apiman.manager.api.core.IMetricsAccessor;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.metrics.MetricsAccessorHelper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.joda.time.DateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An elasticsearch implementation of the {@link IMetricsAccessor} interface.  This
 * implementation knows how to get metrics/analytics information out of an
 * elasticsearch store.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class EsMetricsAccessor extends AbstractEsComponent implements IMetricsAccessor {

    private static final String INDEX_NAME = "apiman_metrics"; //$NON-NLS-1$

    @Inject @ApimanLogger(EsMetricsAccessor.class)
    IApimanLogger log;

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public EsMetricsAccessor(Map<String, String> config) {
        super(config);
    }

    /**
     * Constructor only for the Test-Framework.
     * This constructor sets the elasticsearch client from outside.
     * WARNING: It is not recommended to use it except within the Test-Framework.
     * @param client elasticsearch client
     */
    public EsMetricsAccessor(RestHighLevelClient client) {
        super(client);
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsage(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @SuppressWarnings("nls")
    @Override
    public UsageHistogramBean getUsage(String organizationId, String apiId, String version,
            HistogramIntervalType interval, DateTime from, DateTime to) {
        UsageHistogramBean rval = new UsageHistogramBean();
        Map<String, UsageDataPoint> index = MetricsAccessorHelper.generateHistogramSkeleton(rval, from, to, interval, UsageDataPoint.class);

        try {
            String query =
                    "{" +
                    "    \"query\": {" +
                    "        \"bool\": {" +
                    "            \"filter\": [{" +
                    "                    \"term\": {" +
                    "                        \"apiOrgId\": \"{{apiOrgId}}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiId\": \"{{apiId}}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiVersion\": \"{{apiVersion}}\"" +
                    "                    }" +
                    "                }," +
                    "                {" +
                    "                    \"range\": {" +
                    "                        \"requestStart\": {" +
                    "                            \"gte\": \"{{from}}\"," +
                    "                            \"lte\": \"{{to}}\"" +
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
                    "                \"calendar_interval\": \"{{interval}}\"" +
                    "            }" +
                    "        }" +
                    "    }" +
                    "}";

            Map<String, Object> params = new HashMap<>();
            params.put("from", MetricsAccessorHelper.formatDate(from));
            params.put("to", MetricsAccessorHelper.formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));
            params.put("interval", interval.name());

            SearchResponse response = this.doSearchTemplateRequest(query, params);

            ParsedDateHistogram aggregation = (ParsedDateHistogram) response.getAggregations().asMap().get("histogram");
            if (aggregation != null) {
                List<ParsedDateHistogram.ParsedBucket> buckets = (List<ParsedDateHistogram.ParsedBucket>) aggregation.getBuckets();

                for (ParsedDateHistogram.ParsedBucket entry : buckets) {
                    String keyAsString = entry.getKeyAsString();
                    if (index.containsKey(keyAsString)) {
                        index.get(keyAsString).setCount(entry.getDocCount());
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
                    "                    \"apiOrgId\": \"{{apiOrgId}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiId\": \"{{apiId}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiVersion\": \"{{apiVersion}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"range\": {" +
                    "                    \"requestStart\": {" +
                    "                        \"gte\": \"{{from}}\"," +
                    "                        \"lte\": \"{{to}}\"" +
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

            Map<String, Object> params = new HashMap<>();
            params.put("from", MetricsAccessorHelper.formatDate(from));
            params.put("to", MetricsAccessorHelper.formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));

            SearchResponse response = this.doSearchTemplateRequest(query, params);

            List<ParsedStringTerms.ParsedBucket> buckets = (List<ParsedStringTerms.ParsedBucket>) ((ParsedStringTerms) response.getAggregations().get("usage_by_client")).getBuckets();
            int counter = 0;
            for (ParsedStringTerms.ParsedBucket entry : buckets) {
                rval.getData().put(entry.getKeyAsString(), entry.getDocCount());
                counter++;
                if (counter > 5) {
                    break;
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
                    "                        \"apiOrgId\": \"{{apiOrgId}}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiId\": \"{{apiId}}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiVersion\": \"{{apiVersion}}\"" +
                    "                    }" +
                    "                }," +
                    "                {" +
                    "                    \"range\": {" +
                    "                        \"requestStart\": {" +
                    "                            \"gte\": \"{{from}}\"," +
                    "                            \"lte\": \"{{to}}\"" +
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

            Map<String, Object> params = new HashMap<>();
            params.put("from", MetricsAccessorHelper.formatDate(from));
            params.put("to", MetricsAccessorHelper.formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));

            SearchResponse response = this.doSearchTemplateRequest(query, params);

            Aggregations aggregations = response.getAggregations();

            ParsedStringTerms aggregation = aggregations.get("usage_by_plan"); //$NON-NLS-1$
            if (aggregation != null) {
                List<ParsedStringTerms.ParsedBucket> buckets = (List<ParsedStringTerms.ParsedBucket>) aggregation.getBuckets();
                for (ParsedStringTerms.ParsedBucket entry : buckets) {
                    rval.getData().put(entry.getKeyAsString(), entry.getDocCount());
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
        Map<String, ResponseStatsDataPoint> index = MetricsAccessorHelper.generateHistogramSkeleton(rval, from, to, interval, ResponseStatsDataPoint.class);

        try {
            String query =
                    "{" +
                    "    \"query\": {" +
                    "        \"bool\": {" +
                    "            \"filter\": [{" +
                    "                \"term\": {" +
                    "                    \"apiOrgId\": \"{{apiOrgId}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiId\": \"{{apiId}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiVersion\": \"{{apiVersion}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"range\": {" +
                    "                    \"requestStart\": {" +
                    "                        \"gte\": \"{{from}}\"," +
                    "                        \"lte\": \"{{to}}\"" +
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
                    "                \"calendar_interval\": \"{{interval}}\"" +
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

            Map<String, Object> params = new HashMap<>();
            params.put("from", MetricsAccessorHelper.formatDate(from));
            params.put("to", MetricsAccessorHelper.formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));
            params.put("interval", interval.name());

            SearchResponse searchResponse = this.doSearchTemplateRequest(query, params);

            ParsedDateHistogram aggregation = (ParsedDateHistogram) searchResponse.getAggregations().asMap().get("histogram");
            if (aggregation != null) {
                List<ParsedDateHistogram.ParsedBucket> buckets = (List<ParsedDateHistogram.ParsedBucket>) aggregation.getBuckets();

                for (ParsedDateHistogram.ParsedBucket entry : buckets) {
                    String keyAsString = entry.getKeyAsString();
                    if (index.containsKey(keyAsString)) {
                        ParsedFilter totalFailuresAgg = entry.getAggregations().get("total_failures");
                        ParsedFilter totalErrorsAgg = entry.getAggregations().get("total_errors");
                        long failures = totalFailuresAgg.getDocCount();
                        long errors = totalErrorsAgg.getDocCount();
                        ResponseStatsDataPoint point = index.get(keyAsString);
                        point.setTotal(entry.getDocCount());
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
                    "                    \"apiOrgId\": \"{{apiOrgId}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiId\": \"{{apiId}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiVersion\": \"{{apiVersion}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"range\": {" +
                    "                    \"requestStart\": {" +
                    "                        \"gte\": \"{{from}}\"," +
                    "                        \"lte\": \"{{to}}\"" +
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

            Map<String, Object> params = new HashMap<>();
            params.put("from", MetricsAccessorHelper.formatDate(from));
            params.put("to", MetricsAccessorHelper.formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));

            SearchResponse searchResponse = this.doSearchTemplateRequest(query, params);
            Aggregations aggregations = searchResponse.getAggregations();

            rval.setTotal(searchResponse.getHits().getTotalHits().value);
            rval.setFailures(((ParsedFilter) aggregations.get("total_failures")).getDocCount());
            rval.setErrors(((ParsedFilter) aggregations.get("total_errors")).getDocCount());

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
                    "                        \"apiOrgId\": \"{{apiOrgId}}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiId\": \"{{apiId}}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"term\": {" +
                    "                        \"apiVersion\": \"{{apiVersion}}\"" +
                    "                    }" +
                    "                }, {" +
                    "                    \"range\": {" +
                    "                        \"requestStart\": {" +
                    "                            \"gte\": \"{{from}}\"," +
                    "                            \"lte\": \"{{to}}\"" +
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

            Map<String, Object> params = new HashMap<>();
            params.put("from", MetricsAccessorHelper.formatDate(from));
            params.put("to", MetricsAccessorHelper.formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));

            SearchResponse response = this.doSearchTemplateRequest(query, params);
            List<ParsedStringTerms.ParsedBucket> buckets = (List<ParsedStringTerms.ParsedBucket>) ((ParsedStringTerms) response.getAggregations().get("by_client")).getBuckets();

            int counter = 0;
            for (ParsedStringTerms.ParsedBucket entry : buckets) {
                rval.addDataPoint(entry.getKeyAsString(), entry.getDocCount(), ((ParsedFilter) entry.getAggregations().get("total_failures")).getDocCount(), ((ParsedFilter) entry.getAggregations().get("total_errors")).getDocCount());
                counter++;
                if (counter > 10) {
                    break;
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
                    "                    \"apiOrgId\": \"{{apiOrgId}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiId\": \"{{apiId}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"term\": {" +
                    "                    \"apiVersion\": \"{{apiVersion}}\"" +
                    "                }" +
                    "            }, {" +
                    "                \"range\": {" +
                    "                    \"requestStart\": {" +
                    "                        \"gte\": \"{{from}}\"," +
                    "                        \"lte\": \"{{to}}\"" +
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

            Map<String, Object> params = new HashMap<>();
            params.put("from", MetricsAccessorHelper.formatDate(from));
            params.put("to", MetricsAccessorHelper.formatDate(to));
            params.put("apiOrgId", organizationId.replace('"', '_'));
            params.put("apiId", apiId.replace('"', '_'));
            params.put("apiVersion", version.replace('"', '_'));

            SearchResponse response = this.doSearchTemplateRequest(query, params);

            Aggregations aggregations = response.getAggregations();
            ParsedStringTerms aggregation = aggregations.get("by_plan"); //$NON-NLS-1$
            if (aggregation != null) {
                List<ParsedStringTerms.ParsedBucket> buckets = (List<ParsedStringTerms.ParsedBucket>)  aggregation.getBuckets();
                int counter = 0;
                for (ParsedStringTerms.ParsedBucket entry : buckets) {
                    rval.addDataPoint(entry.getKeyAsString(), entry.getDocCount(), ((ParsedFilter)entry.getAggregations().get("total_failures")).getDocCount(),
                            ((ParsedFilter)entry.getAggregations().get("total_errors")).getDocCount());
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
                    "                        \"clientOrgId\": \"{{clientOrgId}}\"" +
                    "                    }" +
                    "                }," +
                    "                {" +
                    "                    \"term\": {" +
                    "                        \"clientId\": \"{{clientId}}\"" +
                    "                    }" +
                    "                }," +
                    "                {" +
                    "                    \"term\": {" +
                    "                        \"clientVersion\": \"{{clientVersion}}\"" +
                    "                    }" +
                    "                }," +
                    "                {" +
                    "                    \"range\": {" +
                    "                        \"requestStart\": {" +
                    "                            \"gte\": \"{{from}}\"," +
                    "                            \"lte\": \"{{to}}\"" +
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

            Map<String, Object> params = new HashMap<>();
            params.put("from", MetricsAccessorHelper.formatDate(from));
            params.put("to", MetricsAccessorHelper.formatDate(to));
            params.put("clientOrgId", organizationId.replace('"', '_'));
            params.put("clientId", clientId.replace('"', '_'));
            params.put("clientVersion", version.replace('"', '_'));

            SearchResponse response = this.doSearchTemplateRequest(query, params);

            ParsedStringTerms aggregation = response.getAggregations().get("usage_by_api");
            if(aggregation != null) {
                List<ParsedStringTerms.ParsedBucket> buckets = (List<ParsedStringTerms.ParsedBucket>) aggregation.getBuckets();
                for (ParsedStringTerms.ParsedBucket entry : buckets) {
                    rval.getData().put(entry.getKeyAsString(), entry.getDocCount());
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
        return rval;
    }

    /**
     * Does a request against elasticsearch
     * @param query the query template to execute
     * @param params the params for the query template
     * @return SearchResponse of elasticsearch
     * @throws IOException
     */
    private SearchResponse doSearchTemplateRequest(String query, Map<String, Object> params) throws IOException {
        SearchTemplateRequest searchTemplateRequest = new SearchTemplateRequest();
        searchTemplateRequest.setRequest(new SearchRequest(INDEX_NAME));
        searchTemplateRequest.setScriptType(ScriptType.INLINE);
        searchTemplateRequest.setScript(query);
        searchTemplateRequest.setScriptParams(params);
        return getClient().searchTemplate(searchTemplateRequest, RequestOptions.DEFAULT).getResponse();
    }

    /**
     * @see AbstractEsComponent#getDefaultIndexPrefix()
     */
    @Override
    protected String getDefaultIndexPrefix() {
        return EsConstants.METRICS_INDEX_NAME;
    }

    /**
     * @see AbstractEsComponent#getDefaultIndices()
     * @return default indices
     */
    @Override
    protected List<String> getDefaultIndices() {
        String[] indices = {""};
        return Arrays.asList(indices);
    }
}
