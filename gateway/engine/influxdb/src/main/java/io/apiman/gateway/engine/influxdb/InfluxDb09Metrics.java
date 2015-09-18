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

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IRequiresInitialization;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.http.IHttpClientResponse;
import io.apiman.gateway.engine.i18n.Messages;
import io.apiman.gateway.engine.metrics.RequestMetric;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

/**
 * InfluxDB 0.9.x metrics implementation
 *
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class InfluxDb09Metrics implements IMetrics, IRequiresInitialization {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String INFLUX_ENDPOINT = "endpoint";
    private static final String DATABASE = "database";
    private static final String RETENTION_POLICY = "retentionPolicy";
    private static final String SERIES_NAME = "measurement";
    private static final String TIMEPRECISION = "ms";


    private static final Map<String, String> DEFAULT_TAGS = new LinkedHashMap<>();
    static {
        DEFAULT_TAGS.put("generator", "apiman-gateway");  //$NON-NLS-2$
    }

    private String dbName;
    private String retentionPolicy;
    private String seriesName;
    private String influxEndpoint;
    private IHttpClientComponent httpClient;

    private InfluxDb09Driver driver;
    private String username;
    private String password;

    /**
     * Constructor.
     * @param config plugin configuration options
     */
    public InfluxDb09Metrics(Map<String, String> config) {
        this.influxEndpoint = getMandatoryString(config, INFLUX_ENDPOINT);
        this.dbName = getMandatoryString(config, DATABASE);
        this.retentionPolicy = getOptionalString(config, RETENTION_POLICY, null);
        this.seriesName = getMandatoryString(config, SERIES_NAME);
        this.username = getOptionalString(config, USERNAME, null);
        this.password = getOptionalString(config, PASSWORD, null);
    }

    /**
     * @see io.apiman.gateway.engine.IRequiresInitialization#initialize()
     */
    @Override
    public void initialize() {
        driver = new InfluxDb09Driver(httpClient, influxEndpoint, username, password, dbName,
                retentionPolicy, TIMEPRECISION);

        if (!listDatabases().contains(dbName)) {
            throw new ConfigurationParseException(Messages.i18n.format(
                    "InfluxDb09Metrics.databaseDoesNotExist", dbName));
        }
    }


    /**
     * @see io.apiman.gateway.engine.IMetrics#setComponentRegistry(io.apiman.gateway.engine.IComponentRegistry)
     */
    @Override
    public void setComponentRegistry(IComponentRegistry registry) {
        this.httpClient = registry.getComponent(IHttpClientComponent.class);
    }

    /**
     * @see io.apiman.gateway.engine.IMetrics#record(io.apiman.gateway.engine.metrics.RequestMetric)
     */
    @Override
    public void record(RequestMetric metric) {
        driver.write(buildRequest(metric),
                new IAsyncHandler<InfluxException>() {
                    @Override
                    public void handle(InfluxException result) {
                        if (result.isBadResponse()) {
                            IHttpClientResponse response = result.getResponse();
                            System.err.println(String.format("Influx stats error. Code: %s with message: '%s'",
                                    response.getResponseCode(),
                                    response.getResponseMessage()));
                        } else {
                            System.err.println(result.getMessage());
                        }
                    }
                });
    }

    protected String buildRequest(RequestMetric metric) {
        // TODO: calculate capacity more accurately
        StringBuilder sb = new StringBuilder(500);

        // Series name, followed by comma
        sb.append(seriesName + ",");

        // Default tags, comma delimited
        for (Entry<String, String> entry : DEFAULT_TAGS.entrySet()) {
            write(entry.getKey(), entry.getValue(), sb);
        }

        // Metric tags, comma delimited, space at end.
        write("serviceOrgId", quote(metric.getServiceOrgId()), sb);
        write("serviceId", quote(metric.getServiceId()), sb);
        write("serviceVersion", quote(metric.getServiceVersion()), sb);
        write("planId", quote(metric.getPlanId()), sb);
        write("applicationOrgId", quote(metric.getApplicationOrgId()), sb);
        write("applicationId", quote(metric.getApplicationId()), sb);
        write("applicationVersion", quote(metric.getApplicationVersion()), sb);
        write("contractId", quote(metric.getContractId()), sb);
        write("user", quote(metric.getUser()), sb);

        sb.deleteCharAt(sb.length()-1);
        sb.append(' ');

        // Data, comma delimited, space at end.
        write("requestStart", dateToLong(metric.getRequestStart()), sb);
        write("requestEnd", dateToLong(metric.getRequestEnd()), sb);
        write("serviceStart", dateToLong(metric.getServiceStart()), sb);
        write("serviceEnd", dateToLong(metric.getServiceEnd()), sb);
        write("url", quote(metric.getUrl()), sb);
        write("resource", quote(metric.getResource()), sb);
        write("method", quote(metric.getMethod()), sb);
        write("responseCode", Integer.toString(metric.getResponseCode()), sb);
        write("responseMessage", quote(metric.getResponseMessage()), sb);
        write("failureCode", Integer.toString(metric.getFailureCode()), sb);
        write("failureReason", quote(metric.getFailureReason()), sb);
        write("error", Boolean.toString(metric.isError()), sb);
        write("errorMessage", quote(metric.getErrorMessage()), sb);

        sb.deleteCharAt(sb.length()-1);
        sb.append(' ');

        // Timestamp in milliseconds. Newline would be needed after this point for batching.
        sb.append(System.currentTimeMillis());

        return sb.toString();
    }

    private void write(String tagname, String tagValue, StringBuilder sb) {
        if (tagValue == null)
            return;

        sb.append(tagname + "=" + tagValue + ",");
    }

    private String quote(String item) {
        if (item == null)
            return null;
        return "\"" + item  + "\"";
    }

    private String dateToLong(Date date) {
        return Long.toString(date.getTime());
    }

    private String getMandatoryString(Map<String, String> config, String keyname) {
        String value = config.get(keyname);

        if (value == null)
            throw new ConfigurationParseException(Messages.i18n.format(
                    "InfluxDb09Metrics.mandatoryConfigMustBeSet", getClass().getCanonicalName(), keyname));
        return value;
    }

    private String getOptionalString(Map<String, String> config, String key, String dValue) {
        return config.containsKey(key) ? config.get(key) : dValue;
    }

    private List<String> listDatabases() {
        final CountDownLatch endSignal = new CountDownLatch(1);
        final List<String> results = new ArrayList<>();

        driver.listDatabases(new IAsyncResultHandler<List<String>>() {

            @Override
            public void handle(IAsyncResult<List<String>> result) {
                if(result.isSuccess()) {
                    results.addAll(result.getResult());
                } else {
                    throw new InfluxException(result.getError());
                }
                endSignal.countDown();
            }
        });

        try {
            endSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return results;
    }
}
