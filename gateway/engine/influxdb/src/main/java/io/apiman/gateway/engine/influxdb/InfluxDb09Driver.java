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

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A simple async HTTP impl of the influxdb driver. Contains only the subset of functionality we need.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class InfluxDb09Driver {
    private final IHttpClientComponent httpClient;
    private final StringBuilder writeUrl;
    private final String username;
    private final String password;
    private final String token;
    private final String database;
    private final String retentionPolicy;
    private final String timePrecision;

    @SuppressWarnings("nls")
    public InfluxDb09Driver(IHttpClientComponent httpClient, InfluxDbMetricsOptionsParser options, String timePrecision) {
        this.httpClient = httpClient;
        this.username = options.getUsername();
        this.password = options.getPassword();
        this.token = options.getToken();
        this.database = options.getDbName();
        this.retentionPolicy = options.getRetentionPolicy();
        this.timePrecision = timePrecision;
        String endpoint = options.getInfluxEndpoint();

        StringBuilder writeEndpoint = new StringBuilder();

        if (!endpoint.startsWith("http://") || !endpoint.startsWith("https://")) {
            writeEndpoint.append("http://");
        }

        // domain + port
        writeEndpoint.append(endpoint);

        // Add user-name, password, etc
        writeEndpoint.append("/write");
        this.writeUrl = buildParams(writeEndpoint);
    }

    /**
     * Simple write to "/write". Must be valid Influx line format.
     *
     * @param payload document to write, as string (payload may be a batch).
     * @param failureHandler handler in case of failure
     */
    public void write(String payload,
            final IAsyncHandler<InfluxException> failureHandler) {
        // Make request to influx
        IHttpClientRequest request = httpClient.request(writeUrl.toString(), HttpMethod.POST,
                result -> {
                    if (result.isError() || result.getResult().getResponseCode() < 200
                            || result.getResult().getResponseCode() > 299) {
                        failureHandler.handle(new InfluxException(result.getResult()));
                    }
                });
        // For some reason Java's URLEncoding doesn't seem to be parseable by influx?
        //request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        if (token != null) {
            request.addHeader("Authorization", "Token " + token);
        }
        request.addHeader("Content-Type", "text/plain");
        request.write(payload, StandardCharsets.UTF_8.name());
        request.end();
    }

    @SuppressWarnings("nls")
    private StringBuilder buildParams(StringBuilder url) {
        addQueryParam(url, "db", database, "?");
        addQueryParam(url, "u", username, "&");
        addQueryParam(url, "p", password, "&");
        addQueryParam(url, "rp", retentionPolicy, "&");
        addQueryParam(url, "precision", timePrecision, "&");
        return url;
    }

    @SuppressWarnings("nls")
    private void addQueryParam(StringBuilder url, String key, String value, String connector) {
        if (value == null)
            return;

        try {
            url.append(connector).append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
