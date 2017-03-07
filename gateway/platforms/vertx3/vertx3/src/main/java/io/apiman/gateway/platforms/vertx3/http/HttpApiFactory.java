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
import io.apiman.gateway.engine.IApiRequestPathParser;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.util.CaseInsensitiveStringMultiMap;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Construct {@link ApiRequest} and {@link ApiResponse} objects from {@link HttpServerRequest},
 * {@link HttpServerResponse} and {@link HttpClientResponse}
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class HttpApiFactory {

    private final static Set<String> IGNORESET = new HashSet<>();
    static {
        IGNORESET.add(ApimanPathUtils.X_API_VERSION_HEADER);
        IGNORESET.add("Host");
    }

    private static IApiRequestPathParser requestPathParser;

    public static void init(IApiRequestPathParser requestPathParser) {
        HttpApiFactory.requestPathParser = requestPathParser;
    }

    public static ApiResponse buildResponse(HttpClientResponse response, Set<String> suppressHeaders) {
        ApiResponse apimanResponse = new ApiResponse();
        apimanResponse.setCode(response.statusCode());
        apimanResponse.setMessage(response.statusMessage());
        multimapToMap(apimanResponse.getHeaders(), response.headers(), suppressHeaders);
        return apimanResponse;
    }

    public static void buildResponse(HttpServerResponse httpServerResponse, ApiResponse amanResponse, HttpVersion httpVersion) {
        amanResponse.getHeaders().forEach(e -> {
            if (httpVersion == HttpVersion.HTTP_1_0 || httpVersion == HttpVersion.HTTP_1_1 || !e.getKey().equals("Connection")) {
                httpServerResponse.headers().add(e.getKey(), e.getValue());
            }
        });
        httpServerResponse.setStatusCode(amanResponse.getCode());
        httpServerResponse.setStatusMessage(amanResponse.getMessage());
    }

    public static ApiRequest buildRequest(HttpServerRequest req, boolean isTransportSecure) {
        ApiRequest apimanRequest = new ApiRequest();
        apimanRequest.setApiKey(parseApiKey(req));
        apimanRequest.setRemoteAddr(req.remoteAddress().host());
        apimanRequest.setType(req.method().toString());
        apimanRequest.setTransportSecure(isTransportSecure);
        multimapToMap(apimanRequest.getHeaders(), req.headers(), IGNORESET);
        multimapToMap(apimanRequest.getQueryParams(), req.params(), Collections.<String>emptySet());
        parsePath(req, apimanRequest);
        return apimanRequest;
    }

    private static void parsePath(HttpServerRequest request, ApiRequest apimanRequest) {
        // NB: The apiman version of the headers has already been parsed, so the headers have already been filtered/modified.
        // Therefore we wrap the original inbound headers (just get) to efficiently access the necessary data.
        ApiRequestPathInfo parsedPath = requestPathParser.parseEndpoint(request.path(), wrapMultiMap(request.headers()));
        apimanRequest.setApiOrgId(parsedPath.orgId);
        apimanRequest.setApiId(parsedPath.apiId);
        apimanRequest.setApiVersion(parsedPath.apiVersion);
        apimanRequest.setUrl(request.absoluteURI());
        apimanRequest.setDestination(parsedPath.resource);
    }

    // TODO hacky...
    private static HeaderMap wrapMultiMap(MultiMap headers) {
        return new HeaderMap() {
            private static final long serialVersionUID = -1406124274678587935L;

            @Override()
            public String get(String key) {
                return headers.get(key);
            }
        };
    }

    private static void multimapToMap(CaseInsensitiveStringMultiMap map, MultiMap multimap, Set<String> suppressHeaders) {
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

                map.add(key, val);
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
