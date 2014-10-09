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
package org.overlord.apiman.dt.api.fuse6.auth;

import java.security.Principal;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.interceptor.security.JAASLoginInterceptor;
import org.apache.cxf.interceptor.security.NamePasswordCallbackHandler;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.security.SecurityContext;
import org.overlord.apiman.common.auth.AuthPrincipal;
import org.overlord.apiman.common.auth.AuthToken;
import org.overlord.apiman.common.auth.AuthTokenUtil;
import org.overlord.apiman.dt.api.fuse6.security.FuseSecurityContext;

/**
 * An authentication filter that implements both BASIC auth and AuthToken auth.
 * 
 * @author eric.wittmann@redhat.com
 */
public class AuthTokenRequestHandler implements RequestHandler {

    private String realm = "apiman"; //$NON-NLS-1$
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

    private final JAASLoginInterceptor interceptor = new JAASLoginInterceptor() {
        protected CallbackHandler getCallbackHandler(String name, String password) {
            return new NamePasswordCallbackHandler(name, password);
        }
    };

    /**
     * Constructor.
     */
    public AuthTokenRequestHandler() {
        // Set the default values.
        setContextName("karaf"); //$NON-NLS-1$
        setRoleClassifier("RolePrincipal"); //$NON-NLS-1$
        setRoleClassifierType("classname"); //$NON-NLS-1$
        setRealm("apiman-dt-api"); //$NON-NLS-1$
    }

    /**
     * @see org.apache.cxf.jaxrs.ext.RequestHandler#handleRequest(org.apache.cxf.message.Message,
     *      org.apache.cxf.jaxrs.model.ClassResourceInfo)
     */
    @Override
    public Response handleRequest(Message message, ClassResourceInfo cri) {
        String authHeader = new HttpHeadersImpl(message).getHeaderString("Authorization"); //$NON-NLS-1$
        if (authHeader == null) {
            return createAuthResponse(message);
        } else if (authHeader.toUpperCase().startsWith("BASIC")) { //$NON-NLS-1$
            try {
                interceptor.handleMessage(message);
                SecurityContext securityCtx = message.get(SecurityContext.class);
                FuseSecurityContext.set(securityCtx, new HttpHeadersImpl(message));
                return null;
            } catch (AuthenticationException ex) {
                return createAuthResponse(message);
            } catch (SecurityException ex) {
                return createAuthResponse(message);
            }
        } else if (authHeader.toUpperCase().startsWith("AUTH-TOKEN")) { //$NON-NLS-1$
            AuthToken token = parseAuthorizationToken(authHeader);
            if (token == null) {
                return createAuthResponse(message);
            } else {
                doTokenAuth(token, message);
                return null;
            }
        } else {
            return createAuthResponse(message);
        }
    }

    /**
     * Parses the Authorization request to retrieve the Base64 encoded auth token.
     * @param authHeader
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
     * Implements token based authentication.  This simply creates a principal from the {@link AuthToken}
     * and uses it to configure the security context.
     * @param token
     * @param message
     */
    protected void doTokenAuth(AuthToken token, Message message) {
        final AuthPrincipal principal = new AuthPrincipal(token.getPrincipal());
        principal.addRoles(token.getRoles());
        SecurityContext securityCtx = new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return principal;
            }
            @Override
            public boolean isUserInRole(String role) {
                return principal.getRoles().contains(role);
            }
        };
        FuseSecurityContext.set(securityCtx, new HttpHeadersImpl(message));
    }

    /**
     * Creates the proper response to send when the request is not
     * authenticated.
     * 
     * @param message
     */
    protected Response createAuthResponse(Message message) {
        ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED);
        builder.header(HttpHeaders.WWW_AUTHENTICATE, String.format("BASIC realm=\"%1$s\"", getRealm())); //$NON-NLS-1$
        return builder.build();
    }

    /**
     * @return the realm
     */
    public String getRealm() {
        return realm;
    }

    /**
     * @param realm
     *            the realm to set
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * @param name
     */
    public void setContextName(String name) {
        interceptor.setContextName(name);
    }

    /**
     * @param config
     */
    public void setLoginConfig(Configuration config) {
        interceptor.setLoginConfig(config);
    }
    
    /**
     * @param value
     */
    public void setRoleClassifier(String value) {
        interceptor.setRoleClassifier(value);
    }
    
    /**
     * @return the role classifier
     */
    public String getRoleClassifier() {
        return interceptor.getRoleClassifier();
    }
    
    /**
     * @param value the new role classifier type - either 'prefix' or 'classname'
     */
    public void setRoleClassifierType(String value) {
        interceptor.setRoleClassifierType(value);
    }
    
    /**
     * @return the role classifier type
     */
    public String getRoleClassifierType() {
        return interceptor.getRoleClassifierType();
    }
}
