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
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
    
    private static final ObjectMapper readMapper = new ObjectMapper();
    private static final ObjectMapper writeMapper = new ObjectMapper();
    static {
        readMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        writeMapper.setSerializationInclusion(Include.NON_NULL);
    }
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
            RequestBody body = RequestBody.create(JSON, writeMapper.writeValueAsString(bean));
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
            if (response.code() == 204) {
                return Collections.EMPTY_LIST;
            }
            String responseBody = response.body().string();
            return readMapper.reader(new TypeReference<List<MetricBean>>() {}).readValue(responseBody);
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
    public DataPointLongBean addCounterDataPoint(String tenantId, String counterId, Date timestamp, long value) {
        List<DataPointLongBean> dataPoints = new ArrayList<>();
        DataPointLongBean dataPoint = new DataPointLongBean(timestamp, value);
        dataPoints.add(dataPoint);
        addCounterDataPoints(tenantId, counterId, dataPoints);
        return dataPoint;
    }

    /**
     * Adds a single data point (with tags) to the given counter.
     * @param tenantId
     * @param counterId
     * @param timestamp
     * @param value
     * @param tags
     */
    public DataPointLongBean addCounterDataPoint(String tenantId, String counterId, Date timestamp, long value, Map<String, String> tags) {
        List<DataPointLongBean> dataPoints = new ArrayList<>();
        DataPointLongBean dataPoint = new DataPointLongBean(timestamp, value);
        dataPoint.setTags(tags);
        dataPoints.add(dataPoint);
        addCounterDataPoints(tenantId, counterId, dataPoints);
        return dataPoint;
    }

    /**
     * Adds multiple data points to a counter.
     * @param tenantId
     * @param counterId
     * @param dataPoints
     */
    public void addCounterDataPoints(String tenantId, String counterId, List<DataPointLongBean> dataPoints) {
        try {
            URL endpoint = serverUrl.toURI().resolve("counters/" + counterId + "/raw").toURL(); //$NON-NLS-1$ //$NON-NLS-2$
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
            URL endpoint = serverUrl.toURI().resolve("counters/raw").toURL(); //$NON-NLS-1$
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
    public List<BucketDataPointBean> getCounterData(String tenantId, String counterId, Date from, Date to,
            BucketSizeType bucketSize) {
        try {
            StringBuilder params = new StringBuilder();
            params.append("?")
                .append("start=")
                .append(from.getTime())
                .append("&end=")
                .append(to.getTime())
                .append("&bucketDuration=")
                .append(bucketSize.getValue());
            URL endpoint = serverUrl.toURI().resolve("counters/" + counterId + "/stats" + params.toString()).toURL(); //$NON-NLS-1$
            Request request = new Request.Builder()
                    .url(endpoint)
                    .header("Accept", "application/json") //$NON-NLS-1$ //$NON-NLS-2$
                    .header("Hawkular-Tenant", tenantId) //$NON-NLS-1$
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.code() >= 400) {
                throw hawkularMetricsError(response);
            }
            if (response.code() == 204) {
                return Collections.EMPTY_LIST;
            }
            String responseBody = response.body().string();
            return readMapper.reader(new TypeReference<List<BucketDataPointBean>>() {}).readValue(responseBody);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a list of buckets containing aggregate information about data in the
     * indicated counter.  The number of buckets is passed in, the size of each
     * bucket is determined by the number asked for and the time range.
     * @param tenantId
     * @param counterId
     * @param from
     * @param to
     * @param bucketSize
     */
    @SuppressWarnings("nls")
    public List<BucketDataPointBean> getCounterData(String tenantId, String counterId, Date from, Date to,
            int numBuckets) {
        try {
            StringBuilder params = new StringBuilder();
            params.append("?")
                .append("start=")
                .append(from.getTime())
                .append("&end=")
                .append(to.getTime())
                .append("&buckets=")
                .append(numBuckets);
            URL endpoint = serverUrl.toURI().resolve("counters/" + counterId + "/stats" + params.toString()).toURL(); //$NON-NLS-1$
            Request request = new Request.Builder()
                    .url(endpoint)
                    .header("Accept", "application/json") //$NON-NLS-1$ //$NON-NLS-2$
                    .header("Hawkular-Tenant", tenantId) //$NON-NLS-1$
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.code() >= 400) {
                throw hawkularMetricsError(response);
            }
            if (response.code() == 204) {
                return Collections.EMPTY_LIST;
            }
            String responseBody = response.body().string();
            return readMapper.reader(new TypeReference<List<BucketDataPointBean>>() {}).readValue(responseBody);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a list of buckets containing aggregate information about data in the
     * indicated counter.  The buckets returned are based on the values of the tag(s)
     * provided.
     * @param tenantId
     * @param counterId
     * @param from
     * @param to
     * @param bucketSize
     */
    @SuppressWarnings("nls")
    public Map<String, BucketDataPointBean> getCounterData(String tenantId, String counterId, Date from, Date to,
            Map<String, String> tags) {
        try {
            StringBuilder params = new StringBuilder();
            params.append("?")
                .append("start=")
                .append(from.getTime())
                .append("&end=")
                .append(to.getTime());
            URL endpoint = serverUrl.toURI().resolve("counters/" + counterId + "/stats/tags/" + encodeTags(tags) + params.toString()).toURL(); //$NON-NLS-1$
            Request request = new Request.Builder()
                    .url(endpoint)
                    .header("Accept", "application/json") //$NON-NLS-1$ //$NON-NLS-2$
                    .header("Hawkular-Tenant", tenantId) //$NON-NLS-1$
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.code() >= 400) {
                throw hawkularMetricsError(response);
            }
            if (response.code() == 204) {
                return Collections.EMPTY_MAP;
            }
            String responseBody = response.body().string();
            return readMapper.reader(new TypeReference<Map<String, BucketDataPointBean>>() {}).readValue(responseBody);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Simple method to create some tags.
     * @param strings
     * @return
     */
    public static Map<String, String> tags(String ... strings) {
        Map<String, String> tags = new HashMap<>();
        for (int i = 0; i < strings.length - 1; i+=2) {
            String key = strings[i];
            String value = strings[i + 1];
            if (key != null && value != null) {
                tags.put(key, value);
            }
        }
        return tags;
    }

    /**
     * @param response
     */
    private static HawkularMetricsException hawkularMetricsError(Response response) {
        // TODO better error handling goes here.
        return new UnexpectedMetricsException(response.message());
    }

    /**
     * Encodes the tags into the format expected by HM.
     * @param tags
     */
    protected static String encodeTags(Map<String, String> tags) {
        if (tags == null) {
            return null;
        }
        try {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (Entry<String, String> entry : tags.entrySet()) {
                if (!first) {
                    builder.append(',');
                }
                builder.append(URLEncoder.encode(entry.getKey(), "UTF-8")); //$NON-NLS-1$
                builder.append(':');
                builder.append(URLEncoder.encode(entry.getValue(), "UTF-8")); //$NON-NLS-1$
                first = false;
            }
            return builder.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ""; //$NON-NLS-1$
        }
    }

    
    @SuppressWarnings("nls")
    public static void main(String[] args) throws Exception {
        HawkularMetricsClient client = new HawkularMetricsClient("http://localhost:9080/hawkular/metrics"); //$NON-NLS-1$
        List<MetricBean> metrics = client.listCounterMetrics("FOO"); //$NON-NLS-1$
        for (MetricBean metric : metrics) {
            System.out.println("-------------"); //$NON-NLS-1$
            System.out.println(writeMapper.writer().writeValueAsString(metric));
        }
        
        client.addCounterDataPoint("FOO", "counter-1", new Date(), 1, tags("foo", "bar"));
        client.addCounterDataPoint("FOO", "counter-1", new Date(), 1);
        client.addCounterDataPoint("FOO", "counter-2", new Date(), 1, tags("foo", "bar"));
        client.addCounterDataPoint("FOO", "counter-2", new Date(), 1, tags("foo", "bar"));
        client.addCounterDataPoint("FOO", "counter-2", new Date(), 1, tags("foo", "bar"));
        client.addCounterDataPoint("FOO", "counter-2", new Date(), 1, tags("foo", "baz"));
        
        long fiveMinsAgo = System.currentTimeMillis() - (2 * 60 * 1000);
        long now = System.currentTimeMillis();
        Date from = new Date(fiveMinsAgo);
        Date to = new Date(now);
        List<BucketDataPointBean> counterData = client.getCounterData("FOO", "counter-2", from, to, BucketSizeType.Minute);
        System.out.println("+++++++++++++"); //$NON-NLS-1$
        System.out.println(writeMapper.writerWithDefaultPrettyPrinter().writeValueAsString(counterData));
        System.out.println("+++++++++++++"); //$NON-NLS-1$
        
        Map<String, BucketDataPointBean> data = client.getCounterData("FOO", "counter-2", from, to, tags("foo", "*"));
        for (Entry<String, BucketDataPointBean> entry : data.entrySet()) {
            System.out.println("----------- " + entry.getKey() + " ------------"); //$NON-NLS-1$
            System.out.println(writeMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entry.getValue()));
        }
    }

}
