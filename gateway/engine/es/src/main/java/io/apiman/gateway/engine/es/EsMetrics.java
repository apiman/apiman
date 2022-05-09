/*
 * Copyright 2013 JBoss Inc
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

package io.apiman.gateway.engine.es;

import io.apiman.common.es.util.AbstractEsComponent;
import io.apiman.common.es.util.EsConstants;
import io.apiman.common.es.util.builder.index.EsDynamicTemplate;
import io.apiman.common.es.util.builder.index.EsDynamicTemplate.EsDynamicTemplateBuilder;
import io.apiman.common.es.util.builder.index.EsIndexProperties;
import io.apiman.common.es.util.builder.index.EsIndexProperties.EsIndexPropertiesBuilder;
import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.beans.util.QueryMap;
import io.apiman.gateway.engine.metrics.RequestMetric;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import static io.apiman.common.es.util.builder.index.EsIndexUtils.BOOL_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.DATE_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.IP_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.KEYWORD_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.LONG_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.TEXT_AND_KEYWORD_PROP_128;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.TEXT_AND_KEYWORD_PROP_256;
import static io.apiman.gateway.engine.storage.util.BackingStoreUtil.JSON_MAPPER;

/**
 * An elasticsearch implementation of the {@link IMetrics} interface.
 *
 * Can also dynamically capture headers and query parameters.
 * See available options in
 *
 * @author eric.wittmann@redhat.com
 * @author marc@blackparrotlabs.io
 */
