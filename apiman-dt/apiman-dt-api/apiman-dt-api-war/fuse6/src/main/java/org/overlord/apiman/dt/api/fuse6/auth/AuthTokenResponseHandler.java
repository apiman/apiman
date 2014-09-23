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

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.ResponseHandler;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.overlord.apiman.dt.api.fuse6.security.FuseSecurityContext;

/**
 * Clears out the security context (if one was set).  This ensures
 * that the thread locals being configured by the request handler
 * are not stale for the next request.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuthTokenResponseHandler implements ResponseHandler {
    
    /**
     * Constructor.
     */
    public AuthTokenResponseHandler() {
    }

    /**
     * @see org.apache.cxf.jaxrs.ext.ResponseHandler#handleResponse(org.apache.cxf.message.Message, org.apache.cxf.jaxrs.model.OperationResourceInfo, javax.ws.rs.core.Response)
     */
    @Override
    public Response handleResponse(Message arg0, OperationResourceInfo arg1, Response arg2) {
        FuseSecurityContext.clear();
        return null;
    }

}
