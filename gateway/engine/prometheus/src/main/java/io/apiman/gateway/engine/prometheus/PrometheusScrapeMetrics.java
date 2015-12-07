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

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.metrics.RequestMetric;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;

import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;

/**
 * Prometheus scrape metrics.
 *
 * Config:
 * <ul>
 *   <li>port - port for web-server to listen on for scrape requests</li>
 * </ul>
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class PrometheusScrapeMetrics implements IMetrics {

    private static final String APIMAN = "apiman";
    private static final String APPLICATION = "application";
    private static final String API_VERSION = "apiVersion";
    private static final String API = "api";
    private static final String METHOD = "method";
    private static final String RESPONSE_CODE = "responseCode";
    private static final String FAILURE_CODE = "failureCode";

    private final Map<String, String> componentConfig;
    private final Vertx vertx;
    private final HttpServer webServer;

    final CollectorRegistry collectorRegistry = new CollectorRegistry();

    // API
    final Counter requestsCtr = Counter.build()
            .name("requests_total").help("Total requests.")
            .namespace(APIMAN)
            .labelNames(METHOD,
                    RESPONSE_CODE,
                    API,
                    API_VERSION,
                    APPLICATION)
            .register(collectorRegistry);

    final Counter errorsCtr = Counter.build()
            .name("errors_total").help("Total errors.")
            .namespace(APIMAN)
            .labelNames(METHOD,
                    RESPONSE_CODE,
                    API,
                    API_VERSION,
                    APPLICATION)
            .register(collectorRegistry);

    final Counter failureCtr = Counter.build()
            .name("policy_failures_total").help("Total policy failures.")
            .namespace(APIMAN)
            .labelNames(METHOD,
                    RESPONSE_CODE,
                    FAILURE_CODE,
                    API,
                    API_VERSION,
                    APPLICATION)
            .register(collectorRegistry);

    final Summary requestDuration = Summary.build()
            .name("request_duration_milliseconds").help("Request duration in milliseconds.")
            .namespace(APIMAN)
            .labelNames(METHOD,
                    RESPONSE_CODE,
                    API,
                    API_VERSION,
                    APPLICATION)
            .register(collectorRegistry);

    public PrometheusScrapeMetrics(Map<String, String> componentConfig,
            Handler<AsyncResult<HttpServer>> listenHandler) {
        this.vertx = Vertx.vertx();
        this.componentConfig = componentConfig;
        this.webServer = setupWebserver(listenHandler);
    }

    public PrometheusScrapeMetrics(Map<String, String> componentConfig) {
        this.vertx = Vertx.vertx();
        this.componentConfig = componentConfig;
        this.webServer = setupWebserver(null);
    }

    private HttpServer setupWebserver(Handler<AsyncResult<HttpServer>> listenHandler) {
        String port = componentConfig.get("port");
        Objects.requireNonNull(port, "Must specify port for scrape server to listen on");
        return vertx.createHttpServer().requestHandler(request -> {
            HttpServerResponse response = request.response();
            StringWriter sw = new StringWriter();
            try {
                TextFormat.write004(sw, collectorRegistry.metricFamilySamples());
                response.setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader("Content-Type", TextFormat.CONTENT_TYPE_004)
                    .setChunked(true)
                    .write(sw.toString());
            } catch (Exception e) {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                    .write(Json.encode(e));
            }
            response.end();
        }).listen(Integer.parseInt(port), listenHandler);
    }

    @Override
    public void setComponentRegistry(IComponentRegistry componentRegistry) {
    }

    @Override
    public void record(RequestMetric metric) {
        doRequestsCtr(requestsCtr, metric);
        doRequestDuration(metric);

        if (metric.isError()) {
            doRequestsCtr(errorsCtr, metric);
        }

        if (metric.isFailure()) {
            doFailureCtr(metric);
        }
    }

    private void doFailureCtr(RequestMetric metric) {
        failureCtr.labels(metric.getMethod(),
                Integer.toString(metric.getResponseCode()),
                Integer.toString(metric.getFailureCode()),
                metric.getApiId(),
                metric.getApiVersion(),
                metric.getApplicationId()).inc();
    }

    protected void doRequestsCtr(Counter ctr, RequestMetric metric) {
        ctr.labels(metric.getMethod(),
                Integer.toString(metric.getResponseCode()),
                metric.getApiId(),
                metric.getApiVersion(),
                metric.getApplicationId()).inc();
    }

    protected void doRequestDuration(RequestMetric metric) {
        requestDuration.labels(metric.getMethod(),
                Integer.toString(metric.getResponseCode()),
                metric.getApiId(),
                metric.getApiVersion(),
                metric.getApplicationId()).observe(metric.getRequestDuration());
    }

    public void close(Handler<AsyncResult<Void>> completionHandler) {
        webServer.close(completionHandler);
        collectorRegistry.clear();
    }
}
