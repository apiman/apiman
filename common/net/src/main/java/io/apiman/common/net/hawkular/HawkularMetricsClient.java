/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.common.net.hawkular;

import io.apiman.common.config.options.HttpConnectorOptions;
import io.apiman.common.net.hawkular.beans.BucketDataPointBean;
import io.apiman.common.net.hawkular.beans.BucketSizeType;
import io.apiman.common.net.hawkular.beans.DataPointLongBean;
import io.apiman.common.net.hawkular.beans.MetricBean;
import io.apiman.common.net.hawkular.beans.MetricLongBean;
import io.apiman.common.net.hawkular.beans.TenantBean;
import io.apiman.common.net.hawkular.errors.HawkularMetricsException;
import io.apiman.common.net.hawkular.errors.UnexpectedMetricsException;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.CertificatePinner;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Internal;
import com.squareup.okhttp.internal.Network;
import com.squareup.okhttp.internal.Util;
import com.squareup.okhttp.internal.http.AuthenticatorAdapter;

/**
 * A REST client to the Hawkular Metrics server.  For more information about the
 * Hawkular Metrics REST API, see:  http://www.hawkular.org/docs/rest/rest-metrics.html
 * @author eric.wittmann@gmail.com
 */
public class HawkularMetricsClient {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private static URL toURL(String url) {
        try {
            if (!url.endsWith("/")) { //$NON-NLS-1$
                url += "/"; //$NON-NLS-1$
            }
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    private static RequestBody toBody(Object bean) {
        try {
            RequestBody body = RequestBody.create(JSON, mapper.writeValueAsString(bean));
            return body;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8"); //$NON-NLS-1$

    private static final int DEFAULT_READ_TIMEOUT = 10;
    private static final int DEFAULT_WRITE_TIMEOUT = 10;
    private static final int DEFAULT_CONNECT_TIMEOUT = 10;
    private static final List<ConnectionSpec> DEFAULT_CONNECTION_SPECS = Util.immutableList(
            ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT);

    private OkHttpClient httpClient;
    private URL serverUrl;
    
    /**
     * Constructor.
     * @param metricsServer
     */
    public HawkularMetricsClient(String metricsServer) {
        this(toURL(metricsServer));
    }

    /**
     * Constructor.
     * @param metricsServer
     * @param options
     */
    public HawkularMetricsClient(String metricsServer, HttpConnectorOptions options) {
        this(toURL(metricsServer), options);
    }

    /**
     * Constructor.
     * @param metricsServer
     */
    public HawkularMetricsClient(URL metricsServer) {
        this.serverUrl = metricsServer;
        httpClient = new OkHttpClient();
        httpClient.setReadTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS);
        httpClient.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        httpClient.setFollowRedirects(true);
        httpClient.setFollowSslRedirects(true);
        httpClient.setProxySelector(ProxySelector.getDefault());
        httpClient.setCookieHandler(CookieHandler.getDefault());
        httpClient.setCertificatePinner(CertificatePinner.DEFAULT);
        httpClient.setAuthenticator(AuthenticatorAdapter.INSTANCE);
        httpClient.setConnectionPool(ConnectionPool.getDefault());
        httpClient.setProtocols(Util.immutableList(Protocol.HTTP_1_1));
        httpClient.setConnectionSpecs(DEFAULT_CONNECTION_SPECS);
        httpClient.setSocketFactory(SocketFactory.getDefault());
        Internal.instance.setNetwork(httpClient, Network.DEFAULT);
    }

    /**
     * Constructor.
     * @param metricsServer
     * @param options
     */
    public HawkularMetricsClient(URL metricsServer, HttpConnectorOptions options) {
        this(metricsServer);
        httpClient.setReadTimeout(options.getReadTimeout(), TimeUnit.SECONDS);
        httpClient.setWriteTimeout(options.getWriteTimeout(), TimeUnit.SECONDS);
        httpClient.setConnectTimeout(options.getConnectTimeout(), TimeUnit.SECONDS);
        httpClient.setFollowRedirects(options.isFollowRedirects());
        httpClient.setFollowSslRedirects(options.isFollowRedirects());
    }
    
    /**
     * Creates a new tenant.
     * @param tenantId
     */
    public void createTenant(String tenantId) {
        TenantBean tenant = new TenantBean(tenantId);
        try {
            URL endpoint = serverUrl.toURI().resolve("tenants").toURL(); //$NON-NLS-1$
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(toBody(tenant))
                    .header("Hawkular-Tenant", tenantId) //$NON-NLS-1$
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.code() >= 400) {
                throw hawkularMetricsError(response);
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Get a list of all counter metrics for a given tenant.
     * @param tenantId
     */
    public List<MetricBean> listCounterMetrics(String tenantId) {
        try {
            URL endpoint = serverUrl.toURI().resolve("counters").toURL(); //$NON-NLS-1$
            Request request = new Request.Builder()
                    .url(endpoint)
                    .header("Accept", "application/json") //$NON-NLS-1$ //$NON-NLS-2$
                    .header("Hawkular-Tenant", tenantId) //$NON-NLS-1$
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.code() >= 400) {
                throw hawkularMetricsError(response);
            }
            String responseBody = response.body().string();
            return mapper.reader(new TypeReference<List<MetricBean>>() {}).readValue(responseBody);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Adds a single data point to the given counter.
     * @param tenantId
     * @param counterId
     * @param timestamp
     * @param value
     */
    public void addCounterDataPoint(String tenantId, String counterId, Date timestamp, long value) {
        List<DataPointLongBean> dataPoints = new ArrayList<>();
        dataPoints.add(new DataPointLongBean(timestamp, value));
        addCounterDataPoints(tenantId, counterId, dataPoints);
    }
    
    /**
     * Adds multiple data points to a counter.
     * @param tenantId
     * @param counterId
     * @param dataPoints
     */
    public void addCounterDataPoints(String tenantId, String counterId, List<DataPointLongBean> dataPoints) {
        try {
            URL endpoint = serverUrl.toURI().resolve("counters/" + counterId + "/data").toURL(); //$NON-NLS-1$ //$NON-NLS-2$
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(toBody(dataPoints))
                    .header("Hawkular-Tenant", tenantId) //$NON-NLS-1$
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.code() >= 400) {
                throw hawkularMetricsError(response);
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Adds one or more data points for multiple counters all at once!
     * @param tenantId
     * @param data
     */
    public void addMultipleCounterDataPoints(String tenantId, List<MetricLongBean> data) {
        try {
            URL endpoint = serverUrl.toURI().resolve("counters/data").toURL(); //$NON-NLS-1$
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(toBody(data))
                    .header("Hawkular-Tenant", tenantId) //$NON-NLS-1$
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.code() >= 400) {
                throw hawkularMetricsError(response);
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a list of buckets containing aggregate information about data in the
     * indicated counter.  The number of buckets is determined by the bucket size
     * and date/time range specified.
     * @param tenantId
     * @param counterId
     * @param from
     * @param to
     * @param bucketSize
     */
    @SuppressWarnings("nls")
    public List<BucketDataPointBean> getCounterData(String tenantId, String counterId, Date from, Date to, BucketSizeType bucketSize) {
        try {
            StringBuilder params = new StringBuilder();
            params.append("?")
                .append("start=")
                .append(from.getTime())
                .append("&end=")
                .append(to.getTime())
                .append("&bucketDuration=")
                .append(bucketSize.getValue());
            URL endpoint = serverUrl.toURI().resolve("counters/" + counterId + "/data" + params.toString()).toURL(); //$NON-NLS-1$
            Request request = new Request.Builder()
                    .url(endpoint)
                    .header("Accept", "application/json") //$NON-NLS-1$ //$NON-NLS-2$
                    .header("Hawkular-Tenant", tenantId) //$NON-NLS-1$
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.code() >= 400) {
                throw hawkularMetricsError(response);
            }
            String responseBody = response.body().string();
            return mapper.reader(new TypeReference<List<BucketDataPointBean>>() {}).readValue(responseBody);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("nls")
    public List<BucketDataPointBean> getCounterData(String tenantId, String counterId, Date from, Date to, int numBuckets) {
        try {
            StringBuilder params = new StringBuilder();
            params.append("?")
                .append("start=")
                .append(from.getTime())
                .append("&end=")
                .append(to.getTime())
                .append("&buckets=")
                .append(numBuckets);
            URL endpoint = serverUrl.toURI().resolve("counters/" + counterId + "/data" + params.toString()).toURL(); //$NON-NLS-1$
            Request request = new Request.Builder()
                    .url(endpoint)
                    .header("Accept", "application/json") //$NON-NLS-1$ //$NON-NLS-2$
                    .header("Hawkular-Tenant", tenantId) //$NON-NLS-1$
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.code() >= 400) {
                throw hawkularMetricsError(response);
            }
            String responseBody = response.body().string();
            return mapper.reader(new TypeReference<List<BucketDataPointBean>>() {}).readValue(responseBody);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @param response
     */
    private static HawkularMetricsException hawkularMetricsError(Response response) {
        // TODO better error handling goes here.
        return new UnexpectedMetricsException(response.message());
    }

    
    
    @SuppressWarnings("nls")
    public static void main(String[] args) throws Exception {
        HawkularMetricsClient client = new HawkularMetricsClient("http://bluejay:8080/hawkular/metrics"); //$NON-NLS-1$
        List<MetricBean> metrics = client.listCounterMetrics("XYZ"); //$NON-NLS-1$
        for (MetricBean metric : metrics) {
            System.out.println("-------------"); //$NON-NLS-1$
            System.out.println(mapper.writer().writeValueAsString(metric));
        }
        
        client.addCounterDataPoint("FOO", "counter-1", new Date(), 1);
        client.addCounterDataPoint("FOO", "counter-1", new Date(), 1);
        client.addCounterDataPoint("FOO", "counter-2", new Date(), 1);
        client.addCounterDataPoint("FOO", "counter-2", new Date(), 1);
        client.addCounterDataPoint("FOO", "counter-2", new Date(), 1);
        client.addCounterDataPoint("FOO", "counter-2", new Date(), 1);
        
        long fiveMinsAgo = System.currentTimeMillis() - 3 * 60 * 1000;
        long now = System.currentTimeMillis();
        Date from = new Date(fiveMinsAgo);
        Date to = new Date(now);
        List<BucketDataPointBean> counterData = client.getCounterData("FOO", "counter-2", from, to, BucketSizeType.Minute);
        System.out.println("+++++++++++++"); //$NON-NLS-1$
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(counterData));
        System.out.println("+++++++++++++"); //$NON-NLS-1$
    }

}
