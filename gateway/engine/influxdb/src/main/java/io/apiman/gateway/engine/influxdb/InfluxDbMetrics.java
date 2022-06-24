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
package io.apiman.gateway.engine.influxdb;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IRequiresInitialization;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.http.IHttpClientResponse;
import io.apiman.gateway.engine.metrics.BatchedMetricsConsumer;
import io.apiman.gateway.engine.metrics.RequestMetric;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * InfluxDB 1.x metrics implementation.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@SuppressWarnings("nls")
public class InfluxDbMetrics implements IMetrics, IRequiresInitialization {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(InfluxDbMetrics.class);
    private static final String TIME_PRECISION = "ms";
    private final Map<String, String> defaultTags = new LinkedHashMap<>();
    private final InfluxDbMetricsOptionsParser options;
    private InfluxDb09Driver driver;
    private IHttpClientComponent httpClient;
    private final BatchedMetricsConsumer<RequestMetric> metricsConsumer;

    /**
     * Constructor.
     * @param config plugin configuration options
     */
    public InfluxDbMetrics(Map<String, String> config) {
        options = new InfluxDbMetricsOptionsParser(config);
        this.defaultTags.put("generator", options.getGeneratorName());

        metricsConsumer = new BatchedMetricsConsumer<>("InfluxDB-Metrics-Consumer", options.getQueueCapacity(), options.getMaxBatchSize(),
                this::handleMetricBatch);
    }

    @Override
    public void initialize() {
        LOGGER.info("Initialising InfluxDB metrics...");
        driver = new InfluxDb09Driver(httpClient, options, TIME_PRECISION);
        metricsConsumer.start();
    }

    @Override
    public void setComponentRegistry(IComponentRegistry registry) {
        this.httpClient = registry.getComponent(IHttpClientComponent.class);
    }

    @Override
    public void record(RequestMetric metric, ApiRequest apiRequest, ApiResponse apiResponse) {
        this.metricsConsumer.offer(metric);
    }

    private void handleMetricBatch(List<RequestMetric> batch) {
        String encodedBatch = encodeBatch(batch);
        driver.write(encodedBatch, (InfluxException result) -> {
            if (result.isBadResponse()) {
                IHttpClientResponse httpResponse = result.getResponse();
                LOGGER.error(result,
                        "Influx stats error. Code: {0} with message: {1}",
                        httpResponse.getResponseCode(),
                        httpResponse.getResponseMessage());
            } else {
                LOGGER.error(result.getMessage(), result);
            }
        });
    }

    protected String encodeBatch(List<RequestMetric> batch) {
        // TODO: calculate capacity more accurately
        StringBuilder sb = new StringBuilder(512 * batch.size());
        for (Iterator<RequestMetric> iterator = batch.iterator(); iterator.hasNext();) {
            RequestMetric metric = iterator.next();

            // Series name, followed by comma
            sb.append(options.getSeriesName()).append(",");

            // Default tags, comma delimited
            for (Entry<String, String> entry : defaultTags.entrySet()) {
                write(entry.getKey(), entry.getValue(), sb);
            }

            // Metric tags/metadata, comma delimited, space at end.
            // Apparently performance is better if we sort this alphabetically:
            //  https://docs.influxdata.com/influxdb/v2.3/write-data/best-practices/optimize-writes/#sort-tags-by-key
            write("apiId", quote(metric.getApiId()), sb);
            write("apiOrgId", quote(metric.getApiOrgId()), sb);
            write("apiVersion", quote(metric.getApiVersion()), sb);
            write("clientId", quote(metric.getClientId()), sb);
            write("clientOrgId", quote(metric.getClientOrgId()), sb);
            write("clientVersion", quote(metric.getClientVersion()), sb);
            write("contractId", quote(metric.getContractId()), sb);
            write("planId", quote(metric.getPlanId()), sb);
            write("user", quote(metric.getUser()), sb);

            sb.deleteCharAt(sb.length() - 1);
            sb.append(' ');

            // Data/variables, comma delimited, space at end.
            write("requestStart", dateToLong(metric.getRequestStart()), sb);
            write("requestEnd", dateToLong(metric.getRequestEnd()), sb);
            write("apiStart", dateToLong(metric.getApiStart()), sb);
            write("apiEnd", dateToLong(metric.getApiEnd()), sb);
            write("url", quote(metric.getUrl()), sb);
            write("resource", quote(metric.getResource()), sb);
            write("method", quote(metric.getMethod()), sb);
            write("responseCode", Integer.toString(metric.getResponseCode()), sb);
            write("responseMessage", quote(metric.getResponseMessage()), sb);
            write("failureCode", Integer.toString(metric.getFailureCode()), sb);
            write("failureReason", quote(metric.getFailureReason()), sb);
            write("error", Boolean.toString(metric.isError()), sb);
            write("errorMessage", quote(metric.getErrorMessage()), sb);

            sb.deleteCharAt(sb.length() - 1);
            sb.append(' ');

            sb.append(System.currentTimeMillis());

            if (iterator.hasNext()) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private void write(String name, String value, StringBuilder sb) {
        if (value == null)
            return;

        sb.append(name).append("=").append(value).append(",");
    }

    private String quote(String item) {
        if (item == null)
            return null;
        return "\"" + item  + "\"";
    }

    private String dateToLong(Date date) {
        return Long.toString(date.getTime());
    }

}