public class EsMetrics extends AbstractEsComponent implements IMetrics {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EsMetrics.class);
    private static final int DEFAULT_QUEUE_SIZE = 10000;
    private static final int DEFAULT_BATCH_SIZE = 1000;

    protected IComponentRegistry componentRegistry;
    private final BlockingQueue<EsMetricPayload> queue;
    private final int batchSize;
    private final EsMetricsClientOptionsParser options;
    private final Pattern requestHeaderPattern;
    private final Pattern responseHeaderPattern;
    private final Pattern queryParamsPattern;

    /**
     * Constructor.
     *
     * @param config map of configuration options
     */
    public EsMetrics(Map<String, String> config) {
        super(config);
        int queueSize = DEFAULT_QUEUE_SIZE;
        String queueSizeConfig = config.get("queue.size"); //$NON-NLS-1$
        if (queueSizeConfig != null) {
            queueSize = Integer.parseInt(queueSizeConfig);
        }
        queue = new LinkedBlockingDeque<>(queueSize);

        int batchSize = DEFAULT_BATCH_SIZE;
        String batchSizeConfig = config.get("batch.size"); //$NON-NLS-1$
        if (batchSizeConfig != null) {
            batchSize = Integer.parseInt(batchSizeConfig);
        }
        this.batchSize = batchSize;
        this.options = new EsMetricsClientOptionsParser(config);
        requestHeaderPattern = buildRegex(options.getRequestHeaders());
        responseHeaderPattern = buildRegex(options.getResponseHeaders());
        queryParamsPattern = buildRegex(options.getQueryParams());
        startConsumerThread();
    }

    private Pattern buildRegex(List<String> headers) {
        String joinedPattern = String.join("|", headers);
        return Pattern.compile(joinedPattern, Pattern.CASE_INSENSITIVE);
    }

    /**
     * @see IMetrics#setComponentRegistry(IComponentRegistry)
     */
    @Override
    public void setComponentRegistry(IComponentRegistry registry) {
        componentRegistry = registry;
    }

    /**
     * @see IMetrics#record(RequestMetric)
     */
    @Override
    public void record(RequestMetric metric, ApiRequest apiRequest, ApiResponse apiResponse) {
        try {
            EsMetricPayload esMetric = buildEsMetric(metric, apiRequest, apiResponse);

            // Queue#offer returns false if queue is full and unable to accept new records.
            if (!queue.offer(esMetric)) {
                LOGGER.warn("A metrics entry was dropped because the metrics queue is full. You can try to alter `queue.size` and `batch.size`, but a full buffer "
                                    + "is usually caused by Elasticsearch being slow or unavailable, network problems, or OS network stack configuration issues. "
                                    + "Increasing buffer sizes often just delays the problem, but may be helpful in high traffic and bursting scenarios, or to survive "
                                    + "short periods where the network or Elasticsearch are unavailable. Entry dropped: {0}", metric);
            }
        } catch (Exception e) {
            LOGGER.error(e, "A metrics entry was dropped due to error inserting new record into the metrics queue. "
                                    + "Entry dropped: {0}. Error: {1} ", e.getMessage(), metric);
        }
    }

    /**
     * Starts a thread which will serially pull information off the blocking
     * queue and submit that information to ES metrics.
     */
    private void startConsumerThread() {
        Thread thread = new Thread(() -> {
            // TODO(msavy): refactor to spin-wait pattern with Thread#onSpinWait
            while (true) {
                processQueue();
            }
        }, "EsMetricsConsumer"); //$NON-NLS-1$
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Process the next item in the queue.
     */
    protected void processQueue() {
        try {
            RestHighLevelClient client = getClient();
            Collection<EsMetricPayload> batch = new ArrayList<>(this.batchSize);
            EsMetricPayload rm = queue.take();
            batch.add(rm);
            queue.drainTo(batch, this.batchSize - 1);

            BulkRequest request = new BulkRequest();
            for (EsMetricPayload metric : batch) {
                IndexRequest index = new IndexRequest(getIndexPrefix());
                index.source(JSON_MAPPER.writeValueAsString(metric), XContentType.JSON);
                request.add(index);
            }

            ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    if (bulkItemResponses.hasFailures()) {
                        LOGGER.warn("Errors were reported when submitting bulk metrics into Elasticsearch. "
                                            + "This may have resulted in a loss of data: ", bulkItemResponses.buildFailureMessage());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    LOGGER.error("Failed to add metric(s) to ES", e); //$NON-NLS-1$
                }
            };
            client.bulkAsync(request, RequestOptions.DEFAULT, listener);
        } catch (InterruptedException | JsonProcessingException e) {
            LOGGER.error("Failed to add metric(s) to ES", e); //$NON-NLS-1$
        }
    }

    /**
     * @see AbstractEsComponent#getDefaultIndexPrefix()
     */
    @Override
    protected String getDefaultIndexPrefix() {
        return EsConstants.METRICS_INDEX_NAME;
    }

    @Override
    public Map<String, EsIndexProperties> getEsIndices() {
        EsIndexPropertiesBuilder indexBuilder = EsIndexProperties.builder()
                                                        .addProperty(EsConstants.ES_FIELD_API_DURATION, LONG_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_API_END, DATE_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_API_ID, KEYWORD_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_API_ORG_ID, KEYWORD_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_API_START, DATE_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_API_VERSION, KEYWORD_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_BYTES_DOWNLOADED, LONG_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_BYTES_UPLOADED, LONG_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_CLIENT_ID, KEYWORD_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_CLIENT_ORG_ID, KEYWORD_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_CLIENT_VERSION, KEYWORD_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_CONTRACT_ID, KEYWORD_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_ERROR, BOOL_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_ERROR_MESSAGE, TEXT_AND_KEYWORD_PROP_256)
                                                        .addProperty(EsConstants.ES_FIELD_FAILURE, BOOL_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_FAILURE_CODE, LONG_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_FAILURE_REASON, TEXT_AND_KEYWORD_PROP_256)
                                                        .addProperty(EsConstants.ES_FIELD_METHOD, KEYWORD_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_PLAN_ID, KEYWORD_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_REMOTE_ADDR, IP_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_REQUEST_DURATION, LONG_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_REQUEST_END, DATE_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_REQUEST_START, DATE_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_RESOURCE, TEXT_AND_KEYWORD_PROP_256)
                                                        .addProperty(EsConstants.ES_FIELD_RESPONSE_CODE, LONG_PROP)
                                                        .addProperty(EsConstants.ES_FIELD_RESPONSE_MESSAGE, TEXT_AND_KEYWORD_PROP_256)
                                                        .addProperty(EsConstants.ES_FIELD_URL, TEXT_AND_KEYWORD_PROP_256);

        customiseIndex(indexBuilder);

        Map<String, EsIndexProperties> indexMap = new HashMap<>();
        indexMap.put("", indexBuilder.build());
        return indexMap;
    }

    private void customiseIndex(EsIndexPropertiesBuilder indexBuilder) {
        // TODO(msavy): we may be able to condense request and response down to a single match?
        EsDynamicTemplate requestTpl = new EsDynamicTemplateBuilder()
                                               .setPathMatch("headers.request.*")
                                               .setMapping(TEXT_AND_KEYWORD_PROP_128)
                                               .build();

        indexBuilder.addTemplate("request_headers_tpl", requestTpl);

        EsDynamicTemplate responseTpl = new EsDynamicTemplateBuilder()
                                                .setPathMatch("headers.response.*")
                                                .setMapping(TEXT_AND_KEYWORD_PROP_128)
                                                .build();

        indexBuilder.addTemplate("response_headers_tpl", responseTpl);

        EsDynamicTemplate queryParamsTpl = new EsDynamicTemplateBuilder()
                                                   .setPathMatch("queryParams.*")
                                                   .setMapping(TEXT_AND_KEYWORD_PROP_128)
                                                   .build();

        indexBuilder.addTemplate("query_params_tpl", queryParamsTpl);
    }

    private EsMetricPayload buildEsMetric(RequestMetric metricIn, ApiRequest apiRequest, ApiResponse apiResponse) {
        HeaderMap requestHeaders = new HeaderMap();
        Map<String, HeaderMap> headerMap = new HashMap<>();
        headerMap.put("request", requestHeaders);

        // Process request headers
        for (String requestKey : apiRequest.getHeaders().keySet()) {
            if (requestHeaderPattern.matcher(requestKey).matches()) {
                requestHeaders.put(requestKey, apiRequest.getHeaders().get(requestKey));
            }
        }

        // In certain error & failure conditions, the apiResponse might be null (e.g. invalid endpoint).
        if (apiResponse != null) {
            HeaderMap responseHeaders = new HeaderMap();
            headerMap.put("response", responseHeaders);
            // Process response headers
            for (String responseKey : apiResponse.getHeaders().keySet()) {
                if (responseHeaderPattern.matcher(responseKey).matches()) {
                    responseHeaders.put(responseKey, apiResponse.getHeaders().get(responseKey));
                }
            }
        }

        // Process query params
        QueryMap filteredQueryMap = new QueryMap(apiRequest.getQueryParams().size());
        for (String qParamKey : apiRequest.getQueryParams().keySet()) {
            if (queryParamsPattern.matcher(qParamKey).matches()) {
                String value = apiRequest.getQueryParams().get(qParamKey);
                String insertValue = (value != null) ? value : "";
                filteredQueryMap.put(qParamKey, insertValue);
            }
        }

        return new EsMetricPayload(metricIn, headerMap, filteredQueryMap);
    }

    public static final class EsMetricPayload implements Serializable {
        private final Date requestStart;
        private final Date requestEnd;
        private final long requestDuration;
        private final Date apiStart;
        private final Date apiEnd;
        private final long apiDuration;
        private final String url;
        private final String resource;
        private final String method;
        private final String apiOrgId;
        private final String apiId;
        private final String apiVersion;
        private final String planId;
        private final String clientOrgId;
        private final String clientId;
        private final String clientVersion;
        private final String contractId;
        private final String user;
        private final int responseCode;
        private final String responseMessage;
        private final boolean failure;
        private final int failureCode;
        private final String failureReason;
        private final boolean error;
        private final String errorMessage;
        private final long bytesUploaded;
        private final long bytesDownloaded;
        private final Map<String, HeaderMap> headers;
        private final QueryMap queryParams;

        public EsMetricPayload(RequestMetric requestMetricIn, Map<String, HeaderMap> headers, QueryMap queryParams) {
            this.requestStart = requestMetricIn.getRequestStart();
            this.requestEnd = requestMetricIn.getRequestEnd();
            this.requestDuration = requestMetricIn.getRequestDuration();
            this.apiStart = requestMetricIn.getApiStart();
            this.apiEnd = requestMetricIn.getApiEnd();
            this.apiDuration = requestMetricIn.getApiDuration();
            this.url = requestMetricIn.getUrl();
            this.resource = requestMetricIn.getResource();
            this.method = requestMetricIn.getMethod();
            this.apiOrgId = requestMetricIn.getApiOrgId();
            this.apiId = requestMetricIn.getApiId();
            this.apiVersion = requestMetricIn.getApiVersion();
            this.planId = requestMetricIn.getPlanId();
            this.clientOrgId = requestMetricIn.getClientOrgId();
            this.clientId = requestMetricIn.getClientId();
            this.clientVersion = requestMetricIn.getClientVersion();
            this.contractId = requestMetricIn.getContractId();
            this.user = requestMetricIn.getUser();
            this.responseCode = requestMetricIn.getResponseCode();
            this.responseMessage = requestMetricIn.getResponseMessage();
            this.failure = requestMetricIn.isFailure();
            this.failureCode = requestMetricIn.getFailureCode();
            this.failureReason = requestMetricIn.getFailureReason();
            this.error = requestMetricIn.isError();
            this.errorMessage = requestMetricIn.getErrorMessage();
            this.bytesUploaded = requestMetricIn.getBytesUploaded();
            this.bytesDownloaded = requestMetricIn.getBytesDownloaded();
            this.headers = headers;
            this.queryParams = queryParams;
        }

        public Date getRequestStart() {
            return requestStart;
        }

        public Date getRequestEnd() {
            return requestEnd;
        }

        public long getRequestDuration() {
            return requestDuration;
        }

        public Date getApiStart() {
            return apiStart;
        }

        public Date getApiEnd() {
            return apiEnd;
        }

        public long getApiDuration() {
            return apiDuration;
        }

        public String getUrl() {
            return url;
        }

        public String getResource() {
            return resource;
        }

        public String getMethod() {
            return method;
        }

        public String getApiOrgId() {
            return apiOrgId;
        }

        public String getApiId() {
            return apiId;
        }

        public String getApiVersion() {
            return apiVersion;
        }

        public String getPlanId() {
            return planId;
        }

        public String getClientOrgId() {
            return clientOrgId;
        }

        public String getClientId() {
            return clientId;
        }

        public String getClientVersion() {
            return clientVersion;
        }

        public String getContractId() {
            return contractId;
        }

        public String getUser() {
            return user;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public String getResponseMessage() {
            return responseMessage;
        }

        public boolean isFailure() {
            return failure;
        }

        public int getFailureCode() {
            return failureCode;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public boolean isError() {
            return error;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public long getBytesUploaded() {
            return bytesUploaded;
        }

        public long getBytesDownloaded() {
            return bytesDownloaded;
        }

        public Map<String, HeaderMap> getHeaders() {
            return headers;
        }

        public QueryMap getQueryParams() {
            return queryParams;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", EsMetricPayload.class.getSimpleName() + "[", "]")
                           .add("requestStart=" + requestStart)
                           .add("requestEnd=" + requestEnd)
                           .add("requestDuration=" + requestDuration)
                           .add("apiStart=" + apiStart)
                           .add("apiEnd=" + apiEnd)
                           .add("apiDuration=" + apiDuration)
                           .add("url='" + url + "'")
                           .add("resource='" + resource + "'")
                           .add("method='" + method + "'")
                           .add("apiOrgId='" + apiOrgId + "'")
                           .add("apiId='" + apiId + "'")
                           .add("apiVersion='" + apiVersion + "'")
                           .add("planId='" + planId + "'")
                           .add("clientOrgId='" + clientOrgId + "'")
                           .add("clientId='" + clientId + "'")
                           .add("clientVersion='" + clientVersion + "'")
                           .add("contractId='" + contractId + "'")
                           .add("user='" + user + "'")
                           .add("responseCode=" + responseCode)
                           .add("responseMessage='" + responseMessage + "'")
                           .add("failure=" + failure)
                           .add("failureCode=" + failureCode)
                           .add("failureReason='" + failureReason + "'")
                           .add("error=" + error)
                           .add("errorMessage='" + errorMessage + "'")
                           .add("bytesUploaded=" + bytesUploaded)
                           .add("bytesDownloaded=" + bytesDownloaded)
                           .add("headers=" + headers)
                           .add("queryParams=" + queryParams)
                           .toString();
        }
    }

}
