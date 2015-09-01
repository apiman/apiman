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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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
    public void before() {
        client = new OkHttpClient();
        Map<String, String> promConfig = new HashMap<>();
        promConfig.put("port", "9876");
        this.prometheusMetrics = new PrometheusScrapeMetrics(promConfig);
        prometheusMetrics.setComponentRegistry(null);
    }

    @After
    public void after() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        prometheusMetrics.close(result -> {
            if (result.failed())
                throw new RuntimeException(result.cause());
            latch.countDown();
        });

        latch.await();
    }

    @Test
    public void validMetrics() throws IOException {
        @SuppressWarnings("serial")
        Set<String> expected = new LinkedHashSet<String>(){{
            add("apiman_request_duration_milliseconds_count{method=\"GET\",responseCode=\"200\",service=\"serviceId\",serviceVersion=\"serviceVersion\",application=\"applicationId\",} 1.0");
            add("apiman_request_duration_milliseconds_sum{method=\"GET\",responseCode=\"200\",service=\"serviceId\",serviceVersion=\"serviceVersion\",application=\"applicationId\",} 644.0");
            add("apiman_requests_total{method=\"GET\",responseCode=\"200\",service=\"serviceId\",serviceVersion=\"serviceVersion\",application=\"applicationId\",} 1.0");
        }};

        RequestMetric requestMetric = new RequestMetric();
        requestMetric.setApplicationId("applicationId");
        requestMetric.setApplicationOrgId("applicationOrgId");
        requestMetric.setApplicationVersion("applicationVersion");
        requestMetric.setContractId("contractId");
        requestMetric.setError(false);
        requestMetric.setFailure(false);
        requestMetric.setMethod("GET");
        requestMetric.setPlanId("planId");
        requestMetric.setRequestDuration(9001);
        requestMetric.setRequestStart(new Date(1440770200));
        requestMetric.setRequestEnd(new Date(1440770844));
        requestMetric.setResource("/wibble");
        requestMetric.setResponseCode(200);
        requestMetric.setResponseMessage("hamsters are cool");
        requestMetric.setServiceDuration(1000);
        requestMetric.setServiceStart(new Date(1440770233));
        requestMetric.setServiceEnd(new Date(1440770822));
        requestMetric.setServiceId("serviceId");
        requestMetric.setServiceOrgId("serviceOrgId");
        requestMetric.setServiceVersion("serviceVersion");
        requestMetric.setUser("user");
        //Record it
        prometheusMetrics.record(requestMetric);

        Request request = new Request.Builder().url("http://localhost:9876/").get().build();
        Response response = client.newCall(request).execute();
        String rString = response.body().string();
        Assert.assertTrue(equals(expected, rString));
    }

    @Test
    public void errorMetrics() throws IOException {
        @SuppressWarnings("serial")
        Set<String> expected = new LinkedHashSet<String>(){{
            add("apiman_request_duration_milliseconds_count{method=\"GET\",responseCode=\"404\",service=\"serviceId\",serviceVersion=\"serviceVersion\",application=\"applicationId\",} 1.0");
            add("apiman_request_duration_milliseconds_sum{method=\"GET\",responseCode=\"404\",service=\"serviceId\",serviceVersion=\"serviceVersion\",application=\"applicationId\",} 644.0");
            add("apiman_requests_total{method=\"GET\",responseCode=\"404\",service=\"serviceId\",serviceVersion=\"serviceVersion\",application=\"applicationId\",} 1.0");
            add("apiman_errors_total{method=\"GET\",responseCode=\"404\",service=\"serviceId\",serviceVersion=\"serviceVersion\",application=\"applicationId\",} 1.0");
        }};

        RequestMetric requestMetric = new RequestMetric();
        requestMetric.setApplicationId("applicationId");
        requestMetric.setApplicationOrgId("applicationOrgId");
        requestMetric.setApplicationVersion("applicationVersion");
        requestMetric.setContractId("contractId");
        requestMetric.setError(true);
        requestMetric.setFailure(false);
        requestMetric.setMethod("GET");
        requestMetric.setPlanId("planId");
        requestMetric.setRequestDuration(9001);
        requestMetric.setRequestStart(new Date(1440770200));
        requestMetric.setRequestEnd(new Date(1440770844));
        requestMetric.setResource("/wibble");
        requestMetric.setResponseCode(404);
        requestMetric.setResponseMessage("could not find hamsters");
        requestMetric.setServiceDuration(1000);
        requestMetric.setServiceStart(new Date(1440770233));
        requestMetric.setServiceEnd(new Date(1440770822));
        requestMetric.setServiceId("serviceId");
        requestMetric.setServiceOrgId("serviceOrgId");
        requestMetric.setServiceVersion("serviceVersion");
        requestMetric.setUser("user");
        //Record it
        prometheusMetrics.record(requestMetric);

        Request request = new Request.Builder().url("http://localhost:9876/").get().build();
        Response response = client.newCall(request).execute();
        String rString = response.body().string();
        Assert.assertTrue(equals(expected, rString));
    }

    private boolean equals(Set<String> expected, String actualRaw) {
        List<String> lines = new ArrayList<>(Arrays.asList(actualRaw.split(System.getProperty("line.separator"))));

        return lines.stream()
                .filter( e -> !e.isEmpty() && e.charAt(0) != '#' )
                .allMatch( e -> expected.contains(e) );
    }

}
