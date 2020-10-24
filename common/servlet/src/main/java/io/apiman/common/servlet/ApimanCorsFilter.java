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
package io.apiman.common.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.commons.configuration.Configuration;
import io.apiman.common.config.ConfigFactory;

/**
 * A simple CORS filter for apiman.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApimanCorsFilter implements Filter {

    public static final Configuration config;
    static {
        config = ConfigFactory.createConfig();
    }

    public static final String MANAGER_UI_ALLOWED_CORS_ORIGINS = "apiman-manager-ui.allowed-cors-origins";

    private HashSet<String> allowedCorsOrigins = new HashSet<>();

    /**
     * Constructor.
     */
    public ApimanCorsFilter() {
        String corsOrigins = System.getProperty(MANAGER_UI_ALLOWED_CORS_ORIGINS,
                                                config.getString(MANAGER_UI_ALLOWED_CORS_ORIGINS,
                                                                "*"));
        allowedCorsOrigins = new HashSet<>(Arrays.asList(corsOrigins.trim().split(",")));
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        httpResp.setHeader("Access-Control-Origin-Allow", String.join(",", allowedCorsOrigins));
        if (isPreflightRequest(httpReq) && originIsAllowed(httpReq)) {
            httpResp.setHeader("Access-Control-Allow-Origin", httpReq.getHeader("Origin")); //$NON-NLS-1$ //$NON-NLS-2$
            if(!allowedCorsOrigins.contains("*")){
                httpResp.setHeader("Vary", "Origin"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            httpResp.setHeader("Access-Control-Allow-Credentials", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            httpResp.setHeader("Access-Control-Max-Age", "1800"); //$NON-NLS-1$ //$NON-NLS-2$
            httpResp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,HEAD,DELETE"); //$NON-NLS-1$ //$NON-NLS-2$
            httpResp.setHeader("Access-Control-Allow-Headers", "X-Requested-With,Content-Type,Accept,Origin,Authorization"); //$NON-NLS-1$ //$NON-NLS-2$
            httpResp.setHeader("Access-Control-Expose-Headers", "X-Apiman-Error"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            if (hasOriginHeader(httpReq) && originIsAllowed(httpReq)) {
                httpResp.setHeader("Access-Control-Allow-Origin", httpReq.getHeader("Origin")); //$NON-NLS-1$ //$NON-NLS-2$
                if(!allowedCorsOrigins.contains("*")){
                    httpResp.setHeader("Vary", "Origin"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                httpResp.setHeader("Access-Control-Allow-Credentials", "true"); //$NON-NLS-1$ //$NON-NLS-2$
                httpResp.setHeader("Access-Control-Expose-Headers", "X-Apiman-Error"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                httpResp.setHeader("Access-Control-Origin-Not-Allow", httpReq.getHeader("Origin")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            chain.doFilter(httpReq, httpResp);
        }
    }

    /**
     * Check if the origin of the request is in the list of allows cors origins.
     *
     * @param httpReq the http servlet request
     * @return true if the origin is allowed, else false
     */
    private boolean originIsAllowed(HttpServletRequest httpReq) {
        String origin = httpReq.getHeader("Origin").trim();
        return allowedCorsOrigins.contains("*") ||
            allowedCorsOrigins.contains(origin);
    }

    /**
     * Determines whether the request is a CORS preflight request.
     * @param httpReq the http servlet request
     * @return true if preflight, else false
     */
    private boolean isPreflightRequest(HttpServletRequest httpReq) {
        return isOptionsMethod(httpReq) && hasOriginHeader(httpReq);
    }

    /**
     * Returns true if it's an OPTIONS http request.
     * @param httpReq the http servlet request
     * @return true if options method, else false
     */
    private boolean isOptionsMethod(HttpServletRequest httpReq) {
        return "OPTIONS".equals(httpReq.getMethod()); //$NON-NLS-1$
    }

    /**
     * Returns true if the Origin request header is present.
     * @param httpReq the http servlet request
     * @return true if has origin header, else false
     */
    private boolean hasOriginHeader(HttpServletRequest httpReq) {
        String origin = httpReq.getHeader("Origin"); //$NON-NLS-1$
        return origin != null && origin.trim().length() > 0;
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

}
