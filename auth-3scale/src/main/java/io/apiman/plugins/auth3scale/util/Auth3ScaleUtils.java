/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.plugins.auth3scale.util;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.BackendConfiguration;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.ProxyRule;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class Auth3ScaleUtils {

    private static final Cache<String, URI> URI_CACHE = CacheBuilder.newBuilder().maximumSize(1000).build();

    public static <T> T getOrDefault(T val, T def) {
        if (val == null) {
            return def;
        }
        return val;
    }

    public static <T> ParameterMap setIfNotNull(ParameterMap in, String k, T v) {
        if (v == null) {
            return in;
        }
        in.add(k, v);
        return in;
    }

    public static ParameterMap buildRepMetrics(BackendConfiguration config, ApiRequest request) {
        ParameterMap pm = new ParameterMap(); // TODO could be interesting to cache a partially built map and just replace values?

        int[] matches = config.getProxy().match(request.getDestination());
        if (matches.length > 0) {
            for (int matchIndex : matches) {
                // Get specific proxy rule that matches. (e.g. / or /foo/bar)
                ProxyRule proxyRule = config.getProxy().getProxyRules().get(matchIndex);
                // Name of the metric as defined in 3scale
                String metricName = proxyRule.getMetricSystemName();
                // Ensure the matching rule applies to the request's HTTP Method
                if (!proxyRule.getHttpMethod().equalsIgnoreCase(request.getType()))
                    continue;

                if (pm.containsKey(metricName)) {
                    long newValue = pm.getLongValue(metricName) + proxyRule.getDelta(); // Increment delta.
                    pm.setLongValue(metricName, newValue);
                } else {
                    pm.setLongValue(metricName, proxyRule.getDelta()); // Otherwise value is delta.
                }
            }
        }
        return pm;
    }

    public static boolean hasRoutes(BackendConfiguration config, ApiRequest req) {
        return config.getProxy().match(req.getDestination()).length > 0;
    }

    public static ParameterMap buildLog(ApiResponse response) {
        return new ParameterMap().add("code", (long) response.getCode()); //$NON-NLS-1$
    }

    public static String getUserKey(BackendConfiguration config, ApiRequest request)  {
        // Manual for now as there's no mapping in the config.
        String keyFieldName = config.getProxy().getAuthUserKey();
        return getCredentialFromQueryOrHeader(config, request, keyFieldName);
    }

    public static String getCredentialFromQueryOrHeader(BackendConfiguration config, ApiRequest request, String keyFieldName) {
        if (config.getProxy().getCredentialsLocation().equalsIgnoreCase("query")) { //$NON-NLS-1$
            return request.getQueryParams().get(keyFieldName);
        } else { // Else let's assume header
            return request.getHeaders().get(keyFieldName);
        }
    }

    public static URI parseUri(String uri) {
        try {
            return URI_CACHE.get(uri, () -> { return URI.create(uri); });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
