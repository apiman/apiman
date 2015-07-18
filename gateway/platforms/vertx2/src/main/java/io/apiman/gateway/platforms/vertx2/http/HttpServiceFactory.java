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

import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceRequest;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceResponse;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Construct {@link VertxServiceRequest} and {@link VertxServiceResponse} objects from {@link HttpServerRequest},
 * {@link HttpServerResponse} and {@link HttpClientResponse}
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class HttpServiceFactory {

    public static VertxServiceResponse buildResponse(HttpClientResponse response, Set<String> suppressHeaders) {
        VertxServiceResponse apimanResponse = new VertxServiceResponse();
        apimanResponse.setCode(response.statusCode());
        parseHeaders(apimanResponse.getHeaders(), response.headers(), suppressHeaders);
        apimanResponse.setMessage(response.statusMessage());

        return apimanResponse;
    }

    public static void buildResponse(HttpServerResponse httpServerResponse, VertxServiceResponse amanResponse) {
        httpServerResponse.headers().addAll(amanResponse.getHeaders());
        httpServerResponse.setStatusCode(amanResponse.getCode());
        httpServerResponse.setStatusMessage(amanResponse.getMessage());
    }

    public static VertxServiceRequest buildRequest(HttpServerRequest req, String stripFromStart, boolean isTransportSecure) {
        VertxServiceRequest apimanRequest = new VertxServiceRequest();
        apimanRequest.setApiKey(parseApiKey(req));
        parseHeaders(apimanRequest.getHeaders(), req.headers(), Collections.<String>emptySet());

        // Remove the gateway's URI from the start of the path if it's there.
        apimanRequest.setDestination(StringUtils.removeStart(req.path(), "/" + stripFromStart)); //$NON-NLS-1$
        //apimanRequest.setRemoteAddr(req.remoteAddress().getAddress().getHostAddress());
        apimanRequest.setRemoteAddr(req.remoteAddress().host()); // TODO hmm
        apimanRequest.setType(req.method().toString());
        apimanRequest.setTransportSecure(isTransportSecure);

        return apimanRequest;
    }

    private static void parseHeaders(Map<String, String> map, MultiMap multimap, Set<String> suppressHeaders) {
        for (Map.Entry<String, String> entry : multimap) {
            if(!suppressHeaders.contains(entry.getKey())) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private static String parseApiKey(HttpServerRequest req) {
        String headerKey = req.headers().get("X-API-Key"); //$NON-NLS-1$
        if (headerKey == null || headerKey.trim().length() == 0) {
            headerKey = parseApiKeyFromQuery(req);
        }
        return headerKey;
    }

    private static String parseApiKeyFromQuery(HttpServerRequest req) {
        String queryString = req.query();

        if(queryString == null)
            return "<none>"; //$NON-NLS-1$

        int idx = queryString.indexOf("apikey="); //$NON-NLS-1$
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
