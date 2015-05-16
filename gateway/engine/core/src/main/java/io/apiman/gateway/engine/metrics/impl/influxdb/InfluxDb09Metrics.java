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
import io.apiman.gateway.engine.i18n.Messages;
import io.apiman.gateway.engine.metrics.RequestMetric;
import io.apiman.gateway.engine.metrics.impl.influxdb.InfluxDb09Driver.InfluxException;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

/**
 * InfluxDB 0.9.x metrics implementation
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class InfluxDb09Metrics implements IMetrics {
    private static final String INFLUX_ENDPOINT = "endpoint"; //$NON-NLS-1$
    private static final String DATABASE = "database"; //$NON-NLS-1$
    private static final String RETENTION_POLICY = "retentionPolicy"; //$NON-NLS-1$
    private static final String POINTS = "points"; //$NON-NLS-1$
    private static final String TIMESTAMP = "time"; //$NON-NLS-1$
    private static final String SERIES_NAME = "name"; //$NON-NLS-1$
    private static final String TAGS = "tags"; //$NON-NLS-1$
    private static final String FIELDS = "fields"; //$NON-NLS-1$

    private static final Map<String, String> DEFAULT_TAGS = new LinkedHashMap<>();
    static {
        DEFAULT_TAGS.put("component", "apiman-gatway"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static TimeZone tz = TimeZone.getTimeZone("UTC"); //$NON-NLS-1$
    private static DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); //$NON-NLS-1$ ISO-8601
    static {
        iso8601.setTimeZone(tz);
    }

    private JsonFactory jsonFactory = new JsonFactory();
    private Map<String, String> config;
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
        this.config = config;
        this.influxEndpoint = getMandatoryString(INFLUX_ENDPOINT);
        this.dbName = getMandatoryString(DATABASE);
        this.retentionPolicy = getOptionalString(RETENTION_POLICY, null);
        this.seriesName = getMandatoryString(SERIES_NAME);
        this.username = getMandatoryString("username"); //$NON-NLS-1$
        this.password = getMandatoryString("password"); //$NON-NLS-1$      
    }

    public void initialize() {
        driver = new InfluxDb09Driver(httpClient, influxEndpoint, username, password);

        if (!listDatabases().contains(dbName)) {
            throw new ConfigurationParseException(Messages.i18n.format("InfluxDb09Metrics.databaseDoesNotExist", dbName)); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.IMetrics#record(io.apiman.gateway.engine.metrics.RequestMetric)
     */
    @Override
    public void record(RequestMetric metric) {
        driver.write(buildJsonRequest(metric), "utf-8", //$NON-NLS-1$
                new IAsyncHandler<InfluxException>() {
                    @Override
                    public void handle(InfluxException result) {
                        // TODO log
                    }
                });
    }

    /**
     * @see io.apiman.gateway.engine.IMetrics#setComponentRegistry(io.apiman.gateway.engine.IComponentRegistry)
     */
    @Override
    public void setComponentRegistry(IComponentRegistry registry) {
        this.httpClient = registry.getComponent(IHttpClientComponent.class);
    }

    @SuppressWarnings("nls")
    protected String buildJsonRequest(RequestMetric metric) {
        StringWriter out = new StringWriter(500); // TODO calculate capacity

        try {
            JsonGenerator generator = jsonFactory.createJsonGenerator(out);
            generator.writeStartObject(); // {
            generator.writeStringField(DATABASE, dbName); // database : mydb,

            if (retentionPolicy != null) {
                generator.writeStringField(RETENTION_POLICY, retentionPolicy); // retentionPolicy : pol,
            }

            generator.writeArrayFieldStart(POINTS); // points : [
            generator.writeStartObject(); // {
            writeDate(generator, TIMESTAMP, new Date()); // timestamp : 2015-03-09T17:47:01Z,
            generator.writeStringField(SERIES_NAME, seriesName); // name : seriesName,

            generator.writeObjectFieldStart(TAGS); // tags : {
            putAllFields(generator, DEFAULT_TAGS.entrySet()); // tagName : tagValue,
            generator.writeStringField("serviceOrgId", metric.getServiceOrgId());
            generator.writeStringField("serviceId", metric.getServiceId());
            generator.writeStringField("serviceVersion", metric.getServiceVersion());
            generator.writeStringField("applicationOrgId", metric.getApplicationOrgId());
            generator.writeStringField("applicationId", metric.getApplicationId());
            generator.writeStringField("applicationVersion", metric.getApplicationVersion());
            generator.writeStringField("contractId", metric.getContractId());
            generator.writeEndObject(); // },

            generator.writeObjectFieldStart(FIELDS); // fields : {
            writeDate(generator, "requestStart", metric.getRequestStart());
            writeDate(generator, "requestEnd", metric.getRequestEnd());
            generator.writeNumberField("requestDuration", metric.getRequestDuration());

            writeDate(generator, "serviceStart", metric.getServiceStart());
            writeDate(generator, "serviceEnd", metric.getServiceEnd());
            generator.writeNumberField("serviceDuration", metric.getServiceDuration());

            generator.writeStringField("resource", metric.getResource());
            generator.writeStringField("method", metric.getMethod());

            generator.writeNumberField("responseCode", metric.getResponseCode());
            generator.writeStringField("responseMessage", metric.getResponseMessage());

            generator.writeNumberField("failureCode", metric.getFailureCode());
            generator.writeStringField("failureReason", metric.getFailureReason());

            generator.writeBooleanField("error", metric.isError());
            generator.writeStringField("errorMessage", metric.getErrorMessage());
            generator.writeEndObject(); // }
            generator.writeEndObject(); // }
            generator.writeEndArray(); // ]
            generator.writeEndObject(); // }
            generator.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return out.toString();
    }

    private void writeDate(JsonGenerator generator, String fieldName, Date date)
            throws JsonGenerationException, IOException {
        generator.writeStringField(fieldName, iso8601.format(date));
    }

    private void putAllFields(JsonGenerator generator, Set<Map.Entry<String, String>> entrySet)
            throws JsonGenerationException, IOException {
        for (Map.Entry<String, String> entry : entrySet) {
            generator.writeStringField(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("nls")
    private String getMandatoryString(String keyname) {
        String value = config.get(keyname);
        
        if (value == null) {
            throw new ConfigurationParseException(Messages.i18n.format(
                    "InfluxDb09Metrics.mandatoryConfigMustBeSet", getClass().getCanonicalName(), keyname));
        }

        return keyname;
    }

    private String getOptionalString(String key, String dValue) {
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
