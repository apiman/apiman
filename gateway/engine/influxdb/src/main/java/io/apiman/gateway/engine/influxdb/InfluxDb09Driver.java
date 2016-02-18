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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.http.HttpMethod;
import io.apiman.gateway.engine.components.http.IHttpClientRequest;
import io.apiman.gateway.engine.components.http.IHttpClientResponse;

/**
 * A simple async HTTP impl of the influxdb driver. Contains only the subset of functionality we need.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class InfluxDb09Driver {
    private IHttpClientComponent httpClient;

    private StringBuilder writeUrl;
    private StringBuilder queryUrl;

    private String username;
    private String password;
    private String database;
    private String retentionPolicy;
    private String timePrecision;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("nls")
    public InfluxDb09Driver(IHttpClientComponent httpClient, String endpoint, String username,
            String password, String database, String retentionPolicy, String timePrecision) {
        this.httpClient = httpClient;
        this.username = username;
        this.password = password;
        this.database = database;
        this.retentionPolicy = retentionPolicy;
        this.timePrecision = timePrecision;

        StringBuilder writeEndpoint = new StringBuilder();

        if (!endpoint.startsWith("http://") || !endpoint.startsWith("https://")) {
            writeEndpoint.append("http://");
        }

        // domain + port
        writeEndpoint.append(endpoint);

        // Same basic structure, but with /query on end
        StringBuilder queryEndpoint = new StringBuilder().append(writeEndpoint).append("/query");
        this.queryUrl = buildParams(queryEndpoint, "SHOW DATABASES");

        // Add user-name, password, etc
        writeEndpoint.append("/write");
        this.writeUrl = buildParams(writeEndpoint, null);
    }

    /**
     * Simple write to "/write". Must be valid Influx line format.
     *
     * @param lineDocument document to write, as string
     * @param failureHandler handler in case of failure
     */
    public void write(String lineDocument,
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
        request.addHeader("Content-Type", "text/plain"); //$NON-NLS-1$ //$NON-NLS-2$
        request.write(lineDocument, StandardCharsets.UTF_8.name());
        request.end();
    }

    /**
     * List all databases
     *
     * @param handler the result handler
     */
    @SuppressWarnings("nls")
    public void listDatabases(final IAsyncResultHandler<List<String>> handler) {
        IHttpClientRequest request = httpClient.request(queryUrl.toString(), HttpMethod.GET,
                result -> {
                    try {

                        if (result.isError() || result.getResult().getResponseCode() != 200) {
                            handleError(result, handler);
                            return;
                        }

                        List<String> results = new ArrayList<>();

                        // {"results":
                        JsonNode arrNode = objectMapper.readTree(result.getResult().getBody())
                                .path("results").elements().next() // results: [ first-elem
                                .path("series").elements().next(); // series: [ first-elem
                        // values: [[db1], [db2], [...]] => db1, db2
                        flattenArrays(arrNode.get("values"), results);

                        // send results
                        handler.handle(AsyncResultImpl.create(results));

                    } catch (IOException e) {
                        AsyncResultImpl.create(new RuntimeException(
                                "Unable to parse Influx JSON response", e));
                    }
                });

        request.end();
    }

    protected <T> void handleError(IAsyncResult<IHttpClientResponse> result, IAsyncResultHandler<T> handler) {
        if (result.isError()) {
            handler.handle(AsyncResultImpl.<T> create(result.getError()));
        } else if (result.getResult().getResponseCode() != 200) {
            handler.handle(AsyncResultImpl.<T> create(new InfluxException("Influx: " //$NON-NLS-1$
                    + result.getResult().getResponseCode() + " " + result.getResult().getResponseMessage()))); //$NON-NLS-1$
        }
    }

    private void flattenArrays(JsonNode arrNode, List<String> results) {
        if (arrNode.isArray()) {
            for (JsonNode entry : arrNode) {
                flattenArrays(entry, results);
            }
        } else {
            results.add(arrNode.textValue());
        }
    }

    @SuppressWarnings("nls")
    private StringBuilder buildParams(StringBuilder url, String query) {
        addQueryParam(url, "db", database, "?");
        addQueryParam(url, "u", username, "&");
        addQueryParam(url, "p", password, "&");
        addQueryParam(url, "rp", retentionPolicy, "&");
        addQueryParam(url, "precision", timePrecision, "&");
        addQueryParam(url, "q", query, "&");
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
