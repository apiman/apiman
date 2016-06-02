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
package io.apiman.common.servlet;

import io.apiman.common.auth.AuthPrincipal;
import io.apiman.common.auth.AuthToken;
import io.apiman.common.auth.AuthTokenUtil;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

/**
 * A simple implementation of an authentication filter - uses the APIMan
 * {@link AuthToken} concept to implement the equivalent of bearer token
 * authentication.  This filter supports both {@link AuthToken}'s as well
 * as standard BASIC authentication.  The latter is implemented by
 * delegating to the container.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuthenticationFilter implements Filter {

    private String realm;
    @SuppressWarnings("unused")
    private boolean signatureRequired;
    @SuppressWarnings("unused")
    private String keystorePath;
    @SuppressWarnings("unused")
    private String keystorePassword;
    @SuppressWarnings("unused")
    private String keyAlias;
    @SuppressWarnings("unused")
    private String keyPassword;

    /**
     * Constructor.
     */
    public AuthenticationFilter() {
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        // Realm
        String parameter = config.getInitParameter("realm"); //$NON-NLS-1$
        if (parameter != null && parameter.trim().length() > 0) {
            realm = parameter;
        } else {
            realm = defaultRealm();
        }

        // Signature Required
        parameter = config.getInitParameter("signatureRequired"); //$NON-NLS-1$
        if (parameter != null && parameter.trim().length() > 0) {
            signatureRequired = Boolean.parseBoolean(parameter);
        } else {
            signatureRequired = defaultSignatureRequired();
        }

        // Keystore Path
        parameter = config.getInitParameter("keystorePath"); //$NON-NLS-1$
        if (parameter != null && parameter.trim().length() > 0) {
            keystorePath = parameter;
        } else {
            keystorePath = defaultKeystorePath();
        }

        // Keystore Password
        parameter = config.getInitParameter("keystorePassword"); //$NON-NLS-1$
        if (parameter != null && parameter.trim().length() > 0) {
            keystorePassword = parameter;
        } else {
            keystorePassword = defaultKeystorePassword();
        }

        // Key alias
        parameter = config.getInitParameter("keyAlias"); //$NON-NLS-1$
        if (parameter != null && parameter.trim().length() > 0) {
            keyAlias = parameter;
        } else {
            keyAlias = defaultKeyAlias();
        }

        // Key Password
        parameter = config.getInitParameter("keyPassword"); //$NON-NLS-1$
        if (parameter != null && parameter.trim().length() > 0) {
            keyPassword = parameter;
        } else {
            keyPassword = defaultKeyPassword();
        }
    }

    /**
     * @return the default keystore password
     */
    protected String defaultKeystorePassword() {
        return null;
    }

    /**
     * @return the default key alias
     */
    protected String defaultKeyAlias() {
        return null;
    }

    /**
     * @return the default key password
     */
    protected String defaultKeyPassword() {
        return null;
    }

    /**
     * @return the default value of keystorePath
     */
    protected String defaultKeystorePath() {
        return null;
    }

    /**
     * @return the default value of signatureRequired
     */
    protected boolean defaultSignatureRequired() {
        return false;
    }

    /**
     * @return the default value of wrapRequest
     */
    protected boolean defaultWrapRequest() {
        return false;
    }

    /**
     * @return the default set of allowed issuers
     */
    protected Set<String> defaultAllowedIssuers() {
        return Collections.<String>emptySet();
    }

    /**
     * @return the default realm
     */
    protected String defaultRealm() {
        return "apiman"; //$NON-NLS-1$
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String authHeader = req.getHeader("Authorization"); //$NON-NLS-1$
        if (authHeader == null) {
            sendAuthResponse((HttpServletResponse) response);
        } else if (authHeader.toUpperCase().startsWith("BASIC")) { //$NON-NLS-1$
            Creds credentials = parseAuthorizationBasic(authHeader);
            if  (credentials == null) {
                sendAuthResponse((HttpServletResponse) response);
            } else {
                doBasicAuth(credentials, req, (HttpServletResponse) response, chain);
            }
        } else if (authHeader.toUpperCase().startsWith("AUTH-TOKEN")) { //$NON-NLS-1$
            AuthToken token = parseAuthorizationToken(authHeader);
            if (token == null) {
                sendAuthResponse((HttpServletResponse) response);
            } else {
                doTokenAuth(token, req, (HttpServletResponse) response, chain);
            }
        } else {
            sendAuthResponse((HttpServletResponse) response);
        }
    }

    /**
     * Handle BASIC authentication.  Delegates this to the container by invoking 'login'
     * on the inbound http servlet request object.
     * @param credentials the credentials
     * @param request the http servlet request
     * @param response the http servlet respose
     * @param chain the filter chain
     * @throws IOException when I/O failure occurs in filter chain
     * @throws ServletException when servlet exception occurs during auth
     */
    protected void doBasicAuth(Creds credentials, HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        try {
            if (credentials.username.equals(request.getRemoteUser())) {
                // Already logged in as this user - do nothing.  This can happen
                // in some app servers if the app server processes the BASIC auth
                // credentials before this filter gets a crack at them.  WildFly 8
                // works this way, for example (despite the web.xml not specifying
                // any login config!).
            } else if (request.getRemoteUser() != null) {
                // switch user
                request.logout();
                request.login(credentials.username, credentials.password);
            } else {
                request.login(credentials.username, credentials.password);
            }
        } catch (Exception e) {
            // TODO log this error?
            e.printStackTrace();
            sendAuthResponse(response);
            return;
        }
        doFilterChain(request, response, chain, null);
    }

    /**
     * Implements token based authentication.  This simply creates a principal from the {@link AuthToken}
     * and then calls doFilterChain.
     * @param token the token
     * @param request the request
     * @param response the response
     * @param chain the filterchain
     * @throws IOException when I/O failure occurs in filter chain
     * @throws ServletException when servlet exception occurs during auth
     */
    protected void doTokenAuth(AuthToken token, HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        AuthPrincipal principal = new AuthPrincipal(token.getPrincipal());
        principal.addRoles(token.getRoles());
        doFilterChain(request, response, chain, principal);
    }

    /**
     * Further process the filter chain.
     * @param request the request
     * @param response the response
     * @param chain the filter chain
     * @param principal the auth principal
     * @throws IOException when I/O failure occurs in filter chain
     * @throws ServletException when servlet exception occurs during auth
     */
    protected void doFilterChain(ServletRequest request, ServletResponse response, FilterChain chain,
            AuthPrincipal principal) throws IOException, ServletException {
        if (principal == null) {
            chain.doFilter(request, response);
        } else {
            HttpServletRequest hsr;
            hsr = wrapTheRequest(request, principal);
            chain.doFilter(hsr, response);
        }
    }

    /**
     * Wrap the request to provide the principal.
     * @param request the request
     * @param principal the principal
     */
    private HttpServletRequest wrapTheRequest(final ServletRequest request, final AuthPrincipal principal) {
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
     * Parses the Authorization request header into a username and password.
     * @param authHeader the auth header
     */
    private Creds parseAuthorizationBasic(String authHeader) {
        String userpassEncoded = authHeader.substring(6);
        String data = StringUtils.newStringUtf8(Base64.decodeBase64(userpassEncoded));
        int sepIdx = data.indexOf(':');
        if (sepIdx > 0) {
            String username = data.substring(0, sepIdx);
            String password = data.substring(sepIdx + 1);
            return new Creds(username, password);
        } else {
            return new Creds(data, null);
        }
    }

    /**
     * Parses the Authorization request to retrieve the Base64 encoded auth token.
     * @param authHeader the auth header
     */
    private AuthToken parseAuthorizationToken(String authHeader) {
        try {
            String tokenEncoded = authHeader.substring(11);
            return AuthTokenUtil.consumeToken(tokenEncoded);
        } catch (IllegalArgumentException e) {
            // TODO log this error
            return null;
        }
    }

    /**
     * Sends a response that tells the client that authentication is required.
     * @param response the response
     * @throws IOException when an error cannot be sent
     */
    private void sendAuthResponse(HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", String.format("Basic realm=\"%1$s\"", realm)); //$NON-NLS-1$ //$NON-NLS-2$
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

    /**
     * Models inbound basic auth credentials (user/password).
     * @author eric.wittmann@redhat.com
     */
    protected static class Creds {
        public String username;
        public String password;

        /**
         * Constructor.
         *
         * @param username the username
         * @param password the password
         */
        public Creds(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

}
