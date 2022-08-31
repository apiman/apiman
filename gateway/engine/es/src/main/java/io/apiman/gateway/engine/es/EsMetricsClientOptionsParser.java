/*
 * Copyright 2022 Black Parrot Labs Ltd
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

import io.apiman.common.config.options.GenericOptionsParser;
import io.apiman.common.config.options.Predicates;
import io.apiman.common.config.options.exceptions.InvalidOptionConfigurationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private Set<WriteTo> writeTo = Set.of(WriteTo.REMOTE); // Preserve existing default behaviour.

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

        // TODO(msavy): consider adding #getArray into GenericOptionsParser
        String joinedWriteTo = super.getString(keys("write-to"), "REMOTE", Predicates.anyOk(), "");
        if (!joinedWriteTo.isEmpty()) {
            Set<WriteTo> writeTo = new HashSet<>();
            for (String rawEnum : joinedWriteTo.strip().split(",")) {
                try {
                    writeTo.add(WriteTo.valueOf(rawEnum.strip().toUpperCase()));
                } catch(RuntimeException rte) {
                    throw InvalidOptionConfigurationException.constraintFailure(
                            "write-to",
                            "enum",
                            rawEnum,
                            "Valid inputs are: " + Arrays.toString(WriteTo.values())
                    );
                }
            }
            this.writeTo = writeTo;
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

    public Set<WriteTo> getWriteTo() {
        return writeTo;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EsMetricsClientOptionsParser.class.getSimpleName() + "[", "]")
                .add("requestHeaders=" + requestHeaders)
                .add("responseHeaders=" + responseHeaders)
                .add("queryParams=" + queryParams)
                .add("writeTo=" + writeTo)
                .toString();
    }

    public enum WriteTo {
        REMOTE, LOG
    }
}
