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
package io.apiman.gateway.engine.metrics.impl.influxdb;

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.http.IHttpClientResponse;
import io.apiman.gateway.engine.i18n.Messages;
import io.apiman.gateway.engine.metrics.RequestMetric;
import io.apiman.gateway.engine.metrics.impl.influxdb.InfluxDb09Driver.InfluxException;

import java.text.SimpleDateFormat;
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
public class InfluxDb09Metrics implements IMetrics {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String INFLUX_ENDPOINT = "endpoint";
    private static final String DATABASE = "database";
    private static final String RETENTION_POLICY = "retentionPolicy";
    private static final String SERIES_NAME = "measurement";

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

    private SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * Constructor.
     * @param config plugin configuration options
     */
    public InfluxDb09Metrics(Map<String, String> config) {
        System.out.println("Config");

        for ( Entry<String, String> entry : config.entrySet() ) {
            System.out.println("KEY = " + entry.getKey() + " VALUE = " + entry.getValue());
        }


        this.influxEndpoint = getMandatoryString(config, INFLUX_ENDPOINT);
        this.dbName = getMandatoryString(config, DATABASE);
        this.retentionPolicy = getOptionalString(config, RETENTION_POLICY, null);
        this.seriesName = getMandatoryString(config, SERIES_NAME);
        this.username = getOptionalString(config, USERNAME, null);
        this.password = getOptionalString(config, PASSWORD, null);
    }

    public void initialize() {
        driver = new InfluxDb09Driver(httpClient, influxEndpoint, username, password, dbName, retentionPolicy);

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

        // Series name
        sb.append(seriesName);

        // Default Tags
        for (Entry<String, String> entry : DEFAULT_TAGS.entrySet()) {
            write(entry.getKey(), entry.getValue(), sb, false);
        }

        // Metric Tags
        write("serviceOrgId", metric.getServiceOrgId(), sb, false);
        write("serviceId", metric.getServiceId(), sb, false);
        write("serviceVersion", metric.getServiceVersion(), sb, false);
        write("applicationOrgId", metric.getApplicationOrgId(), sb, false);
        write("applicationId", metric.getApplicationId(), sb, false);
        write("applicationVersion", metric.getApplicationVersion(), sb, false);
        write("contractId", metric.getContractId(), sb, true);

        // Data
        write("requestStart", dateToRfc3330(metric.getRequestStart()), sb, false);
        write("requestEnd", dateToRfc3330(metric.getRequestEnd()), sb, false);
        write("serviceStart", dateToRfc3330(metric.getServiceStart()), sb, false);
        write("serviceEnd", dateToRfc3330(metric.getServiceEnd()), sb, false);
        write("resource", metric.getResource(), sb, false);
        write("method", metric.getMethod(), sb, false);
        write("responseCode", Integer.toString(metric.getResponseCode()), sb, false);
        write("responseMessage", metric.getResponseMessage(), sb, false);
        write("failureCode", Integer.toString(metric.getFailureCode()), sb, false);
        write("failureReason", metric.getFailureReason(), sb, false);
        write("error", Boolean.toString(metric.isError()), sb, false);
        write("errorMessage", metric.getErrorMessage(), sb, true);

        // Timestamp in millis
        sb.append(System.currentTimeMillis() + "ms");

        return sb.toString();
    }

    private void write(String tagname, String tagValue, StringBuilder sb, boolean lastElem) {
        String separator = " ";
        if (!lastElem)
            separator = ",";

        sb.append(tagname + "=" + tagValue + separator);
    }

    private String dateToRfc3330(Date date) {
        return rfc3339.format(date);
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
                    throw (InfluxException) result.getError();
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
