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
package io.apiman.gateway.platforms.vertx3.http;

import io.apiman.common.util.ApimanPathUtils;
import io.apiman.common.util.ApimanPathUtils.ApiRequestPathInfo;
import io.apiman.gateway.platforms.vertx3.io.VertxApiRequest;
import io.apiman.gateway.platforms.vertx3.io.VertxApiResponse;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Construct {@link VertxApiRequest} and {@link VertxApiResponse} objects from {@link HttpServerRequest},
 * {@link HttpServerResponse} and {@link HttpClientResponse}
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class HttpApiFactory {

    final static Set<String> IGNORESET = new HashSet<>();
    static {
        IGNORESET.add(ApimanPathUtils.X_API_VERSION_HEADER);
        IGNORESET.add("Host");
    }

    public static VertxApiResponse buildResponse(HttpClientResponse response, Set<String> suppressHeaders) {
        VertxApiResponse apimanResponse = new VertxApiResponse();
        apimanResponse.setCode(response.statusCode());
        apimanResponse.setMessage(response.statusMessage());
        multimapToMap(apimanResponse.getHeaders(), response.headers(), suppressHeaders);
        return apimanResponse;
    }

    public static void buildResponse(HttpServerResponse httpServerResponse, VertxApiResponse amanResponse) {
        httpServerResponse.headers().addAll(amanResponse.getHeaders());
        httpServerResponse.setStatusCode(amanResponse.getCode());
        httpServerResponse.setStatusMessage(amanResponse.getMessage());
    }

    public static VertxApiRequest buildRequest(HttpServerRequest req, boolean isTransportSecure) {
        VertxApiRequest apimanRequest = new VertxApiRequest();
        apimanRequest.setApiKey(parseApiKey(req));
        apimanRequest.setRemoteAddr(req.remoteAddress().host());
        apimanRequest.setType(req.method().toString());
        apimanRequest.setTransportSecure(isTransportSecure);
        multimapToMap(apimanRequest.getHeaders(), req.headers(), IGNORESET);
        multimapToMap(apimanRequest.getQueryParams(), req.params(), Collections.<String>emptySet());
        mungePath(req, apimanRequest);
        return apimanRequest;
    }

    private static void mungePath(HttpServerRequest request, VertxApiRequest apimanRequest) {
        ApiRequestPathInfo parsedPath = ApimanPathUtils.parseApiRequestPath(
                request.getHeader(ApimanPathUtils.X_API_VERSION_HEADER),
                request.getHeader(ApimanPathUtils.ACCEPT_HEADER),
                request.path());

        apimanRequest.setApiOrgId(parsedPath.orgId);
        apimanRequest.setApiId(parsedPath.apiId);
        apimanRequest.setApiVersion(parsedPath.apiVersion);
        apimanRequest.setUrl(request.absoluteURI());
        apimanRequest.setDestination(parsedPath.resource);

        if (apimanRequest.getApiOrgId() == null) {
            throw new IllegalArgumentException("Invalid endpoint provided: " + request.path());
        }
    }

    private static void multimapToMap(Map<String, String> map, MultiMap multimap, Set<String> suppressHeaders) {
        for (Map.Entry<String, String> entry : multimap) {
            if(!suppressHeaders.contains(entry.getKey())) {
                String key = entry.getKey();
                String val = entry.getValue();

                if (key != null && key.equalsIgnoreCase("accept") && val != null
                        && val.startsWith("application/apiman.")) { //$NON-NLS-1$
                    if (val.contains("+json")) { //$NON-NLS-1$
                        val = "application/json"; //$NON-NLS-1$
                    } else if (val.contains("+xml")) { //$NON-NLS-1$
                        val = "text/xml"; //$NON-NLS-1$
                    }
                }

                map.put(key, val);
            }
        }
    }

    private static String parseApiKey(HttpServerRequest req) {
        String headerKey = req.headers().get("X-API-Key");
        if (headerKey == null || headerKey.trim().length() == 0) {
            headerKey = req.getParam("apikey");
        }
        return headerKey;
    }
}
