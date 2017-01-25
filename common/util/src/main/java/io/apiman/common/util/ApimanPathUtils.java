/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.common.util;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("nls")
public class ApimanPathUtils {

    public static final String X_API_VERSION_HEADER = "X-API-Version";
    public static final String ACCEPT_HEADER = "Accept";

    private ApimanPathUtils() {
    }

    /**
     * Parses the HTTP request and returns an object containing all of the API
     * information (Org, Id, Version).
     * @param apiVersionHeader the API Version header
     * @param acceptHeader the accept header
     * @param pathInfo the path info
     * @return the parsed object containing API information.
     */
    public static final ApiRequestPathInfo parseApiRequestPath(String apiVersionHeader, String acceptHeader, String pathInfo) {
        ApiRequestPathInfo info = new ApiRequestPathInfo();

        boolean versionFound = false;

        if (apiVersionHeader != null && apiVersionHeader.trim().length() > 0) {
            info.apiVersion = apiVersionHeader;
            versionFound = true;
        } else {
            if (acceptHeader != null && acceptHeader.startsWith("application/apiman.")) {
                String [] split = acceptHeader.split("\\+");
                info.apiVersion = split[0].substring("application/apiman.".length());
                versionFound = true;
            }
        }

        int minParts = versionFound ? 3 : 4;

        if (pathInfo != null) {
            String[] split = pathInfo.split("/");
            if (split.length >= minParts) {
                info.orgId = split[1];
                info.apiId = split[2];
                if (!versionFound) {
                    info.apiVersion = split[3];
                }
                if (split.length > minParts) {
                    StringBuilder resource = new StringBuilder();
                    for (int idx = minParts; idx < split.length; idx++) {
                        resource.append('/');
                        resource.append(urlEncode(split[idx]));
                    }
                    if (pathInfo.endsWith("/")) {
                        resource.append('/');
                    }
                    info.resource = resource.toString();
                } else if (pathInfo.endsWith("/")) {
                	info.resource = "/";
                }
            }
        }
        return info;
    }

    /**
     * @param string string to replace # with %23
     * @return the encoded string
     */
    public static String urlEncode(String string) {
        return string.replace("#", "%23");
    }

    /**
     * Join endpoint and path with sensible / behaviour.
     *
     * @param endpoint the endpoint
     * @param path the destination (path)
     * @return the joined endpoint + destination.
     */
    public static String join(String endpoint, String path) {
        if (endpoint == null || endpoint.isEmpty())
            return path;
        if (path == null || path.isEmpty())
            return endpoint;

        if (StringUtils.endsWith(endpoint, "/") && path.startsWith("/")) {
            return endpoint + path.substring(1);
        } else if (StringUtils.endsWith(endpoint, "/") ^ path.startsWith("/")) {
            return endpoint + path;
        }
        return endpoint + "/" + path;
    }

    /**
     * Parsed API request path information.
     */
    public static class ApiRequestPathInfo {
        public String orgId;
        public String apiId;
        public String apiVersion;
        public String resource;
    }

}
