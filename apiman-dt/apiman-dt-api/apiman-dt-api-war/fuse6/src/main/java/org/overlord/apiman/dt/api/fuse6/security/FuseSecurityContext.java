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
package org.overlord.apiman.dt.api.fuse6.security;

import javax.ws.rs.core.HttpHeaders;

import org.apache.cxf.security.SecurityContext;
import org.overlord.apiman.dt.api.security.ISecurityContext;
import org.overlord.apiman.dt.api.security.impl.AbstractSecurityContext;

/**
 * A Fuse version of the apiman-dt-api security context {@link ISecurityContext}.
 *
 * @author eric.wittmann@redhat.com
 */
public class FuseSecurityContext extends AbstractSecurityContext {
    
    private static final ThreadLocal<SecurityContext> cxfSecurityContext = new ThreadLocal<SecurityContext>();
    private static final ThreadLocal<HttpHeaders> headers = new ThreadLocal<HttpHeaders>();

    /**
     * Constructor.
     */
    public FuseSecurityContext() {
    }
    
    /**
     * @see org.overlord.apiman.dt.api.security.ISecurityContext#getRequestHeader(java.lang.String)
     */
    @Override
    public String getRequestHeader(String headerName) {
        return headers.get().getHeaderString(headerName);
    }

    /**
     * @see org.overlord.apiman.dt.api.security.ISecurityContext#getCurrentUser()
     */
    @Override
    public String getCurrentUser() {
        return cxfSecurityContext.get().getUserPrincipal().getName();
    }

    /**
     * @see org.overlord.apiman.dt.api.security.ISecurityContext#isAdmin()
     */
    @Override
    public boolean isAdmin() {
        // TODO warning - hard coded role value here
        return cxfSecurityContext.get().isUserInRole("apiadmin"); //$NON-NLS-1$
    }

    /**
     * Called to set the current context.
     * @param ctx
     * @param reqHeaders
     */
    public static void set(SecurityContext ctx, HttpHeaders reqHeaders) {
        cxfSecurityContext.set(ctx);
        headers.set(reqHeaders);
    }
    
    /**
     * Called to clear the current thread local permissions bean.
     */
    public static void clear() {
        AbstractSecurityContext.clearPermissions();
        cxfSecurityContext.remove();
        headers.remove();
    }

}
