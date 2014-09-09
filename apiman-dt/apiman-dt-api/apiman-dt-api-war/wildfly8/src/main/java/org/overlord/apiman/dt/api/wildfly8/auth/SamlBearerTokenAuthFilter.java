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
package org.overlord.apiman.dt.api.wildfly8.auth;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.overlord.commons.auth.filters.SimplePrincipal;


/**
 * Extends the auth filter to work in WildFly (undertow).
 *
 * @author eric.wittmann@redhat.com
 */
public class SamlBearerTokenAuthFilter extends org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter {

    // Indicates that the request has been logged in and does not need to be wrapped.
    private static final SimplePrincipal NO_PROXY = new SimplePrincipal(null);

    /**
     * Constructor.
     */
    public SamlBearerTokenAuthFilter() {
    }

    /**
     * Further process the filter chain.
     * @param request
     * @param response
     * @param chain
     * @param principal
     * @throws IOException
     * @throws ServletException
     */
    protected void doFilterChain(ServletRequest request, ServletResponse response, FilterChain chain,
            SimplePrincipal principal) throws IOException, ServletException {
        if (principal == NO_PROXY) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(wrapRequest(request, principal), response);
        }
    }

    /**
     * Wrap the request to provide the principal.
     * @param request
     * @param principal
     */
    private ServletRequest wrapRequest(final ServletRequest request, final SimplePrincipal principal) {
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request) {
            @Override
            public Principal getUserPrincipal() {
                return principal;
            }
            
            @Override
            public boolean isUserInRole(String role) {
                return principal.getRoles().contains(role);
            }
            
            @Override
            public String getRemoteUser() {
                return principal.getName();
            }
        };
        return wrapper;
    }

    /**
     * Fall back to standard basic authentication.  Subclasses may implement this
     * method if {@link HttpServletRequest#login(String, String)} is not sufficient.
     * @param username
     * @param password
     * @param request 
     * @throws IOException
     */
    protected SimplePrincipal doBasicLogin(String username, String password, HttpServletRequest request) throws IOException {
        try {
            if (username.equals(request.getRemoteUser())) {
                // already logged in as this user - do nothing
            } else if (request.getRemoteUser() != null) {
                // switch user
                request.logout();
                request.login(username, password);
            } else {
                request.login(username, password);
            }
            return NO_PROXY;
        } catch (Exception e) {
            return null;
        }
    }

}
