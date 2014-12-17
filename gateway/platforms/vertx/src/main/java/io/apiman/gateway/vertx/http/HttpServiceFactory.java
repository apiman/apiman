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
package io.apiman.gateway.vertx.http;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

/**
 * Construct {@link ServiceRequest} and {@link ServiceResponse} objects from {@link HttpServerRequest},
 * {@link HttpServerResponse} and {@link HttpClientResponse}
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class HttpServiceFactory {
    
    public static ServiceResponse buildResponse(HttpClientResponse response, Set<String> suppressHeaders) {
        ServiceResponse apimanResponse = new ServiceResponse();
        apimanResponse.setCode(response.statusCode());
        parseHeaders(apimanResponse.getHeaders(), response.headers(), suppressHeaders);
        apimanResponse.setMessage(response.statusMessage());
        
        return apimanResponse;
    }
    
    public static ServiceResponse buildResponse(HttpServerResponse response) {
        return buildResponse(response, new ServiceResponse());
    }
    
    public static ServiceResponse buildResponse(HttpServerResponse response, ServiceResponse amanResponse) {
        response.headers().add(amanResponse.getHeaders());
        response.setStatusCode(amanResponse.getCode());
        response.setStatusMessage(amanResponse.getMessage());
        
        return amanResponse;
    }
    
    public static ServiceRequest build(HttpServerRequest req, String stripFromStart) {
        ServiceRequest apimanRequest = new ServiceRequest();
        apimanRequest.setApiKey(parseApiKey(req));
        parseHeaders(apimanRequest.getHeaders(), req.headers(), Collections.<String>emptySet());
        
        // Remove the gateway's URI from the start of the path if it's there.
        apimanRequest.setDestination(StringUtils.removeStart(req.path(), "/" + stripFromStart)); //$NON-NLS-1$
        apimanRequest.setRemoteAddr(req.remoteAddress().getAddress().getHostAddress());
        apimanRequest.setType(req.method());

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
