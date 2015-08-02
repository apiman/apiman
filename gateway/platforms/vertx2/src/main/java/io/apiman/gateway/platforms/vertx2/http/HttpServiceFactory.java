/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.platforms.vertx2.http;

import io.apiman.gateway.platforms.vertx2.services.VertxServiceRequest;
import io.apiman.gateway.platforms.vertx2.services.VertxServiceResponse;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.exceptions.IllegalArgumentException;

/**
 * Construct {@link VertxServiceRequest} and {@link VertxServiceResponse} objects from {@link HttpServerRequest},
 * {@link HttpServerResponse} and {@link HttpClientResponse}
 *
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class HttpServiceFactory {

    public static VertxServiceResponse buildResponse(HttpClientResponse response, Set<String> suppressHeaders) {
        VertxServiceResponse apimanResponse = new VertxServiceResponse();
        apimanResponse.setCode(response.statusCode());
        apimanResponse.setMessage(response.statusMessage());
        multimapToMap(apimanResponse.getHeaders(), response.headers(), suppressHeaders);

        return apimanResponse;
    }

    public static void buildResponse(HttpServerResponse httpServerResponse, VertxServiceResponse amanResponse) {
        httpServerResponse.headers().addAll(amanResponse.getHeaders());
        httpServerResponse.setStatusCode(amanResponse.getCode());
        httpServerResponse.setStatusMessage(amanResponse.getMessage());
    }

    public static VertxServiceRequest buildRequest(HttpServerRequest req, boolean isTransportSecure) {
        VertxServiceRequest apimanRequest = new VertxServiceRequest();
        apimanRequest.setApiKey(parseApiKey(req));
        apimanRequest.setRemoteAddr(req.remoteAddress().host()); // TODO hmm
        apimanRequest.setType(req.method().toString());
        apimanRequest.setTransportSecure(isTransportSecure);
        multimapToMap(apimanRequest.getHeaders(), req.headers(), Collections.<String>emptySet());
        multimapToMap(apimanRequest.getQueryParams(), req.params(), Collections.<String>emptySet());
        mungePath(req, apimanRequest);

        return apimanRequest;
    }

    private static void mungePath(HttpServerRequest request, VertxServiceRequest apimanRequest) {
        String pathInfo = request.path();

        if (pathInfo != null) {
            String[] split = pathInfo.split("/"); //$NON-NLS-1$
            if (split.length >= 4) {
                apimanRequest.setServiceOrgId(split[1]);
                apimanRequest.setServiceId(split[2]);
                apimanRequest.setServiceVersion(split[3]);
                if (split.length > 4) {
                    StringBuilder resourceSb = new StringBuilder();
                    for (int idx = 4; idx < split.length; idx++) {
                        resourceSb.append('/');
                        resourceSb.append(split[idx]);
                    }
                    if (pathInfo.endsWith("/")) { //$NON-NLS-1$
                        resourceSb.append('/');
                    }
                    apimanRequest.setDestination(resourceSb.toString());
                }
            }
        }

        if (apimanRequest.getServiceOrgId() == null) {
            throw new IllegalArgumentException("Invalid endpoint provided"); //$NON-NLS-1$
        }
    }

    private static void multimapToMap(Map<String, String> map, MultiMap multimap, Set<String> suppressHeaders) {
        for (Map.Entry<String, String> entry : multimap) {
            if(!suppressHeaders.contains(entry.getKey())) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private static String parseApiKey(HttpServerRequest req) {
        String headerKey = req.headers().get("X-API-Key");
        if (headerKey == null || headerKey.trim().length() == 0) {
            headerKey = parseApiKeyFromQuery(req);
        }
        return headerKey;
    }

    private static String parseApiKeyFromQuery(HttpServerRequest req) {
        String queryString = req.query();

        if(queryString == null)
            return "<none>";

        int idx = queryString.indexOf("apikey=");
        if (idx >= 0) {
            int endIdx = queryString.indexOf('&', idx);
            if (endIdx == -1) {
                endIdx = queryString.length();
            }
            return queryString.substring(idx + 7, endIdx);
        } else {
            return null;
        }
    }
}
