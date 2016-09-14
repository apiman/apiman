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
package io.apiman.plugins.auth3scale.authrep;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.ProxyRule;
import io.apiman.plugins.auth3scale.authrep.apikey.ApiKeyAuthReporter;
import io.apiman.plugins.auth3scale.util.ParameterMap;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.AbstractReporter;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.ReportData;

import java.net.URI;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 * @param <T> the type
 */
public abstract class AbstractRepExecutor<T extends AbstractReporter<? extends ReportData>>
        implements IdentityFromContext {

    protected static final String DEFAULT_BACKEND = "http://su1.3scale.net:80"; // TODO add to config
    protected static final String REPORT_PATH = "/transactions.xml"; //$NON-NLS-1$
    protected static final URI REPORT_ENDPOINT = URI.create(DEFAULT_BACKEND+REPORT_PATH);

    protected final ApiRequest request;
    protected final ApiResponse response;
    protected final IPolicyFailureFactoryComponent failureFactory;
    protected final IHttpClientComponent httpClient;
    protected final ParameterMap paramMap;
    protected final Api api;
    protected final ApiKeyAuthReporter reporter;

    protected IAsyncHandler<PolicyFailure> policyFailureHandler;
    protected IPolicyContext context;
    protected Content config;

    private AbstractRepExecutor(Content config, ApiRequest request, ApiResponse response, Api api, IPolicyContext context,
            ApiKeyAuthReporter reporter) {
        this.request = request;
        this.response = response;
        this.api = api;
        this.config = config;
        this.reporter = reporter;
        this.failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
        this.httpClient = context.getComponent(IHttpClientComponent.class);
        this.context = context;
        this.paramMap = new ParameterMap();
    }

    public AbstractRepExecutor(Content config, ApiRequest request, ApiResponse response, IPolicyContext context,
            ApiKeyAuthReporter reporter, CachingAuthenticator authCache) {
        this(config, request, response, request.getApi(), context, reporter);
    }

    public abstract AbstractRepExecutor<T> rep();

    public AbstractRepExecutor<T> setPolicyFailureHandler(IAsyncHandler<PolicyFailure> policyFailureHandler) {
        this.policyFailureHandler = policyFailureHandler;
        return this;
    }

    /**
     * Mapping Rules Syntax A Mapping Rule has to start with '/'. This looks for
     * matches from the beginning of the string, ignoring the rest if the first
     * characters match. The pattern can only contain valid URL characters and
     * 'wildcards' - words inside curly brackets ('{}') that match any string up
     * to the following slash, ampersand or question mark.
     *
     * Examples /v1/word/{whateverword}.json /{version}/word/{foo}.json
     * /{version}/word/{bar}.json?action=create
     * /{version}/word/{baz}.json?action={my_action} More than one Mapping Rule
     * can match the request path but if none matches, the request is discarded
     * (404).
     *
     * Add a dollar sign ($) to the end of a pattern to apply stricter matching.
     * For example, /foo/bar$ will only match /foo/bar requests and won't match
     * /foo/bar/baz requests.
     */
    protected ParameterMap buildRepMetrics(Api api) {
        ParameterMap pm = new ParameterMap(); // TODO could be interesting to cache a partially built map and just replace values?

        int[] matches = config.getProxy().getRouteMatcher().match(request.getDestination());
        if (matches.length > 0) { // TODO could put this into request and process it up front. This logic could be removed from bean.
            for (int matchIndex : matches) {
                ProxyRule proxyRule = config.getProxy().getProxyRules().get(matchIndex+1); // Get specific proxy rule that matches. (e.g. / or /foo/bar)
                String metricName = proxyRule.getMetricSystemName();

                if (pm.containsKey(metricName)) {
                    long newValue = pm.getLongValue(metricName) + proxyRule.getDelta(); // Add delta.
                    pm.setLongValue(metricName, newValue);
                } else {
                    pm.setLongValue(metricName, proxyRule.getDelta()); // Otherwise value is delta.
                }

                // I don't understand this, the nginx code never seems to actually DO anything with the values... Confused.
                //              Pattern regex = proxyRule.getRegex(); // Regex with group matching support.
                //              Matcher matcher = regex.matcher(request.getDestination()); // Match against path
                //              for (int g = 1; g < matcher.groupCount(); g++) {
                //                  String metricName = proxyRule.getMetricSystemName();
                //                  String metricValue = matcher.group(g);
                //              }
            }
        }
        return pm;
    }

    protected ParameterMap buildLog() {
        return new ParameterMap().add("code", (long) response.getCode());
    }

    protected ParameterMap setIfNotNull(ParameterMap in, String k, String v) {
        if (v == null)
            return in;

        in.add(k, v);
        return in;
    }
}
