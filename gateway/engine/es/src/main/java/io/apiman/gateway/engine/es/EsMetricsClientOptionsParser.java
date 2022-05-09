package io.apiman.gateway.engine.es;

import io.apiman.common.config.options.GenericOptionsParser;
import io.apiman.common.config.options.Predicates;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Options parser for Elasticsearch metrics
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class EsMetricsClientOptionsParser extends GenericOptionsParser {

    private List<String> requestHeaders = Collections.emptyList();
    private List<String> responseHeaders = Collections.emptyList();
    private List<String> queryParams = Collections.emptyList();

    public EsMetricsClientOptionsParser(Map<String, String> options) {
        super(options);
        parseOptions();
    }

    private void parseOptions() {
        String joinedRequestHeaders = super.getString(keys("custom.headers.request"), "", Predicates.anyOk(), "");
        if (!joinedRequestHeaders.isEmpty()) {
            requestHeaders = Arrays.asList(joinedRequestHeaders.split(","));
        }

        String joinedResponseHeaders = super.getString(keys("custom.headers.response"), "", Predicates.anyOk(), "");
        if (!joinedResponseHeaders.isEmpty()) {
            responseHeaders = Arrays.asList(joinedResponseHeaders.split(","));
        }

        String joinedQueryParams = super.getString(keys("custom.query-params"), "", Predicates.anyOk(), "");
        if (!joinedQueryParams.isEmpty()) {
            queryParams = Arrays.asList(joinedQueryParams.split(","));
        }
    }

    public List<String> getRequestHeaders() {
        return requestHeaders;
    }

    public List<String> getResponseHeaders() {
        return responseHeaders;
    }

    public List<String> getQueryParams() {
        return queryParams;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EsMetricsClientOptionsParser.class.getSimpleName() + "[", "]")
                .add("requestHeaders=" + requestHeaders)
                .add("responseHeaders=" + responseHeaders)
                .add("queryParams=" + queryParams)
                .toString();
    }
}
