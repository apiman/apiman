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
package io.apiman.gateway.platforms.war.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * A simple filter that makes the current http request available via a thread local variable.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpRequestThreadLocalFilter implements Filter {
    
    private static final ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<>();
    private static final void setCurrentRequest(HttpServletRequest request) {
        currentRequest.set(request);
    }
    private static final void clearCurrentRequest() {
        currentRequest.remove();
    }
    public static final HttpServletRequest getCurrentRequest() {
        return currentRequest.get();
    }
    
    /**
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        setCurrentRequest((HttpServletRequest) request);
        try {
            chain.doFilter(request, response);
        } finally {
            clearCurrentRequest();
        }
    }
    
    /**
     * @see Filter#destroy()
     */
    @Override
    public void destroy() {
    }

}
