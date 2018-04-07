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
package io.apiman.gateway.engine.prometheus;

import io.apiman.gateway.engine.metrics.RequestMetric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import static org.junit.Assert.assertTrue;

/**
 * Prometheus scrape metrics tests.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class BasicMetricsTest {

    private PrometheusScrapeMetrics prometheusMetrics;
    private OkHttpClient client;

    @Before
    public void before() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        client = new OkHttpClient();

        final Map<String, String> promConfig = new HashMap<>();
        promConfig.put("port", "9876");
        this.prometheusMetrics = new PrometheusScrapeMetrics(promConfig, result -> latch.countDown());
        prometheusMetrics.setComponentRegistry(null);

        latch.await();
    }

    @After
    public void after() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        prometheusMetrics.close(result -> {
            if (result.failed()) throw new RuntimeException(result.cause());
            latch.countDown();
        });

        latch.await();
    }

    @Test
    public void validMetrics_WithClientId() throws IOException {
        @SuppressWarnings("serial")
        final Set<String> expected = new LinkedHashSet<String>(){{
            add("apiman_request_duration_milliseconds_count{method=\"GET\",responseCode=\"200\",api=\"apiId\",apiVersion=\"apiVersion\",client=\"clientId\",} 1.0");
            add("apiman_request_duration_milliseconds_sum{method=\"GET\",responseCode=\"200\",api=\"apiId\",apiVersion=\"apiVersion\",client=\"clientId\",} 644.0");
            add("apiman_requests_total{method=\"GET\",responseCode=\"200\",api=\"apiId\",apiVersion=\"apiVersion\",client=\"clientId\",} 1.0");
        }};

        final RequestMetric requestMetric = buildRequestMetric("clientId", false, 200, "hamsters are cool");
        prometheusMetrics.record(requestMetric);

        final Request request = new Request.Builder().url("http://localhost:9876/").get().build();
        final Response response = client.newCall(request).execute();
        final String rString = response.body().string();
        assertTrue(equals(expected, rString));
    }

    @Test
    public void validMetrics_NullClientId() throws IOException {
        @SuppressWarnings("serial")
        final Set<String> expected = new LinkedHashSet<String>(){{
            add("apiman_request_duration_milliseconds_count{method=\"GET\",responseCode=\"200\",api=\"apiId\",apiVersion=\"apiVersion\",client=\"\",} 1.0");
            add("apiman_request_duration_milliseconds_sum{method=\"GET\",responseCode=\"200\",api=\"apiId\",apiVersion=\"apiVersion\",client=\"\",} 644.0");
            add("apiman_requests_total{method=\"GET\",responseCode=\"200\",api=\"apiId\",apiVersion=\"apiVersion\",client=\"\",} 1.0");
        }};

        final RequestMetric requestMetric = buildRequestMetric(null, false, 200, "hamsters are cool");
        prometheusMetrics.record(requestMetric);

        final Request request = new Request.Builder().url("http://localhost:9876/").get().build();
        final Response response = client.newCall(request).execute();
        final String rString = response.body().string();
        assertTrue(equals(expected, rString));
    }

    @Test
    public void errorMetrics() throws IOException {
        @SuppressWarnings("serial")
        final Set<String> expected = new LinkedHashSet<String>(){{
            add("apiman_request_duration_milliseconds_count{method=\"GET\",responseCode=\"404\",api=\"apiId\",apiVersion=\"apiVersion\",client=\"clientId\",} 1.0");
            add("apiman_request_duration_milliseconds_sum{method=\"GET\",responseCode=\"404\",api=\"apiId\",apiVersion=\"apiVersion\",client=\"clientId\",} 644.0");
            add("apiman_requests_total{method=\"GET\",responseCode=\"404\",api=\"apiId\",apiVersion=\"apiVersion\",client=\"clientId\",} 1.0");
            add("apiman_errors_total{method=\"GET\",responseCode=\"404\",api=\"apiId\",apiVersion=\"apiVersion\",client=\"clientId\",} 1.0");
        }};

        final RequestMetric requestMetric = buildRequestMetric("clientId", true, 404, "could not find hamsters");
        prometheusMetrics.record(requestMetric);

        final Request request = new Request.Builder().url("http://localhost:9876/").get().build();
        final Response response = client.newCall(request).execute();
        final String rString = response.body().string();
        assertTrue(equals(expected, rString));
    }

    private RequestMetric buildRequestMetric(String clientId, boolean error, int responseCode, String responseMessage) {
        final RequestMetric requestMetric = new RequestMetric();
        requestMetric.setClientId(clientId);
        requestMetric.setClientOrgId("clientOrgId");
        requestMetric.setClientVersion("clientVersion");
        requestMetric.setContractId("contractId");
        requestMetric.setError(error);
        requestMetric.setFailure(false);
        requestMetric.setMethod("GET");
        requestMetric.setPlanId("planId");
        requestMetric.setRequestDuration(9001);
        requestMetric.setRequestStart(new Date(1440770200));
        requestMetric.setRequestEnd(new Date(1440770844));
        requestMetric.setResource("/wibble");
        requestMetric.setResponseCode(responseCode);
        requestMetric.setResponseMessage(responseMessage);
        requestMetric.setApiDuration(1000);
        requestMetric.setApiStart(new Date(1440770233));
        requestMetric.setApiEnd(new Date(1440770822));
        requestMetric.setApiId("apiId");
        requestMetric.setApiOrgId("apiOrgId");
        requestMetric.setApiVersion("apiVersion");
        requestMetric.setUser("user");
        return requestMetric;
    }

    private boolean equals(Set<String> expected, String actualRaw) {
        final List<String> lines = new ArrayList<>(Arrays.asList(actualRaw.split(System.getProperty("line.separator"))));

        return lines.stream()
                .filter( e -> !e.isEmpty() && e.charAt(0) != '#' )
                .allMatch(expected::contains);
    }
}
